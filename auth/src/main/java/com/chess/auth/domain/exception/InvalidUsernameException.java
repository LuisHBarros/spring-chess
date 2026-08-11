package com.chess.auth.domain.exception;

public class InvalidUsernameException extends DomainException {
    public InvalidUsernameException(String message) {
        super(message);
    }
}
