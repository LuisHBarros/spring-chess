package com.chess.social.infrastructure.web.controller;

import com.chess.social.domain.model.Friendship;
import com.chess.social.domain.model.FriendshipId;
import com.chess.social.domain.model.UserId;
import com.chess.social.domain.service.FriendshipDomainService;
import com.chess.social.infrastructure.web.dto.FriendRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FriendshipController.class)
class FriendshipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FriendshipDomainService friendshipDomainService;

    @Test
    @DisplayName("POST /api/v1/friendships/request should return 201 Created")
    void shouldSendFriendRequest() throws Exception {
        UUID requesterId = UUID.randomUUID();
        UUID addresseeId = UUID.randomUUID();
        FriendRequestDto dto = new FriendRequestDto(requesterId, addresseeId);

        Friendship friendship = Friendship.request(UserId.from(requesterId), UserId.from(addresseeId));
        when(friendshipDomainService.sendFriendRequest(any(), any())).thenReturn(friendship);

        mockMvc.perform(post("/api/v1/friendships/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.requesterId").value(requesterId.toString()))
                .andExpect(jsonPath("$.data.addresseeId").value(addresseeId.toString()));
    }

    @Test
    @DisplayName("PUT /api/v1/friendships/{id}/accept should return 200 OK")
    void shouldAcceptFriendRequest() throws Exception {
        UUID friendshipId = UUID.randomUUID();
        UUID addresseeId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        Friendship friendship = Friendship.request(UserId.from(requesterId), UserId.from(addresseeId));
        friendship.accept(UserId.from(addresseeId));

        when(friendshipDomainService.acceptFriendRequest(any(), any())).thenReturn(friendship);

        mockMvc.perform(put("/api/v1/friendships/{id}/accept", friendshipId)
                        .param("addresseeId", addresseeId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"));
    }
}
