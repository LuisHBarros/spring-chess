package com.chess.chat.infrastructure.web.controller;

import com.chess.chat.domain.model.ChatParticipant;
import com.chess.chat.domain.model.ChatRoom;
import com.chess.chat.domain.model.ChatRoomId;
import com.chess.chat.domain.model.ParticipantRole;
import com.chess.chat.domain.model.RoomTitle;
import com.chess.chat.domain.model.UserId;
import com.chess.chat.domain.repository.ChatRoomRepository;
import com.chess.chat.domain.service.ChatRoomDomainService;
import com.chess.chat.infrastructure.web.GlobalExceptionHandler;
import com.chess.chat.infrastructure.web.dto.AddParticipantRequestDto;
import com.chess.chat.infrastructure.web.dto.CreateGuildChannelRequestDto;
import com.chess.chat.infrastructure.web.dto.CreateMatchChatRequestDto;
import com.chess.chat.infrastructure.web.dto.UpdateTitleRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ChatRoomControllerEndpointsTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ChatRoomDomainService chatRoomDomainService;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @InjectMocks
    private ChatRoomController chatRoomController;

    private UserId user1;
    private UserId user2;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(chatRoomController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        user1 = UserId.generate();
        user2 = UserId.generate();
    }

    @Test
    void shouldCreateGuildChannelEndpoint() throws Exception {
        ChatRoom room = ChatRoom.createGuildChannel(RoomTitle.of("Guild General"), user1, "guild-123");
        when(chatRoomDomainService.createGuildChannelChat(any(), any(), eq("guild-123"))).thenReturn(room);

        CreateGuildChannelRequestDto dto = new CreateGuildChannelRequestDto("Guild General", user1.getValue(), "guild-123");

        mockMvc.perform(post("/api/v1/chat/rooms/guild")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.type").value("GUILD_CHANNEL"))
                .andExpect(jsonPath("$.data.title").value("Guild General"))
                .andExpect(jsonPath("$.data.targetReferenceId").value("guild-123"));
    }

    @Test
    void shouldCreateMatchChatEndpoint() throws Exception {
        ChatRoom room = ChatRoom.createMatch(user1, user2, "match-456");
        when(chatRoomDomainService.createMatchChat(any(), any(), eq("match-456"))).thenReturn(room);

        CreateMatchChatRequestDto dto = new CreateMatchChatRequestDto(user1.getValue(), user2.getValue(), "match-456");

        mockMvc.perform(post("/api/v1/chat/rooms/match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.type").value("MATCH"))
                .andExpect(jsonPath("$.data.targetReferenceId").value("match-456"));
    }

    @Test
    void shouldGetUserChatRoomsEndpoint() throws Exception {
        ChatRoom room1 = ChatRoom.createDirect(user1, user2);
        when(chatRoomRepository.findByParticipantUserId(user1)).thenReturn(List.of(room1));

        mockMvc.perform(get("/api/v1/chat/rooms/user/{userId}", user1.getValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(room1.getId().toString()));
    }

    @Test
    void shouldAddParticipantEndpoint() throws Exception {
        ChatRoom room = ChatRoom.createGroup(RoomTitle.of("Group"), user1, List.of());
        room.addParticipant(ChatParticipant.create(user2, ParticipantRole.MEMBER));

        when(chatRoomDomainService.addParticipant(any(), any(), any(), eq(ParticipantRole.MEMBER))).thenReturn(room);

        AddParticipantRequestDto dto = new AddParticipantRequestDto(user1.getValue(), user2.getValue(), ParticipantRole.MEMBER);

        mockMvc.perform(post("/api/v1/chat/rooms/{id}/participants", room.getId().getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.participants").isArray());
    }

    @Test
    void shouldRemoveParticipantEndpoint() throws Exception {
        ChatRoom room = ChatRoom.createGroup(RoomTitle.of("Group"), user1, List.of(user2));
        when(chatRoomDomainService.removeParticipant(any(), any(), any())).thenReturn(room);

        mockMvc.perform(delete("/api/v1/chat/rooms/{id}/participants/{participantId}", room.getId().getValue(), user2.getValue())
                        .param("actorId", user1.getValue().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldUpdateTitleEndpoint() throws Exception {
        ChatRoom room = ChatRoom.createGroup(RoomTitle.of("New Title"), user1, List.of());
        when(chatRoomDomainService.updateTitle(any(), any(), any())).thenReturn(room);

        UpdateTitleRequestDto dto = new UpdateTitleRequestDto(user1.getValue(), "New Title");

        mockMvc.perform(put("/api/v1/chat/rooms/{id}/title", room.getId().getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("New Title"));
    }

    @Test
    void shouldArchiveChatRoomEndpoint() throws Exception {
        ChatRoom room = ChatRoom.createDirect(user1, user2);
        room.archive();
        when(chatRoomDomainService.archiveChatRoom(any(), any())).thenReturn(room);

        mockMvc.perform(put("/api/v1/chat/rooms/{id}/archive", room.getId().getValue())
                        .param("actorId", user1.getValue().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ARCHIVED"));
    }

    @Test
    void shouldActivateChatRoomEndpoint() throws Exception {
        ChatRoom room = ChatRoom.createDirect(user1, user2);
        when(chatRoomDomainService.activateChatRoom(any(), any())).thenReturn(room);

        mockMvc.perform(put("/api/v1/chat/rooms/{id}/activate", room.getId().getValue())
                        .param("actorId", user1.getValue().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void shouldDeleteChatRoomEndpoint() throws Exception {
        UUID roomId = UUID.randomUUID();
        doNothing().when(chatRoomDomainService).deleteChatRoom(any(), any());

        mockMvc.perform(delete("/api/v1/chat/rooms/{id}", roomId)
                        .param("actorId", user1.getValue().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Chat room deleted successfully"));

        verify(chatRoomDomainService).deleteChatRoom(ChatRoomId.from(roomId), user1);
    }
}
