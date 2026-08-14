package com.chess.chat.infrastructure.persistence.entity;

import com.chess.chat.domain.model.ChatParticipant;
import com.chess.chat.domain.model.ChatRoom;
import com.chess.chat.domain.model.ChatRoomStatus;
import com.chess.chat.domain.model.ChatRoomType;
import com.chess.chat.domain.model.ParticipantRole;
import com.chess.chat.domain.model.RoomTitle;
import com.chess.chat.domain.model.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ChatRoomJpaEntityTest {

    private UserId creator;
    private UserId member;

    @BeforeEach
    void setUp() {
        creator = UserId.generate();
        member = UserId.generate();
    }

    @Test
    void shouldMapDirectChatRoomToEntityAndBack() {
        ChatRoom domainRoom = ChatRoom.createDirect(creator, member);
        ChatRoomJpaEntity entity = ChatRoomJpaEntity.fromDomain(domainRoom);

        assertNotNull(entity);
        assertEquals(domainRoom.getId().getValue(), entity.getId());
        assertEquals(ChatRoomType.DIRECT, entity.getType());
        assertNull(entity.getTitle());
        assertEquals(creator.getValue(), entity.getCreatorId());
        assertNull(entity.getTargetReferenceId());
        assertEquals(2, entity.getParticipants().size());
        assertEquals(ChatRoomStatus.ACTIVE, entity.getStatus());
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());

        ChatRoom reconstituted = entity.toDomain();
        assertEquals(domainRoom.getId(), reconstituted.getId());
        assertEquals(domainRoom.getType(), reconstituted.getType());
        assertEquals(domainRoom.getCreatorId(), reconstituted.getCreatorId());
        assertEquals(2, reconstituted.getParticipants().size());
        assertEquals(ChatRoomStatus.ACTIVE, reconstituted.getStatus());
    }

    @Test
    void shouldMapGroupChatRoomToEntityAndBack() {
        RoomTitle title = RoomTitle.of("Grandmasters");
        ChatRoom domainRoom = ChatRoom.createGroup(title, creator, List.of(member));
        ChatRoomJpaEntity entity = ChatRoomJpaEntity.fromDomain(domainRoom);

        assertEquals("Grandmasters", entity.getTitle());
        assertEquals(ChatRoomType.GROUP, entity.getType());

        ChatRoom reconstituted = entity.toDomain();
        assertEquals("Grandmasters", reconstituted.getTitle().getValue());
        assertEquals(ChatRoomType.GROUP, reconstituted.getType());
    }

    @Test
    void shouldMapGuildChannelRoomToEntityAndBack() {
        RoomTitle title = RoomTitle.of("Guild Chat");
        ChatRoom domainRoom = ChatRoom.createGuildChannel(title, creator, "guild-99");
        ChatRoomJpaEntity entity = ChatRoomJpaEntity.fromDomain(domainRoom);

        assertEquals("guild-99", entity.getTargetReferenceId());
        assertEquals(ChatRoomType.GUILD, entity.getType());

        ChatRoom reconstituted = entity.toDomain();
        assertEquals("guild-99", reconstituted.getTargetReferenceId());
    }

    @Test
    void shouldMapMatchRoomToEntityAndBack() {
        ChatRoom domainRoom = ChatRoom.createMatch(creator, member, "match-888");
        ChatRoomJpaEntity entity = ChatRoomJpaEntity.fromDomain(domainRoom);

        assertEquals("match-888", entity.getTargetReferenceId());
        assertEquals(ChatRoomType.MATCH, entity.getType());

        ChatRoom reconstituted = entity.toDomain();
        assertEquals("match-888", reconstituted.getTargetReferenceId());
    }

    @Test
    void shouldConstructWithDefaultConstructorAndGetters() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        ChatRoomJpaEntity entity = new ChatRoomJpaEntity(
                id,
                ChatRoomType.DIRECT,
                "Direct",
                creator.getValue(),
                "ref-123",
                null,
                null,
                ChatRoomStatus.ARCHIVED,
                now,
                now
        );

        assertEquals(id, entity.getId());
        assertEquals(ChatRoomType.DIRECT, entity.getType());
        assertEquals("Direct", entity.getTitle());
        assertEquals(creator.getValue(), entity.getCreatorId());
        assertEquals("ref-123", entity.getTargetReferenceId());
        assertTrue(entity.getParticipants().isEmpty());
        assertEquals(ChatRoomStatus.ARCHIVED, entity.getStatus());
        assertEquals(now, entity.getCreatedAt());
        assertEquals(now, entity.getUpdatedAt());
    }
}
