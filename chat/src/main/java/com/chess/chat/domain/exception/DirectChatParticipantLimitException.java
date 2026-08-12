package com.chess.chat.domain.exception;

public class DirectChatParticipantLimitException extends DomainException {
    public DirectChatParticipantLimitException(String message) {
        super(message);
    }
}
