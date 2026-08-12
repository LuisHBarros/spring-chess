package com.chess.chat.domain;

import com.chess.chat.domain.exception.InvalidMessageContentException;
import com.chess.chat.domain.model.MessageContent;
import com.chess.chat.domain.model.RoomTitle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageContentTest {

    @Test
    void shouldCreateValidMessageContent() {
        MessageContent content = MessageContent.of("Hello world!");
        assertEquals("Hello world!", content.getValue());
    }

    @Test
    void shouldThrowExceptionWhenContentIsEmptyOrNull() {
        assertThrows(InvalidMessageContentException.class, () -> MessageContent.of(null));
        assertThrows(InvalidMessageContentException.class, () -> MessageContent.of("   "));
    }

    @Test
    void shouldThrowExceptionWhenContentExceedsMaxLength() {
        String longText = "a".repeat(2001);
        assertThrows(InvalidMessageContentException.class, () -> MessageContent.of(longText));
    }

    @Test
    void shouldCreateValidRoomTitle() {
        RoomTitle title = RoomTitle.of("General Chat");
        assertEquals("General Chat", title.getValue());
    }

    @Test
    void shouldThrowExceptionWhenRoomTitleInvalid() {
        assertThrows(IllegalArgumentException.class, () -> RoomTitle.of(null));
        assertThrows(IllegalArgumentException.class, () -> RoomTitle.of("   "));
        assertThrows(IllegalArgumentException.class, () -> RoomTitle.of("a".repeat(101)));
    }
}
