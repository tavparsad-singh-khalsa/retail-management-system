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
public class UpdateAttributeValueRequest {

    @NotBlank(message = "Value is required")
    @Size(
            min = 1,
            max = 100,
            message = "Value must be between 1 and 100 characters"
    )
    private String value;
}
