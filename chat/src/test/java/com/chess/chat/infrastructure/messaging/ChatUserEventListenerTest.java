package com.chess.chat.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChatUserEventListenerTest {

    @Mock
    private SqsClient sqsClient;

    private ChatUserEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new ChatUserEventListener(sqsClient, new ObjectMapper());
    }

    @Test
    void shouldProcessAndDeleteUserEventMessage() {
        Message message = Message.builder()
                .messageId("msg-123")
                .receiptHandle("handle-123")
                .body("{\"eventType\":\"USER_CREATED\",\"userId\":\"123e4567-e89b-12d3-a456-426614174000\"}")
                .build();

        listener.processMessage(message);

        verify(sqsClient).deleteMessage(any(DeleteMessageRequest.class));
    }
}
