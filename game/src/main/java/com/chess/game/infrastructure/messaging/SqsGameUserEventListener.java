package com.chess.game.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;

@Component
public class SqsGameUserEventListener {

    private static final Logger log = LoggerFactory.getLogger(SqsGameUserEventListener.class);
    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.game-user-events-queue-url}")
    private String queueUrl;

    public SqsGameUserEventListener(SqsClient sqsClient, ObjectMapper objectMapper) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000)
    public void pollMessages() {
        try {
            ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(10)
                    .waitTimeSeconds(5)
                    .build();

            List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).messages();

            for (Message message : messages) {
                processMessage(message);
                deleteMessage(message);
            }
        } catch (Exception e) {
            log.error("Error polling messages from SQS", e);
        }
    }

    private void processMessage(Message message) {
        try {
            JsonNode rootNode = objectMapper.readTree(message.body());
            String messageStr = rootNode.has("Message") ? rootNode.get("Message").asText() : message.body();
            JsonNode payloadNode = objectMapper.readTree(messageStr);

            String eventType = payloadNode.has("eventType") ? payloadNode.get("eventType").asText() : "UNKNOWN";
            
            log.info("Game service received user event: {}", eventType);
            // Process the user event appropriately (e.g. USER_CREATED, USER_UPDATED)
        } catch (Exception e) {
            log.error("Error processing message body: {}", message.body(), e);
        }
    }

    private void deleteMessage(Message message) {
        try {
            DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(message.receiptHandle())
                    .build();
            sqsClient.deleteMessage(deleteMessageRequest);
        } catch (Exception e) {
            log.error("Error deleting message from SQS", e);
        }
    }
}
