package com.audit.platform.supplier.controller;

import com.audit.platform.supplier.dto.request.SupplierRequestDTO;
import com.audit.platform.supplier.dto.response.SupplierResponseDTO;
import com.audit.platform.supplier.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/suppliers", "/suppliers"})
@RequiredArgsConstructor
@Slf4j
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    public ResponseEntity<SupplierResponseDTO> createSupplier(
            @Valid @RequestBody SupplierRequestDTO request) {
        log.info("REST request to create Supplier: {}", request.getName());
        SupplierResponseDTO response = supplierService.createSupplier(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponseDTO> getSupplierById(@PathVariable Long id) {
        log.info("REST request to get Supplier by id: {}", id);
        SupplierResponseDTO response = supplierService.getSupplierById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<SupplierResponseDTO>> getAllSuppliers() {
        log.info("REST request to get all Suppliers");
        List<SupplierResponseDTO> responses = supplierService.getAllSuppliers();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponseDTO> updateSupplier(
            @PathVariable Long id,
            @Valid @RequestBody SupplierRequestDTO request) {
        log.info("REST request to update Supplier with id: {}", id);
        SupplierResponseDTO response = supplierService.updateSupplier(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        log.info("REST request to delete Supplier with id: {}", id);
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }
}

