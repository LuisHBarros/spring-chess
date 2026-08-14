package com.chess.chat.domain.service;

import com.chess.chat.domain.exception.ChatRoomNotFoundException;
import com.chess.chat.domain.exception.DuplicateDirectChatException;
import com.chess.chat.domain.model.ChatRoom;
import com.chess.chat.domain.model.ChatRoomId;
import com.chess.chat.domain.model.ParticipantRole;
import com.chess.chat.domain.model.RoomTitle;
import com.chess.chat.domain.model.UserId;
import com.chess.chat.domain.port.ChatEventPublisherPort;
import com.chess.chat.domain.repository.ChatRoomRepository;
import com.chess.chat.domain.repository.MessageRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Transactional
public class ChatRoomDomainService {
    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final ChatEventPublisherPort eventPublisher;

    public ChatRoomDomainService(
            ChatRoomRepository chatRoomRepository,
            MessageRepository messageRepository,
            ChatEventPublisherPort eventPublisher) {
        if (chatRoomRepository == null) {
            throw new IllegalArgumentException("ChatRoomRepository cannot be null");
        }
        if (messageRepository == null) {
            throw new IllegalArgumentException("MessageRepository cannot be null");
        }
        this.chatRoomRepository = chatRoomRepository;
        this.messageRepository = messageRepository;
        this.eventPublisher = eventPublisher;
    }

    public ChatRoom createDirectChat(UserId creatorId, UserId addresseeId) {
        Optional<ChatRoom> existing = chatRoomRepository.findDirectRoomBetweenUsers(creatorId, addresseeId);
        if (existing.isPresent()) {
            throw new DuplicateDirectChatException("A direct chat room already exists between users " + creatorId + " and " + addresseeId);
        }

        ChatRoom chatRoom = ChatRoom.createDirect(creatorId, addresseeId);
        ChatRoom saved;
        try {
            saved = chatRoomRepository.save(chatRoom);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateDirectChatException("A direct chat room already exists between users " + creatorId + " and " + addresseeId);
        }

        publishEvent("CHAT_ROOM_CREATED", saved.getId().toString(), Map.of(
                "roomId", saved.getId().toString(),
                "type", saved.getType().name(),
                "creatorId", creatorId.toString(),
                "addresseeId", addresseeId.toString()
        ));

        return saved;
    }

    public ChatRoom createGroupChat(RoomTitle title, UserId creatorId, List<UserId> memberIds) {
        ChatRoom chatRoom = ChatRoom.createGroup(title, creatorId, memberIds);
        ChatRoom saved = chatRoomRepository.save(chatRoom);

        publishEvent("CHAT_ROOM_CREATED", saved.getId().toString(), Map.of(
                "roomId", saved.getId().toString(),
                "type", saved.getType().name(),
                "title", title.getValue(),
                "creatorId", creatorId.toString()
        ));

        return saved;
    }

    public ChatRoom createGuildChannelChat(RoomTitle title, UserId creatorId, String guildId) {
        ChatRoom chatRoom = ChatRoom.createGuildChannel(title, creatorId, guildId);
        ChatRoom saved = chatRoomRepository.save(chatRoom);

        publishEvent("GUILD_CHAT_CREATED", saved.getId().toString(), Map.of(
                "roomId", saved.getId().toString(),
                "guildId", guildId,
                "title", title.getValue(),
                "creatorId", creatorId.toString()
        ));

        return saved;
    }

    public ChatRoom createMatchChat(UserId player1Id, UserId player2Id, String matchId) {
        ChatRoom chatRoom = ChatRoom.createMatchChat(player1Id, player2Id, matchId);
        ChatRoom saved = chatRoomRepository.save(chatRoom);

        publishEvent("MATCH_CHAT_CREATED", saved.getId().toString(), Map.of(
                "roomId", saved.getId().toString(),
                "matchId", matchId,
                "player1Id", player1Id.toString(),
                "player2Id", player2Id.toString()
        ));

        return saved;
    }

    public ChatRoom addParticipant(ChatRoomId roomId, UserId actorId, UserId newParticipantId, ParticipantRole role) {
        ChatRoom chatRoom = getChatRoomOrThrow(roomId);
        chatRoom.addParticipant(actorId, newParticipantId, role);
        ChatRoom saved = chatRoomRepository.save(chatRoom);

        publishEvent("PARTICIPANT_ADDED", saved.getId().toString(), Map.of(
                "roomId", saved.getId().toString(),
                "addedUserId", newParticipantId.toString(),
                "actorId", actorId.toString()
        ));

        return saved;
    }

    public ChatRoom removeParticipant(ChatRoomId roomId, UserId actorId, UserId targetParticipantId) {
        ChatRoom chatRoom = getChatRoomOrThrow(roomId);
        chatRoom.removeParticipant(actorId, targetParticipantId);
        ChatRoom saved = chatRoomRepository.save(chatRoom);

        publishEvent("PARTICIPANT_REMOVED", saved.getId().toString(), Map.of(
                "roomId", saved.getId().toString(),
                "removedUserId", targetParticipantId.toString(),
                "actorId", actorId.toString()
        ));

        return saved;
    }

    public ChatRoom updateTitle(ChatRoomId roomId, UserId actorId, RoomTitle newTitle) {
        ChatRoom chatRoom = getChatRoomOrThrow(roomId);
        chatRoom.updateTitle(actorId, newTitle);
        return chatRoomRepository.save(chatRoom);
    }

    public ChatRoom archiveChatRoom(ChatRoomId roomId, UserId actorId) {
        ChatRoom chatRoom = getChatRoomOrThrow(roomId);
        chatRoom.archive(actorId);
        return chatRoomRepository.save(chatRoom);
    }

    public ChatRoom activateChatRoom(ChatRoomId roomId, UserId actorId) {
        ChatRoom chatRoom = getChatRoomOrThrow(roomId);
        chatRoom.activate(actorId);
        return chatRoomRepository.save(chatRoom);
    }

    public void deleteChatRoom(ChatRoomId roomId, UserId actorId) {
        ChatRoom chatRoom = getChatRoomOrThrow(roomId);
        chatRoom.archive(actorId); // verify management authority
        messageRepository.deleteAllByChatRoomId(roomId);
        chatRoomRepository.delete(chatRoom);

        publishEvent("CHAT_ROOM_DELETED", roomId.toString(), Map.of(
                "roomId", roomId.toString(),
                "actorId", actorId.toString()
        ));
    }

    private ChatRoom getChatRoomOrThrow(ChatRoomId roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException("Chat room not found with ID: " + roomId));
    }

    private void publishEvent(String eventType, String entityId, Map<String, Object> eventData) {
        if (eventPublisher != null) {
            eventPublisher.publishChatEvent(eventType, entityId, eventData);
        }
    }
}
