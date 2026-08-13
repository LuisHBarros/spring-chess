package com.chess.chat.infrastructure.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityUtilsTest {

    @BeforeEach
    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldExtractAuthenticatedUserIdFromJwtSubject() {
        UUID userId = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("mock-jwt-token")
                .header("alg", "RS256")
                .subject(userId.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(jwt, null, AuthorityUtils.NO_AUTHORITIES);
        SecurityContextHolder.getContext().setAuthentication(auth);

        UUID extracted = SecurityUtils.getAuthenticatedUserId();

        assertEquals(userId, extracted);
    }

    @Test
    void shouldExtractAuthenticatedUserIdFromUserIdClaimWhenSubjectNotUuid() {
        UUID userId = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("mock-jwt-token")
                .header("alg", "RS256")
                .subject("not-a-uuid")
                .claim("userId", userId.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(jwt, null, AuthorityUtils.NO_AUTHORITIES);
        SecurityContextHolder.getContext().setAuthentication(auth);

        UUID extracted = SecurityUtils.getAuthenticatedUserId();

        assertEquals(userId, extracted);
    }

    @Test
    void shouldReturnNullWhenNoJwtOrInvalidPrincipal() {
        assertNull(SecurityUtils.getAuthenticatedUserId());

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("string-principal", null, AuthorityUtils.NO_AUTHORITIES);
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertNull(SecurityUtils.getAuthenticatedUserId());
    }

    @Test
    void shouldValidateUserIdentitySuccessfullyWhenIdsMatch() {
        UUID userId = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("mock-jwt-token")
                .header("alg", "RS256")
                .subject(userId.toString())
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(jwt, null, AuthorityUtils.NO_AUTHORITIES);
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertDoesNotThrow(() -> SecurityUtils.validateUserIdentity(userId));
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenRequestedIdDoesNotMatchToken() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        Jwt jwt = Jwt.withTokenValue("mock-jwt-token")
                .header("alg", "RS256")
                .subject(userId.toString())
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(jwt, null, AuthorityUtils.NO_AUTHORITIES);
        SecurityContextHolder.getContext().setAuthentication(auth);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> SecurityUtils.validateUserIdentity(otherUserId));

        assertEquals("Authenticated user ID does not match the requested player/user ID", exception.getMessage());
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenTokenHasNoValidUserId() {
        UUID requestedUserId = UUID.randomUUID();

        Jwt jwt = Jwt.withTokenValue("mock-jwt-token")
                .header("alg", "RS256")
                .subject("invalid-uuid")
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(jwt, null, AuthorityUtils.NO_AUTHORITIES);
        SecurityContextHolder.getContext().setAuthentication(auth);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> SecurityUtils.validateUserIdentity(requestedUserId));

        assertEquals("Authenticated user identity could not be verified from token", exception.getMessage());
    }

    @Test
    void shouldNotValidateWhenAnonymousOrUnauthenticated() {
        UUID userId = UUID.randomUUID();

        AnonymousAuthenticationToken anonAuth = new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
        SecurityContextHolder.getContext().setAuthentication(anonAuth);

        assertDoesNotThrow(() -> SecurityUtils.validateUserIdentity(userId));
    }
}
