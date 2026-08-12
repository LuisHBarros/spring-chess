package com.chess.chat.infrastructure.web.controller;

import com.chess.chat.domain.model.ChatRoomId;
import com.chess.chat.domain.model.Message;
import com.chess.chat.domain.model.MessageContent;
import com.chess.chat.domain.model.MessageType;
import com.chess.chat.domain.model.UserId;
import com.chess.chat.domain.repository.MessageRepository;
import com.chess.chat.domain.service.MessageDomainService;
import com.chess.chat.infrastructure.web.GlobalExceptionHandler;
import com.chess.chat.infrastructure.web.dto.SendMessageRequestDto;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MessageControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private MessageDomainService messageDomainService;

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private MessageController messageController;

    private ChatRoomId roomId;
    private UserId senderId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(messageController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        roomId = ChatRoomId.generate();
        senderId = UserId.generate();
    }

    @Test
    void shouldSendMessageEndpoint() throws Exception {
        Message message = Message.send(roomId, senderId, MessageContent.of("1. e4 e5"));
        when(messageDomainService.sendMessage(any(), any(), any(), any(), any())).thenReturn(message);

        SendMessageRequestDto dto = new SendMessageRequestDto(roomId.getValue(), senderId.getValue(), "1. e4 e5", MessageType.TEXT, null);

        mockMvc.perform(post("/api/v1/chat/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("1. e4 e5"));
    }

    @Test
    void shouldGetRoomMessagesEndpoint() throws Exception {
        Message message = Message.send(roomId, senderId, MessageContent.of("Hello!"));
        when(messageRepository.findByChatRoomId(eq(roomId), eq(0), eq(50))).thenReturn(List.of(message));

        mockMvc.perform(get("/api/v1/chat/messages/room/{roomId}", roomId.getValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].content").value("Hello!"));
    }
}
