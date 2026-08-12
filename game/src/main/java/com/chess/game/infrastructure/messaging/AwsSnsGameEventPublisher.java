package com.chess.game.infrastructure.messaging;

import com.chess.game.domain.port.GameEventPublisherPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class AwsSnsGameEventPublisher implements GameEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(AwsSnsGameEventPublisher.class);
    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sns.game-events-topic-arn}")
    private String topicArn;

    public AwsSnsGameEventPublisher(SnsClient snsClient, ObjectMapper objectMapper) {
        this.snsClient = snsClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publishGameEvent(String eventType, String entityId, Map<String, Object> eventData) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("eventId", UUID.randomUUID().toString());
            payload.put("eventType", eventType);
            payload.put("entityId", entityId);
            payload.put("timestamp", System.currentTimeMillis());
            payload.put("data", eventData);

            String messageBody = objectMapper.writeValueAsString(payload);

            PublishRequest request = PublishRequest.builder()
                    .topicArn(topicArn)
                    .message(messageBody)
                    .build();

            snsClient.publish(request);
            log.info("Published game event: {} for entity: {}", eventType, entityId);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize game event: {}", eventType, e);
        } catch (Exception e) {
            log.error("Failed to publish game event: {}", eventType, e);
        }
    }
}
