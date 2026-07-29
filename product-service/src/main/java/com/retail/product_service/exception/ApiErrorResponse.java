package com.retail.product_service.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    private LocalDateTime timestamp;
    private int status; // ⭐ Improved: Switched to primitive int
    private String error;
    private String message;
    private String path;

    // Only included in the JSON when there are validation errors (e.g., 400 Bad Request)
    private Map<String, String> validationErrors;
}