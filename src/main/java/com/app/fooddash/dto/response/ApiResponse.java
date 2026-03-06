package com.app.fooddash.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    
    public static <T> ApiResponse<T> ok(String msg, T data) {
        return ApiResponse.<T>builder().success(true).message(msg).data(data).build();
    }

    public static <T> ApiResponse<T> fail(String msg) {
        return ApiResponse.<T>builder().success(false).message(msg).build();
    }
}

