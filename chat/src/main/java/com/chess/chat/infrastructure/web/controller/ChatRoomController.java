package com.chess.chat.infrastructure.web.controller;

import com.chess.chat.domain.model.ChatRoom;
import com.chess.chat.domain.model.ChatRoomId;
import com.chess.chat.domain.model.RoomTitle;
import com.chess.chat.domain.model.UserId;
import com.chess.chat.domain.repository.ChatRoomRepository;
import com.chess.chat.domain.service.ChatRoomDomainService;
import com.chess.chat.infrastructure.web.dto.AddParticipantRequestDto;
import com.chess.chat.infrastructure.web.dto.ApiResponseDto;
import com.chess.chat.infrastructure.web.dto.ChatRoomResponseDto;
import com.chess.chat.infrastructure.web.dto.CreateDirectChatRequestDto;
import com.chess.chat.infrastructure.web.dto.CreateGroupChatRequestDto;
import com.chess.chat.infrastructure.web.dto.CreateGuildChannelRequestDto;
import com.chess.chat.infrastructure.web.dto.CreateMatchChatRequestDto;
import com.chess.chat.infrastructure.web.dto.UpdateTitleRequestDto;
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
@RequestMapping("/api/v1/chat/rooms")
@Tag(name = "Chat Rooms", description = "Endpoints for managing direct, group, guild, and match chat rooms")
public class ChatRoomController {

    private final ChatRoomDomainService chatRoomDomainService;
    private final ChatRoomRepository chatRoomRepository;

    public ChatRoomController(ChatRoomDomainService chatRoomDomainService, ChatRoomRepository chatRoomRepository) {
        this.chatRoomDomainService = chatRoomDomainService;
        this.chatRoomRepository = chatRoomRepository;
    }

