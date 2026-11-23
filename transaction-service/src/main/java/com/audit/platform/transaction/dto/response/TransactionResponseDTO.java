package com.audit.platform.transaction.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponseDTO {

    private Long id;
    private String reference;
    private Long supplierId;
    private BigDecimal amount;
    private String currency;
    private String type;
    private LocalDateTime transactionDate;
    private String createdBy;
    private String status;
}

