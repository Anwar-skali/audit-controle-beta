package com.audit.platform.finding.dto.response;

import com.audit.platform.finding.dto.external.TransactionDetailResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Aggregated DTO (Jalon V4) combining Finding data with remote Transaction & Supplier details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FindingDetailResponseDTO {
    private Long id;
    private Long transactionId;
    private String controlCode;
    private String severity;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private TransactionDetailResponseDTO transaction;
}

