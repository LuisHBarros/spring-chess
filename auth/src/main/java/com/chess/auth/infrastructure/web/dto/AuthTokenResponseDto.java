package com.chess.auth.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.chess.auth.domain.model.AuthToken;

@Schema(description = "Authentication token response containing JWT tokens and optional user info")
public class AuthTokenResponseDto {

    @Schema(description = "JWT access token for authenticating requests", example = "eyJhbGciOiJSUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "JWT refresh token for obtaining new access tokens", example = "eyJhbGciOiJSUzI1NiJ9...")
    private String refreshToken;

    @Schema(description = "Token type", example = "Bearer")
    private String tokenType;

    @Schema(description = "Access token expiry time in seconds", example = "3600")
    private long expiresInSeconds;

    @Schema(description = "Authenticated user details (null on token refresh)")
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
