package com.chess.social.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SocialMatchEventListenerTest {

    private SqsClient sqsClient;
    private ObjectMapper objectMapper;
    private SocialMatchEventListener listener;

    @BeforeEach
    void setUp() {
        sqsClient = mock(SqsClient.class);
        objectMapper = new ObjectMapper();
        listener = new SocialMatchEventListener(sqsClient, objectMapper);
    }

    @Test
    @DisplayName("Should process received match event in Social service and delete SQS message")
    void shouldProcessAndDeleteMatchEventMessage() {
        Message message = Message.builder()
                .messageId("msg-match-event-1")
                .receiptHandle("handle-xyz")
                .body("{\"matchId\":\"match-001\",\"winner\":\"user-123\",\"loser\":\"user-456\"}")
                .messageAttributes(Map.of("traceId", MessageAttributeValue.builder().stringValue("trace-222").build()))
                .build();

        when(sqsClient.deleteMessage(any(DeleteMessageRequest.class))).thenReturn(DeleteMessageResponse.builder().build());

        assertDoesNotThrow(() -> listener.processMessage(message));

        verify(sqsClient, times(1)).deleteMessage(any(DeleteMessageRequest.class));
    }
}
