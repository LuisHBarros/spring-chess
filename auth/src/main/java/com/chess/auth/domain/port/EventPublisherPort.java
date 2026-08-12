package com.chess.auth.domain.port;

import java.util.Map;

public interface EventPublisherPort {
    void publishUserEvent(String eventType, String userId, Map<String, Object> eventData);
}
