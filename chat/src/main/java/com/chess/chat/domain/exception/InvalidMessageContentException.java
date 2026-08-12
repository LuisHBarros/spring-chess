package com.chess.chat.domain.exception;

public class InvalidMessageContentException extends DomainException {
    public InvalidMessageContentException(String message) {
        super(message);
    }
}
