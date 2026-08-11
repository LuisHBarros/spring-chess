package com.chess.auth.domain.exception;

public class RateLimitExceededException extends DomainException {
    public RateLimitExceededException(String message) {
        super(message);
    }
}
