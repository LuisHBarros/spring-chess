package com.chess.auth.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request payload for user logout")
public class LogoutRequestDto {

    @Schema(description = "The access token to invalidate", example = "eyJhbGciOiJSUzI1NiJ9...")
    private String accessToken;

    public LogoutRequestDto() {}

    public LogoutRequestDto(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
}
