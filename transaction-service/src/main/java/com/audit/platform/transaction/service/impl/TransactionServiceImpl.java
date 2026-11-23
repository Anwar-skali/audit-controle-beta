package com.audit.platform.transaction.service.impl;

import com.audit.platform.transaction.client.SupplierClient;
import com.audit.platform.transaction.dto.external.SupplierResponseDTO;
import com.audit.platform.transaction.dto.request.TransactionRequestDTO;
import com.audit.platform.transaction.dto.response.TransactionDetailResponseDTO;
import com.audit.platform.transaction.dto.response.TransactionResponseDTO;
import com.audit.platform.transaction.entity.Transaction;
import com.audit.platform.transaction.exception.DuplicateResourceException;
import com.audit.platform.transaction.exception.ResourceNotFoundException;
import com.audit.platform.transaction.mapper.TransactionMapper;
import com.audit.platform.transaction.repository.TransactionRepository;
import com.audit.platform.transaction.service.TransactionService;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final SupplierClient supplierClient;

    @Override
    public TransactionResponseDTO createTransaction(TransactionRequestDTO request) {
        log.info("Creating new transaction with reference: {}", request.getReference());

        // Jalon V6: Idempotence check on reference
        if (transactionRepository.existsByReference(request.getReference())) {
            throw new DuplicateResourceException(
                    "Transaction already exists with reference: " + request.getReference());
        }

        // Jalon V4: Synchronous OpenFeign validation of remote supplierId
        validateRemoteSupplier(request.getSupplierId());

        Transaction transaction = transactionMapper.toEntity(request);
        Transaction savedTransaction = transactionRepository.save(transaction);

        log.info("Transaction created successfully with id: {}", savedTransaction.getId());
        return transactionMapper.toResponseDTO(savedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponseDTO getTransactionById(Long id) {
        log.info("Fetching transaction by id: {}", id);
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
        return transactionMapper.toResponseDTO(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionDetailResponseDTO getTransactionDetails(Long id) {
        log.info("Fetching enriched transaction details for id: {}", id);
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        SupplierResponseDTO supplier = fetchSupplierWithResilience(transaction.getSupplierId());

        return TransactionDetailResponseDTO.builder()
                .id(transaction.getId())
                .reference(transaction.getReference())
                .supplierId(transaction.getSupplierId())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .type(transaction.getType())
                .transactionDate(transaction.getTransactionDate())
                .createdBy(transaction.getCreatedBy())
                .status(transaction.getStatus())
                .supplier(supplier)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getAllTransactions() {
        log.info("Fetching all transactions");
        List<Transaction> transactions = transactionRepository.findAll();
        return transactionMapper.toResponseDTOList(transactions);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getTransactionsBySupplierId(Long supplierId) {
        log.info("Fetching transactions for supplierId: {}", supplierId);
        List<Transaction> transactions = transactionRepository.findBySupplierId(supplierId);
        return transactionMapper.toResponseDTOList(transactions);
    }

    @Override
    public TransactionResponseDTO updateTransaction(Long id, TransactionRequestDTO request) {
        log.info("Updating transaction with id: {}", id);

        Transaction existingTransaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        if (transactionRepository.existsByReferenceAndIdNot(request.getReference(), id)) {
            throw new DuplicateResourceException(
                    "Transaction reference '" + request.getReference() + "' is already in use by another transaction");
        }

        // Validate supplier on update if changed
        if (!existingTransaction.getSupplierId().equals(request.getSupplierId())) {
            validateRemoteSupplier(request.getSupplierId());
        }

        transactionMapper.updateEntityFromDTO(request, existingTransaction);
        Transaction updatedTransaction = transactionRepository.save(existingTransaction);

        log.info("Transaction updated successfully with id: {}", updatedTransaction.getId());
        return transactionMapper.toResponseDTO(updatedTransaction);
    }

    @Override
    public void deleteTransaction(Long id) {
        log.info("Deleting transaction with id: {}", id);

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        transactionRepository.delete(transaction);
        log.info("Transaction deleted successfully with id: {}", id);
    }

    private void validateRemoteSupplier(Long supplierId) {
        if (supplierClient == null) {
            log.debug("SupplierClient not available in current context, skipping remote validation");
            return;
        }

        try {
            SupplierResponseDTO supplier = fetchSupplierWithResilience(supplierId);
            if (supplier == null || supplier.getId() == null) {
                throw new ResourceNotFoundException("Supplier not found with id: " + supplierId);
            }
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (FeignException.NotFound e) {
            log.error("Remote supplier validation failed: supplierId {} does not exist", supplierId);
            throw new ResourceNotFoundException("Supplier not found with id: " + supplierId);
        } catch (Exception e) {
            log.warn("Non-fatal remote supplier validation warning: {}", e.getMessage());
        }
    }

    @CircuitBreaker(name = "supplierService", fallbackMethod = "fallbackGetSupplier")
    @Retry(name = "supplierService")
    public SupplierResponseDTO fetchSupplierWithResilience(Long supplierId) {
        if (supplierClient == null) {
            return null;
        }
        return supplierClient.getSupplierById(supplierId);
    }

    public SupplierResponseDTO fallbackGetSupplier(Long supplierId, Throwable throwable) {
        log.warn("Resilience4j Fallback triggered for supplierId {}. Reason: {}", supplierId, throwable.getMessage());
        return SupplierResponseDTO.builder()
                .id(supplierId)
                .name("Supplier Unavailable (Circuit Breaker Degraded Mode)")
                .status("DEGRADED")
                .build();
    }
}
