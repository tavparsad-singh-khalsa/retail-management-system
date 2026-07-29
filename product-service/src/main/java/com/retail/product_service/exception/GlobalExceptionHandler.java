package com.retail.product_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Validation Errors (400 Bad Request)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            // ⭐ Improved: Keeps the first (often most relevant) validation message
            errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Validation failed")
                .path(request.getRequestURI())
                .validationErrors(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 2. Duplicate Records (409 Conflict)
    @ExceptionHandler({
            CategoryAlreadyExistsException.class,
            BrandAlreadyExistsException.class,
            ProductAlreadyExistsException.class
    })
    public ResponseEntity<ApiErrorResponse> handleConflictExceptions(
            RuntimeException ex, HttpServletRequest request) {
        return buildErrorResponse(ex, HttpStatus.CONFLICT, request);
    }

    // 3. Resource Not Found (404 Not Found)
    @ExceptionHandler({
            CategoryNotFoundException.class,
            BrandNotFoundException.class,
            ProductNotFoundException.class,
            ProductVariantNotFoundException.class,
            AttributeNotFoundException.class,
            AttributeValueNotFoundException.class,
            VariantAttributeNotFoundException.class,
            ProductImageNotFoundException.class,
            InventoryNotFoundException.class,
            StockMovementNotFoundException.class
    })
    public ResponseEntity<ApiErrorResponse> handleNotFoundExceptions(
            RuntimeException ex, HttpServletRequest request) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, request);
    }

    // 4. Business Rule Violations (400 Bad Request)
    @ExceptionHandler({
            CategoryHierarchyException.class, // ⭐ Fixed: Replaced CategoryHasActiveChildrenException
            InvalidStockMovementException.class,
            InsufficientStockException.class,
            InvalidProductImageException.class,
            InvalidPriceException.class,
            InvalidAttributeValueException.class,
            CategoryInactiveException.class,
            BrandInactiveException.class,
            ProductInactiveException.class,
            ProductImageInactiveException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBusinessRuleExceptions(
            RuntimeException ex, HttpServletRequest request) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, request);
    }

    // 5. Unexpected Errors (500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGlobalException(
            Exception ex, HttpServletRequest request) {

        log.error("Unexpected exception", ex);

        // ⭐ Fixed: Building the response directly instead of throwing a fake RuntimeException
        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("An unexpected error occurred")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {

        return buildErrorResponse(ex, HttpStatus.METHOD_NOT_ALLOWED, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        return buildErrorResponse(
                new IllegalArgumentException("Invalid value for '" + ex.getName() + "'"),
                HttpStatus.BAD_REQUEST,
                request
        );
    }

    // --- Private Helper Method to Build Responses ---
    private ResponseEntity<ApiErrorResponse> buildErrorResponse(
            Exception ex, HttpStatus status, HttpServletRequest request) {

        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(status).body(response);
    }
}