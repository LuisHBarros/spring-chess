package com.chess.auth.infrastructure.security;

import com.chess.auth.domain.model.AuthToken;
import com.chess.auth.domain.model.Email;
import com.chess.auth.domain.model.User;
import com.chess.auth.domain.model.UserId;
import com.chess.auth.domain.port.TokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenProviderAdapter implements TokenProvider {

    private final RsaKeyPairProvider keyPairProvider;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtTokenProviderAdapter(
            RsaKeyPairProvider keyPairProvider,
            @Value("${jwt.access-token-expiration-ms:3600000}") long accessTokenExpirationMs,
            @Value("${jwt.refresh-token-expiration-ms:604800000}") long refreshTokenExpirationMs) {
        this.keyPairProvider = keyPairProvider;
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    @Override
    public AuthToken generateTokens(User user) {
        Date now = new Date();

        Date accessExpiry = new Date(now.getTime() + accessTokenExpirationMs);
        String accessToken = Jwts.builder()
                .header().keyId(keyPairProvider.getKeyId()).and()
                .subject(user.getId().getValue().toString())
                .claim("username", user.getUsername().getValue())
                .claim("email", user.getEmail().getValue())
                .claim("type", "access")
                .issuedAt(now)
                .expiration(accessExpiry)
                .signWith(keyPairProvider.getPrivateKey(), Jwts.SIG.RS256)
                .compact();

        Date refreshExpiry = new Date(now.getTime() + refreshTokenExpirationMs);
        String refreshToken = Jwts.builder()
                .header().keyId(keyPairProvider.getKeyId()).and()
                .subject(user.getEmail().getValue())
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(refreshExpiry)
                .signWith(keyPairProvider.getPrivateKey(), Jwts.SIG.RS256)
                .compact();

        return new AuthToken(accessToken, refreshToken, "Bearer", accessTokenExpirationMs / 1000);
    }

    @Override
    public boolean validateAccessToken(String token) {
        return validateTokenWithType(token, "access");
    }

    @Override
    public boolean validateRefreshToken(String token) {
        return validateTokenWithType(token, "refresh");
    }

    private boolean validateTokenWithType(String token, String expectedType) {
        try {
            Claims claims = getClaims(token);
            String type = claims.get("type", String.class);
            return expectedType.equals(type) && !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public UserId extractUserIdFromAccessToken(String token) {
        Claims claims = getClaims(token);
        return UserId.fromString(claims.getSubject());
    }

    @Override
    public Email extractEmailFromRefreshToken(String token) {
        Claims claims = getClaims(token);
        return new Email(claims.getSubject());
    }

    @Override
    public long getRemainingExpirationSeconds(String token) {
        try {
            Claims claims = getClaims(token);
            long diffMs = claims.getExpiration().getTime() - System.currentTimeMillis();
            return Math.max(0, diffMs / 1000);
        } catch (Exception e) {
            return 0;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(keyPairProvider.getPublicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
