package com.chess.auth.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.util.List;
import java.util.Map;

@Component
public class AuthMatchEventListener {

    private static final Logger log = LoggerFactory.getLogger(AuthMatchEventListener.class);

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.auth-match-events-queue-url:http://localhost:4566/000000000000/auth-match-events-queue.fifo}")
    private String authMatchEventsQueueUrl;

    @Autowired
    public AuthMatchEventListener(SqsClient sqsClient, ObjectMapper objectMapper) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000)
    public void pollMatchEvents() {
        try {
            ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                    .queueUrl(authMatchEventsQueueUrl)
                    .maxNumberOfMessages(10)
                    .waitTimeSeconds(2)
                    .messageAttributeNames("All")
                    .build();

            ReceiveMessageResponse response = sqsClient.receiveMessage(receiveRequest);
            List<Message> messages = response.messages();

            for (Message message : messages) {
                processMessage(message);
            }
        } catch (Exception e) {
            log.warn("Error polling SQS queue {}: {}", authMatchEventsQueueUrl, e.getMessage());
        }
    }

    public void processMessage(Message message) {
        String traceId = extractTraceId(message);
        if (traceId != null) {
            MDC.put("traceId", traceId);
        }

        try {
            log.info("Received match event message ID: {}, traceId: {}", message.messageId(), traceId);
            String body = message.body();

            @SuppressWarnings("unchecked")
            Map<String, Object> eventMap = objectMapper.readValue(body, Map.class);
            log.info("Processing match event for Auth domain: {}", eventMap);

            // Delete processed message from SQS FIFO queue
            DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                    .queueUrl(authMatchEventsQueueUrl)
                    .receiptHandle(message.receiptHandle())
                    .build();

            sqsClient.deleteMessage(deleteRequest);
            log.info("Successfully processed and deleted message ID: {}", message.messageId());

        } catch (Exception e) {
            log.error("Failed to process SQS match event message ID: {}", message.messageId(), e);
        } finally {
            MDC.remove("traceId");
        }
    }

    private String extractTraceId(Message message) {
        if (message.messageAttributes() != null && message.messageAttributes().containsKey("traceId")) {
            return message.messageAttributes().get("traceId").stringValue();
        }
        return null;
    }
}
