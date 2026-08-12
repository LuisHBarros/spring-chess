package com.chess.chat.domain;

import com.chess.chat.domain.exception.ChatParticipantNotFoundException;
import com.chess.chat.domain.exception.ChatRoomArchivedException;
import com.chess.chat.domain.exception.DirectChatParticipantLimitException;
import com.chess.chat.domain.exception.DuplicateParticipantException;
import com.chess.chat.domain.exception.UnauthorizedChatOperationException;
import com.chess.chat.domain.model.ChatParticipant;
import com.chess.chat.domain.model.ChatRoom;
import com.chess.chat.domain.model.ChatRoomStatus;
import com.chess.chat.domain.model.ChatRoomType;
import com.chess.chat.domain.model.ParticipantRole;
import com.chess.chat.domain.model.RoomTitle;
import com.chess.chat.domain.model.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChatRoomTest {

    private UserId user1;
    private UserId user2;
    private UserId user3;

    @BeforeEach
    void setUp() {
        user1 = UserId.generate();
        user2 = UserId.generate();
        user3 = UserId.generate();
    }

    @Test
    void shouldCreateDirectChatRoom() {
        ChatRoom room = ChatRoom.createDirect(user1, user2);

        assertNotNull(room.getId());
        assertEquals(ChatRoomType.DIRECT, room.getType());
        assertEquals(ChatRoomStatus.ACTIVE, room.getStatus());
        assertEquals(2, room.getParticipants().size());
        assertTrue(room.isParticipant(user1));
        assertTrue(room.isParticipant(user2));
    }

    @Test
    void shouldThrowExceptionWhenDirectChatWithSelf() {
        assertThrows(IllegalArgumentException.class, () -> ChatRoom.createDirect(user1, user1));
    }

    @Test
    void shouldNotAllowAddingParticipantToDirectChat() {
        ChatRoom room = ChatRoom.createDirect(user1, user2);
        assertThrows(DirectChatParticipantLimitException.class, () -> room.addParticipant(user1, user3, ParticipantRole.MEMBER));
    }

    @Test
    void shouldCreateGroupChatRoomAndAddParticipant() {
        ChatRoom room = ChatRoom.createGroup(RoomTitle.of("Chess Enthusiasts"), user1, List.of(user2));

        assertEquals(2, room.getParticipants().size());
        assertTrue(room.isParticipant(user1));
        assertTrue(room.isParticipant(user2));

        room.addParticipant(user1, user3, ParticipantRole.MEMBER);
        assertEquals(3, room.getParticipants().size());
        assertTrue(room.isParticipant(user3));
    }

    @Test
    void shouldThrowExceptionWhenAddingDuplicateParticipant() {
        ChatRoom room = ChatRoom.createGroup(RoomTitle.of("Group"), user1, List.of(user2));
        assertThrows(DuplicateParticipantException.class, () -> room.addParticipant(user1, user2, ParticipantRole.MEMBER));
    }

    @Test
    void shouldThrowExceptionWhenUnauthorizedUserAddsParticipant() {
        ChatRoom room = ChatRoom.createGroup(RoomTitle.of("Group"), user1, List.of(user2));
        assertThrows(UnauthorizedChatOperationException.class, () -> room.addParticipant(user2, user3, ParticipantRole.MEMBER));
    }

    @Test
    void shouldRemoveParticipantFromGroupChat() {
        ChatRoom room = ChatRoom.createGroup(RoomTitle.of("Group"), user1, List.of(user2, user3));
        assertEquals(3, room.getParticipants().size());

        room.removeParticipant(user1, user3);
        assertEquals(2, room.getParticipants().size());
        assertFalse(room.isParticipant(user3));
    }

    @Test
    void shouldAllowUserToLeaveGroupChat() {
        ChatRoom room = ChatRoom.createGroup(RoomTitle.of("Group"), user1, List.of(user2));
        room.removeParticipant(user2, user2);
        assertFalse(room.isParticipant(user2));
    }

    @Test
    void shouldUpdateRoomTitle() {
        ChatRoom room = ChatRoom.createGroup(RoomTitle.of("Old Title"), user1, List.of(user2));
        room.updateTitle(user1, RoomTitle.of("New Title"));
        assertEquals("New Title", room.getTitle().getValue());
    }

    @Test
    void shouldArchiveAndPreventModification() {
        ChatRoom room = ChatRoom.createGroup(RoomTitle.of("Group"), user1, List.of(user2));
        room.archive(user1);

        assertEquals(ChatRoomStatus.ARCHIVED, room.getStatus());
        assertThrows(ChatRoomArchivedException.class, () -> room.addParticipant(user1, user3, ParticipantRole.MEMBER));
    }

    @Test
    void shouldCreateGuildChannelAndMatchChat() {
        ChatRoom guildRoom = ChatRoom.createGuildChannel(RoomTitle.of("Guild General"), user1, "guild-123");
        assertEquals(ChatRoomType.GUILD, guildRoom.getType());
        assertEquals("guild-123", guildRoom.getTargetReferenceId());

        ChatRoom matchRoom = ChatRoom.createMatchChat(user1, user2, "match-999");
        assertEquals(ChatRoomType.MATCH, matchRoom.getType());
        assertEquals("match-999", matchRoom.getTargetReferenceId());
        assertEquals(2, matchRoom.getParticipants().size());
    }
}
