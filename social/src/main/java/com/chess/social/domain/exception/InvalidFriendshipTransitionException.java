package com.chess.social.domain.exception;

public class InvalidFriendshipTransitionException extends DomainException {
    public InvalidFriendshipTransitionException(String message) {
        super(message);
    }
}
