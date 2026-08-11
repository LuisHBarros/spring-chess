package com.chess.auth.domain.port;

import com.chess.auth.domain.model.Email;

public interface EmailService {
    void sendPasswordRecoveryEmail(Email recipient, String recoveryToken);
}
