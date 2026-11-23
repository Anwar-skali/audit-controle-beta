package com.audit.platform.finding.service.impl;

import com.audit.platform.finding.client.TransactionClient;
import com.audit.platform.finding.dto.external.TransactionDetailResponseDTO;
import com.audit.platform.finding.dto.external.TransactionResponseDTO;
import com.audit.platform.finding.dto.request.FindingRequestDTO;
import com.audit.platform.finding.dto.response.FindingDetailResponseDTO;
import com.audit.platform.finding.dto.response.FindingResponseDTO;
import com.audit.platform.finding.entity.Finding;
import com.audit.platform.finding.event.FindingCreatedEvent;
import com.audit.platform.finding.exception.ResourceNotFoundException;
import com.audit.platform.finding.mapper.FindingMapper;
import com.audit.platform.finding.repository.FindingRepository;
import com.audit.platform.finding.service.FindingService;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FindingServiceImpl implements FindingService {

    private final FindingRepository findingRepository;
    private final FindingMapper findingMapper;
    private final TransactionClient transactionClient;
    private final RabbitTemplate rabbitTemplate;

    @Value("${audit.rabbitmq.exchange:audit.events.exchange}")
    private String exchange;

    @Value("${audit.rabbitmq.routingkey:audit.finding.created}")
    private String routingKey;

    @Override
    public FindingResponseDTO createFinding(FindingRequestDTO request) {
        log.info("Creating new finding for transactionId: {}, controlCode: {}",
                request.getTransactionId(), request.getControlCode());

        // Jalon V4: Synchronous OpenFeign validation of remote transactionId
        validateRemoteTransaction(request.getTransactionId());

        Finding finding = findingMapper.toEntity(request);
        Finding savedFinding = findingRepository.save(finding);

        // Jalon V5: Asynchronous event publication to RabbitMQ
        publishFindingCreatedEvent(savedFinding);

        log.info("Finding created successfully with id: {}", savedFinding.getId());
        return findingMapper.toResponseDTO(savedFinding);
    }

    @Override
    @Transactional(readOnly = true)
    public FindingResponseDTO getFindingById(Long id) {
        log.info("Fetching finding by id: {}", id);
        Finding finding = findingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Finding not found with id: " + id));
        return findingMapper.toResponseDTO(finding);
    }

    @Override
    @Transactional(readOnly = true)
    public FindingDetailResponseDTO getFindingDetails(Long id) {
        log.info("Fetching enriched finding details for id: {}", id);
        Finding finding = findingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Finding not found with id: " + id));

        TransactionDetailResponseDTO transaction = fetchTransactionDetailsWithResilience(finding.getTransactionId());

        return FindingDetailResponseDTO.builder()
                .id(finding.getId())
                .transactionId(finding.getTransactionId())
                .controlCode(finding.getControlCode())
                .severity(finding.getSeverity())
                .description(finding.getDescription())
                .status(finding.getStatus())
                .createdAt(finding.getCreatedAt())
                .transaction(transaction)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FindingResponseDTO> getAllFindings() {
        log.info("Fetching all findings");
        List<Finding> findings = findingRepository.findAll();
        return findingMapper.toResponseDTOList(findings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FindingResponseDTO> getFindingsByTransactionId(Long transactionId) {
        log.info("Fetching findings for transactionId: {}", transactionId);
        List<Finding> findings = findingRepository.findByTransactionId(transactionId);
        return findingMapper.toResponseDTOList(findings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FindingResponseDTO> getFindingsBySeverity(String severity) {
        log.info("Fetching findings with severity: {}", severity);
        List<Finding> findings = findingRepository.findBySeverity(severity);
        return findingMapper.toResponseDTOList(findings);
    }

    @Override
    public FindingResponseDTO updateFinding(Long id, FindingRequestDTO request) {
        log.info("Updating finding with id: {}", id);

        Finding existingFinding = findingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Finding not found with id: " + id));

        // Validate transaction if changed
        if (!existingFinding.getTransactionId().equals(request.getTransactionId())) {
            validateRemoteTransaction(request.getTransactionId());
        }

        findingMapper.updateEntityFromDTO(request, existingFinding);
        Finding updatedFinding = findingRepository.save(existingFinding);

        log.info("Finding updated successfully with id: {}", updatedFinding.getId());
        return findingMapper.toResponseDTO(updatedFinding);
    }

    @Override
    public void deleteFinding(Long id) {
        log.info("Deleting finding with id: {}", id);

        Finding finding = findingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Finding not found with id: " + id));

        findingRepository.delete(finding);
        log.info("Finding deleted successfully with id: {}", id);
    }

    private void validateRemoteTransaction(Long transactionId) {
        if (transactionClient == null) {
            log.debug("TransactionClient not available in current context, skipping remote validation");
            return;
        }

        try {
            TransactionResponseDTO tx = transactionClient.getTransactionById(transactionId);
            if (tx == null || tx.getId() == null) {
                throw new ResourceNotFoundException("Transaction not found with id: " + transactionId);
            }
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (FeignException.NotFound e) {
            log.error("Remote transaction validation failed: transactionId {} does not exist", transactionId);
            throw new ResourceNotFoundException("Transaction not found with id: " + transactionId);
        } catch (Exception e) {
            log.warn("Non-fatal remote transaction validation warning: {}", e.getMessage());
        }
    }

    @CircuitBreaker(name = "transactionService", fallbackMethod = "fallbackGetTransactionDetails")
    @Retry(name = "transactionService")
    public TransactionDetailResponseDTO fetchTransactionDetailsWithResilience(Long transactionId) {
        if (transactionClient == null) {
            return null;
        }
        return transactionClient.getTransactionDetails(transactionId);
    }

    public TransactionDetailResponseDTO fallbackGetTransactionDetails(Long transactionId, Throwable throwable) {
        log.warn("Resilience4j Fallback triggered for transactionId {}. Reason: {}", transactionId, throwable.getMessage());
        return TransactionDetailResponseDTO.builder()
                .id(transactionId)
                .reference("REF-DEGRADED-" + transactionId)
                .status("DEGRADED")
                .build();
    }

    private void publishFindingCreatedEvent(Finding finding) {
        if (rabbitTemplate == null) {
            log.debug("RabbitTemplate not available, skipping event publication");
            return;
        }

        try {
            FindingCreatedEvent event = FindingCreatedEvent.builder()
                    .findingId(finding.getId())
                    .transactionId(finding.getTransactionId())
                    .controlCode(finding.getControlCode())
                    .severity(finding.getSeverity())
                    .description(finding.getDescription())
                    .status(finding.getStatus())
                    .createdAt(finding.getCreatedAt())
                    .build();

            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            log.info("Published FindingCreatedEvent to exchange '{}' with routingKey '{}' for findingId: {}",
                    exchange, routingKey, finding.getId());
        } catch (Exception e) {
            log.warn("Failed to publish FindingCreatedEvent to RabbitMQ (non-blocking): {}", e.getMessage());
        }
    }
}
