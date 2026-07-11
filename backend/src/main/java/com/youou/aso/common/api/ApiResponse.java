package com.youou.aso.common.api;

public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data,
        Object args,
        String requestId
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "OK", "OK", data, null, null);
    }

    public static <T> ApiResponse<T> error(String code, String message, Object args, String requestId) {
        return new ApiResponse<>(false, code, message, null, args, requestId);
    }
}
