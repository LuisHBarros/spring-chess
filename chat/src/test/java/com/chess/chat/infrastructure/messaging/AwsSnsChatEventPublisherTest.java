package com.chess.chat.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AwsSnsChatEventPublisherTest {

    @Mock
    private SnsClient snsClient;

    @Mock
    private ObjectMapper objectMapper;

    private AwsSnsChatEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new AwsSnsChatEventPublisher(snsClient, objectMapper);
    }

    @Test
    void shouldPublishChatEventSuccessfullyWithMdcTraceId() throws Exception {
        MDC.put("traceId", "test-trace-123");
        try {
            when(objectMapper.writeValueAsString(any())).thenReturn("{\"eventType\":\"CHAT_ROOM_CREATED\"}");
            when(snsClient.publish(any(PublishRequest.class)))
                    .thenReturn(PublishResponse.builder().messageId("sns-msg-1").build());

            publisher.publishChatEvent("CHAT_ROOM_CREATED", "room-456", Map.of("title", "Test Room"));

            ArgumentCaptor<PublishRequest> captor = ArgumentCaptor.forClass(PublishRequest.class);
            verify(snsClient).publish(captor.capture());

            PublishRequest request = captor.getValue();
            assertEquals("{\"eventType\":\"CHAT_ROOM_CREATED\"}", request.message());
            assertTrue(request.messageAttributes().containsKey("eventType"));
            assertEquals("CHAT_ROOM_CREATED", request.messageAttributes().get("eventType").stringValue());
            assertTrue(request.messageAttributes().containsKey("traceId"));
            assertEquals("test-trace-123", request.messageAttributes().get("traceId").stringValue());
        } finally {
            MDC.remove("traceId");
        }
    }

    @Test
    void shouldPublishChatEventWithFallbackGeneratedTraceIdWhenMdcEmpty() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"eventType\":\"MESSAGE_SENT\"}");
        when(snsClient.publish(any(PublishRequest.class)))
                .thenReturn(PublishResponse.builder().messageId("sns-msg-2").build());

        publisher.publishChatEvent("MESSAGE_SENT", "msg-789", Map.of("content", "Hello"));

        ArgumentCaptor<PublishRequest> captor = ArgumentCaptor.forClass(PublishRequest.class);
        verify(snsClient).publish(captor.capture());

        PublishRequest request = captor.getValue();
        assertTrue(request.messageAttributes().containsKey("traceId"));
        assertNotNull(request.messageAttributes().get("traceId").stringValue());
    }

    @Test
    void shouldWrapJsonProcessingExceptionInRuntimeException() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("Serialization failed") {});

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                publisher.publishChatEvent("EVENT", "id-1", Map.of()));

        assertEquals("Failed to serialize SNS chat event", exception.getMessage());
    }

    @Test
    void shouldWrapSnsClientExceptionInRuntimeException() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(snsClient.publish(any(PublishRequest.class))).thenThrow(new RuntimeException("SNS network error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                publisher.publishChatEvent("EVENT", "id-1", Map.of()));

        assertEquals("Failed to publish SNS chat event", exception.getMessage());
    }
}
