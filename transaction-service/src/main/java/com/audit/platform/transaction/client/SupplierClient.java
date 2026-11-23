package com.audit.platform.transaction.client;

import com.audit.platform.transaction.dto.external.SupplierResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "supplier-service", path = "/api/suppliers")
public interface SupplierClient {

    @GetMapping("/{id}")
    SupplierResponseDTO getSupplierById(@PathVariable("id") Long id);
}

