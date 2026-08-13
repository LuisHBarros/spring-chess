package com.chess.social.infrastructure.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityUtilsTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should extract authenticated user ID from JWT sub claim")
    void shouldExtractUserIdFromSubClaim() {
        UUID expectedId = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .subject(expectedId.toString())
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(jwt, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(SecurityUtils.getAuthenticatedUserId()).isEqualTo(expectedId);
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when requested user ID does not match JWT sub")
    void shouldThrowWhenUserIdMismatches() {
        UUID authenticatedId = UUID.randomUUID();
        UUID differentId = UUID.randomUUID();

        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .subject(authenticatedId.toString())
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(jwt, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThatThrownBy(() -> SecurityUtils.validateUserIdentity(differentId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Authenticated user ID does not match");
    }

    @Test
    @DisplayName("Should pass validation when requested user ID matches JWT sub")
    void shouldPassWhenUserIdMatches() {
        UUID authenticatedId = UUID.randomUUID();

        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .subject(authenticatedId.toString())
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(jwt, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThatCode(() -> SecurityUtils.validateUserIdentity(authenticatedId))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should fail closed when token principal has unparseable UUID")
    void shouldFailClosedWhenPrincipalUuidIsInvalid() {
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .subject("invalid-uuid-string")
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(jwt, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThatThrownBy(() -> SecurityUtils.validateUserIdentity(UUID.randomUUID()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Authenticated user identity could not be verified");
    }
}
