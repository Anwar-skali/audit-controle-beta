package com.audit.platform.supplier.service.impl;

import com.audit.platform.supplier.dto.request.SupplierRequestDTO;
import com.audit.platform.supplier.dto.response.SupplierResponseDTO;
import com.audit.platform.supplier.entity.Supplier;
import com.audit.platform.supplier.exception.DuplicateResourceException;
import com.audit.platform.supplier.exception.ResourceNotFoundException;
import com.audit.platform.supplier.mapper.SupplierMapper;
import com.audit.platform.supplier.repository.SupplierRepository;
import com.audit.platform.supplier.service.SupplierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    @Override
    public SupplierResponseDTO createSupplier(SupplierRequestDTO request) {
        log.info("Creating new supplier with taxId: {}", request.getTaxId());

        if (supplierRepository.existsByTaxId(request.getTaxId())) {
            throw new DuplicateResourceException(
                    "Supplier already exists with taxId: " + request.getTaxId());
        }

        Supplier supplier = supplierMapper.toEntity(request);
        Supplier savedSupplier = supplierRepository.save(supplier);

        log.info("Supplier created successfully with id: {}", savedSupplier.getId());
        return supplierMapper.toResponseDTO(savedSupplier);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponseDTO getSupplierById(Long id) {
        log.info("Fetching supplier by id: {}", id);
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
        return supplierMapper.toResponseDTO(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponseDTO> getAllSuppliers() {
        log.info("Fetching all suppliers");
        List<Supplier> suppliers = supplierRepository.findAll();
        return supplierMapper.toResponseDTOList(suppliers);
    }

    @Override
    public SupplierResponseDTO updateSupplier(Long id, SupplierRequestDTO request) {
        log.info("Updating supplier with id: {}", id);

        Supplier existingSupplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        if (supplierRepository.existsByTaxIdAndIdNot(request.getTaxId(), id)) {
            throw new DuplicateResourceException(
                    "Tax ID '" + request.getTaxId() + "' is already in use by another supplier");
        }

        supplierMapper.updateEntityFromDTO(request, existingSupplier);
        Supplier updatedSupplier = supplierRepository.save(existingSupplier);

        log.info("Supplier updated successfully with id: {}", updatedSupplier.getId());
        return supplierMapper.toResponseDTO(updatedSupplier);
    }

    @Override
    public void deleteSupplier(Long id) {
        log.info("Deleting supplier with id: {}", id);

        if (!supplierRepository.existsById(id)) {
            throw new ResourceNotFoundException("Supplier not found with id: " + id);
        }

        supplierRepository.deleteById(id);
        log.info("Supplier deleted successfully with id: {}", id);
    }
}

