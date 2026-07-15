package com.retail.product_service.dto.response;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private Long id;

    private String productCode;

    private String name;

    private String description;

    private Long categoryId;

    private String categoryName;

    private Long brandId;

    private String brandName;

    private Boolean hasVariants;

    private Boolean isActive;

    private  LocalDateTime createdAt;

    private String primaryImageUrl;

}
