package com.chess.auth.domain.port;

import com.chess.auth.domain.model.Email;

public interface PasswordRecoveryTokenService {
    String generateToken(Email email);
    boolean validateToken(String token, Email email);
    void invalidateToken(String token);
}
