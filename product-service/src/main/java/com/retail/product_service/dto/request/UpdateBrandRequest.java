package com.retail.product_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBrandRequest {

    @NotBlank(message = "Brand is required")
    @Size(
            min = 2,
            max = 100,
            message = "Brand name must be between 2 and 100 characters"
    )
    private String name;


    @NotBlank(message = "Description of Brand is required")
    @Size(max = 500)
    private String description;
}
