package com.retail.billingservice.exception;

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
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }
        ApiErrorResponse resp = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Validation failed")
                .path(request.getRequestURI())
                .validationErrors(errors)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
    }

    @ExceptionHandler({InvoiceNotFoundException.class, SaleNotFoundException.class, CustomerNotFoundException.class, InvoiceSettingsNotFoundException.class})
    public ResponseEntity<ApiErrorResponse> handleNotFound(RuntimeException ex, HttpServletRequest request) {
        return build(ex, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler({InvoiceAlreadyExistsException.class, InvoiceAlreadyPaidException.class})
    public ResponseEntity<ApiErrorResponse> handleConflict(RuntimeException ex, HttpServletRequest request) {
        return build(ex, HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler({InvalidPaymentException.class, InvalidInvoiceException.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequest(RuntimeException ex, HttpServletRequest request) {
        return build(ex, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler({SaleNotFinalizedException.class})
    public ResponseEntity<ApiErrorResponse> handleSaleNotFinalized(RuntimeException ex, HttpServletRequest request) {
        return build(ex, HttpStatus.UNPROCESSABLE_ENTITY, request);
    }

    @ExceptionHandler({SalesServiceException.class, CustomerServiceException.class})
    public ResponseEntity<ApiErrorResponse> handleExternalServiceError(RuntimeException ex, HttpServletRequest request) {
        log.error("External service communication error: {}", ex.getMessage());
        return build(ex, HttpStatus.BAD_GATEWAY, request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {
        return build(ex, HttpStatus.METHOD_NOT_ALLOWED, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        return build(
                new IllegalArgumentException("Invalid value for '" + ex.getName() + "'"),
                HttpStatus.BAD_REQUEST,
                request
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFound(
            NoResourceFoundException ex,
            HttpServletRequest request) {
        return build(ex, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAll(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error", ex);
        return build(new RuntimeException("An unexpected error occurred"), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    private ResponseEntity<ApiErrorResponse> build(Exception ex, HttpStatus status, HttpServletRequest request) {
        ApiErrorResponse resp = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(status).body(resp);
    }
}
