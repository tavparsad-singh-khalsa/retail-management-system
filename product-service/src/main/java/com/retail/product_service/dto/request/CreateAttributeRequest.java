package com.retail.product_service.dto.request;

import com.retail.product_service.enums.AttributeDataType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAttributeRequest {

    @NotBlank(message = "Attribute name is required")
    @Size(
            min = 2,
            max = 100,
            message = "Attribute name must be between 2 and 100 characters"
    )
    private String name;

    @NotNull(message = "Attribute data type is required")
    private AttributeDataType dataType;

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

}
