package com.chess.auth.infrastructure.web.dto;

public class ApiResponseDto {
    private boolean success;
    private String message;

    public ApiResponseDto() {}

    public ApiResponseDto(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static ApiResponseDto ok(String message) {
        return new ApiResponseDto(true, message);
    }

    public static ApiResponseDto error(String message) {
        return new ApiResponseDto(false, message);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}
