package com.audit.platform.supplier;

import com.audit.platform.supplier.dto.request.SupplierRequestDTO;
import com.audit.platform.supplier.dto.response.SupplierResponseDTO;
import com.audit.platform.supplier.entity.Supplier;
import com.audit.platform.supplier.exception.DuplicateResourceException;
import com.audit.platform.supplier.exception.ResourceNotFoundException;
import com.audit.platform.supplier.mapper.SupplierMapper;
import com.audit.platform.supplier.repository.SupplierRepository;
import com.audit.platform.supplier.service.impl.SupplierServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private SupplierMapper supplierMapper;

    @InjectMocks
    private SupplierServiceImpl supplierService;

    private Supplier supplier;
    private SupplierRequestDTO requestDTO;
    private SupplierResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        supplier = Supplier.builder()
                .id(1L)
                .name("Acme Corporation")
                .taxId("FR123456789")
                .country("France")
                .bankAccount("FR7630006000011234567890189")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .build();

        requestDTO = SupplierRequestDTO.builder()
                .name("Acme Corporation")
                .taxId("FR123456789")
                .country("France")
                .bankAccount("FR7630006000011234567890189")
                .status("ACTIVE")
                .build();

        responseDTO = SupplierResponseDTO.builder()
                .id(1L)
                .name("Acme Corporation")
                .taxId("FR123456789")
                .country("France")
                .bankAccount("FR7630006000011234567890189")
                .status("ACTIVE")
                .createdAt(supplier.getCreatedAt())
                .build();
    }

    @Test
    @DisplayName("createSupplier - Should succeed when taxId is unique")
    void createSupplier_Success() {
        when(supplierRepository.existsByTaxId(requestDTO.getTaxId())).thenReturn(false);
        when(supplierMapper.toEntity(requestDTO)).thenReturn(supplier);
        when(supplierRepository.save(supplier)).thenReturn(supplier);
        when(supplierMapper.toResponseDTO(supplier)).thenReturn(responseDTO);

        SupplierResponseDTO result = supplierService.createSupplier(requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTaxId()).isEqualTo("FR123456789");
        verify(supplierRepository, times(1)).save(supplier);
    }

    @Test
    @DisplayName("createSupplier - Should throw DuplicateResourceException when taxId already exists")
    void createSupplier_DuplicateTaxId() {
        when(supplierRepository.existsByTaxId(requestDTO.getTaxId())).thenReturn(true);

        assertThatThrownBy(() -> supplierService.createSupplier(requestDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Supplier already exists with taxId");

        verify(supplierRepository, never()).save(any());
    }

    @Test
    @DisplayName("getSupplierById - Should return DTO when found")
    void getSupplierById_Found() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(supplierMapper.toResponseDTO(supplier)).thenReturn(responseDTO);

        SupplierResponseDTO result = supplierService.getSupplierById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Acme Corporation");
    }

    @Test
    @DisplayName("getSupplierById - Should throw ResourceNotFoundException when not found")
    void getSupplierById_NotFound() {
        when(supplierRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supplierService.getSupplierById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Supplier not found with id: 99");
    }

    @Test
    @DisplayName("getAllSuppliers - Should return list of suppliers")
    void getAllSuppliers_Success() {
        List<Supplier> list = List.of(supplier);
        List<SupplierResponseDTO> dtoList = List.of(responseDTO);

        when(supplierRepository.findAll()).thenReturn(list);
        when(supplierMapper.toResponseDTOList(list)).thenReturn(dtoList);

        List<SupplierResponseDTO> results = supplierService.getAllSuppliers();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Acme Corporation");
    }

    @Test
    @DisplayName("updateSupplier - Should succeed when supplier exists")
    void updateSupplier_Success() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(supplierRepository.existsByTaxIdAndIdNot(requestDTO.getTaxId(), 1L)).thenReturn(false);
        doNothing().when(supplierMapper).updateEntityFromDTO(requestDTO, supplier);
        when(supplierRepository.save(supplier)).thenReturn(supplier);
        when(supplierMapper.toResponseDTO(supplier)).thenReturn(responseDTO);

        SupplierResponseDTO result = supplierService.updateSupplier(1L, requestDTO);

        assertThat(result).isNotNull();
        verify(supplierRepository, times(1)).save(supplier);
    }

    @Test
    @DisplayName("updateSupplier - Should throw DuplicateResourceException when taxId taken by another")
    void updateSupplier_TaxIdConflict() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(supplierRepository.existsByTaxIdAndIdNot(requestDTO.getTaxId(), 1L)).thenReturn(true);

        assertThatThrownBy(() -> supplierService.updateSupplier(1L, requestDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already in use by another supplier");

        verify(supplierRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteSupplier - Should succeed when supplier exists")
    void deleteSupplier_Success() {
        when(supplierRepository.existsById(1L)).thenReturn(true);
        doNothing().when(supplierRepository).deleteById(1L);

        supplierService.deleteSupplier(1L);

        verify(supplierRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteSupplier - Should throw ResourceNotFoundException when supplier does not exist")
    void deleteSupplier_NotFound() {
        when(supplierRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> supplierService.deleteSupplier(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Supplier not found with id: 99");

        verify(supplierRepository, never()).deleteById(any());
    }
}

