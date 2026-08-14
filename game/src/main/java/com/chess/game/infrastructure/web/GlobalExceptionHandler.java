package com.chess.game.infrastructure.web;

import com.chess.game.domain.exception.DomainException;
import com.chess.game.domain.exception.GameAlreadyFinishedException;
import com.chess.game.domain.exception.GameNotFoundException;
import com.chess.game.domain.exception.InvalidMoveException;
import com.chess.game.domain.exception.NotPlayerTurnException;
import com.chess.game.infrastructure.web.dto.ApiResponseDto;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GameNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponseDto<Void> handleGameNotFoundException(GameNotFoundException ex) {
        return ApiResponseDto.error(ex.getMessage());
    }

    @ExceptionHandler({NotPlayerTurnException.class, InvalidMoveException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponseDto<Void> handleBadRequestExceptions(DomainException ex) {
        return ApiResponseDto.error(ex.getMessage());
    }

    @ExceptionHandler(GameAlreadyFinishedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponseDto<Void> handleGameAlreadyFinishedException(GameAlreadyFinishedException ex) {
        return ApiResponseDto.error(ex.getMessage());
    }

    @ExceptionHandler(DomainException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponseDto<Void> handleDomainException(DomainException ex) {
        return ApiResponseDto.error(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponseDto<Void> handleValidationException(MethodArgumentNotValidException ex) {
        return ApiResponseDto.error("Validation error: " + ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponseDto<Void> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ApiResponseDto.error(ex.getMessage());
    }

    @ExceptionHandler({OptimisticLockingFailureException.class, ObjectOptimisticLockingFailureException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponseDto<Void> handleOptimisticLockException(Exception ex) {
        return ApiResponseDto.error("Concurrent move conflict: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponseDto<Void> handleGeneralException(Exception ex) {
        return ApiResponseDto.error("An unexpected error occurred: " + ex.getMessage());
    }
}
