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

class SocialUserEventListenerTest {

    private SqsClient sqsClient;
    private ObjectMapper objectMapper;
    private SocialUserEventListener listener;

    @BeforeEach
    void setUp() {
        sqsClient = mock(SqsClient.class);
        objectMapper = new ObjectMapper();
        listener = new SocialUserEventListener(sqsClient, objectMapper);
    }

    @Test
    @DisplayName("Should process received user event in Social service and delete SQS message")
    void shouldProcessAndDeleteUserEventMessage() {
        Message message = Message.builder()
                .messageId("msg-user-event-1")
                .receiptHandle("handle-abc")
                .body("{\"eventType\":\"USER_REGISTERED\",\"userId\":\"user-123\"}")
                .messageAttributes(Map.of("traceId", MessageAttributeValue.builder().stringValue("trace-111").build()))
                .build();

        when(sqsClient.deleteMessage(any(DeleteMessageRequest.class))).thenReturn(DeleteMessageResponse.builder().build());

        assertDoesNotThrow(() -> listener.processMessage(message));

        verify(sqsClient, times(1)).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    @DisplayName("Should process SQS user event when no traceId attribute is present")
    void shouldProcessMessageWithoutTraceId() {
        Message message = Message.builder()
                .messageId("msg-user-event-2")
                .receiptHandle("handle-def")
                .body("{\"eventType\":\"USER_LOGGED_IN\",\"userId\":\"user-999\"}")
                .build();

        when(sqsClient.deleteMessage(any(DeleteMessageRequest.class))).thenReturn(DeleteMessageResponse.builder().build());

        assertDoesNotThrow(() -> listener.processMessage(message));

        verify(sqsClient, times(1)).deleteMessage(any(DeleteMessageRequest.class));
    }
}
