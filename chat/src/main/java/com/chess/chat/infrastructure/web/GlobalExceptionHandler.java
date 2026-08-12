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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            ChatRoomNotFoundException.class,
            MessageNotFoundException.class,
            ChatParticipantNotFoundException.class
    })
    public ResponseEntity<ApiResponseDto<Void>> handleNotFoundExceptions(DomainException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDto.error(ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedChatOperationException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleUnauthorizedException(UnauthorizedChatOperationException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponseDto.error(ex.getMessage()));
    }

    @ExceptionHandler({
            DuplicateDirectChatException.class,
            DuplicateParticipantException.class
    })
    public ResponseEntity<ApiResponseDto<Void>> handleConflictExceptions(DomainException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponseDto.error(ex.getMessage()));
    }

    @ExceptionHandler({
            InvalidMessageContentException.class,
            DirectChatParticipantLimitException.class,
            ChatRoomArchivedException.class
    })
    public ResponseEntity<ApiResponseDto<Void>> handleBadRequestExceptions(DomainException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error(ex.getMessage()));
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleGeneralDomainException(DomainException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMsg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse("Invalid request body");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error(errorMsg));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("An unexpected error occurred: " + ex.getMessage()));
    }
}
