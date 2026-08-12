package com.chess.chat.infrastructure.messaging;

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
public class ChatUserEventListener {

    private static final Logger log = LoggerFactory.getLogger(ChatUserEventListener.class);

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.chat-user-events-queue-url:http://localhost:4566/000000000000/chat-user-events-queue}")
    private String chatUserEventsQueueUrl;

    @Autowired
    public ChatUserEventListener(SqsClient sqsClient, ObjectMapper objectMapper) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000)
    public void pollUserEvents() {
        try {
            ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                    .queueUrl(chatUserEventsQueueUrl)
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
            log.warn("Error polling SQS queue {}: {}", chatUserEventsQueueUrl, e.getMessage());
        }
    }

    public void processMessage(Message message) {
        String traceId = extractTraceId(message);
        if (traceId != null) {
            MDC.put("traceId", traceId);
        }

        try {
            log.info("Chat service received user event message ID: {}, traceId: {}", message.messageId(), traceId);
            String body = message.body();

            @SuppressWarnings("unchecked")
            Map<String, Object> eventMap = objectMapper.readValue(body, Map.class);
            log.info("Processing user event for Chat domain: {}", eventMap);

            DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                    .queueUrl(chatUserEventsQueueUrl)
                    .receiptHandle(message.receiptHandle())
            .build();

            sqsClient.deleteMessage(deleteRequest);
            log.info("Successfully processed and deleted SQS message ID: {}", message.messageId());

        } catch (Exception e) {
            log.error("Failed to process SQS user event message ID: {}", message.messageId(), e);
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
