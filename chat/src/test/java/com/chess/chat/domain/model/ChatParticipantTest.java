package com.chess.chat.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ChatParticipantTest {

    private UserId userId;

    @BeforeEach
    void setUp() {
        userId = UserId.generate();
    }

    @Test
    void shouldCreateChatParticipantWithDefaultValues() {
        ChatParticipant participant = ChatParticipant.create(userId, ParticipantRole.OWNER);

        assertEquals(userId, participant.getUserId());
        assertEquals(ParticipantRole.OWNER, participant.getRole());
        assertNotNull(participant.getJoinedAt());
        assertNotNull(participant.getLastReadAt());
        assertFalse(participant.isMuted());
    }

    @Test
    void shouldThrowExceptionWhenCreatingWithNullParameters() {
        assertThrows(IllegalArgumentException.class, () -> ChatParticipant.create(null, ParticipantRole.MEMBER));
        assertThrows(IllegalArgumentException.class, () -> ChatParticipant.create(userId, null));
    }

    @Test
    void shouldReconstituteChatParticipant() {
        Instant joinedAt = Instant.now().minusSeconds(3600);
        Instant lastReadAt = Instant.now().minusSeconds(600);

        ChatParticipant participant = ChatParticipant.reconstitute(userId, ParticipantRole.ADMIN, joinedAt, lastReadAt, true);

        assertEquals(userId, participant.getUserId());
        assertEquals(ParticipantRole.ADMIN, participant.getRole());
        assertEquals(joinedAt, participant.getJoinedAt());
        assertEquals(lastReadAt, participant.getLastReadAt());
        assertTrue(participant.isMuted());
    }

    @Test
    void shouldUpdateRole() {
        ChatParticipant participant = ChatParticipant.create(userId, ParticipantRole.MEMBER);
        participant.updateRole(ParticipantRole.ADMIN);

        assertEquals(ParticipantRole.ADMIN, participant.getRole());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingRoleToNull() {
        ChatParticipant participant = ChatParticipant.create(userId, ParticipantRole.MEMBER);
        assertThrows(IllegalArgumentException.class, () -> participant.updateRole(null));
    }

    @Test
    void shouldUpdateLastReadTimestamp() throws InterruptedException {
        ChatParticipant participant = ChatParticipant.create(userId, ParticipantRole.MEMBER);
        Instant initialReadAt = participant.getLastReadAt();

        Thread.sleep(10);
        participant.updateLastRead();

        assertTrue(participant.getLastReadAt().isAfter(initialReadAt));
    }

    @Test
    void shouldSetMutedStatus() {
        ChatParticipant participant = ChatParticipant.create(userId, ParticipantRole.MEMBER);
        assertFalse(participant.isMuted());

        participant.setMuted(true);
        assertTrue(participant.isMuted());

        participant.setMuted(false);
        assertFalse(participant.isMuted());
    }

    @Test
    void shouldVerifyEqualsAndHashCodeBasedOnUserId() {
        ChatParticipant p1 = ChatParticipant.create(userId, ParticipantRole.MEMBER);
        ChatParticipant p2 = ChatParticipant.create(userId, ParticipantRole.ADMIN);
        ChatParticipant p3 = ChatParticipant.create(UserId.generate(), ParticipantRole.MEMBER);

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1, p3);
    }
}
