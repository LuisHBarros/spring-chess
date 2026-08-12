package com.chess.social.domain.model;

import java.util.Objects;

public final class CategoryName {
    private static final int MAX_LENGTH = 50;

    private final String value;

    private CategoryName(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }
        String trimmed = value.trim();
        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Category name cannot exceed " + MAX_LENGTH + " characters");
        }
        this.value = trimmed;
    }

    public static CategoryName of(String value) {
        return new CategoryName(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryName that = (CategoryName) o;
        return Objects.equals(value, that.value);
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
