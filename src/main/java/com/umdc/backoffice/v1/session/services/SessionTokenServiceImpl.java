/*
 *  @(#)SessionTokenServiceImpl.java
 *
 *  Copyright (c) Luis Antonio Mata Mata. All rights reserved.
 *
 *   All rights to this product are owned by Luis Antonio Mata Mata and may only
 *  be used under the terms of its associated license document. You may NOT
 *  copy, modify, sublicense, or distribute this source file or portions of
 *  it unless previously authorized in writing by Luis Antonio Mata Mata.
 *  In any event, this notice and the above copyright must always be included
 *  verbatim with this file.
 */

package com.umdc.backoffice.v1.session.services;

import com.umdc.backoffice.constant.keys.AuthKey;
import com.umdc.backoffice.messages.JWTMessage;
import com.umdc.backoffice.security.jwt.JwtConfigProperties;
import com.umdc.commons.exception.StandardException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Owns all JJWT mechanics for session and refresh tokens: signing key
 * management, token issuance, claims parsing (with expiry grace window),
 * validity checks against the JTI deny-list, and revocation.
 * <p>
 * Extracted out of {@link SessionServiceImpl} (previously marked
 * {@code @SuppressWarnings("PMD.GodClass")}) so login/refresh orchestration
 * and low-level token mechanics are no longer a single class.
 * </p>
 *
 * @author Luis Mata
 */
@Service
public class SessionTokenServiceImpl implements SessionJwtService {

    private static final long REFRESH_TOKEN_MULTIPLIER = 7L;
    private static final long REFRESH_GRACE_SECONDS = 604_800L; // 7 days

    private final JwtConfigProperties jwtConfigProperties;
    private final JtiDenyListService jtiDenyListService;
    private final SecretKey key;

    public SessionTokenServiceImpl(JwtConfigProperties jwtConfigProperties, JtiDenyListService jtiDenyListService) {
        this.jwtConfigProperties = jwtConfigProperties;
        this.jtiDenyListService = jtiDenyListService;
        this.key = generateKey();
    }

    /**
     * {@inheritDoc}
     * Adds {@code iss} and {@code aud} claims when configured.
     */
    @Override
    public String generateSessionToken(String username, Map<String, String> parameters) {
        Map<String, Object> claims = new ConcurrentHashMap<>();
        claims.put(AuthKey.JTI.value, UUID.randomUUID().toString());
        claims.put(AuthKey.TYPE.value, SESSION_TOKEN_KEY);
        claims.put(AuthKey.IAT.value, new Date());
        if (Objects.nonNull(parameters) && !parameters.isEmpty()) {
            claims.putAll(parameters);
        }

        var builder = Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtConfigProperties.getExpirationMs()));
        applyIssuerAndAudience(builder);
        return builder.signWith(key).compact();
    }

    /**
     * Generates a refresh token for the given user ID.
     * The refresh token carries {@code type=refresh-token} and has a TTL of
     * {@value #REFRESH_TOKEN_MULTIPLIER}x the configured access-token expiry.
     *
     * @param userId the user whose ID is set as subject and {@code uid} claim
     * @return the compact refresh token string
     */
    public String generateRefreshToken(UUID userId) {
        long refreshTtlMs = jwtConfigProperties.getExpirationMs() * REFRESH_TOKEN_MULTIPLIER;
        Map<String, Object> refreshClaims = new ConcurrentHashMap<>();
        refreshClaims.put(AuthKey.JTI.value, UUID.randomUUID().toString());
        refreshClaims.put(AuthKey.TYPE.value, REFRESH_TOKEN_KEY);
        refreshClaims.put(AuthKey.USER_ID.value, userId.toString());
        refreshClaims.put(AuthKey.IAT.value, new Date());

        var builder = Jwts.builder()
                .claims(refreshClaims)
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTtlMs));
        applyIssuerAndAudience(builder);
        return builder.signWith(key).compact();
    }

    private void applyIssuerAndAudience(JwtBuilder builder) {
        String issuer = jwtConfigProperties.getIssuer();
        if (Objects.nonNull(issuer) && !issuer.isEmpty()) {
            builder.issuer(issuer);
        }
        String audience = jwtConfigProperties.getAudience();
        if (Objects.nonNull(audience) && !audience.isEmpty()) {
            builder.audience().add(audience).and();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Claims getTokenClaims(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            // Allow a grace window beyond the expiry time
            long expMs = e.getClaims().getExpiration().getTime();
            long graceMs = REFRESH_GRACE_SECONDS * 1000L;
            if (System.currentTimeMillis() > expMs + graceMs) {
                throw new StandardException(JWTMessage.TOKEN_EXPIRED, e);
            }
            claims = e.getClaims();
        } catch (Exception e) {
            throw new StandardException(JWTMessage.TOKEN_INVALID, e);
        }
        return claims;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * {@inheritDoc}
     * Also rejects tokens whose JTI is in the deny-list.
     */
    @Override
    public boolean isValid(String token) {
        try {
            Claims claims = getTokenClaims(token);
            String jti = (String) claims.get(AuthKey.JTI.value);
            if (Objects.nonNull(jti) && jtiDenyListService.isDenied(jti)) {
                return false;
            }
            return SESSION_TOKEN_KEY.equals(claims.get(AuthKey.TYPE.value))
                    && new Date().before(claims.getExpiration());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Adds the token's JTI to the deny-list with its remaining TTL so it is
     * evicted automatically on natural expiry.
     *
     * @param token the session token to revoke
     * @throws IllegalArgumentException if the token has no {@code jti} claim
     */
    public void denyToken(String token) {
        Claims claims = getTokenClaims(token);
        String jti = (String) claims.get(AuthKey.JTI.value);
        if (Objects.isNull(jti)) {
            throw new IllegalArgumentException("Token has no jti claim");
        }
        long remainingTtlSeconds = (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000L;
        if (remainingTtlSeconds > 0) {
            jtiDenyListService.denyJti(jti, remainingTtlSeconds);
        }
    }

    private SecretKey generateKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtConfigProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
