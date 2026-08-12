package com.chess.social.infrastructure.web.controller;

import com.chess.social.domain.model.Guild;
import com.chess.social.domain.model.GuildId;
import com.chess.social.domain.model.GuildName;
import com.chess.social.domain.model.UserId;
import com.chess.social.domain.repository.GuildRepository;
import com.chess.social.domain.service.GuildDomainService;
import com.chess.social.infrastructure.web.dto.CreateGuildRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GuildController.class)
class GuildControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GuildDomainService guildDomainService;

    @MockBean
    private GuildRepository guildRepository;

    @Test
    @DisplayName("POST /api/v1/guilds should create a new Guild and return 201 Created")
    void shouldCreateGuild() throws Exception {
        UUID creatorId = UUID.randomUUID();
        CreateGuildRequestDto dto = new CreateGuildRequestDto("Grandmasters", "Guild description", creatorId, null);

        Guild guild = Guild.create(GuildName.of("Grandmasters"), "Guild description", UserId.from(creatorId));
        when(guildDomainService.createGuild(any(), any(), any(), any())).thenReturn(guild);

        mockMvc.perform(post("/api/v1/guilds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Grandmasters"))
                .andExpect(jsonPath("$.data.ownerId").value(creatorId.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/guilds/{id} should return Guild details")
    void shouldGetGuildById() throws Exception {
        UUID guildId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        Guild guild = Guild.create(GuildName.of("Strategy Masters"), "Desc", UserId.from(creatorId));

        when(guildRepository.findById(any())).thenReturn(Optional.of(guild));

        mockMvc.perform(get("/api/v1/guilds/{id}", guildId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Strategy Masters"));
    }
}
