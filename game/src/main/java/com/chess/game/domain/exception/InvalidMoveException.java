package com.chess.game.domain.exception;

public class InvalidMoveException extends DomainException {
    public InvalidMoveException(String message) {
        super(message);
    }
}
