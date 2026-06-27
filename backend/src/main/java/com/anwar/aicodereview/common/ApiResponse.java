package com.anwar.aicodereview.common;

import java.time.Instant;
import java.util.Map;

public record ApiResponse<T>(boolean success, String message, T data, Instant timestamp, Map<String, Object> meta) {

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, Instant.now(), null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return success("Request completed successfully", data);
    }

    public static <T> ApiResponse<T> failure(String message) {
        return new ApiResponse<>(false, message, null, Instant.now(), null);
    }
}
