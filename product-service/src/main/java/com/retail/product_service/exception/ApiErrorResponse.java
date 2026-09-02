package com.retail.product_service.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard error response returned when a request fails")
public class ApiErrorResponse {

    @Schema(description = "Time at which the error occurred")
    private LocalDateTime timestamp;
    @Schema(description = "HTTP status code of the error", example = "400")
    private int status; // ⭐ Improved: Switched to primitive int
    @Schema(description = "HTTP status reason phrase", example = "Bad Request")
    private String error;
    @Schema(description = "Human-readable error description")
    private String message;
    @Schema(description = "Request path that produced the error", example = "/api/v1/products")
    private String path;

    // Only included in the JSON when there are validation errors (e.g., 400 Bad Request)
    @Schema(description = "Field-specific validation errors; present only for validation failures (400)")
    private Map<String, String> validationErrors;
}