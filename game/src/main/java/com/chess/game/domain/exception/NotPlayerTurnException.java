package com.chess.game.domain.exception;

public class NotPlayerTurnException extends DomainException {
    public NotPlayerTurnException(String message) {
        super(message);
    }
}
