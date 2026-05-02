package com.ecoshop.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        T data,
        String error,
        String traceId,
        Instant timestamp
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, null, Instant.now());
    }
    public static <T> ApiResponse<T> fail(String error, String traceId) {
        return new ApiResponse<>(false, null, error, traceId, Instant.now());
    }
}
