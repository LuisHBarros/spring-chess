package com.chess.auth.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenApiConfigTest {

    private final OpenApiConfig openApiConfig = new OpenApiConfig();

    @Test
    @DisplayName("Should create OpenAPI configuration bean with title, info, and security scheme")
    void shouldCreateOpenApiBean() {
        OpenAPI openAPI = openApiConfig.chessAuthOpenAPI();

        assertNotNull(openAPI);
        assertNotNull(openAPI.getInfo());
        assertEquals("Chess Auth Service API", openAPI.getInfo().getTitle());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());

        assertNotNull(openAPI.getComponents());
        assertTrue(openAPI.getComponents().getSecuritySchemes().containsKey("bearerAuth"));
        assertEquals("bearer", openAPI.getComponents().getSecuritySchemes().get("bearerAuth").getScheme());

        assertNotNull(openAPI.getSecurity());
        assertNotNull(openAPI.getServers());
    }
}
