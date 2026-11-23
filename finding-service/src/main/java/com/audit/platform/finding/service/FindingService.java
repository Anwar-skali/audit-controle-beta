package com.audit.platform.finding.service;

import com.audit.platform.finding.dto.request.FindingRequestDTO;
import com.audit.platform.finding.dto.response.FindingDetailResponseDTO;
import com.audit.platform.finding.dto.response.FindingResponseDTO;

import java.util.List;

public interface FindingService {

    FindingResponseDTO createFinding(FindingRequestDTO request);

    FindingResponseDTO getFindingById(Long id);

    FindingDetailResponseDTO getFindingDetails(Long id);

    List<FindingResponseDTO> getAllFindings();

    List<FindingResponseDTO> getFindingsByTransactionId(Long transactionId);

    List<FindingResponseDTO> getFindingsBySeverity(String severity);

    FindingResponseDTO updateFinding(Long id, FindingRequestDTO request);

    void deleteFinding(Long id);
}
