package com.chess.chat.domain.exception;

public class ChatParticipantNotFoundException extends DomainException {
    public ChatParticipantNotFoundException(String message) {
        super(message);
    }
}
