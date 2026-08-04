package com.retail.reportservice.dto.external;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalProductDto {

    private Long id;
    private String sku;
    private String name;
    private Integer stockQuantity;
    private Integer minStockLevel;
    private BigDecimal price;
    private Boolean active;
}
