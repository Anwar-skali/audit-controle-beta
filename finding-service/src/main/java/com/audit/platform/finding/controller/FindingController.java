package com.audit.platform.finding.controller;

import com.audit.platform.finding.dto.request.FindingRequestDTO;
import com.audit.platform.finding.dto.response.FindingDetailResponseDTO;
import com.audit.platform.finding.dto.response.FindingResponseDTO;
import com.audit.platform.finding.service.FindingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/findings", "/findings"})
@RequiredArgsConstructor
@Slf4j
public class FindingController {

    private final FindingService findingService;

    @PostMapping
    public ResponseEntity<FindingResponseDTO> createFinding(
            @Valid @RequestBody FindingRequestDTO request) {
        log.info("REST request to create Finding for transactionId: {}", request.getTransactionId());
        FindingResponseDTO response = findingService.createFinding(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FindingResponseDTO> getFindingById(@PathVariable Long id) {
        log.info("REST request to get Finding by id: {}", id);
        FindingResponseDTO response = findingService.getFindingById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Jalon V4: Enriched consultation endpoint returning aggregated Finding + Transaction + Supplier DTO.
     */
    @GetMapping("/{id}/details")
    public ResponseEntity<FindingDetailResponseDTO> getFindingDetails(@PathVariable Long id) {
        log.info("REST request to get enriched Finding details by id: {}", id);
        FindingDetailResponseDTO response = findingService.getFindingDetails(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<FindingResponseDTO>> getAllFindings(
            @RequestParam(required = false) Long transactionId,
            @RequestParam(required = false) String severity) {
        if (transactionId != null) {
            log.info("REST request to get Findings by transactionId: {}", transactionId);
            return ResponseEntity.ok(findingService.getFindingsByTransactionId(transactionId));
        }
        if (severity != null) {
            log.info("REST request to get Findings by severity: {}", severity);
            return ResponseEntity.ok(findingService.getFindingsBySeverity(severity));
        }
        log.info("REST request to get all Findings");
        return ResponseEntity.ok(findingService.getAllFindings());
    }

    @PutMapping("/{id}")
    public ResponseEntity<FindingResponseDTO> updateFinding(
            @PathVariable Long id,
            @Valid @RequestBody FindingRequestDTO request) {
        log.info("REST request to update Finding with id: {}", id);
        FindingResponseDTO response = findingService.updateFinding(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFinding(@PathVariable Long id) {
        log.info("REST request to delete Finding with id: {}", id);
        findingService.deleteFinding(id);
        return ResponseEntity.noContent().build();
    }
}
