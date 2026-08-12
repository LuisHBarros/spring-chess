package com.chess.chat.domain.exception;

public class UnauthorizedChatOperationException extends DomainException {
    public UnauthorizedChatOperationException(String message) {
        super(message);
    }
}
