package com.ecoshop.common.web;

import com.ecoshop.common.dto.ApiResponse;
import com.ecoshop.common.exception.BusinessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.MDC;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(ApiResponse.fail(ex.getMessage(), MDC.get("traceId")));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(msg, MDC.get("traceId")));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAny(Exception ex) {
        return ResponseEntity.internalServerError()
                .body(ApiResponse.fail("Internal error: " + ex.getMessage(), MDC.get("traceId")));
    }
}
