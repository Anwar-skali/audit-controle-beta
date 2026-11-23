package com.audit.platform.supplier;

import com.audit.platform.supplier.dto.request.SupplierRequestDTO;
import com.audit.platform.supplier.entity.Supplier;
import com.audit.platform.supplier.repository.SupplierRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SupplierControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanUp() {
        supplierRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/suppliers - Should create supplier and return 201")
    void createSupplier_Success() throws Exception {
        SupplierRequestDTO request = SupplierRequestDTO.builder()
                .name("Global Audit Supplies")
                .taxId("TAX-2026-001")
                .country("Morocco")
                .bankAccount("MA6401123456789012345678")
                .status("ACTIVE")
                .build();

        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Global Audit Supplies"))
                .andExpect(jsonPath("$.taxId").value("TAX-2026-001"))
                .andExpect(jsonPath("$.country").value("Morocco"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/suppliers - Should return 400 when validation fails")
    void createSupplier_ValidationError() throws Exception {
        SupplierRequestDTO request = SupplierRequestDTO.builder()
                .name("") // Blank name
                .taxId("TAX-2026-002")
                .country("") // Blank country
                .bankAccount("")
                .build();

        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.validationErrors", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @DisplayName("POST /api/suppliers - Should return 409 when taxId already exists")
    void createSupplier_DuplicateTaxId() throws Exception {
        Supplier existing = Supplier.builder()
                .name("Initial Supplier")
                .taxId("TAX-DUPLICATE")
                .country("France")
                .bankAccount("FR76000111222333")
                .status("ACTIVE")
                .build();
        supplierRepository.save(existing);

        SupplierRequestDTO duplicateRequest = SupplierRequestDTO.builder()
                .name("Second Supplier")
                .taxId("TAX-DUPLICATE")
                .country("Germany")
                .bankAccount("DE89000111222333")
                .status("ACTIVE")
                .build();

        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message", containsString("already exists with taxId")));
    }

    @Test
    @DisplayName("GET /api/suppliers - Should return list of suppliers")
    void getAllSuppliers_Success() throws Exception {
        Supplier s1 = Supplier.builder()
                .name("Supplier Alpha")
                .taxId("TAX-ALPHA")
                .country("Morocco")
                .bankAccount("MA640001")
                .status("ACTIVE")
                .build();
        supplierRepository.save(s1);

        mockMvc.perform(get("/api/suppliers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Supplier Alpha"));
    }

    @Test
    @DisplayName("GET /api/suppliers/{id} - Should return supplier when found")
    void getSupplierById_Success() throws Exception {
        Supplier s = Supplier.builder()
                .name("Supplier Beta")
                .taxId("TAX-BETA")
                .country("Spain")
                .bankAccount("ES640002")
                .status("ACTIVE")
                .build();
        Supplier saved = supplierRepository.save(s);

        mockMvc.perform(get("/api/suppliers/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Supplier Beta"));
    }

    @Test
    @DisplayName("GET /api/suppliers/{id} - Should return 404 when not found")
    void getSupplierById_NotFound() throws Exception {
        mockMvc.perform(get("/api/suppliers/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("PUT /api/suppliers/{id} - Should update supplier and return 200")
    void updateSupplier_Success() throws Exception {
        Supplier s = Supplier.builder()
                .name("Supplier Gamma")
                .taxId("TAX-GAMMA")
                .country("Italy")
                .bankAccount("IT640003")
                .status("ACTIVE")
                .build();
        Supplier saved = supplierRepository.save(s);

        SupplierRequestDTO updateRequest = SupplierRequestDTO.builder()
                .name("Supplier Gamma Updated")
                .taxId("TAX-GAMMA")
                .country("Italy")
                .bankAccount("IT640003-NEW")
                .status("INACTIVE")
                .build();

        mockMvc.perform(put("/api/suppliers/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Supplier Gamma Updated"))
                .andExpect(jsonPath("$.bankAccount").value("IT640003-NEW"))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    @DisplayName("DELETE /api/suppliers/{id} - Should delete supplier and return 204")
    void deleteSupplier_Success() throws Exception {
        Supplier s = Supplier.builder()
                .name("Supplier Delta")
                .taxId("TAX-DELTA")
                .country("USA")
                .bankAccount("US640004")
                .status("ACTIVE")
                .build();
        Supplier saved = supplierRepository.save(s);

        mockMvc.perform(delete("/api/suppliers/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/suppliers/{id}", saved.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /suppliers - Also works with stripped route (/suppliers)")
    void getSuppliersDirectRoute_Success() throws Exception {
        mockMvc.perform(get("/suppliers"))
                .andExpect(status().isOk());
    }
}

