package com.hospital.token.dto.response;

import java.util.Map;

public class ApiResponse<T> {

    private boolean success;
    private T data;
    private String error;
    private Map<String, Object> meta;

    public ApiResponse(
            boolean success,
            T data,
            String error,
            Map<String, Object> meta) {

        this.success = success;
        this.data = data;
        this.error = error;
        this.meta = meta;
    }

    // SUCCESS WITHOUT META
    public static <T> ApiResponse<T> success(T data) {

        return new ApiResponse<>(
                true,
                data,
                null,
                Map.of()
        );
    }

    // SUCCESS WITH META
    public static <T> ApiResponse<T> success(
            T data,
            Map<String, Object> meta) {

        return new ApiResponse<>(
                true,
                data,
                null,
                meta
        );
    }

    // ERROR
    public static <T> ApiResponse<T> error(
            String error) {

        return new ApiResponse<>(
                false,
                null,
                error,
                Map.of()
        );
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public String getError() {
        return error;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }
}