package com.audit.platform.supplier.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierRequestDTO {

    @NotBlank(message = "Supplier name is required")
    @Size(max = 150, message = "Supplier name must not exceed 150 characters")
    private String name;

    @NotBlank(message = "Tax ID is required")
    @Size(max = 50, message = "Tax ID must not exceed 50 characters")
    private String taxId;

    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @NotBlank(message = "Bank account is required")
    @Size(max = 100, message = "Bank account must not exceed 100 characters")
    private String bankAccount;

    @Size(max = 30, message = "Status must not exceed 30 characters")
    private String status;
}

