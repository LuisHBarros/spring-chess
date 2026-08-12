package com.chess.chat.domain.port;

import java.util.Map;

public interface ChatEventPublisherPort {
    void publishChatEvent(String eventType, String entityId, Map<String, Object> eventData);
}
