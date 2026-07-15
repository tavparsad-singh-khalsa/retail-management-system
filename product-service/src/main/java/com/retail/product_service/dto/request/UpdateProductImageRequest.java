package com.retail.product_service.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductImageRequest {

    @NotBlank(message = "Image URL is required")
    @Size(
            max = 500,
            message = "Image URL cannot exceed 500 characters"
    )
    private String imageUrl;

    @NotNull
    private Boolean isPrimary;

    @NotNull
    @PositiveOrZero
    private Integer displayOrder;
}
