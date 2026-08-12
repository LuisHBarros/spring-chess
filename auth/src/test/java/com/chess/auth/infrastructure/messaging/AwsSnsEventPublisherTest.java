package com.chess.auth.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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
}
