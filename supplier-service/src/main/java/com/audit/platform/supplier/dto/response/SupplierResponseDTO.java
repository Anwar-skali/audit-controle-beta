package com.audit.platform.supplier.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
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

