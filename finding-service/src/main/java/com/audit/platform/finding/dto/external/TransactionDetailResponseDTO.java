package com.audit.platform.finding.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDetailResponseDTO {
    private Long id;
    private String reference;
    private Long supplierId;
    private BigDecimal amount;
    private String currency;
    private String type;
    private LocalDateTime transactionDate;
    private String createdBy;
    private String status;
    private Object supplier;
}

