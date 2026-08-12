package com.chess.social.domain;

import com.chess.social.domain.model.Avatar;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AvatarTest {

    @Test
    @DisplayName("Should create avatar with valid URL")
    void shouldCreateAvatarWithValidUrl() {
        String url = "https://cdn.chess.com/avatars/grandmaster.png";
        Avatar avatar = Avatar.of(url);

        assertEquals(url, avatar.getUrl());
        assertEquals(url, avatar.toString());
    }

    @Test
    @DisplayName("Should create default avatar")
    void shouldCreateDefaultAvatar() {
        Avatar avatar = Avatar.defaultAvatar();

        assertNotNull(avatar.getUrl());
        assertTrue(avatar.getUrl().contains("default.png"));
    }

    @Test
    @DisplayName("Should throw exception when URL is empty or null")
    void shouldThrowWhenUrlIsEmptyOrNull() {
        assertThrows(IllegalArgumentException.class, () -> Avatar.of(null));
        assertThrows(IllegalArgumentException.class, () -> Avatar.of("   "));
    }

    @Test
    @DisplayName("Should test value equality")
    void shouldTestValueEquality() {
        Avatar a1 = Avatar.of("https://example.com/avatar1.png");
        Avatar a2 = Avatar.of("https://example.com/avatar1.png");
        Avatar a3 = Avatar.of("https://example.com/avatar2.png");

        assertEquals(a1, a2);
        assertNotEquals(a1, a3);
        assertEquals(a1.hashCode(), a2.hashCode());
    }
}
