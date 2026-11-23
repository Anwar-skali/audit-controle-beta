package com.audit.platform.transaction;

import com.audit.platform.transaction.client.SupplierClient;
import com.audit.platform.transaction.dto.external.SupplierResponseDTO;
import com.audit.platform.transaction.dto.request.TransactionRequestDTO;
import com.audit.platform.transaction.dto.response.TransactionDetailResponseDTO;
import com.audit.platform.transaction.dto.response.TransactionResponseDTO;
import com.audit.platform.transaction.entity.Transaction;
import com.audit.platform.transaction.exception.DuplicateResourceException;
import com.audit.platform.transaction.exception.ResourceNotFoundException;
import com.audit.platform.transaction.mapper.TransactionMapper;
import com.audit.platform.transaction.repository.TransactionRepository;
import com.audit.platform.transaction.service.impl.TransactionServiceImpl;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private SupplierClient supplierClient;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Transaction transaction;
    private TransactionRequestDTO requestDTO;
    private TransactionResponseDTO responseDTO;
    private SupplierResponseDTO supplierDTO;

    @BeforeEach
    void setUp() {
        transaction = Transaction.builder()
                .id(1L)
                .reference("INV-2026-001")
                .supplierId(10L)
                .amount(new BigDecimal("15000.00"))
                .currency("MAD")
                .type("INVOICE")
                .transactionDate(LocalDateTime.now())
                .createdBy("auditor1")
                .status("PENDING")
                .build();

        requestDTO = TransactionRequestDTO.builder()
                .reference("INV-2026-001")
                .supplierId(10L)
                .amount(new BigDecimal("15000.00"))
                .currency("MAD")
                .type("INVOICE")
                .createdBy("auditor1")
                .build();

        responseDTO = TransactionResponseDTO.builder()
                .id(1L)
                .reference("INV-2026-001")
                .supplierId(10L)
                .amount(new BigDecimal("15000.00"))
                .currency("MAD")
                .type("INVOICE")
                .transactionDate(transaction.getTransactionDate())
                .createdBy("auditor1")
                .status("PENDING")
                .build();

        supplierDTO = SupplierResponseDTO.builder()
                .id(10L)
                .name("Fournisseur Atlas")
                .taxId("TAX-MA-12345")
                .country("Maroc")
                .status("ACTIVE")
                .build();
    }

    @Test
    @DisplayName("Should create transaction successfully when reference is unique and supplier exists")
    void createTransaction_Success() {
        when(transactionRepository.existsByReference("INV-2026-001")).thenReturn(false);
        when(supplierClient.getSupplierById(10L)).thenReturn(supplierDTO);
        when(transactionMapper.toEntity(requestDTO)).thenReturn(transaction);
        when(transactionRepository.save(transaction)).thenReturn(transaction);
        when(transactionMapper.toResponseDTO(transaction)).thenReturn(responseDTO);

        TransactionResponseDTO result = transactionService.createTransaction(requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getReference()).isEqualTo("INV-2026-001");
        verify(transactionRepository).save(transaction);
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when reference already exists")
    void createTransaction_DuplicateReference_ThrowsException() {
        when(transactionRepository.existsByReference("INV-2026-001")).thenReturn(true);

        assertThatThrownBy(() -> transactionService.createTransaction(requestDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Transaction already exists with reference: INV-2026-001");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when remote supplier does not exist (V4 validation)")
    void createTransaction_SupplierNotFound_ThrowsException() {
        when(transactionRepository.existsByReference("INV-2026-001")).thenReturn(false);
        when(supplierClient.getSupplierById(10L)).thenThrow(new ResourceNotFoundException("Supplier not found with id: 10"));

        assertThatThrownBy(() -> transactionService.createTransaction(requestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Supplier not found with id: 10");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fetch enriched transaction details with supplier info (V4 aggregation)")
    void getTransactionDetails_Success() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(supplierClient.getSupplierById(10L)).thenReturn(supplierDTO);

        TransactionDetailResponseDTO result = transactionService.getTransactionDetails(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getSupplier()).isNotNull();
        assertThat(result.getSupplier().getName()).isEqualTo("Fournisseur Atlas");
    }

    @Test
    @DisplayName("Should return transaction by id when found")
    void getTransactionById_Success() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionMapper.toResponseDTO(transaction)).thenReturn(responseDTO);

        TransactionResponseDTO result = transactionService.getTransactionById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when transaction id not found")
    void getTransactionById_NotFound() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransactionById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Transaction not found with id: 99");
    }

    @Test
    @DisplayName("Should return all transactions")
    void getAllTransactions_Success() {
        when(transactionRepository.findAll()).thenReturn(List.of(transaction));
        when(transactionMapper.toResponseDTOList(List.of(transaction))).thenReturn(List.of(responseDTO));

        List<TransactionResponseDTO> result = transactionService.getAllTransactions();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should return transactions by supplierId")
    void getTransactionsBySupplierId_Success() {
        when(transactionRepository.findBySupplierId(10L)).thenReturn(List.of(transaction));
        when(transactionMapper.toResponseDTOList(List.of(transaction))).thenReturn(List.of(responseDTO));

        List<TransactionResponseDTO> result = transactionService.getTransactionsBySupplierId(10L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should update transaction successfully")
    void updateTransaction_Success() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.existsByReferenceAndIdNot("INV-2026-001", 1L)).thenReturn(false);
        when(transactionRepository.save(transaction)).thenReturn(transaction);
        when(transactionMapper.toResponseDTO(transaction)).thenReturn(responseDTO);

        TransactionResponseDTO result = transactionService.updateTransaction(1L, requestDTO);

        assertThat(result).isNotNull();
        verify(transactionRepository).save(transaction);
    }

    @Test
    @DisplayName("Should delete transaction when found")
    void deleteTransaction_Success() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        doNothing().when(transactionRepository).delete(transaction);

        transactionService.deleteTransaction(1L);

        verify(transactionRepository).delete(transaction);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent transaction")
    void deleteTransaction_NotFound() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.deleteTransaction(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Transaction not found with id: 99");
    }
}
