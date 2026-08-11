package com.chess.auth.domain.service;

import com.chess.auth.domain.exception.InvalidTokenException;
import com.chess.auth.domain.exception.UserNotFoundException;
import com.chess.auth.domain.model.AuthToken;
import com.chess.auth.domain.model.Email;
import com.chess.auth.domain.model.User;
import com.chess.auth.domain.port.TokenBlacklistService;
import com.chess.auth.domain.port.TokenProvider;
import com.chess.auth.domain.repository.UserRepository;

public class TokenAuthenticationService {

    private final TokenProvider tokenProvider;
    private final TokenBlacklistService blacklistService;
    private final UserRepository userRepository;

    public TokenAuthenticationService(TokenProvider tokenProvider,
                                       TokenBlacklistService blacklistService,
                                       UserRepository userRepository) {
        if (tokenProvider == null) {
            throw new IllegalArgumentException("TokenProvider cannot be null");
        }
        if (blacklistService == null) {
            throw new IllegalArgumentException("TokenBlacklistService cannot be null");
        }
        if (userRepository == null) {
            throw new IllegalArgumentException("UserRepository cannot be null");
        }
        this.tokenProvider = tokenProvider;
        this.blacklistService = blacklistService;
        this.userRepository = userRepository;
    }

    public AuthToken generateTokens(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        return tokenProvider.generateTokens(user);
    }

    public AuthToken refreshToken(String refreshToken) {
        if (!tokenProvider.validateRefreshToken(refreshToken)) {
            throw new InvalidTokenException("Invalid or expired refresh token");
        }

        Email email = tokenProvider.extractEmailFromRefreshToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User associated with refresh token not found"));

        return tokenProvider.generateTokens(user);
    }

    public void logout(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return;
        }
        String cleanToken = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        if (!tokenProvider.validateAccessToken(cleanToken)) {
            return;
        }

        long remainingTtl = tokenProvider.getRemainingExpirationSeconds(cleanToken);
        if (remainingTtl > 0) {
            blacklistService.blacklistToken(cleanToken, remainingTtl);
        }
    }

    public boolean isTokenRevoked(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return true;
        }
        String cleanToken = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        return blacklistService.isBlacklisted(cleanToken);
    }
}
