package com.chess.auth.domain.port;

import com.chess.auth.domain.model.Password;

public interface PasswordEncoder {
    Password encode(Password rawPassword);
    boolean matches(Password rawPassword, Password hashedPassword);
}
