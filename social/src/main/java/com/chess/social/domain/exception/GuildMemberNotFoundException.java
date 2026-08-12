package com.chess.social.domain.exception;

public class GuildMemberNotFoundException extends DomainException {
    public GuildMemberNotFoundException(String message) {
        super(message);
    }
}
