package com.chess.game.domain.exception;

public class GameAlreadyFinishedException extends DomainException {
    public GameAlreadyFinishedException(String message) {
        super(message);
    }
}
