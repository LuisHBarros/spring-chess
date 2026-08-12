package com.chess.social.domain.exception;

public class GuildMemberAlreadyExistsException extends DomainException {
    public GuildMemberAlreadyExistsException(String message) {
        super(message);
    }
}
