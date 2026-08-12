package com.chess.chat.infrastructure.web.controller;

import com.chess.chat.domain.model.ChatRoomId;
import com.chess.chat.domain.model.Message;
import com.chess.chat.domain.model.MessageContent;
import com.chess.chat.domain.model.MessageId;
import com.chess.chat.domain.model.UserId;
import com.chess.chat.domain.repository.MessageRepository;
import com.chess.chat.domain.service.MessageDomainService;
import com.chess.chat.infrastructure.web.dto.AddReactionRequestDto;
import com.chess.chat.infrastructure.web.dto.ApiResponseDto;
import com.chess.chat.infrastructure.web.dto.EditMessageRequestDto;
import com.chess.chat.infrastructure.web.dto.MessageResponseDto;
import com.chess.chat.infrastructure.web.dto.SendMessageRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/chat/messages")
@Tag(name = "Chat Messages", description = "Endpoints for sending, editing, deleting, reacting to, and fetching chat messages")
public class MessageController {

    private final MessageDomainService messageDomainService;
    private final MessageRepository messageRepository;

    public MessageController(MessageDomainService messageDomainService, MessageRepository messageRepository) {
        this.messageDomainService = messageDomainService;
        this.messageRepository = messageRepository;
    }

    @PostMapping
    @Operation(summary = "Send message", description = "Sends a new message to a chat room")
    public ResponseEntity<ApiResponseDto<MessageResponseDto>> sendMessage(
            @Valid @RequestBody SendMessageRequestDto dto) {
        com.chess.chat.infrastructure.security.SecurityUtils.validateUserIdentity(dto.getSenderId());
        Message message = messageDomainService.sendMessage(
                ChatRoomId.from(dto.getChatRoomId()),
                UserId.from(dto.getSenderId()),
                MessageContent.of(dto.getContent()),
                dto.getType(),
                dto.getReplyToMessageId() != null ? MessageId.from(dto.getReplyToMessageId()) : null
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Message sent successfully", MessageResponseDto.fromDomain(message)));
    }

    @GetMapping("/room/{roomId}")
    @Operation(summary = "Get room messages", description = "Fetches paginated messages for a given chat room")
    public ResponseEntity<ApiResponseDto<List<MessageResponseDto>>> getRoomMessages(
            @PathVariable("roomId") UUID roomId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "50") int size) {
        List<MessageResponseDto> messages = messageRepository.findByChatRoomId(ChatRoomId.from(roomId), page, size).stream()
                .map(MessageResponseDto::fromDomain)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponseDto.success("Room messages retrieved", messages));
    }

    @GetMapping("/room/{roomId}/unread/count")
    @Operation(summary = "Get unread count", description = "Counts unread messages in a chat room for a specific user")
    public ResponseEntity<ApiResponseDto<Long>> getUnreadCount(
            @PathVariable("roomId") UUID roomId,
            @RequestParam("userId") UUID userId) {
        com.chess.chat.infrastructure.security.SecurityUtils.validateUserIdentity(userId);
        long count = messageRepository.countUnreadMessages(ChatRoomId.from(roomId), UserId.from(userId));
        return ResponseEntity.ok(ApiResponseDto.success("Unread message count retrieved", count));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edit message", description = "Edits message content (only allowed for original sender)")
    public ResponseEntity<ApiResponseDto<MessageResponseDto>> editMessage(
            @PathVariable("id") UUID messageId,
            @Valid @RequestBody EditMessageRequestDto dto) {
        com.chess.chat.infrastructure.security.SecurityUtils.validateUserIdentity(dto.getActorId());
        Message message = messageDomainService.editMessage(
                MessageId.from(messageId),
                UserId.from(dto.getActorId()),
                MessageContent.of(dto.getNewContent())
        );

        return ResponseEntity.ok(ApiResponseDto.success("Message edited successfully", MessageResponseDto.fromDomain(message)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete message", description = "Soft-deletes a message from a chat room")
    public ResponseEntity<ApiResponseDto<MessageResponseDto>> deleteMessage(
            @PathVariable("id") UUID messageId,
            @RequestParam("actorId") UUID actorId) {
        com.chess.chat.infrastructure.security.SecurityUtils.validateUserIdentity(actorId);
        Message message = messageDomainService.deleteMessage(
                MessageId.from(messageId),
                UserId.from(actorId)
        );

        return ResponseEntity.ok(ApiResponseDto.success("Message deleted successfully", MessageResponseDto.fromDomain(message)));
    }

    @PostMapping("/{id}/reactions")
    @Operation(summary = "Add reaction", description = "Adds an emoji reaction to a message")
    public ResponseEntity<ApiResponseDto<MessageResponseDto>> addReaction(
            @PathVariable("id") UUID messageId,
            @Valid @RequestBody AddReactionRequestDto dto) {
        com.chess.chat.infrastructure.security.SecurityUtils.validateUserIdentity(dto.getActorId());
        Message message = messageDomainService.addReaction(
                MessageId.from(messageId),
                UserId.from(dto.getActorId()),
                dto.getEmoji()
        );

        return ResponseEntity.ok(ApiResponseDto.success("Reaction added successfully", MessageResponseDto.fromDomain(message)));
    }

    @DeleteMapping("/{id}/reactions")
    @Operation(summary = "Remove reaction", description = "Removes an emoji reaction from a message")
    public ResponseEntity<ApiResponseDto<MessageResponseDto>> removeReaction(
            @PathVariable("id") UUID messageId,
            @RequestParam("actorId") UUID actorId,
            @RequestParam("emoji") String emoji) {
        com.chess.chat.infrastructure.security.SecurityUtils.validateUserIdentity(actorId);
        Message message = messageDomainService.removeReaction(
                MessageId.from(messageId),
                UserId.from(actorId),
                emoji
        );

        return ResponseEntity.ok(ApiResponseDto.success("Reaction removed successfully", MessageResponseDto.fromDomain(message)));
    }

    @PutMapping("/room/{roomId}/read")
    @Operation(summary = "Mark messages as read", description = "Marks all messages in a chat room as read for a user")
    public ResponseEntity<ApiResponseDto<Void>> markAsRead(
            @PathVariable("roomId") UUID roomId,
            @RequestParam("userId") UUID userId) {
        com.chess.chat.infrastructure.security.SecurityUtils.validateUserIdentity(userId);
        messageDomainService.markMessagesAsRead(ChatRoomId.from(roomId), UserId.from(userId));
        return ResponseEntity.ok(ApiResponseDto.success("Messages marked as read", null));
    }
}
