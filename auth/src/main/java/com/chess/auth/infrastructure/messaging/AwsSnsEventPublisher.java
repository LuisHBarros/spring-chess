package com.chess.auth.infrastructure.messaging;

import com.chess.auth.domain.port.EventPublisherPort;
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
public class AwsSnsEventPublisher implements EventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(AwsSnsEventPublisher.class);

    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;
    private final Tracer tracer;

    @Value("${aws.sns.user-events-topic-arn:arn:aws:sns:us-east-1:000000000000:user-events}")
    private String userEventsTopicArn;

    @Autowired
    public AwsSnsEventPublisher(SnsClient snsClient, ObjectMapper objectMapper, @Autowired(required = false) Tracer tracer) {
        this.snsClient = snsClient;
        this.objectMapper = objectMapper;
        this.tracer = tracer;
    }

    @Override
    public void publishUserEvent(String eventType, String userId, Map<String, Object> eventData) {
        try {
            Map<String, Object> payload = new HashMap<>(eventData);
            payload.put("eventType", eventType);
            payload.put("userId", userId);
            payload.put("timestamp", System.currentTimeMillis());

            String messageBody = objectMapper.writeValueAsString(payload);

            Map<String, MessageAttributeValue> attributes = new HashMap<>();
            attributes.put("eventType", MessageAttributeValue.builder()
                    .dataType("String")
                    .stringValue(eventType)
                    .build());

            // Trace Context Propagation as documented in section 05 of architecture.html
            String traceId = getCurrentTraceId();
            if (traceId != null) {
                attributes.put("traceId", MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(traceId)
                        .build());
            }

            PublishRequest publishRequest = PublishRequest.builder()
                    .topicArn(userEventsTopicArn)
                    .message(messageBody)
                    .messageAttributes(attributes)
                    .build();

            PublishResponse response = snsClient.publish(publishRequest);
            log.info("Published event {} for user {} to SNS topic {}. MessageId: {}",
                    eventType, userId, userEventsTopicArn, response.messageId());

        } catch (JsonProcessingException e) {
            log.error("Error serializing event payload for user event: {}", eventType, e);
            throw new RuntimeException("Failed to serialize SNS event", e);
        } catch (Exception e) {
            log.error("Failed to publish user event {} to SNS topic {}", eventType, userEventsTopicArn, e);
            throw new RuntimeException("Failed to publish SNS event", e);
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
