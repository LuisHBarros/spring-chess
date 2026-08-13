package com.chess.chat.infrastructure.web.controller;

import com.chess.chat.domain.model.ChatRoomId;
import com.chess.chat.domain.model.Message;
import com.chess.chat.domain.model.MessageContent;
import com.chess.chat.domain.model.MessageId;
import com.chess.chat.domain.model.MessageType;
import com.chess.chat.domain.model.UserId;
import com.chess.chat.domain.repository.MessageRepository;
import com.chess.chat.domain.service.MessageDomainService;
import com.chess.chat.infrastructure.web.GlobalExceptionHandler;
import com.chess.chat.infrastructure.web.dto.AddReactionRequestDto;
import com.chess.chat.infrastructure.web.dto.EditMessageRequestDto;
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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MessageControllerEndpointsTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private MessageDomainService messageDomainService;

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private MessageController messageController;

    private UserId sender;
    private ChatRoomId chatRoomId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(messageController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        sender = UserId.generate();
        chatRoomId = ChatRoomId.generate();
    }

    @Test
    void shouldGetUnreadCountEndpoint() throws Exception {
        when(messageRepository.countUnreadMessages(eq(chatRoomId), eq(sender))).thenReturn(5L);

        mockMvc.perform(get("/api/v1/chat/messages/room/{roomId}/unread/count", chatRoomId.getValue())
                        .param("userId", sender.getValue().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(5));

        verify(messageRepository).countUnreadMessages(chatRoomId, sender);
    }

    @Test
    void shouldEditMessageEndpoint() throws Exception {
        Message message = Message.create(chatRoomId, sender, MessageContent.of("Original"), MessageType.TEXT, null);
        message.editContent(MessageContent.of("Updated Content"));

        when(messageDomainService.editMessage(any(), any(), any())).thenReturn(message);

        EditMessageRequestDto dto = new EditMessageRequestDto(sender.getValue(), "Updated Content");

        mockMvc.perform(put("/api/v1/chat/messages/{id}", message.getId().getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("Updated Content"));
    }

    @Test
    void shouldDeleteMessageEndpoint() throws Exception {
        Message message = Message.create(chatRoomId, sender, MessageContent.of("To Delete"), MessageType.TEXT, null);
        message.softDelete();

        when(messageDomainService.deleteMessage(any(), any())).thenReturn(message);

        mockMvc.perform(delete("/api/v1/chat/messages/{id}", message.getId().getValue())
                        .param("actorId", sender.getValue().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.deleted").value(true));
    }

    @Test
    void shouldAddReactionEndpoint() throws Exception {
        Message message = Message.create(chatRoomId, sender, MessageContent.of("React to this"), MessageType.TEXT, null);
        message.addReaction(sender, "👍");

        when(messageDomainService.addReaction(any(), any(), eq("👍"))).thenReturn(message);

        AddReactionRequestDto dto = new AddReactionRequestDto(sender.getValue(), "👍");

        mockMvc.perform(post("/api/v1/chat/messages/{id}/reactions", message.getId().getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reactions").isArray());
    }

    @Test
    void shouldRemoveReactionEndpoint() throws Exception {
        Message message = Message.create(chatRoomId, sender, MessageContent.of("Remove reaction"), MessageType.TEXT, null);
        when(messageDomainService.removeReaction(any(), any(), eq("👍"))).thenReturn(message);

        mockMvc.perform(delete("/api/v1/chat/messages/{id}/reactions", message.getId().getValue())
                        .param("actorId", sender.getValue().toString())
                        .param("emoji", "👍"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldMarkAsReadEndpoint() throws Exception {
        doNothing().when(messageDomainService).markMessagesAsRead(eq(chatRoomId), eq(sender));

        mockMvc.perform(put("/api/v1/chat/messages/room/{roomId}/read", chatRoomId.getValue())
                        .param("userId", sender.getValue().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Messages marked as read"));

        verify(messageDomainService).markMessagesAsRead(chatRoomId, sender);
    }
}
