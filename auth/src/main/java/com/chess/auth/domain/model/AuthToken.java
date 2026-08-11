package com.chess.auth.domain.model;

import java.util.Objects;

public final class AuthToken {
    private final String accessToken;
    private final String refreshToken;
    private final String tokenType;
    private final long expiresInSeconds;

    public AuthToken(String accessToken, String refreshToken, String tokenType, long expiresInSeconds) {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Access token cannot be null or empty");
        }
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Refresh token cannot be null or empty");
        }
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType != null ? tokenType : "Bearer";
        this.expiresInSeconds = expiresInSeconds;
    }

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getTokenType() { return tokenType; }
    public long getExpiresInSeconds() { return expiresInSeconds; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthToken authToken = (AuthToken) o;
        return Objects.equals(accessToken, authToken.accessToken) && Objects.equals(refreshToken, authToken.refreshToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessToken, refreshToken);
    }
}
