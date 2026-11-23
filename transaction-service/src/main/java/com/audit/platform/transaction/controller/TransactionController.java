package com.audit.platform.transaction.controller;

import com.audit.platform.transaction.dto.request.TransactionRequestDTO;
import com.audit.platform.transaction.dto.response.TransactionDetailResponseDTO;
import com.audit.platform.transaction.dto.response.TransactionResponseDTO;
import com.audit.platform.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/transactions", "/transactions"})
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponseDTO> createTransaction(
            @Valid @RequestBody TransactionRequestDTO request) {
        log.info("REST request to create Transaction with reference: {}", request.getReference());
        TransactionResponseDTO response = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> getTransactionById(@PathVariable Long id) {
        log.info("REST request to get Transaction by id: {}", id);
        TransactionResponseDTO response = transactionService.getTransactionById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Jalon V4: Enriched consultation endpoint returning aggregated Transaction + Supplier DTO.
     */
    @GetMapping("/{id}/details")
    public ResponseEntity<TransactionDetailResponseDTO> getTransactionDetails(@PathVariable Long id) {
        log.info("REST request to get enriched Transaction details by id: {}", id);
        TransactionDetailResponseDTO response = transactionService.getTransactionDetails(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponseDTO>> getAllTransactions(
            @RequestParam(required = false) Long supplierId) {
        if (supplierId != null) {
            log.info("REST request to get Transactions by supplierId: {}", supplierId);
            return ResponseEntity.ok(transactionService.getTransactionsBySupplierId(supplierId));
        }
        log.info("REST request to get all Transactions");
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequestDTO request) {
        log.info("REST request to update Transaction with id: {}", id);
        TransactionResponseDTO response = transactionService.updateTransaction(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        log.info("REST request to delete Transaction with id: {}", id);
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
}
