package com.retail.product_service.dto.response;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {

    private Long id;;

    private String name;

    private String description;

    private boolean isActive;

    private Long parentCategoryId;

    private LocalDateTime createdAt;


}
