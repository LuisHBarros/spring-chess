package com.chess.game.domain.exception;

public class PlayerNotFoundException extends DomainException {
    public PlayerNotFoundException(String message) {
        super(message);
    }
}
