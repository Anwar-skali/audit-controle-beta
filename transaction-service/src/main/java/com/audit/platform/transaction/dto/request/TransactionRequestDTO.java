package com.audit.platform.transaction.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequestDTO {

    @NotBlank(message = "Transaction reference is required")
    @Size(max = 100, message = "Reference must not exceed 100 characters")
    private String reference;

    @NotNull(message = "Supplier ID is required")
    @Positive(message = "Supplier ID must be positive")
    private Long supplierId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code (e.g., EUR, USD)")
    private String currency;

    @NotBlank(message = "Transaction type is required")
    @Size(max = 50, message = "Type must not exceed 50 characters")
    private String type;

    private LocalDateTime transactionDate;

    @NotBlank(message = "Created by is required")
    @Size(max = 100, message = "Created by must not exceed 100 characters")
    private String createdBy;

    @Size(max = 30, message = "Status must not exceed 30 characters")
    private String status;
}

