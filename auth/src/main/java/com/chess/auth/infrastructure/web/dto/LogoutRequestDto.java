package com.chess.auth.infrastructure.web.dto;

public class LogoutRequestDto {
    private String accessToken;

    public LogoutRequestDto() {}

    public LogoutRequestDto(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
}
