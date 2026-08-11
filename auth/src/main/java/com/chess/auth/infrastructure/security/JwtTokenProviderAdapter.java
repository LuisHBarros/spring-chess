package com.chess.auth.infrastructure.security;

import com.chess.auth.domain.model.AuthToken;
import com.chess.auth.domain.model.Email;
import com.chess.auth.domain.model.User;
import com.chess.auth.domain.model.UserId;
import com.chess.auth.domain.port.TokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProviderAdapter implements TokenProvider {

    private final SecretKey key;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtTokenProviderAdapter(
            @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}") String secret,
            @Value("${jwt.access-token-expiration-ms:3600000}") long accessTokenExpirationMs,
            @Value("${jwt.refresh-token-expiration-ms:604800000}") long refreshTokenExpirationMs) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    @Override
    public AuthToken generateTokens(User user) {
        Date now = new Date();

        Date accessExpiry = new Date(now.getTime() + accessTokenExpirationMs);
        String accessToken = Jwts.builder()
                .subject(user.getId().getValue().toString())
                .claim("username", user.getUsername().getValue())
                .claim("email", user.getEmail().getValue())
                .claim("type", "access")
                .issuedAt(now)
                .expiration(accessExpiry)
                .signWith(key)
                .compact();

        Date refreshExpiry = new Date(now.getTime() + refreshTokenExpirationMs);
        String refreshToken = Jwts.builder()
                .subject(user.getEmail().getValue())
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(refreshExpiry)
                .signWith(key)
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
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
