package com.chess.auth.infrastructure.security;

import com.chess.auth.domain.model.Password;
import com.chess.auth.domain.port.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordEncoderAdapter implements PasswordEncoder {

    private final BCryptPasswordEncoder delegate;

    public BCryptPasswordEncoderAdapter() {
        this.delegate = new BCryptPasswordEncoder();
    }

    @Override
    public Password encode(Password rawPassword) {
        String encoded = delegate.encode(rawPassword.getValue());
        return Password.fromHash(encoded);
    }

    @Override
    public boolean matches(Password rawPassword, Password hashedPassword) {
        return delegate.matches(rawPassword.getValue(), hashedPassword.getValue());
    }
}
