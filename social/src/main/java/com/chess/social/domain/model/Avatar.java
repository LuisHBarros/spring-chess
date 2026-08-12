package com.chess.social.domain.model;

import java.util.Objects;

public final class Avatar {
    private static final String DEFAULT_AVATAR_URL = "https://cdn.chess.com/avatars/default.png";
    private static final int MAX_URL_LENGTH = 500;

    private final String url;

    private Avatar(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("Avatar URL cannot be null or empty");
        }
        String trimmed = url.trim();
        if (trimmed.length() > MAX_URL_LENGTH) {
            throw new IllegalArgumentException("Avatar URL cannot exceed " + MAX_URL_LENGTH + " characters");
        }
        this.url = trimmed;
    }

    public static Avatar of(String url) {
        return new Avatar(url);
    }

    public static Avatar defaultAvatar() {
        return new Avatar(DEFAULT_AVATAR_URL);
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Avatar avatar = (Avatar) o;
        return Objects.equals(url, avatar.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    @Override
    public String toString() {
        return url;
    }
}
