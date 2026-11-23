package com.audit.platform.transaction;

import com.audit.platform.transaction.client.SupplierClient;
import com.audit.platform.transaction.dto.external.SupplierResponseDTO;
import com.audit.platform.transaction.dto.request.TransactionRequestDTO;
import com.audit.platform.transaction.entity.Transaction;
import com.audit.platform.transaction.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SupplierClient supplierClient;

    @BeforeEach
    void cleanUp() {
        transactionRepository.deleteAll();
        when(supplierClient.getSupplierById(anyLong())).thenReturn(
                SupplierResponseDTO.builder()
                        .id(100L)
                        .name("Fournisseur Test")
                        .taxId("TAX-TEST-100")
                        .status("ACTIVE")
                        .build()
        );
    }

    @Test
    @DisplayName("POST /api/transactions - Should create transaction and return 201")
    void createTransaction_Success() throws Exception {
        TransactionRequestDTO request = TransactionRequestDTO.builder()
                .reference("TX-2026-001")
                .supplierId(100L)
                .amount(new BigDecimal("4500.00"))
                .currency("EUR")
                .type("INVOICE")
                .createdBy("auditor_lead")
                .status("PENDING")
                .build();

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.reference").value("TX-2026-001"))
                .andExpect(jsonPath("$.supplierId").value(100))
                .andExpect(jsonPath("$.amount").value(4500.00))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.type").value("INVOICE"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /api/transactions - Should return 400 when validation fails")
    void createTransaction_ValidationError() throws Exception {
        TransactionRequestDTO request = TransactionRequestDTO.builder()
                .reference("") // blank
                .supplierId(-5L) // negative
                .amount(new BigDecimal("-100.00")) // negative amount
                .currency("TOOLONG") // not 3 letters
                .type("")
                .createdBy("")
                .build();

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.validationErrors", hasSize(greaterThanOrEqualTo(4))));
    }

    @Test
    @DisplayName("POST /api/transactions - Should return 409 when reference already exists")
    void createTransaction_DuplicateReference() throws Exception {
        Transaction existing = Transaction.builder()
                .reference("TX-DUP-REF")
                .supplierId(10L)
                .amount(new BigDecimal("1000.00"))
                .currency("USD")
                .type("PAYMENT")
                .transactionDate(LocalDateTime.now())
                .createdBy("admin")
                .status("POSTED")
                .build();
        transactionRepository.save(existing);

        TransactionRequestDTO duplicateRequest = TransactionRequestDTO.builder()
                .reference("TX-DUP-REF")
                .supplierId(20L)
                .amount(new BigDecimal("2000.00"))
                .currency("USD")
                .type("PAYMENT")
                .createdBy("admin")
                .status("PENDING")
                .build();

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message", containsString("already exists with reference")));
    }

    @Test
    @DisplayName("GET /api/transactions - Should return list of transactions")
    void getAllTransactions_Success() throws Exception {
        Transaction t = Transaction.builder()
                .reference("TX-LIST-1")
                .supplierId(15L)
                .amount(new BigDecimal("990.00"))
                .currency("EUR")
                .type("INVOICE")
                .transactionDate(LocalDateTime.now())
                .createdBy("auditor")
                .status("PENDING")
                .build();
        transactionRepository.save(t);

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].reference").value("TX-LIST-1"));
    }

    @Test
    @DisplayName("GET /api/transactions?supplierId=15 - Should filter by supplierId")
    void getTransactionsBySupplierId_Success() throws Exception {
        Transaction t1 = Transaction.builder()
                .reference("TX-SUPP-1")
                .supplierId(15L)
                .amount(new BigDecimal("500.00"))
                .currency("EUR")
                .type("INVOICE")
                .transactionDate(LocalDateTime.now())
                .createdBy("auditor")
                .status("PENDING")
                .build();
        Transaction t2 = Transaction.builder()
                .reference("TX-SUPP-2")
                .supplierId(25L)
                .amount(new BigDecimal("700.00"))
                .currency("EUR")
                .type("INVOICE")
                .transactionDate(LocalDateTime.now())
                .createdBy("auditor")
                .status("PENDING")
                .build();
        transactionRepository.save(t1);
        transactionRepository.save(t2);

        mockMvc.perform(get("/api/transactions").param("supplierId", "15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].reference").value("TX-SUPP-1"));
    }

    @Test
    @DisplayName("GET /api/transactions/{id} - Should return transaction when found")
    void getTransactionById_Success() throws Exception {
        Transaction t = Transaction.builder()
                .reference("TX-SINGLE")
                .supplierId(30L)
                .amount(new BigDecimal("1234.50"))
                .currency("EUR")
                .type("INVOICE")
                .transactionDate(LocalDateTime.now())
                .createdBy("auditor")
                .status("PENDING")
                .build();
        Transaction saved = transactionRepository.save(t);

        mockMvc.perform(get("/api/transactions/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.reference").value("TX-SINGLE"));
    }

    @Test
    @DisplayName("GET /api/transactions/{id} - Should return 404 when not found")
    void getTransactionById_NotFound() throws Exception {
        mockMvc.perform(get("/api/transactions/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("PUT /api/transactions/{id} - Should update transaction and return 200")
    void updateTransaction_Success() throws Exception {
        Transaction t = Transaction.builder()
                .reference("TX-UPDATE")
                .supplierId(40L)
                .amount(new BigDecimal("100.00"))
                .currency("EUR")
                .type("INVOICE")
                .transactionDate(LocalDateTime.now())
                .createdBy("auditor")
                .status("PENDING")
                .build();
        Transaction saved = transactionRepository.save(t);

        TransactionRequestDTO updateRequest = TransactionRequestDTO.builder()
                .reference("TX-UPDATE")
                .supplierId(40L)
                .amount(new BigDecimal("250.00"))
                .currency("EUR")
                .type("INVOICE")
                .createdBy("auditor")
                .status("VALIDATED")
                .build();

        mockMvc.perform(put("/api/transactions/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.amount").value(250.00))
                .andExpect(jsonPath("$.status").value("VALIDATED"));
    }

    @Test
    @DisplayName("DELETE /api/transactions/{id} - Should delete transaction and return 204")
    void deleteTransaction_Success() throws Exception {
        Transaction t = Transaction.builder()
                .reference("TX-DELETE")
                .supplierId(50L)
                .amount(new BigDecimal("100.00"))
                .currency("EUR")
                .type("INVOICE")
                .transactionDate(LocalDateTime.now())
                .createdBy("auditor")
                .status("PENDING")
                .build();
        Transaction saved = transactionRepository.save(t);

        mockMvc.perform(delete("/api/transactions/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/transactions/{id}", saved.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /transactions - Also works with stripped route (/transactions)")
    void getTransactionsDirectRoute_Success() throws Exception {
        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/transactions/{id}/details - Jalon V4: Should return aggregated transaction and supplier details")
    void getTransactionDetails_Success() throws Exception {
        Transaction t = Transaction.builder()
                .reference("TX-DETAILS-001")
                .supplierId(100L)
                .amount(new BigDecimal("9999.00"))
                .currency("MAD")
                .type("PAYMENT")
                .transactionDate(LocalDateTime.now())
                .createdBy("auditor")
                .status("PENDING")
                .build();
        Transaction saved = transactionRepository.save(t);

        mockMvc.perform(get("/api/transactions/{id}/details", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.reference").value("TX-DETAILS-001"))
                .andExpect(jsonPath("$.supplier.name").value("Fournisseur Test"))
                .andExpect(jsonPath("$.supplier.taxId").value("TAX-TEST-100"));
    }
}

