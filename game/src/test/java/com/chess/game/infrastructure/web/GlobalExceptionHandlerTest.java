package com.chess.game.infrastructure.web;

import com.chess.game.domain.exception.GameNotFoundException;
import com.chess.game.infrastructure.web.dto.ApiResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Should handle GameNotFoundException returning error response")
    void shouldHandleGameNotFoundException() {
        GameNotFoundException ex = new GameNotFoundException("Game not found");
        ApiResponseDto<Void> response = handler.handleGameNotFoundException(ex);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Game not found");
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException returning error response")
    void shouldHandleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");
        ApiResponseDto<Void> response = handler.handleIllegalArgumentException(ex);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Invalid argument");
    }

    @Test
    @DisplayName("Should handle general exception returning internal server error message")
    void shouldHandleGeneralException() {
        Exception ex = new Exception("Database failure");
        ApiResponseDto<Void> response = handler.handleGeneralException(ex);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("An unexpected error occurred");
    }
}
