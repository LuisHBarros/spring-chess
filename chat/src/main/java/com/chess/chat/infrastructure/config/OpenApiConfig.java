package com.chess.chat.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Chat Microservice API")
                        .version("1.0.0")
                        .description("REST API for Real-Time Messaging, Group Chats, Guild Channels, and Match Messaging using Spring Boot 3 & Domain-Driven Design.")
                        .contact(new Contact()
                                .name("Chess Engineering Team")
                                .email("engineering@chess.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
