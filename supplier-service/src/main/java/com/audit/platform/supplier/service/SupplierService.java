package com.audit.platform.supplier.service;

import com.audit.platform.supplier.dto.request.SupplierRequestDTO;
import com.audit.platform.supplier.dto.response.SupplierResponseDTO;

import java.util.List;

public interface SupplierService {

    SupplierResponseDTO createSupplier(SupplierRequestDTO request);

    SupplierResponseDTO getSupplierById(Long id);

    List<SupplierResponseDTO> getAllSuppliers();

    SupplierResponseDTO updateSupplier(Long id, SupplierRequestDTO request);

    void deleteSupplier(Long id);
}

