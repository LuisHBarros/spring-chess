package com.chess.game.infrastructure.web.dto;

public record ApiResponseDto<T>(boolean success, T data, String message) {
    public static <T> ApiResponseDto<T> success(T data) {
        return new ApiResponseDto<>(true, data, null);
    }
    public static <T> ApiResponseDto<T> error(String message) {
        return new ApiResponseDto<>(false, null, message);
    }
}
