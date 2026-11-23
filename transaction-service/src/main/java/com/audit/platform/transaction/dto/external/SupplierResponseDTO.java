package com.audit.platform.transaction.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierResponseDTO {
    private Long id;
    private String name;
    private String taxId;
    private String country;
    private String bankAccount;
    private String status;
    private LocalDateTime createdAt;
}
