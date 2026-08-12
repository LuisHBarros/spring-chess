package com.chess.game.domain.exception;

public class GameNotFoundException extends DomainException {
    public GameNotFoundException(String message) {
        super(message);
    }
}
