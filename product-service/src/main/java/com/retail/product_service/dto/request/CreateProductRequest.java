package com.retail.product_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductRequest {

    @NotBlank(message = "Product is required")
    @Size(
            min = 2,
            max = 100,
            message = "Product name must be between 2 and 100 characters"
    )
    private String name;

    @NotBlank(message = "Description of Product is required")
    @Size(
            min = 5,
            max = 500,
            message = "Product Description must be between 5 and 500 characters"
    )
    private String description;

    @NotNull(message = "Category is required")
    @Positive(message = "Invalid category selected")
    private Long categoryId;

    @NotNull(message = "Brand is required")
    @Positive(message = "Invalid brand selected")
    private Long brandId;

}
