package com.assignment.backend.exception;

import com.assignment.backend.dto.ErrorResponse;
import com.assignment.backend.metrics.AppMetricsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final AppMetricsService metricsService;

    public GlobalExceptionHandler(AppMetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            HttpMessageNotReadableException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception exception, HttpServletRequest request) {
        List<String> details = exception instanceof MethodArgumentNotValidException invalidException
                ? invalidException.getBindingResult().getFieldErrors().stream().map(FieldError::getDefaultMessage).toList()
                : Collections.emptyList();
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", exception.getMessage(), request.getRequestURI(), details, exception);
    }

    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleUnauthorized(Exception exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", "Invalid credentials", request.getRequestURI(), Collections.emptyList(), exception);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", exception.getMessage(), request.getRequestURI(), Collections.emptyList(), exception);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "Conflict", exception.getMessage(), request.getRequestURI(), Collections.emptyList(), exception);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred",
                request.getRequestURI(), Collections.emptyList(), exception);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String error, String message,
                                                        String path, List<String> details, Exception exception) {
        metricsService.incrementErrors();
        log.error("event=request_error status={} path={} message={}", status.value(), path, exception.getMessage(), exception);
        ErrorResponse body = new ErrorResponse(OffsetDateTime.now(), status.value(), error, message, path, details);
        return ResponseEntity.status(status).body(body);
    }
}
