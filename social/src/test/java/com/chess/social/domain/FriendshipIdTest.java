package com.chess.social.domain;

import com.chess.social.domain.model.FriendshipId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FriendshipIdTest {

    @Test
    @DisplayName("Should generate and wrap FriendshipId")
    void shouldCreateFriendshipId() {
        FriendshipId id = FriendshipId.generate();
        assertThat(id.getValue()).isNotNull();

        UUID uuid = UUID.randomUUID();
        FriendshipId fromUuid = FriendshipId.from(uuid);
        assertThat(fromUuid.getValue()).isEqualTo(uuid);
    }
}
