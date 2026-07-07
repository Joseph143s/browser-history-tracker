package com.kishuu.browserhistory.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Generic success wrapper so every endpoint has a consistent shape */
@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> ApiResponse<T> of(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }
}
