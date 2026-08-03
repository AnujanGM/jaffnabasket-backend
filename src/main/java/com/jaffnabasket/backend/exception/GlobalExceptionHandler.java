package com.jaffnabasket.backend.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException ex) {
        return build(ex.getStatus(), ex.getCode(), ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<ApiErrorResponse.FieldErrorItem> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ApiErrorResponse.FieldErrorItem(fe.getField(), fe.getDefaultMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Validation failed", fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<ApiErrorResponse.FieldErrorItem> fieldErrors = ex.getConstraintViolations().stream()
                .map(cv -> new ApiErrorResponse.FieldErrorItem(cv.getPropertyPath().toString(), cv.getMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Validation failed", fieldErrors);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid credentials", null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthentication(AuthenticationException ex) {
        return build(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Authentication required", null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Access denied", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred", null);
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String code, String message,
                                                     List<ApiErrorResponse.FieldErrorItem> fieldErrors) {
        ApiErrorResponse body = ApiErrorResponse.of(code, message, fieldErrors, CorrelationIdHolder.current());
        return ResponseEntity.status(status).body(body);
    }
}
