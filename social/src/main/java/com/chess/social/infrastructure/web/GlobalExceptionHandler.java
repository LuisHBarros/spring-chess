package com.chess.social.infrastructure.web;

import com.chess.social.domain.exception.CategoryNotFoundException;
import com.chess.social.domain.exception.DomainException;
import com.chess.social.domain.exception.DuplicateCategoryException;
import com.chess.social.domain.exception.DuplicateRankException;
import com.chess.social.domain.exception.FriendshipAlreadyExistsException;
import com.chess.social.domain.exception.FriendshipNotFoundException;
import com.chess.social.domain.exception.GuildMemberAlreadyExistsException;
import com.chess.social.domain.exception.GuildMemberNotFoundException;
import com.chess.social.domain.exception.GuildNotFoundException;
import com.chess.social.domain.exception.RankNotFoundException;
import com.chess.social.domain.exception.UnauthorizedGuildOperationException;
import com.chess.social.infrastructure.web.dto.ApiResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            GuildNotFoundException.class,
            FriendshipNotFoundException.class,
            CategoryNotFoundException.class,
            RankNotFoundException.class,
            GuildMemberNotFoundException.class
    })
    public ResponseEntity<ApiResponseDto<Void>> handleNotFoundExceptions(DomainException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDto.error(ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedGuildOperationException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleUnauthorizedException(UnauthorizedGuildOperationException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponseDto.error(ex.getMessage()));
    }

    @ExceptionHandler({
            FriendshipAlreadyExistsException.class,
            GuildMemberAlreadyExistsException.class,
            DuplicateCategoryException.class,
            DuplicateRankException.class
    })
    public ResponseEntity<ApiResponseDto<Void>> handleConflictExceptions(DomainException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
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
