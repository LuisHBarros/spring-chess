package com.chess.game.infrastructure.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static UUID getAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            String sub = jwt.getSubject();
            if (sub != null) {
                try {
                    return UUID.fromString(sub);
                } catch (IllegalArgumentException ignored) {
                }
            }
            String userIdClaim = jwt.getClaimAsString("userId");
            if (userIdClaim != null) {
                try {
                    return UUID.fromString(userIdClaim);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return null;
    }

    public static void validateUserIdentity(UUID requestedUserId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            UUID authenticatedId = getAuthenticatedUserId();
            if (authenticatedId == null) {
                throw new AccessDeniedException("Authenticated user identity could not be verified from token");
            }
            if (requestedUserId != null && !authenticatedId.equals(requestedUserId)) {
                throw new AccessDeniedException("Authenticated user ID does not match the requested player/user ID");
            }
        }
    }
}
