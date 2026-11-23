package com.audit.platform.finding.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FindingRequestDTO {

    @NotNull(message = "Transaction ID is required")
    @Positive(message = "Transaction ID must be positive")
    private Long transactionId;

    @NotBlank(message = "Control code is required")
    @Size(max = 100, message = "Control code must not exceed 100 characters")
    private String controlCode;

    @NotBlank(message = "Severity is required")
    @Pattern(regexp = "LOW|MEDIUM|HIGH|CRITICAL", message = "Severity must be LOW, MEDIUM, HIGH, or CRITICAL")
    private String severity;

    @NotBlank(message = "Description is required")
    private String description;

    @Size(max = 30, message = "Status must not exceed 30 characters")
    private String status;
}

