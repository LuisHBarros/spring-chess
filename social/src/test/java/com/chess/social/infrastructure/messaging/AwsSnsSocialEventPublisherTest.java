package com.chess.social.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AwsSnsSocialEventPublisherTest {

    private SnsClient snsClient;
    private ObjectMapper objectMapper;
    private AwsSnsSocialEventPublisher publisher;

    @BeforeEach
    void setUp() {
        snsClient = mock(SnsClient.class);
        objectMapper = new ObjectMapper();
        publisher = new AwsSnsSocialEventPublisher(snsClient, objectMapper);
    }

    @Test
    @DisplayName("Should successfully publish social event to SNS with traceId attribute")
    void shouldPublishSocialEvent() {
        PublishResponse publishResponse = PublishResponse.builder().messageId("msg-social-1").build();
        when(snsClient.publish(any(PublishRequest.class))).thenReturn(publishResponse);

        publisher.publishSocialEvent("FRIEND_REQUEST_SENT", "user-123", Map.of("addresseeId", "user-456"));

        ArgumentCaptor<PublishRequest> captor = ArgumentCaptor.forClass(PublishRequest.class);
        verify(snsClient, times(1)).publish(captor.capture());

        PublishRequest request = captor.getValue();
        assertNotNull(request);
        assertTrue(request.message().contains("FRIEND_REQUEST_SENT"));
        assertTrue(request.message().contains("user-123"));
        assertEquals("FRIEND_REQUEST_SENT", request.messageAttributes().get("eventType").stringValue());
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

        ReflectionTestUtils.setField(publisher, "tracer", tracer);

        when(snsClient.publish(any(PublishRequest.class))).thenReturn(PublishResponse.builder().messageId("msg-social-2").build());

        publisher.publishSocialEvent("GUILD_INVITE_SENT", "user-2", Map.of("guildId", "guild-1"));

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
            when(snsClient.publish(any(PublishRequest.class))).thenReturn(PublishResponse.builder().messageId("msg-social-3").build());

            publisher.publishSocialEvent("FRIEND_ACCEPTED", "user-3", Map.of("friendId", "user-4"));

            ArgumentCaptor<PublishRequest> captor = ArgumentCaptor.forClass(PublishRequest.class);
            verify(snsClient, times(1)).publish(captor.capture());

            PublishRequest request = captor.getValue();
            assertEquals("mdc-trace-456", request.messageAttributes().get("traceId").stringValue());
        } finally {
            MDC.remove("traceId");
        }
    }
}
