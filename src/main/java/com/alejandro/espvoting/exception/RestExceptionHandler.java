package com.alejandro.espvoting.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for REST controllers.
 * This class provides centralized exception handling across all controllers.
 */
@ControllerAdvice
@Slf4j
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handle EntityNotFoundException.
     * Triggered when an entity is not found in the database.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    protected ResponseEntity<Object> handleEntityNotFound(EntityNotFoundException ex, WebRequest request) {
        log.error("Entity not found", ex);
        
        ApiError apiError = new ApiError(
                HttpStatus.NOT_FOUND,
                "Entity not found",
                ex.getMessage(),
                LocalDateTime.now()
        );
        
        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle IllegalArgumentException.
     * Triggered when an invalid argument is passed to a method.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        log.error("Invalid argument", ex);
        
        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST,
                "Invalid argument",
                ex.getMessage(),
                LocalDateTime.now()
        );
        
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle DataIntegrityViolationException.
     * Triggered when a database constraint is violated.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {
        log.error("Data integrity violation", ex);
        
        ApiError apiError = new ApiError(
                HttpStatus.CONFLICT,
                "Data integrity violation",
                "The operation would violate data integrity constraints. Please check your input.",
                LocalDateTime.now()
        );
        
        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }

    /**
     * Handle ConstraintViolationException.
     * Triggered when a validation constraint is violated.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        log.error("Constraint violation", ex);
        
        // Extract validation errors
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        violation -> violation.getMessage(),
                        (error1, error2) -> error1 + ", " + error2
                ));
        
        ValidationError validationError = new ValidationError(
                HttpStatus.BAD_REQUEST,
                "Validation error",
                "The request contains invalid data. Please check the errors field for details.",
                LocalDateTime.now(),
                errors
        );
        
        return new ResponseEntity<>(validationError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle MethodArgumentNotValidException.
     * Triggered when @Valid validation fails on a method argument.
     */
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.error("Method argument not valid", ex);
        
        // Extract validation errors
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ValidationError validationError = new ValidationError(
                HttpStatus.BAD_REQUEST,
                "Validation error",
                "The request contains invalid data. Please check the errors field for details.",
                LocalDateTime.now(),
                errors
        );
        
        return new ResponseEntity<>(validationError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle all other exceptions.
     * This is a fallback handler for any exception not explicitly handled above.
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        log.error("Internal server error", ex);
        
        ApiError apiError = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                "An unexpected error occurred. Please try again later or contact support.",
                LocalDateTime.now()
        );
        
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * API Error response class.
     * Used to structure error responses in a consistent format.
     */
    private static class ApiError {
        private final HttpStatus status;
        private final String error;
        private final String message;
        private final LocalDateTime timestamp;

        public ApiError(HttpStatus status, String error, String message, LocalDateTime timestamp) {
            this.status = status;
            this.error = error;
            this.message = message;
            this.timestamp = timestamp;
        }

        public HttpStatus getStatus() {
            return status;
        }

        public String getError() {
            return error;
        }

        public String getMessage() {
            return message;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }

    /**
     * Validation Error response class.
     * Extends ApiError to include field-specific validation errors.
     */
    private static class ValidationError extends ApiError {
        private final Map<String, String> errors;

        public ValidationError(HttpStatus status, String error, String message, LocalDateTime timestamp, Map<String, String> errors) {
            super(status, error, message, timestamp);
            this.errors = errors;
        }

        public Map<String, String> getErrors() {
            return errors;
        }
    }
}