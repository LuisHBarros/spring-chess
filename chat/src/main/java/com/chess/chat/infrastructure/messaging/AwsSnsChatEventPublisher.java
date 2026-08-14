package com.chess.chat.infrastructure.messaging;

import com.chess.chat.domain.port.ChatEventPublisherPort;
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
public class AwsSnsChatEventPublisher implements ChatEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(AwsSnsChatEventPublisher.class);

    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private Tracer tracer;

    @Value("${aws.sns.chat-events-topic-arn:arn:aws:sns:us-east-1:000000000000:chat-events}")
    private String chatEventsTopicArn;

    @Autowired
    public AwsSnsChatEventPublisher(SnsClient snsClient, ObjectMapper objectMapper) {
        this.snsClient = snsClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publishChatEvent(String eventType, String entityId, Map<String, Object> eventData) {
        try {
            Map<String, Object> payload = new HashMap<>(eventData);
            payload.put("eventType", eventType);
            payload.put("entityId", entityId);
            payload.put("timestamp", System.currentTimeMillis());

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

            PublishRequest publishRequest = PublishRequest.builder()
                    .topicArn(chatEventsTopicArn)
                    .message(messageBody)
                    .messageAttributes(attributes)
                    .build();

            PublishResponse response = snsClient.publish(publishRequest);
            log.info("Published chat event {} for entity {} to SNS topic {}. MessageId: {}",
                    eventType, entityId, chatEventsTopicArn, response.messageId());

        } catch (JsonProcessingException e) {
            log.error("Error serializing event payload for chat event: {}", eventType, e);
            throw new RuntimeException("Failed to serialize SNS chat event", e);
        } catch (Exception e) {
            log.error("Failed to publish chat event {} to SNS topic {}", eventType, chatEventsTopicArn, e);
            throw new RuntimeException("Failed to publish SNS chat event", e);
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
