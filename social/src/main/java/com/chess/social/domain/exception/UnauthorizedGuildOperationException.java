package com.chess.social.domain.exception;

public class UnauthorizedGuildOperationException extends DomainException {
    public UnauthorizedGuildOperationException(String message) {
        super(message);
    }
}
