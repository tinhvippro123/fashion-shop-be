package com.fashionshop.be.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String code;
    private String errorCode;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code("SUCCESS")
                .message("Thành công")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code("SUCCESS")
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ok(message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .code("ERROR")
                .errorCode("ERROR")
                .message(message)
                .data(null)
                .build();
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(code)
                .errorCode(code)
                .message(message)
                .data(null)
                .build();
    }
}
