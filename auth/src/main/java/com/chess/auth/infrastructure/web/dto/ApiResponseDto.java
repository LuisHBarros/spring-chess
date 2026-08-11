package com.chess.auth.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard API response wrapper")
public class ApiResponseDto {

    @Schema(description = "Whether the operation was successful", example = "true")
    private boolean success;

    @Schema(description = "Human-readable response message", example = "Operation completed successfully")
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
