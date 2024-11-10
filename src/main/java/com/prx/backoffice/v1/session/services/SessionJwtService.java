package com.prx.backoffice.v1.session.services;

import com.prx.backoffice.config.jwt.JwtConfigProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service class for handling JWT operations related to sessions.
 */
@Service
public class SessionJwtService {

    private final JwtConfigProperties jwtConfigProperties;
    private final SecretKey key;

    /**
     * Constructor to initialize SessionJwtService with JwtConfigProperties.
     *
     * @param jwtConfigProperties the configuration properties for JWT
     */
    public SessionJwtService(JwtConfigProperties jwtConfigProperties) {
        this.jwtConfigProperties = jwtConfigProperties;
        this.key = generateKey();
    }

    /**
     * Checks if the given token is expired.
     *
     * @param token the JWT token
     * @return true if the token is expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getTokenClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Retrieves the Keycloak user ID from the given token.
     *
     * @param token the JWT token
     * @return an Optional containing the user ID if present, otherwise an empty Optional
     */
    public Optional<String> getKeycloakUserIdFromToken(String token) {
        try {
            Claims claims = getTokenClaims(token);
            return Optional.ofNullable(claims.getSubject());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Generates a session token for the given username.
     *
     * @param username the username for which to generate the session token
     * @return the generated session token
     */
    public String generateSessionToken(String username) {
        Map<String, Object> claims = new ConcurrentHashMap<>();
        claims.put("type", "Session-Token");
        claims.put("sessionId", username);
        claims.put("iat", new Date());
        claims.put("jti", UUID.randomUUID().toString());

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtConfigProperties.getExpirationMs()))
                .signWith(key)
                .compact();
    }

    /**
     * Retrieves the claims from the given token.
     *
     * @param token the JWT token
     * @return the claims contained in the token
     */
    public Claims getTokenClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Retrieves the username from the given token.
     *
     * @param token the JWT token
     * @return the username contained in the token
     */
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Generates a SecretKey from the JWT configuration properties.
     *
     * @return the generated SecretKey
     */
    private SecretKey generateKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtConfigProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
