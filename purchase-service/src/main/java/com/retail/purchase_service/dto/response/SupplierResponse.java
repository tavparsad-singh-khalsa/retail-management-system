package com.retail.purchase_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponse {

    private Long id;
    private String name;
    private String contactPerson;
    private String phone;
    private String gstNumber;
    private String email;
    private String address;
    private Boolean isActive;

    // We can expose audit fields to the frontend if needed for display like "Registered On"
    // private LocalDateTime createdAt;
    // private String createdBy;
}