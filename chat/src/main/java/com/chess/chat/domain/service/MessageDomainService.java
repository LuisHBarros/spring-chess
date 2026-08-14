package com.chess.chat.domain.service;

import com.chess.chat.domain.exception.ChatRoomArchivedException;
import com.chess.chat.domain.exception.ChatRoomNotFoundException;
import com.chess.chat.domain.exception.MessageNotFoundException;
import com.chess.chat.domain.exception.UnauthorizedChatOperationException;
import com.chess.chat.domain.model.ChatRoom;
import com.chess.chat.domain.model.ChatRoomId;
import com.chess.chat.domain.model.ChatRoomStatus;
import com.chess.chat.domain.model.Message;
import com.chess.chat.domain.model.MessageContent;
import com.chess.chat.domain.model.MessageId;
import com.chess.chat.domain.model.MessageType;
import com.chess.chat.domain.model.ChatRoomType;
import com.chess.chat.domain.model.UserId;
import com.chess.chat.domain.port.ChatEventPublisherPort;
import com.chess.chat.domain.port.GuildPermissionPort;
import com.chess.chat.domain.repository.ChatRoomRepository;
import com.chess.chat.domain.repository.MessageRepository;

import java.util.List;
import java.util.Map;

public class MessageDomainService {
    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final ChatEventPublisherPort eventPublisher;
    private final GuildPermissionPort guildPermissionPort;

    public MessageDomainService(
            ChatRoomRepository chatRoomRepository,
            MessageRepository messageRepository,
            ChatEventPublisherPort eventPublisher,
            GuildPermissionPort guildPermissionPort) {
        if (chatRoomRepository == null) {
            throw new IllegalArgumentException("ChatRoomRepository cannot be null");
        }
        if (messageRepository == null) {
            throw new IllegalArgumentException("MessageRepository cannot be null");
        }
        if (guildPermissionPort == null) {
            throw new IllegalArgumentException("GuildPermissionPort cannot be null");
        }
        this.chatRoomRepository = chatRoomRepository;
        this.messageRepository = messageRepository;
        this.eventPublisher = eventPublisher;
        this.guildPermissionPort = guildPermissionPort;
    }

    public Message sendMessage(
            ChatRoomId chatRoomId,
            UserId senderId,
            MessageContent content,
            MessageType type,
            MessageId replyToMessageId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatRoomNotFoundException("Chat room not found with ID: " + chatRoomId));

        if (chatRoom.getStatus() == ChatRoomStatus.ARCHIVED) {
            throw new ChatRoomArchivedException("Cannot send message to an archived chat room");
        }

        if (!chatRoom.isParticipant(senderId)) {
            throw new UnauthorizedChatOperationException("Sender " + senderId + " is not a participant in chat room " + chatRoomId);
        }

        if (chatRoom.getType() == ChatRoomType.GUILD) {
            if (!guildPermissionPort.hasChatAccess(senderId, chatRoom.getTargetReferenceId())) {
                throw new UnauthorizedChatOperationException("Sender " + senderId + " does not have CHAT_ACCESS permission for this guild");
            }
        }

        if (replyToMessageId != null) {
            messageRepository.findById(replyToMessageId)
                    .orElseThrow(() -> new MessageNotFoundException("Replied message not found with ID: " + replyToMessageId));
        }

        long nextSequence = messageRepository.findTopByChatRoomIdOrderBySequenceDesc(chatRoomId)
                .map(Message::getSequence)
                .orElse(0L) + 1;

        Message message = Message.send(chatRoomId, senderId, content, type, replyToMessageId, nextSequence);
        Message saved = messageRepository.save(message);

        publishEvent("MESSAGE_SENT", saved.getId().toString(), Map.of(
                "messageId", saved.getId().toString(),
                "chatRoomId", chatRoomId.toString(),
                "senderId", senderId.toString(),
                "type", saved.getType().name()
        ));

        return saved;
    }

    public Message sendMessage(ChatRoomId chatRoomId, UserId senderId, MessageContent content) {
        return sendMessage(chatRoomId, senderId, content, MessageType.TEXT, null);
    }

    public Message editMessage(MessageId messageId, UserId actorId, MessageContent newContent) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException("Message not found with ID: " + messageId));

        message.edit(actorId, newContent);
        Message saved = messageRepository.save(message);

        publishEvent("MESSAGE_EDITED", saved.getId().toString(), Map.of(
                "messageId", saved.getId().toString(),
                "chatRoomId", saved.getChatRoomId().toString(),
                "actorId", actorId.toString()
        ));

        return saved;
    }

    public Message deleteMessage(MessageId messageId, UserId actorId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException("Message not found with ID: " + messageId));

        message.delete(actorId);
        Message saved = messageRepository.save(message);

        publishEvent("MESSAGE_DELETED", saved.getId().toString(), Map.of(
                "messageId", saved.getId().toString(),
                "chatRoomId", saved.getChatRoomId().toString(),
                "actorId", actorId.toString()
        ));

        return saved;
    }

    public Message addReaction(MessageId messageId, UserId actorId, String emoji) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException("Message not found with ID: " + messageId));

        ChatRoom chatRoom = chatRoomRepository.findById(message.getChatRoomId())
                .orElseThrow(() -> new ChatRoomNotFoundException("Chat room not found with ID: " + message.getChatRoomId()));

        if (!chatRoom.isParticipant(actorId)) {
            throw new UnauthorizedChatOperationException("User " + actorId + " is not a participant in chat room");
        }

        message.addReaction(actorId, emoji);
        return messageRepository.save(message);
    }

    public Message removeReaction(MessageId messageId, UserId actorId, String emoji) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException("Message not found with ID: " + messageId));

        message.removeReaction(actorId, emoji);
        return messageRepository.save(message);
    }

    public void markMessagesAsRead(ChatRoomId chatRoomId, UserId userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatRoomNotFoundException("Chat room not found with ID: " + chatRoomId));

        chatRoom.updateLastRead(userId);
        chatRoomRepository.save(chatRoom);

        List<Message> unreadMessages = messageRepository.findUnreadMessages(chatRoomId, userId);
        for (Message unread : unreadMessages) {
            unread.markAsRead();
            messageRepository.save(unread);
        }
    }

    private void publishEvent(String eventType, String entityId, Map<String, Object> eventData) {
        if (eventPublisher != null) {
            eventPublisher.publishChatEvent(eventType, entityId, eventData);
        }
    }
}
