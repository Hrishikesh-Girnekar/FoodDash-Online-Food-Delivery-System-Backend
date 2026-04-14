package com.app.fooddash.exception;

import com.app.fooddash.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private String getTraceId() {
        return MDC.get("traceId");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFound(ResourceNotFoundException ex,
                                                         HttpServletRequest request) {

        log.warn("ResourceNotFound uri={} message={} traceId={}",
                request.getRequestURI(), ex.getMessage(), getTraceId());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, ex.getMessage(), getTraceId()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<?>> handleBadRequest(BadRequestException ex,
                                                           HttpServletRequest request) {

        log.warn("BadRequest uri={} message={} traceId={}",
                request.getRequestURI(), ex.getMessage(), getTraceId());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, ex.getMessage(), getTraceId()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<?>> handleUnauthorized(UnauthorizedException ex,
                                                             HttpServletRequest request) {

        log.warn("Unauthorized uri={} message={} traceId={}",
                request.getRequestURI(), ex.getMessage(), getTraceId());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(false, ex.getMessage(), getTraceId()));
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ApiResponse<?>> handlePayment(PaymentException ex,
                                                        HttpServletRequest request) {

        log.error("PaymentException uri={} message={} traceId={}",
                request.getRequestURI(), ex.getMessage(), getTraceId(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, ex.getMessage(), getTraceId()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneral(Exception ex,
                                                        HttpServletRequest request) {

        log.error("UnhandledException uri={} message={} traceId={}",
                request.getRequestURI(), ex.getMessage(), getTraceId(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Something went wrong", getTraceId()));
    }
}