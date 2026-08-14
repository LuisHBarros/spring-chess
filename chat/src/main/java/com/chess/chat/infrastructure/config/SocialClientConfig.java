package com.chess.chat.infrastructure.config;

import com.chess.chat.domain.port.GuildPermissionPort;
import com.chess.chat.infrastructure.client.JwtTokenPropagatingInterceptor;
import com.chess.chat.infrastructure.client.SocialGuildPermissionAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
public class SocialClientConfig {

    @Bean
    public JwtTokenPropagatingInterceptor jwtTokenPropagatingInterceptor() {
        return new JwtTokenPropagatingInterceptor();
    }

    @Bean
    public RestTemplate socialRestTemplate(JwtTokenPropagatingInterceptor interceptor) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(List.of(interceptor));
        return restTemplate;
    }

    @Bean
    public GuildPermissionPort guildPermissionPort(
            RestTemplate socialRestTemplate,
            @Value("${social.service.url:http://localhost:8081}") String socialBaseUrl) {
        return new SocialGuildPermissionAdapter(socialRestTemplate, socialBaseUrl);
    }
}
