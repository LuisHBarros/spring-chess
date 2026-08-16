package com.chess.auth.infrastructure.messaging;

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

class AuthMatchEventListenerTest {

    private SqsClient sqsClient;
    private ObjectMapper objectMapper;
    private AuthMatchEventListener matchEventListener;

    @BeforeEach
    void setUp() {
        sqsClient = mock(SqsClient.class);
        objectMapper = new ObjectMapper();
        matchEventListener = new AuthMatchEventListener(sqsClient, objectMapper);
    }

    @Test
    @DisplayName("Should process received SQS match event and delete message from queue")
    void shouldProcessAndDeleteMatchEventMessage() {
        Message message = Message.builder()
                .messageId("msg-fifo-100")
                .receiptHandle("handle-xyz")
                .body("{\"matchId\":\"match-001\",\"winner\":\"user-789\",\"ratingDelta\":15}")
                .messageAttributes(Map.of("traceId", MessageAttributeValue.builder().stringValue("trace-999").build()))
                .build();

        when(sqsClient.deleteMessage(any(DeleteMessageRequest.class))).thenReturn(DeleteMessageResponse.builder().build());

        assertDoesNotThrow(() -> matchEventListener.processMessage(message));

        verify(sqsClient, times(1)).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    @DisplayName("Should process SQS match event when no traceId attribute is present")
    void shouldProcessMessageWithoutTraceId() {
        Message message = Message.builder()
                .messageId("msg-fifo-101")
                .receiptHandle("handle-xyz")
                .body("{\"matchId\":\"match-002\",\"winner\":\"user-abc\",\"ratingDelta\":10}")
                .build();

        when(sqsClient.deleteMessage(any(DeleteMessageRequest.class))).thenReturn(DeleteMessageResponse.builder().build());

        assertDoesNotThrow(() -> matchEventListener.processMessage(message));

        verify(sqsClient, times(1)).deleteMessage(any(DeleteMessageRequest.class));
    }
}
