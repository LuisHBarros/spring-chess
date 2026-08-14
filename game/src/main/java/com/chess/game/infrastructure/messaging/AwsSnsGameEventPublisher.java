package com.chess.game.infrastructure.messaging;

import com.chess.game.domain.port.GameEventPublisherPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class AwsSnsGameEventPublisher implements GameEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(AwsSnsGameEventPublisher.class);
    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private Tracer tracer;

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

            Map<String, MessageAttributeValue> attributes = new HashMap<>();
            attributes.put("eventType", MessageAttributeValue.builder()
                    .dataType("String")
                    .stringValue(eventType)
                    .build());

            String traceId = getCurrentTraceId();
            if (traceId != null) {
                attributes.put("traceId", MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(traceId)
                        .build());
            }

            PublishRequest request = PublishRequest.builder()
                    .topicArn(topicArn)
                    .message(messageBody)
                    .messageAttributes(attributes)
                    .build();

            PublishResponse response = snsClient.publish(request);
            log.info("Published game event: {} for entity: {}. MessageId: {}", eventType, entityId, response.messageId());

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize game event: {}", eventType, e);
        } catch (Exception e) {
            log.error("Failed to publish game event: {}", eventType, e);
        }
    }

    private String getCurrentTraceId() {
        if (tracer != null && tracer.currentSpan() != null) {
            return tracer.currentSpan().context().traceId();
        }
        String mdcTraceId = MDC.get("traceId");
        if (mdcTraceId != null && !mdcTraceId.isBlank()) {
            return mdcTraceId;
        }
        return UUID.randomUUID().toString();
    }
}
