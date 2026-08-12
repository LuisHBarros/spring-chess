package com.chess.chat.domain.model;

import com.chess.chat.domain.exception.ChatParticipantNotFoundException;
import com.chess.chat.domain.exception.ChatRoomArchivedException;
import com.chess.chat.domain.exception.DirectChatParticipantLimitException;
import com.chess.chat.domain.exception.DuplicateParticipantException;
import com.chess.chat.domain.exception.UnauthorizedChatOperationException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ChatRoom {
    private final ChatRoomId id;
    private final ChatRoomType type;
    private RoomTitle title;
    private final UserId creatorId;
    private final String targetReferenceId;
    private final List<ChatParticipant> participants;
    private ChatRoomStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private ChatRoom(
            ChatRoomId id,
            ChatRoomType type,
            RoomTitle title,
            UserId creatorId,
            String targetReferenceId,
            List<ChatParticipant> participants,
            ChatRoomStatus status,
            Instant createdAt,
            Instant updatedAt) {
        if (id == null) {
            throw new IllegalArgumentException("ChatRoomId cannot be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("ChatRoomType cannot be null");
        }
        if (creatorId == null) {
            throw new IllegalArgumentException("Creator UserId cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("ChatRoomStatus cannot be null");
        }
        if (createdAt == null || updatedAt == null) {
            throw new IllegalArgumentException("Timestamps cannot be null");
        }

        this.id = id;
        this.type = type;
        this.title = title;
        this.creatorId = creatorId;
        this.targetReferenceId = targetReferenceId != null ? targetReferenceId : "";
        this.participants = participants != null ? new ArrayList<>(participants) : new ArrayList<>();
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ChatRoom createDirect(UserId creatorId, UserId addresseeId) {
        if (creatorId.equals(addresseeId)) {
            throw new IllegalArgumentException("Cannot create a direct chat room with oneself");
        }
        Instant now = Instant.now();
        ChatRoomId roomId = ChatRoomId.generate();

        List<ChatParticipant> initialParticipants = new ArrayList<>();
        initialParticipants.add(ChatParticipant.create(creatorId, ParticipantRole.OWNER));
        initialParticipants.add(ChatParticipant.create(addresseeId, ParticipantRole.MEMBER));

        RoomTitle defaultTitle = RoomTitle.of("Direct Chat");
        return new ChatRoom(roomId, ChatRoomType.DIRECT, defaultTitle, creatorId, null, initialParticipants, ChatRoomStatus.ACTIVE, now, now);
    }

    public static ChatRoom createGroup(RoomTitle title, UserId creatorId, List<UserId> memberIds) {
        if (title == null) {
            throw new IllegalArgumentException("RoomTitle cannot be null for group chats");
        }
        Instant now = Instant.now();
        ChatRoomId roomId = ChatRoomId.generate();

        List<ChatParticipant> initialParticipants = new ArrayList<>();
        initialParticipants.add(ChatParticipant.create(creatorId, ParticipantRole.OWNER));

        if (memberIds != null) {
            for (UserId memberId : memberIds) {
                if (!memberId.equals(creatorId) && initialParticipants.stream().noneMatch(p -> p.getUserId().equals(memberId))) {
                    initialParticipants.add(ChatParticipant.create(memberId, ParticipantRole.MEMBER));
                }
            }
        }

        return new ChatRoom(roomId, ChatRoomType.GROUP, title, creatorId, null, initialParticipants, ChatRoomStatus.ACTIVE, now, now);
    }

    public static ChatRoom createGuildChannel(RoomTitle title, UserId creatorId, String guildId) {
        if (title == null) {
            throw new IllegalArgumentException("RoomTitle cannot be null for guild channels");
        }
        if (guildId == null || guildId.trim().isEmpty()) {
            throw new IllegalArgumentException("GuildId targetReference cannot be null or empty");
        }
        Instant now = Instant.now();
        ChatRoomId roomId = ChatRoomId.generate();

        List<ChatParticipant> initialParticipants = new ArrayList<>();
        initialParticipants.add(ChatParticipant.create(creatorId, ParticipantRole.OWNER));

        return new ChatRoom(roomId, ChatRoomType.GUILD, title, creatorId, guildId.trim(), initialParticipants, ChatRoomStatus.ACTIVE, now, now);
    }

    public static ChatRoom createMatchChat(UserId player1Id, UserId player2Id, String matchId) {
        if (matchId == null || matchId.trim().isEmpty()) {
            throw new IllegalArgumentException("MatchId targetReference cannot be null or empty");
        }
        Instant now = Instant.now();
        ChatRoomId roomId = ChatRoomId.generate();

        List<ChatParticipant> initialParticipants = new ArrayList<>();
        initialParticipants.add(ChatParticipant.create(player1Id, ParticipantRole.MEMBER));
        initialParticipants.add(ChatParticipant.create(player2Id, ParticipantRole.MEMBER));

        RoomTitle defaultTitle = RoomTitle.of("Match Chat");
        return new ChatRoom(roomId, ChatRoomType.MATCH, defaultTitle, player1Id, matchId.trim(), initialParticipants, ChatRoomStatus.ACTIVE, now, now);
    }

    public static ChatRoom reconstitute(
            ChatRoomId id,
            ChatRoomType type,
            RoomTitle title,
            UserId creatorId,
            String targetReferenceId,
            List<ChatParticipant> participants,
            ChatRoomStatus status,
            Instant createdAt,
            Instant updatedAt) {
        return new ChatRoom(id, type, title, creatorId, targetReferenceId, participants, status, createdAt, updatedAt);
    }

    public void addParticipant(UserId actorId, UserId newParticipantId, ParticipantRole role) {
        verifyNotArchived();
        verifyAuthorizedForManagement(actorId);

        if (type == ChatRoomType.DIRECT) {
            throw new DirectChatParticipantLimitException("Cannot add participants to a direct chat room");
        }

        if (isParticipant(newParticipantId)) {
            throw new DuplicateParticipantException("User " + newParticipantId + " is already a participant in chat room");
        }

        ChatParticipant participant = ChatParticipant.create(newParticipantId, role != null ? role : ParticipantRole.MEMBER);
        participants.add(participant);
        this.updatedAt = Instant.now();
    }

    public void removeParticipant(UserId actorId, UserId targetParticipantId) {
        verifyNotArchived();

        ChatParticipant target = findParticipant(targetParticipantId)
                .orElseThrow(() -> new ChatParticipantNotFoundException("User " + targetParticipantId + " is not a participant in chat room"));

        if (!actorId.equals(targetParticipantId)) {
            verifyAuthorizedForManagement(actorId);
        }

        if (target.getRole() == ParticipantRole.OWNER && participants.size() > 1 && actorId.equals(targetParticipantId)) {
            throw new UnauthorizedChatOperationException("Room owner cannot leave without transferring ownership first");
        }

        participants.remove(target);
        this.updatedAt = Instant.now();
    }

    public void updateParticipantRole(UserId actorId, UserId targetUserId, ParticipantRole newRole) {
        verifyNotArchived();
        verifyOwner(actorId);

        ChatParticipant target = findParticipant(targetUserId)
                .orElseThrow(() -> new ChatParticipantNotFoundException("User " + targetUserId + " is not a participant in chat room"));

        target.updateRole(newRole);
        this.updatedAt = Instant.now();
    }

    public void updateTitle(UserId actorId, RoomTitle newTitle) {
        verifyNotArchived();
        verifyAuthorizedForManagement(actorId);

        if (newTitle == null) {
            throw new IllegalArgumentException("Room title cannot be null");
        }

        this.title = newTitle;
        this.updatedAt = Instant.now();
    }

    public void archive(UserId actorId) {
        verifyAuthorizedForManagement(actorId);
        this.status = ChatRoomStatus.ARCHIVED;
        this.updatedAt = Instant.now();
    }

    public void activate(UserId actorId) {
        verifyAuthorizedForManagement(actorId);
        this.status = ChatRoomStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void updateLastRead(UserId userId) {
        verifyNotArchived();
        ChatParticipant participant = findParticipant(userId)
                .orElseThrow(() -> new ChatParticipantNotFoundException("User " + userId + " is not a participant in chat room"));

        participant.updateLastRead();
        this.updatedAt = Instant.now();
    }

    public boolean isParticipant(UserId userId) {
        return participants.stream().anyMatch(p -> p.getUserId().equals(userId));
    }

    public Optional<ChatParticipant> findParticipant(UserId userId) {
        return participants.stream().filter(p -> p.getUserId().equals(userId)).findFirst();
    }

    private void verifyNotArchived() {
        if (status == ChatRoomStatus.ARCHIVED) {
            throw new ChatRoomArchivedException("Cannot modify an archived chat room");
        }
    }

    private void verifyOwner(UserId actorId) {
        ChatParticipant actor = findParticipant(actorId)
                .orElseThrow(() -> new UnauthorizedChatOperationException("Actor is not a participant of this chat room"));

        if (actor.getRole() != ParticipantRole.OWNER) {
            throw new UnauthorizedChatOperationException("Operation requires room OWNER role");
        }
    }

    private void verifyAuthorizedForManagement(UserId actorId) {
        ChatParticipant actor = findParticipant(actorId)
                .orElseThrow(() -> new UnauthorizedChatOperationException("Actor is not a participant of this chat room"));

        if (actor.getRole() != ParticipantRole.OWNER && actor.getRole() != ParticipantRole.ADMIN) {
            throw new UnauthorizedChatOperationException("Actor does not have management permissions in this chat room");
        }
    }

    public ChatRoomId getId() {
        return id;
    }

    public ChatRoomType getType() {
        return type;
    }

    public RoomTitle getTitle() {
        return title;
    }

    public UserId getCreatorId() {
        return creatorId;
    }

    public String getTargetReferenceId() {
        return targetReferenceId;
    }

    public List<ChatParticipant> getParticipants() {
        return Collections.unmodifiableList(participants);
    }

    public ChatRoomStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatRoom chatRoom = (ChatRoom) o;
        return Objects.equals(id, chatRoom.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
