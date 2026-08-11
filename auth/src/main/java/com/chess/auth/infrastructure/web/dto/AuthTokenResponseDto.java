package com.chess.auth.infrastructure.web.dto;

import com.chess.auth.domain.model.AuthToken;

public class AuthTokenResponseDto {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresInSeconds;
    private UserResponseDto user;

    public AuthTokenResponseDto() {}

    public AuthTokenResponseDto(String accessToken, String refreshToken, String tokenType, long expiresInSeconds, UserResponseDto user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiresInSeconds = expiresInSeconds;
        this.user = user;
    }

    public static AuthTokenResponseDto from(AuthToken token, UserResponseDto user) {
        return new AuthTokenResponseDto(
                token.getAccessToken(),
                token.getRefreshToken(),
                token.getTokenType(),
                token.getExpiresInSeconds(),
                user
        );
    }

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getTokenType() { return tokenType; }
    public long getExpiresInSeconds() { return expiresInSeconds; }
    public UserResponseDto getUser() { return user; }
}
