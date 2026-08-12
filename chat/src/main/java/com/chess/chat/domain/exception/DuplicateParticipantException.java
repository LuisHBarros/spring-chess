package com.chess.chat.domain.exception;

public class DuplicateParticipantException extends DomainException {
    public DuplicateParticipantException(String message) {
        super(message);
    }
}
