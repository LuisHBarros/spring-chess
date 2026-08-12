package com.chess.social.domain.exception;

public class FriendshipAlreadyExistsException extends DomainException {
    public FriendshipAlreadyExistsException(String message) {
        super(message);
    }
}
