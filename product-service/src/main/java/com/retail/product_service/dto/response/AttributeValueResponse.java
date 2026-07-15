package com.retail.product_service.dto.response;


import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttributeValueResponse {

    private Long id;

    private String value;

    private Long attributeId;

    private String attributeName;

    private Boolean isActive;

    private LocalDateTime createdAt;
}
