package com.chess.auth.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AwsSnsEventPublisherTest {

    private SnsClient snsClient;
    private ObjectMapper objectMapper;
    private AwsSnsEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        snsClient = mock(SnsClient.class);
        objectMapper = new ObjectMapper();
        eventPublisher = new AwsSnsEventPublisher(snsClient, objectMapper, null);
    }

    @Test
    @DisplayName("Should successfully publish user event to SNS with traceId message attribute")
    void shouldPublishUserEvent() {
        PublishResponse publishResponse = PublishResponse.builder()
                .messageId("msg-12345")
                .build();
        when(snsClient.publish(any(PublishRequest.class))).thenReturn(publishResponse);

        eventPublisher.publishUserEvent("USER_REGISTERED", "user-789", Map.of("username", "grandmaster"));

        ArgumentCaptor<PublishRequest> captor = ArgumentCaptor.forClass(PublishRequest.class);
        verify(snsClient, times(1)).publish(captor.capture());

        PublishRequest request = captor.getValue();
        assertNotNull(request);
        assertTrue(request.message().contains("USER_REGISTERED"));
        assertTrue(request.message().contains("user-789"));
        assertNotNull(request.messageAttributes().get("eventType"));
        assertEquals("USER_REGISTERED", request.messageAttributes().get("eventType").stringValue());
        assertNotNull(request.messageAttributes().get("traceId"));
    }

    @Test
    @DisplayName("Should use traceId from current Micrometer span when available")
    void shouldUseTraceIdFromCurrentSpan() {
        Tracer tracer = mock(Tracer.class);
        Span span = mock(Span.class);
        TraceContext context = mock(TraceContext.class);
        when(tracer.currentSpan()).thenReturn(span);
        when(span.context()).thenReturn(context);
        when(context.traceId()).thenReturn("span-trace-123");

        AwsSnsEventPublisher publisherWithTracer = new AwsSnsEventPublisher(snsClient, objectMapper, tracer);

        when(snsClient.publish(any(PublishRequest.class))).thenReturn(PublishResponse.builder().messageId("msg-2").build());

        publisherWithTracer.publishUserEvent("USER_REGISTERED", "user-2", Map.of("username", "master"));

        ArgumentCaptor<PublishRequest> captor = ArgumentCaptor.forClass(PublishRequest.class);
        verify(snsClient, times(1)).publish(captor.capture());

        PublishRequest request = captor.getValue();
        assertEquals("span-trace-123", request.messageAttributes().get("traceId").stringValue());
    }

    @Test
    @DisplayName("Should use traceId from MDC when no tracer span is available")
    void shouldUseTraceIdFromMdc() {
        MDC.put("traceId", "mdc-trace-456");
        try {
            when(snsClient.publish(any(PublishRequest.class))).thenReturn(PublishResponse.builder().messageId("msg-3").build());

            eventPublisher.publishUserEvent("USER_LOGGED_IN", "user-3", Map.of("ip", "127.0.0.1"));

            ArgumentCaptor<PublishRequest> captor = ArgumentCaptor.forClass(PublishRequest.class);
            verify(snsClient, times(1)).publish(captor.capture());

            PublishRequest request = captor.getValue();
            assertEquals("mdc-trace-456", request.messageAttributes().get("traceId").stringValue());
        } finally {
            MDC.remove("traceId");
        }
    }

    @Test
    @DisplayName("Should throw RuntimeException when serialization fails")
    void shouldThrowRuntimeExceptionWhenSerializationFails() throws JsonProcessingException {
        ObjectMapper failingMapper = mock(ObjectMapper.class);
        when(failingMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("fail") {});

        AwsSnsEventPublisher failingPublisher = new AwsSnsEventPublisher(snsClient, failingMapper, null);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> failingPublisher.publishUserEvent("USER_REGISTERED", "user-789", Map.of("username", "grandmaster")));

        assertTrue(exception.getMessage().contains("Failed to serialize"));
    }

    @Test
    @DisplayName("Should throw RuntimeException when SNS publish fails")
    void shouldThrowRuntimeExceptionWhenPublishFails() {
        when(snsClient.publish(any(PublishRequest.class))).thenThrow(new RuntimeException("SNS down"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> eventPublisher.publishUserEvent("USER_REGISTERED", "user-789", Map.of("username", "grandmaster")));

        assertTrue(exception.getMessage().contains("Failed to publish"));
    }
}
