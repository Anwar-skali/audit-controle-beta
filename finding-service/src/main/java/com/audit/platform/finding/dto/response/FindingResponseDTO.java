package com.audit.platform.finding.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FindingResponseDTO {

    private Long id;
    private Long transactionId;
    private String controlCode;
    private String severity;
    private String description;
    private String status;
    private LocalDateTime createdAt;
}

