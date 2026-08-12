package com.chess.game.domain.port;

import java.util.Map;

public interface GameEventPublisherPort {
    void publishGameEvent(String eventType, String entityId, Map<String, Object> eventData);
}
