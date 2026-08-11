package com.chess.auth.domain.port;

public interface TokenBlacklistService {
    void blacklistToken(String token, long timeToLiveSeconds);
    boolean isBlacklisted(String token);
}
