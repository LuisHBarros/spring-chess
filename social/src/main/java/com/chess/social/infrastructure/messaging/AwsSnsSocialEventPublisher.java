package com.chess.social.infrastructure.messaging;

import com.chess.social.domain.port.SocialEventPublisherPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class AwsSnsSocialEventPublisher implements SocialEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(AwsSnsSocialEventPublisher.class);

    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sns.social-events-topic-arn:arn:aws:sns:us-east-1:000000000000:social-events}")
    private String socialEventsTopicArn;

    @Autowired
    public AwsSnsSocialEventPublisher(SnsClient snsClient, ObjectMapper objectMapper) {
        this.snsClient = snsClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publishSocialEvent(String eventType, String entityId, Map<String, Object> eventData) {
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
                    .topicArn(socialEventsTopicArn)
                    .message(messageBody)
                    .messageAttributes(attributes)
                    .build();

            PublishResponse response = snsClient.publish(publishRequest);
            log.info("Published social event {} for entity {} to SNS topic {}. MessageId: {}",
                    eventType, entityId, socialEventsTopicArn, response.messageId());

        } catch (JsonProcessingException e) {
            log.error("Error serializing event payload for social event: {}", eventType, e);
            throw new RuntimeException("Failed to serialize SNS event", e);
        } catch (Exception e) {
            log.error("Failed to publish social event {} to SNS topic {}", eventType, socialEventsTopicArn, e);
            throw new RuntimeException("Failed to publish SNS event", e);
        }
    }

    private String getCurrentTraceId() {
        String mdcTraceId = MDC.get("traceId");
        if (mdcTraceId != null && !mdcTraceId.isBlank()) {
            return mdcTraceId;
        }
        return UUID.randomUUID().toString();
    }
}