    @PostMapping("/direct")
    @Operation(summary = "Create direct chat", description = "Creates a 1-on-1 direct messaging room between two users")
    public ResponseEntity<ApiResponseDto<ChatRoomResponseDto>> createDirectChat(
            @Valid @RequestBody CreateDirectChatRequestDto dto) {
        ChatRoom room = chatRoomDomainService.createDirectChat(
                UserId.from(dto.getCreatorId()),
                UserId.from(dto.getAddresseeId())
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Direct chat created successfully", ChatRoomResponseDto.fromDomain(room)));
    }

    @PostMapping("/group")
    @Operation(summary = "Create group chat", description = "Creates a multi-user group chat room")
    public ResponseEntity<ApiResponseDto<ChatRoomResponseDto>> createGroupChat(
            @Valid @RequestBody CreateGroupChatRequestDto dto) {
        List<UserId> members = dto.getMemberIds() != null
                ? dto.getMemberIds().stream().map(UserId::from).collect(Collectors.toList())
                : List.of();

        ChatRoom room = chatRoomDomainService.createGroupChat(
                RoomTitle.of(dto.getTitle()),
                UserId.from(dto.getCreatorId()),
                members
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Group chat created successfully", ChatRoomResponseDto.fromDomain(room)));
    }

    @PostMapping("/guild")
    @Operation(summary = "Create guild channel", description = "Creates a guild channel linked to a Social Guild")
    public ResponseEntity<ApiResponseDto<ChatRoomResponseDto>> createGuildChannel(
            @Valid @RequestBody CreateGuildChannelRequestDto dto) {
        ChatRoom room = chatRoomDomainService.createGuildChannelChat(
                RoomTitle.of(dto.getTitle()),
                UserId.from(dto.getCreatorId()),
                dto.getGuildId()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Guild channel created successfully", ChatRoomResponseDto.fromDomain(room)));
    }

    @PostMapping("/match")
    @Operation(summary = "Create match chat", description = "Creates an in-game match chat room")
    public ResponseEntity<ApiResponseDto<ChatRoomResponseDto>> createMatchChat(
            @Valid @RequestBody CreateMatchChatRequestDto dto) {
        ChatRoom room = chatRoomDomainService.createMatchChat(
                UserId.from(dto.getPlayer1Id()),
                UserId.from(dto.getPlayer2Id()),
                dto.getMatchId()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Match chat created successfully", ChatRoomResponseDto.fromDomain(room)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get chat room by ID", description = "Fetches a chat room details by ID")
    public ResponseEntity<ApiResponseDto<ChatRoomResponseDto>> getChatRoomById(@PathVariable("id") UUID id) {
        ChatRoom room = chatRoomRepository.findById(ChatRoomId.from(id))
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found with ID: " + id));

        return ResponseEntity.ok(ApiResponseDto.success("Chat room retrieved", ChatRoomResponseDto.fromDomain(room)));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user chat rooms", description = "Fetches all chat rooms where the specified user is a participant")
    public ResponseEntity<ApiResponseDto<List<ChatRoomResponseDto>>> getUserChatRooms(@PathVariable("userId") UUID userId) {
        List<ChatRoomResponseDto> rooms = chatRoomRepository.findByParticipantUserId(UserId.from(userId)).stream()
                .map(ChatRoomResponseDto::fromDomain)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponseDto.success("User chat rooms retrieved", rooms));
    }

    @PostMapping("/{id}/participants")
    @Operation(summary = "Add participant", description = "Adds a user to a group or guild chat room")
    public ResponseEntity<ApiResponseDto<ChatRoomResponseDto>> addParticipant(
            @PathVariable("id") UUID roomId,
            @Valid @RequestBody AddParticipantRequestDto dto) {
        ChatRoom room = chatRoomDomainService.addParticipant(
                ChatRoomId.from(roomId),
                UserId.from(dto.getActorId()),
                UserId.from(dto.getNewParticipantId()),
                dto.getRole()
        );

        return ResponseEntity.ok(ApiResponseDto.success("Participant added successfully", ChatRoomResponseDto.fromDomain(room)));
    }

    @DeleteMapping("/{id}/participants/{participantId}")
    @Operation(summary = "Remove participant", description = "Removes a participant or leaves a chat room")
    public ResponseEntity<ApiResponseDto<ChatRoomResponseDto>> removeParticipant(
            @PathVariable("id") UUID roomId,
            @PathVariable("participantId") UUID participantId,
            @RequestParam("actorId") UUID actorId) {
        ChatRoom room = chatRoomDomainService.removeParticipant(
                ChatRoomId.from(roomId),
                UserId.from(actorId),
                UserId.from(participantId)
        );

        return ResponseEntity.ok(ApiResponseDto.success("Participant removed successfully", ChatRoomResponseDto.fromDomain(room)));
    }

    @PutMapping("/{id}/title")
    @Operation(summary = "Update chat room title", description = "Updates the title of a group or guild chat room")
    public ResponseEntity<ApiResponseDto<ChatRoomResponseDto>> updateTitle(
            @PathVariable("id") UUID roomId,
            @Valid @RequestBody UpdateTitleRequestDto dto) {
        ChatRoom room = chatRoomDomainService.updateTitle(
                ChatRoomId.from(roomId),
                UserId.from(dto.getActorId()),
                RoomTitle.of(dto.getNewTitle())
        );

        return ResponseEntity.ok(ApiResponseDto.success("Title updated successfully", ChatRoomResponseDto.fromDomain(room)));
    }

    @PutMapping("/{id}/archive")
    @Operation(summary = "Archive chat room", description = "Archives a chat room to disable messaging")
    public ResponseEntity<ApiResponseDto<ChatRoomResponseDto>> archiveChatRoom(
            @PathVariable("id") UUID roomId,
            @RequestParam("actorId") UUID actorId) {
        ChatRoom room = chatRoomDomainService.archiveChatRoom(
                ChatRoomId.from(roomId),
                UserId.from(actorId)
        );

        return ResponseEntity.ok(ApiResponseDto.success("Chat room archived successfully", ChatRoomResponseDto.fromDomain(room)));
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Activate chat room", description = "Re-activates an archived chat room")
    public ResponseEntity<ApiResponseDto<ChatRoomResponseDto>> activateChatRoom(
            @PathVariable("id") UUID roomId,
            @RequestParam("actorId") UUID actorId) {
        ChatRoom room = chatRoomDomainService.activateChatRoom(
                ChatRoomId.from(roomId),
                UserId.from(actorId)
        );

        return ResponseEntity.ok(ApiResponseDto.success("Chat room activated successfully", ChatRoomResponseDto.fromDomain(room)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete chat room", description = "Deletes a chat room and all associated messages")
    public ResponseEntity<ApiResponseDto<Void>> deleteChatRoom(
            @PathVariable("id") UUID roomId,
            @RequestParam("actorId") UUID actorId) {
        chatRoomDomainService.deleteChatRoom(
                ChatRoomId.from(roomId),
                UserId.from(actorId)
        );

        return ResponseEntity.ok(ApiResponseDto.success("Chat room deleted successfully", null));
    }
}
