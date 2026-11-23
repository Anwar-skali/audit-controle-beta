package com.audit.platform.finding;

import com.audit.platform.finding.client.TransactionClient;
import com.audit.platform.finding.dto.external.TransactionDetailResponseDTO;
import com.audit.platform.finding.dto.external.TransactionResponseDTO;
import com.audit.platform.finding.dto.request.FindingRequestDTO;
import com.audit.platform.finding.entity.Finding;
import com.audit.platform.finding.repository.FindingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FindingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FindingRepository findingRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionClient transactionClient;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void cleanUp() {
        findingRepository.deleteAll();
        when(transactionClient.getTransactionById(anyLong())).thenReturn(
                TransactionResponseDTO.builder().id(101L).reference("TX-101").status("PENDING").build()
        );
        when(transactionClient.getTransactionDetails(anyLong())).thenReturn(
                TransactionDetailResponseDTO.builder().id(101L).reference("TX-101").status("PENDING").build()
        );
    }

    @Test
    @DisplayName("POST /api/findings - Should create finding and return 201")
    void createFinding_Success() throws Exception {
        FindingRequestDTO request = FindingRequestDTO.builder()
                .transactionId(101L)
                .controlCode("CTRL_THRESHOLD_EXCEEDED")
                .severity("HIGH")
                .description("Montant de la facture supérieur au plafond autorisé")
                .status("OPEN")
                .build();

        mockMvc.perform(post("/api/findings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.transactionId").value(101))
                .andExpect(jsonPath("$.controlCode").value("CTRL_THRESHOLD_EXCEEDED"))
                .andExpect(jsonPath("$.severity").value("HIGH"))
                .andExpect(jsonPath("$.description").value("Montant de la facture supérieur au plafond autorisé"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/findings - Should return 400 when validation fails")
    void createFinding_ValidationError() throws Exception {
        FindingRequestDTO request = FindingRequestDTO.builder()
                .transactionId(-1L) // negative
                .controlCode("") // blank
                .severity("INVALID_SEVERITY") // invalid pattern
                .description("") // blank
                .build();

        mockMvc.perform(post("/api/findings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.validationErrors", hasSize(greaterThanOrEqualTo(4))));
    }

    @Test
    @DisplayName("GET /api/findings - Should return list of all findings")
    void getAllFindings_Success() throws Exception {
        Finding f = Finding.builder()
                .transactionId(200L)
                .controlCode("CTRL_SPLIT_PO")
                .severity("MEDIUM")
                .description("Suspicion de fractionnement de bon de commande")
                .status("OPEN")
                .build();
        findingRepository.save(f);

        mockMvc.perform(get("/api/findings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].controlCode").value("CTRL_SPLIT_PO"));
    }

    @Test
    @DisplayName("GET /api/findings?transactionId=200 - Should filter by transactionId")
    void getFindingsByTransactionId_Success() throws Exception {
        Finding f1 = Finding.builder()
                .transactionId(200L)
                .controlCode("CTRL_SPLIT_1")
                .severity("LOW")
                .description("Test finding 1")
                .status("OPEN")
                .build();
        Finding f2 = Finding.builder()
                .transactionId(300L)
                .controlCode("CTRL_SPLIT_2")
                .severity("LOW")
                .description("Test finding 2")
                .status("OPEN")
                .build();
        findingRepository.save(f1);
        findingRepository.save(f2);

        mockMvc.perform(get("/api/findings").param("transactionId", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].transactionId").value(200));
    }

    @Test
    @DisplayName("GET /api/findings?severity=CRITICAL - Should filter by severity")
    void getFindingsBySeverity_Success() throws Exception {
        Finding f1 = Finding.builder()
                .transactionId(400L)
                .controlCode("CTRL_CRIT")
                .severity("CRITICAL")
                .description("Paiement suspect vers paradis fiscal")
                .status("OPEN")
                .build();
        Finding f2 = Finding.builder()
                .transactionId(401L)
                .controlCode("CTRL_LOW")
                .severity("LOW")
                .description("Erreur mineure de libellé")
                .status("OPEN")
                .build();
        findingRepository.save(f1);
        findingRepository.save(f2);

        mockMvc.perform(get("/api/findings").param("severity", "CRITICAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].controlCode").value("CTRL_CRIT"))
                .andExpect(jsonPath("$[0].severity").value("CRITICAL"));
    }

    @Test
    @DisplayName("GET /api/findings/{id} - Should return finding when found")
    void getFindingById_Success() throws Exception {
        Finding f = Finding.builder()
                .transactionId(500L)
                .controlCode("CTRL_DUP_IBAN")
                .severity("HIGH")
                .description("IBAN utilisé par un tiers non référencé")
                .status("OPEN")
                .build();
        Finding saved = findingRepository.save(f);

        mockMvc.perform(get("/api/findings/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.controlCode").value("CTRL_DUP_IBAN"));
    }

    @Test
    @DisplayName("GET /api/findings/{id} - Should return 404 when not found")
    void getFindingById_NotFound() throws Exception {
        mockMvc.perform(get("/api/findings/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("PUT /api/findings/{id} - Should update finding and return 200")
    void updateFinding_Success() throws Exception {
        Finding f = Finding.builder()
                .transactionId(600L)
                .controlCode("CTRL_INITIAL")
                .severity("MEDIUM")
                .description("Description initiale")
                .status("OPEN")
                .build();
        Finding saved = findingRepository.save(f);

        FindingRequestDTO updateRequest = FindingRequestDTO.builder()
                .transactionId(600L)
                .controlCode("CTRL_INITIAL")
                .severity("HIGH")
                .description("Description enrichie après analyse")
                .status("IN_REVIEW")
                .build();

        mockMvc.perform(put("/api/findings/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.severity").value("HIGH"))
                .andExpect(jsonPath("$.description").value("Description enrichie après analyse"))
                .andExpect(jsonPath("$.status").value("IN_REVIEW"));
    }

    @Test
    @DisplayName("DELETE /api/findings/{id} - Should delete finding and return 204")
    void deleteFinding_Success() throws Exception {
        Finding f = Finding.builder()
                .transactionId(700L)
                .controlCode("CTRL_TO_DELETE")
                .severity("LOW")
                .description("Constat à supprimer")
                .status("OPEN")
                .build();
        Finding saved = findingRepository.save(f);

        mockMvc.perform(delete("/api/findings/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/findings/{id}", saved.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /findings - Also works with stripped route (/findings)")
    void getFindingsDirectRoute_Success() throws Exception {
        mockMvc.perform(get("/findings"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/findings/{id}/details - Jalon V4: Should return aggregated finding with transaction and supplier details")
    void getFindingDetails_Success() throws Exception {
        Finding f = Finding.builder()
                .transactionId(101L)
                .controlCode("CTRL_AGGREGATED")
                .severity("HIGH")
                .description("Constat détaillé agrégé")
                .status("OPEN")
                .createdAt(LocalDateTime.now())
                .build();
        Finding saved = findingRepository.save(f);

        mockMvc.perform(get("/api/findings/{id}/details", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.controlCode").value("CTRL_AGGREGATED"))
                .andExpect(jsonPath("$.transaction.reference").value("TX-101"));
    }
}

