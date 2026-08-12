package com.chess.chat.domain.exception;

public class MessageNotFoundException extends DomainException {
    public MessageNotFoundException(String message) {
        super(message);
    }
}
