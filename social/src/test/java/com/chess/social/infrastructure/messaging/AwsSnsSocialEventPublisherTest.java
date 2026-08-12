package com.chess.social.infrastructure.messaging;

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
}
