/*
 *  @(#)SessionTokenServiceImplTest.java
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

import com.umdc.backoffice.security.jwt.JwtConfigProperties;
import com.umdc.commons.exception.StandardException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Luis Mata
 */
class SessionTokenServiceImplTest {

    private static final String TEST_SECRET =
            "dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tdGVzdGluZy1wdXJwb3Nlcy1vbmx5LW1pbmltdW0tMjU2LWJpdHM=";
    private static final long EXPIRATION_MS = 3_600_000L;

    @Mock
    private JwtConfigProperties jwtConfigProperties;

    @Mock
    private JtiDenyListService jtiDenyListService;

    private SessionTokenServiceImpl sessionTokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(jwtConfigProperties.getSecret()).thenReturn(TEST_SECRET);
        when(jwtConfigProperties.getExpirationMs()).thenReturn(EXPIRATION_MS);
        sessionTokenService = new SessionTokenServiceImpl(jwtConfigProperties, jtiDenyListService);
    }

    @Test
    @DisplayName("generateSessionToken embeds the given parameters as claims")
    void generateSessionTokenEmbedsParameters() {
        String token = sessionTokenService.generateSessionToken("alice", Map.of("roles", "[1,2]"));

        Claims claims = sessionTokenService.getTokenClaims(token);

        assertEquals("alice", claims.getSubject());
        assertEquals("session-token", claims.get("type"));
        assertEquals("[1,2]", claims.get("roles"));
    }

    @Test
    @DisplayName("generateSessionToken tolerates a null parameters map")
    void generateSessionTokenWithNullParameters() {
        String token = sessionTokenService.generateSessionToken("alice", null);

        Claims claims = sessionTokenService.getTokenClaims(token);

        assertEquals("alice", claims.getSubject());
    }

    @Test
    @DisplayName("generateSessionToken adds issuer and audience when configured")
    void generateSessionTokenAddsIssuerAndAudience() {
        when(jwtConfigProperties.getIssuer()).thenReturn("backbone-rest");
        when(jwtConfigProperties.getAudience()).thenReturn("backbone-rest-client");

        String token = sessionTokenService.generateSessionToken("alice", null);
        Claims claims = sessionTokenService.getTokenClaims(token);

        assertEquals("backbone-rest", claims.getIssuer());
        assertTrue(claims.getAudience().contains("backbone-rest-client"));
    }

    @Test
    @DisplayName("generateRefreshToken embeds the user id as subject and uid claim")
    void generateRefreshTokenEmbedsUserId() {
        UUID userId = UUID.randomUUID();

        String token = sessionTokenService.generateRefreshToken(userId);
        Claims claims = sessionTokenService.getTokenClaims(token);

        assertEquals(userId.toString(), claims.getSubject());
        assertEquals("refresh-token", claims.get("type"));
    }

    @Test
    @DisplayName("getUsernameFromToken returns the token subject")
    void getUsernameFromTokenReturnsSubject() {
        String token = sessionTokenService.generateSessionToken("bob", null);

        assertEquals("bob", sessionTokenService.getUsernameFromToken(token));
    }

    @Test
    @DisplayName("getTokenClaims rejects a garbage token")
    void getTokenClaimsRejectsGarbageToken() {
        assertThrows(StandardException.class, () -> sessionTokenService.getTokenClaims("not-a-jwt"));
    }

    @Test
    @DisplayName("isValid returns true for a freshly issued, non-denied token")
    void isValidReturnsTrueForFreshToken() {
        String token = sessionTokenService.generateSessionToken("alice", null);

        assertTrue(sessionTokenService.isValid(token));
    }

    @Test
    @DisplayName("isValid returns false when the JTI is denied")
    void isValidReturnsFalseWhenJtiDenied() {
        when(jtiDenyListService.isDenied(anyString())).thenReturn(true);
        String token = sessionTokenService.generateSessionToken("alice", null);

        assertFalse(sessionTokenService.isValid(token));
    }

    @Test
    @DisplayName("isValid returns false for a garbage token instead of throwing")
    void isValidReturnsFalseForGarbageToken() {
        assertFalse(sessionTokenService.isValid("not-a-jwt"));
    }

    @Test
    @DisplayName("denyToken forwards the JTI and remaining TTL to the deny-list")
    void denyTokenForwardsJtiAndTtl() {
        String token = sessionTokenService.generateSessionToken("alice", null);

        sessionTokenService.denyToken(token);

        verify(jtiDenyListService, times(1)).denyJti(anyString(), anyLong());
    }

    @Test
    @DisplayName("denyToken throws when the token has no jti claim")
    void denyTokenThrowsWhenNoJti() {
        // A token built without going through generateSessionToken has no jti claim.
        String token = Jwts.builder()
                .subject("alice")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET)))
                .compact();

        assertThrows(IllegalArgumentException.class, () -> sessionTokenService.denyToken(token));
    }
}
