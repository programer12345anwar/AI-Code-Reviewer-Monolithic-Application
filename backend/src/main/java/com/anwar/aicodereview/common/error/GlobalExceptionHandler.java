package com.anwar.aicodereview.common.error;

import com.anwar.aicodereview.exception.AiAnalysisException;
import com.anwar.aicodereview.exception.AiProviderConfigurationException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        log.warn("Validation failed: {}", details);
        return ResponseEntity.badRequest().body(new ApiErrorResponse("VALIDATION_ERROR", "Validation failed", details, Instant.now()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        List<String> details = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .toList();
        return ResponseEntity.badRequest().body(new ApiErrorResponse("VALIDATION_ERROR", "Constraint violation", details, Instant.now()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ApiErrorResponse("BAD_REQUEST", ex.getMessage(), List.of(), Instant.now()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String message = "Invalid value for path or query parameter: " + ex.getName();
        return ResponseEntity.badRequest().body(new ApiErrorResponse("BAD_REQUEST", message, List.of(), Instant.now()));
    }

    @ExceptionHandler(AiProviderConfigurationException.class)
    public ResponseEntity<ApiErrorResponse> handleAiProviderConfigurationException(AiProviderConfigurationException ex) {
        log.error("AI provider is not configured correctly: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ApiErrorResponse("AI_PROVIDER_CONFIGURATION_ERROR", ex.getMessage(), List.of(), Instant.now()));
    }

    @ExceptionHandler(AiAnalysisException.class)
    public ResponseEntity<ApiErrorResponse> handleAiAnalysisException(AiAnalysisException ex) {
        log.error("AI analysis failed", ex);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ApiErrorResponse("AI_ANALYSIS_ERROR", ex.getMessage(), List.of(), Instant.now()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntimeException(RuntimeException ex) {
        log.error("Unhandled runtime exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse("INTERNAL_SERVER_ERROR", "Unexpected server error", List.of(), Instant.now()));
    }
}
