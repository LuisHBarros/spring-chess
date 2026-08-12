package com.chess.social.domain;

import com.chess.social.domain.model.Avatar;
import com.chess.social.domain.model.UserId;
import com.chess.social.domain.model.UserProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserProfileTest {

    @Test
    @DisplayName("Should create user profile with default avatar")
    void shouldCreateUserProfileWithDefaultAvatar() {
        UserId userId = UserId.generate();
        UserProfile profile = UserProfile.create(userId, "BobbyFischer", null);

        assertEquals(userId, profile.getUserId());
        assertEquals("BobbyFischer", profile.getDisplayName());
        assertNotNull(profile.getAvatar());
        assertTrue(profile.getAvatar().getUrl().contains("default.png"));
    }

    @Test
    @DisplayName("Should update user profile avatar")
    void shouldUpdateUserProfileAvatar() {
        UserId userId = UserId.generate();
        UserProfile profile = UserProfile.create(userId, "MagnusCarlsen", Avatar.defaultAvatar());

        Avatar customAvatar = Avatar.of("https://cdn.chess.com/avatars/magnus.jpg");
        profile.updateAvatar(customAvatar);

        assertEquals(customAvatar, profile.getAvatar());
    }
}
