package com.chess.chat.infrastructure.web.controller;

import com.chess.chat.domain.model.ChatRoom;
import com.chess.chat.domain.model.ChatRoomId;
import com.chess.chat.domain.model.RoomTitle;
import com.chess.chat.domain.model.UserId;
import com.chess.chat.domain.repository.ChatRoomRepository;
import com.chess.chat.domain.service.ChatRoomDomainService;
import com.chess.chat.infrastructure.web.GlobalExceptionHandler;
import com.chess.chat.infrastructure.web.dto.CreateDirectChatRequestDto;
import com.chess.chat.infrastructure.web.dto.CreateGroupChatRequestDto;
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
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ChatRoomControllerTest {

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
    void shouldCreateDirectChatEndpoint() throws Exception {
        ChatRoom room = ChatRoom.createDirect(user1, user2);
        when(chatRoomDomainService.createDirectChat(any(UserId.class), any(UserId.class))).thenReturn(room);

        CreateDirectChatRequestDto dto = new CreateDirectChatRequestDto(user1.getValue(), user2.getValue());

        mockMvc.perform(post("/api/v1/chat/rooms/direct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.type").value("DIRECT"));
    }

    @Test
    void shouldCreateGroupChatEndpoint() throws Exception {
        ChatRoom room = ChatRoom.createGroup(RoomTitle.of("Chess Club"), user1, List.of(user2));
        when(chatRoomDomainService.createGroupChat(any(), any(), any())).thenReturn(room);

        CreateGroupChatRequestDto dto = new CreateGroupChatRequestDto("Chess Club", user1.getValue(), List.of(user2.getValue()));

        mockMvc.perform(post("/api/v1/chat/rooms/group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Chess Club"));
    }

    @Test
    void shouldGetChatRoomByIdEndpoint() throws Exception {
        ChatRoom room = ChatRoom.createDirect(user1, user2);
        when(chatRoomRepository.findById(room.getId())).thenReturn(Optional.of(room));

        mockMvc.perform(get("/api/v1/chat/rooms/{id}", room.getId().getValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(room.getId().toString()));
    }
}
