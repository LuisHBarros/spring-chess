package com.chess.auth.domain.model;

import com.chess.auth.domain.exception.InvalidPasswordException;

import java.util.Objects;

public final class Password {
    private final String value;
    private final boolean hashed;

    private Password(String value, boolean hashed) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidPasswordException("Password cannot be null or empty");
        }
        this.value = value;
        this.hashed = hashed;
    }

    public static Password fromRaw(String rawPassword) {
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new InvalidPasswordException("Password cannot be null or empty");
        }
        if (rawPassword.length() < 8) {
            throw new InvalidPasswordException("Password must be at least 8 characters long");
        }
        return new Password(rawPassword, false);
    }

    public static Password fromHash(String hash) {
        if (hash == null || hash.trim().isEmpty()) {
            throw new InvalidPasswordException("Password hash cannot be null or empty");
        }
        return new Password(hash, true);
    }

    public String getValue() {
        return value;
    }

    public boolean isHashed() {
        return hashed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Password password = (Password) o;
        return hashed == password.hashed && Objects.equals(value, password.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, hashed);
    }

    @Override
    public String toString() {
        return hashed ? "[HASHED_PASSWORD]" : "[RAW_PASSWORD]";
    }
}
