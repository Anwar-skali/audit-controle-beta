package com.audit.platform.finding;

import com.audit.platform.finding.client.TransactionClient;
import com.audit.platform.finding.dto.external.TransactionDetailResponseDTO;
import com.audit.platform.finding.dto.external.TransactionResponseDTO;
import com.audit.platform.finding.dto.request.FindingRequestDTO;
import com.audit.platform.finding.dto.response.FindingDetailResponseDTO;
import com.audit.platform.finding.dto.response.FindingResponseDTO;
import com.audit.platform.finding.entity.Finding;
import com.audit.platform.finding.exception.ResourceNotFoundException;
import com.audit.platform.finding.mapper.FindingMapper;
import com.audit.platform.finding.repository.FindingRepository;
import com.audit.platform.finding.service.impl.FindingServiceImpl;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindingServiceTest {

    @Mock
    private FindingRepository findingRepository;

    @Mock
    private FindingMapper findingMapper;

    @Mock
    private TransactionClient transactionClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private FindingServiceImpl findingService;

    private Finding finding;
    private FindingRequestDTO requestDTO;
    private FindingResponseDTO responseDTO;
    private TransactionResponseDTO txResponseDTO;
    private TransactionDetailResponseDTO txDetailDTO;

    @BeforeEach
    void setUp() {
        finding = Finding.builder()
                .id(1L)
                .transactionId(100L)
                .controlCode("CTRL_DUP_INV")
                .severity("HIGH")
                .description("Duplicate invoice detected")
                .status("OPEN")
                .createdAt(LocalDateTime.now())
                .build();

        requestDTO = FindingRequestDTO.builder()
                .transactionId(100L)
                .controlCode("CTRL_DUP_INV")
                .severity("HIGH")
                .description("Duplicate invoice detected")
                .build();

        responseDTO = FindingResponseDTO.builder()
                .id(1L)
                .transactionId(100L)
                .controlCode("CTRL_DUP_INV")
                .severity("HIGH")
                .description("Duplicate invoice detected")
                .status("OPEN")
                .createdAt(finding.getCreatedAt())
                .build();

        txResponseDTO = TransactionResponseDTO.builder()
                .id(100L)
                .reference("INV-100")
                .amount(new BigDecimal("5000.00"))
                .currency("MAD")
                .status("PENDING")
                .build();

        txDetailDTO = TransactionDetailResponseDTO.builder()
                .id(100L)
                .reference("INV-100")
                .amount(new BigDecimal("5000.00"))
                .currency("MAD")
                .status("PENDING")
                .build();
    }

    @Test
    @DisplayName("Should create finding successfully when transaction exists (V4 validation & V5 event)")
    void createFinding_Success() {
        when(transactionClient.getTransactionById(100L)).thenReturn(txResponseDTO);
        when(findingMapper.toEntity(requestDTO)).thenReturn(finding);
        when(findingRepository.save(finding)).thenReturn(finding);
        when(findingMapper.toResponseDTO(finding)).thenReturn(responseDTO);

        FindingResponseDTO result = findingService.createFinding(requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getControlCode()).isEqualTo("CTRL_DUP_INV");
        verify(findingRepository).save(finding);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when remote transaction not found")
    void createFinding_TransactionNotFound_ThrowsException() {
        when(transactionClient.getTransactionById(100L)).thenThrow(new ResourceNotFoundException("Transaction not found with id: 100"));

        assertThatThrownBy(() -> findingService.createFinding(requestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Transaction not found with id: 100");

        verify(findingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return enriched finding details (V4 aggregation)")
    void getFindingDetails_Success() {
        when(findingRepository.findById(1L)).thenReturn(Optional.of(finding));
        when(transactionClient.getTransactionDetails(100L)).thenReturn(txDetailDTO);

        FindingDetailResponseDTO result = findingService.getFindingDetails(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTransaction()).isNotNull();
        assertThat(result.getTransaction().getReference()).isEqualTo("INV-100");
    }

    @Test
    @DisplayName("Should return finding by id when found")
    void getFindingById_Success() {
        when(findingRepository.findById(1L)).thenReturn(Optional.of(finding));
        when(findingMapper.toResponseDTO(finding)).thenReturn(responseDTO);

        FindingResponseDTO result = findingService.getFindingById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when finding id not found")
    void getFindingById_NotFound() {
        when(findingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> findingService.getFindingById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Finding not found with id: 99");
    }

    @Test
    @DisplayName("Should return all findings")
    void getAllFindings_Success() {
        when(findingRepository.findAll()).thenReturn(List.of(finding));
        when(findingMapper.toResponseDTOList(List.of(finding))).thenReturn(List.of(responseDTO));

        List<FindingResponseDTO> result = findingService.getAllFindings();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should return findings by transactionId")
    void getFindingsByTransactionId_Success() {
        when(findingRepository.findByTransactionId(100L)).thenReturn(List.of(finding));
        when(findingMapper.toResponseDTOList(List.of(finding))).thenReturn(List.of(responseDTO));

        List<FindingResponseDTO> result = findingService.getFindingsByTransactionId(100L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should return findings by severity")
    void getFindingsBySeverity_Success() {
        when(findingRepository.findBySeverity("HIGH")).thenReturn(List.of(finding));
        when(findingMapper.toResponseDTOList(List.of(finding))).thenReturn(List.of(responseDTO));

        List<FindingResponseDTO> result = findingService.getFindingsBySeverity("HIGH");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should update finding successfully")
    void updateFinding_Success() {
        when(findingRepository.findById(1L)).thenReturn(Optional.of(finding));
        when(findingRepository.save(finding)).thenReturn(finding);
        when(findingMapper.toResponseDTO(finding)).thenReturn(responseDTO);

        FindingResponseDTO result = findingService.updateFinding(1L, requestDTO);

        assertThat(result).isNotNull();
        verify(findingRepository).save(finding);
    }

    @Test
    @DisplayName("Should delete finding when found")
    void deleteFinding_Success() {
        when(findingRepository.findById(1L)).thenReturn(Optional.of(finding));
        doNothing().when(findingRepository).delete(finding);

        findingService.deleteFinding(1L);

        verify(findingRepository).delete(finding);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent finding")
    void deleteFinding_NotFound() {
        when(findingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> findingService.deleteFinding(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Finding not found with id: 99");
    }
}
