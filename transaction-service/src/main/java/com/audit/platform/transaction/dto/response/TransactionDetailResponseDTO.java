package com.audit.platform.transaction.dto.response;

import com.audit.platform.transaction.dto.external.SupplierResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Aggregated DTO (Jalon V4) combining Transaction data with remote Supplier details.
 */
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
    private SupplierResponseDTO supplier;
}

