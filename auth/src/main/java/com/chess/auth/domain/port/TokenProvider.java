package com.chess.auth.domain.port;

import com.chess.auth.domain.model.AuthToken;
import com.chess.auth.domain.model.Email;
import com.chess.auth.domain.model.User;
import com.chess.auth.domain.model.UserId;

public interface TokenProvider {
    AuthToken generateTokens(User user);
    boolean validateAccessToken(String token);
    boolean validateRefreshToken(String token);
    UserId extractUserIdFromAccessToken(String token);
    Email extractEmailFromRefreshToken(String token);
    long getRemainingExpirationSeconds(String token);
    int extractRefreshTokenVersion(String token);
}
