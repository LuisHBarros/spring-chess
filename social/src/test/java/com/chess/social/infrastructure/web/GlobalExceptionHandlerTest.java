package com.chess.social.infrastructure.web;

import com.chess.social.domain.exception.GuildNotFoundException;

import com.chess.social.infrastructure.web.dto.ApiResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Should handle GuildNotFoundException returning error response")
    void shouldHandleGuildNotFoundException() {
        GuildNotFoundException ex = new GuildNotFoundException("Guild not found");
        ApiResponseDto<Void> response = handler.handleGuildNotFoundException(ex);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Guild not found");
    }

    @Test
    @DisplayName("Should handle general Exception returning internal server error")
    void shouldHandleGeneralException() {
        Exception ex = new Exception("Database error");
        ApiResponseDto<Void> response = handler.handleGeneralException(ex);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("An unexpected error occurred");
    }
}
