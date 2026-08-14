package com.chess.chat.infrastructure.client;

import com.chess.chat.domain.model.UserId;
import com.chess.chat.domain.port.GuildPermissionPort;
import com.chess.chat.infrastructure.web.dto.ApiResponseDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class SocialGuildPermissionAdapter implements GuildPermissionPort {

    private final RestTemplate restTemplate;
    private final String socialBaseUrl;

    public SocialGuildPermissionAdapter(RestTemplate restTemplate, String socialBaseUrl) {
        this.restTemplate = restTemplate;
        this.socialBaseUrl = socialBaseUrl;
    }

    @Override
    public boolean hasChatAccess(UserId userId, String guildId) {
        if (guildId == null || guildId.isBlank()) {
            return false;
        }

        String url = UriComponentsBuilder.fromHttpUrl(socialBaseUrl)
                .path("/api/v1/guilds/{guildId}/members/{userId}/permissions")
                .queryParam("permission", "CHAT_ACCESS")
                .buildAndExpand(guildId, userId.toString())
                .toUriString();

        try {
            ResponseEntity<ApiResponseDto<Boolean>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()),
                    new ParameterizedTypeReference<>() {}
            );

            ApiResponseDto<Boolean> body = response.getBody();
            return body != null && body.isSuccess() && Boolean.TRUE.equals(body.getData());
        } catch (RestClientException ex) {
            return false;
        }
    }
}
