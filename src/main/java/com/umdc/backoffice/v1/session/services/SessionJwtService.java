package com.umdc.backoffice.v1.session.services;

import io.jsonwebtoken.Claims;

import java.util.Date;
import java.util.Map;
import java.util.Optional;


/**
 * Create 2024-11-11
 *
 * @author Luis Mata
 * @since 17
 */
public interface SessionJwtService {

    String SESSION_TOKEN_KEY = "session-token";

    /**
     * Token type claim value for refresh tokens.
     * Refresh tokens carry a longer TTL and are used exclusively by
     * {@code POST /api/v1/session/refresh} to obtain new access tokens.
     */
    String REFRESH_TOKEN_KEY = "refresh-token";

    String AUTHORIZATION_HEADER = "Authorization";

    String BEARER_PREFIX = "Bearer ";

    /**
     * Checks if the given token is expired.
     *
     * @param token the JWT token
     * @return true if the token is expired, false otherwise
     */
    default boolean isTokenExpired(String token) {
        try {
            Claims claims = getTokenClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Validates the given token.
     *
     * @param token the JWT token
     * @return true if the token is valid, false otherwise
     */
    default boolean isValid(String token) {
        try {
            Claims claims = getTokenClaims(token);
            return SESSION_TOKEN_KEY.equals(getTokenClaims(token).get("type")) && new Date().before(claims.getExpiration());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Retrieves the subject (user ID) from the given token.
     *
     * @param token the JWT token
     * @return an Optional containing the subject if present, otherwise an empty Optional
     */
    default Optional<String> getSubjectFromToken(String token) {
        try {
            Claims claims = getTokenClaims(token);
            return Optional.ofNullable(claims.getSubject());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Generates a session token for the given username and parameters.
     *
     * @param username   the username for which the session token is generated
     * @param parameters a map of additional parameters to include in the token
     * @return the generated session token
     */
    String generateSessionToken(String username, Map<String, String> parameters);

    /**
     * Retrieves the claims from the given token.
     *
     * @param token the JWT token
     * @return the claims contained in the token
     */
    default Claims getTokenClaims(String token) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Retrieves the username from the given token.
     *
     * @param token the JWT token
     * @return the username contained in the token
     */
    String getUsernameFromToken(String token);
}
