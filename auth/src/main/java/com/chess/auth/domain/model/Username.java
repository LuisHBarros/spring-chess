package com.chess.auth.domain.model;

import com.chess.auth.domain.exception.InvalidUsernameException;

import java.util.Objects;
import java.util.regex.Pattern;

public final class Username {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,30}$");

    private final String value;

    public Username(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidUsernameException("Username cannot be null or empty");
        }
        String trimmed = value.trim();
        if (!USERNAME_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidUsernameException("Username must be between 3 and 30 characters and contain only letters, numbers, and underscores");
        }
        this.value = trimmed;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Username username = (Username) o;
        return Objects.equals(value, username.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
