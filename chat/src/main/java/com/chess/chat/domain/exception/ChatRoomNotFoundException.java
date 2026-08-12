package com.chess.chat.domain.exception;

public class ChatRoomNotFoundException extends DomainException {
    public ChatRoomNotFoundException(String message) {
        super(message);
    }
}
