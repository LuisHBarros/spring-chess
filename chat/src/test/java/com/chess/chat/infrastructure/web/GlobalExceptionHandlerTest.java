package com.chess.chat.infrastructure.web;

import com.chess.chat.domain.exception.ChatParticipantNotFoundException;
import com.chess.chat.domain.exception.ChatRoomArchivedException;
import com.chess.chat.domain.exception.ChatRoomNotFoundException;
import com.chess.chat.domain.exception.DirectChatParticipantLimitException;
import com.chess.chat.domain.exception.DomainException;
import com.chess.chat.domain.exception.DuplicateDirectChatException;
import com.chess.chat.domain.exception.DuplicateParticipantException;
import com.chess.chat.domain.exception.InvalidMessageContentException;
import com.chess.chat.domain.exception.MessageNotFoundException;
import com.chess.chat.domain.exception.UnauthorizedChatOperationException;
import com.chess.chat.infrastructure.web.dto.ApiResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void shouldHandleNotFoundExceptionsWith404() {
        ChatRoomNotFoundException ex = new ChatRoomNotFoundException("Room not found");
        ResponseEntity<ApiResponseDto<Void>> response = exceptionHandler.handleNotFoundExceptions(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Room not found", response.getBody().getMessage());

        MessageNotFoundException msgEx = new MessageNotFoundException("Message not found");
        assertEquals(HttpStatus.NOT_FOUND, exceptionHandler.handleNotFoundExceptions(msgEx).getStatusCode());

        ChatParticipantNotFoundException partEx = new ChatParticipantNotFoundException("Participant not found");
        assertEquals(HttpStatus.NOT_FOUND, exceptionHandler.handleNotFoundExceptions(partEx).getStatusCode());
    }

    @Test
    void shouldHandleUnauthorizedExceptionWith403() {
        UnauthorizedChatOperationException ex = new UnauthorizedChatOperationException("User not authorized");
        ResponseEntity<ApiResponseDto<Void>> response = exceptionHandler.handleUnauthorizedException(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("User not authorized", response.getBody().getMessage());
    }

    @Test
    void shouldHandleConflictExceptionsWith409() {
        DuplicateDirectChatException ex = new DuplicateDirectChatException("Direct chat already exists");
        ResponseEntity<ApiResponseDto<Void>> response = exceptionHandler.handleConflictExceptions(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Direct chat already exists", response.getBody().getMessage());

        DuplicateParticipantException partEx = new DuplicateParticipantException("Participant already added");
        assertEquals(HttpStatus.CONFLICT, exceptionHandler.handleConflictExceptions(partEx).getStatusCode());
    }

    @Test
    void shouldHandleBadRequestExceptionsWith400() {
        InvalidMessageContentException ex = new InvalidMessageContentException("Content cannot be empty");
        ResponseEntity<ApiResponseDto<Void>> response = exceptionHandler.handleBadRequestExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Content cannot be empty", response.getBody().getMessage());

        DirectChatParticipantLimitException limitEx = new DirectChatParticipantLimitException("Limit reached");
        assertEquals(HttpStatus.BAD_REQUEST, exceptionHandler.handleBadRequestExceptions(limitEx).getStatusCode());

        ChatRoomArchivedException archiveEx = new ChatRoomArchivedException("Room is archived");
        assertEquals(HttpStatus.BAD_REQUEST, exceptionHandler.handleBadRequestExceptions(archiveEx).getStatusCode());
    }

    @Test
    void shouldHandleGeneralDomainExceptionWith400() {
        DomainException ex = new DomainException("General domain error") {};
        ResponseEntity<ApiResponseDto<Void>> response = exceptionHandler.handleGeneralDomainException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("General domain error", response.getBody().getMessage());
    }

    @Test
    void shouldHandleValidationExceptionWith400() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("dto", "title", "Title is required");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ApiResponseDto<Void>> response = exceptionHandler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("title: Title is required", response.getBody().getMessage());
    }

    @Test
    void shouldHandleIllegalArgumentExceptionWith400() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument format");
        ResponseEntity<ApiResponseDto<Void>> response = exceptionHandler.handleIllegalArgumentException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid argument format", response.getBody().getMessage());
    }

    @Test
    void shouldHandleGenericExceptionWith500() {
        Exception ex = new RuntimeException("Database connection timeout");
        ResponseEntity<ApiResponseDto<Void>> response = exceptionHandler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("An unexpected error occurred: Database connection timeout"));
    }
}
