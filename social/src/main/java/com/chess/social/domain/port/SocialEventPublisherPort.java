package com.chess.social.domain.port;

import java.util.Map;

public interface SocialEventPublisherPort {
    void publishSocialEvent(String eventType, String entityId, Map<String, Object> eventData);
}
