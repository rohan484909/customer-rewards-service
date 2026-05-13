package com.retailer.rewards.exception;


import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Centralized exception handler for all REST endpoints.
 * Returns a consistent JSON error structure for every exception type.
 *
 * Response format:
 * {
 *   "timestamp": "2024-01-15T10:30:00",
 *   "status":    404,
 *   "error":     "Not Found",
 *   "message":   "Customer not found: 99",
 *   "path":      (optional – field errors list)
 * }
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // -----------------------------------------------------------------------
    // 1. NoSuchElementException → 404 Not Found
    //    Thrown by RewardsService when customerId doesn't exist in DB
    // -----------------------------------------------------------------------
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNoSuchElement(
            NoSuchElementException ex) {

        log.error("Resource not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // 2. MethodArgumentNotValidException → 400 Bad Request
    //    Thrown when @Valid fails on a @RequestBody (e.g. missing fields)
    //    Returns a list of all field-level validation errors
    // -----------------------------------------------------------------------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {

        List<String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        log.error("Validation failed: {}", fieldErrors);

        Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, "Validation failed");
        body.put("errors", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // -----------------------------------------------------------------------
    // 3. IllegalArgumentException → 400 Bad Request
    //    Thrown for bad business input e.g. negative amount, start > end date
    // -----------------------------------------------------------------------
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex) {

        log.error("Illegal argument: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // 4. HttpMessageNotReadableException → 400 Bad Request
    //    Thrown when request body JSON is malformed or missing
    // -----------------------------------------------------------------------
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex) {

        log.error("Malformed JSON request: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST,
                "Malformed JSON request body. Please check your request format.");
    }

    // -----------------------------------------------------------------------
    // 5. ConstraintViolationException → 400 Bad Request
    //    Thrown when @Validated fails on @PathVariable or @RequestParam
    //    e.g. GET /api/rewards/-1  (negative ID)
    // -----------------------------------------------------------------------
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(
            ConstraintViolationException ex) {

        List<String> violations = ex.getConstraintViolations()
                .stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .toList();

        log.error("Constraint violations: {}", violations);

        Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, "Constraint violation");
        body.put("errors", violations);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // -----------------------------------------------------------------------
    // 6. Exception → 500 Internal Server Error
    //    Catch-all fallback for any unexpected exception
    // -----------------------------------------------------------------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {

        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.");
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status, String message) {
        return ResponseEntity.status(status).body(baseBody(status, message));
    }

    private Map<String, Object> baseBody(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status",    status.value());
        body.put("error",     status.getReasonPhrase());
        body.put("message",   message);
        return body;
    }
}
