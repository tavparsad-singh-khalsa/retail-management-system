package com.retail.product_service.dto.response;

import com.retail.product_service.enums.AttributeDataType;
import lombok.*;


import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttributeResponse {

    private Long id;

    private String name;

    private String description;

    private AttributeDataType dataType;

    private Boolean isActive;

    private Long numberOfValues;

    private LocalDateTime createdAt;
}
