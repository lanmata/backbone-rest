package com.umdc.backoffice.v1.iam.tokens.service;

import com.umdc.backoffice.v1.iam.tokens.api.to.TokenIntrospectRequest;
import com.umdc.backoffice.v1.iam.tokens.api.to.TokenIntrospectResponse;
import com.umdc.backoffice.v1.session.services.SessionService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class TokenIntrospectServiceImplTest {

    private static final String VALID_TOKEN = "valid.session.token";
    private static final String INVALID_TOKEN = "invalid.session.token";
    private static final String TEST_SUBJECT = "testUser";
    private static final String TEST_ISSUER = "test-issuer";
    private static final String SESSION_TYPE = "SESSION";

    @Mock
    private SessionService sessionService;

    @Mock
    private Claims claims;

    private TokenIntrospectServiceImpl tokenIntrospectService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tokenIntrospectService = new TokenIntrospectServiceImpl(sessionService);
    }

    // ── blank / null token ────────────────────────────────────────────────────

    @Test
    void introspect_nullToken_returnsBadRequestWithActiveFalse() {
        // Arrange
        TokenIntrospectRequest request = new TokenIntrospectRequest(null);

        // Act
        ResponseEntity<TokenIntrospectResponse> response = tokenIntrospectService.introspect(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().active());
    }

    @Test
    void introspect_blankToken_returnsBadRequestWithActiveFalse() {
        // Arrange — whitespace-only string counts as blank
        TokenIntrospectRequest request = new TokenIntrospectRequest("   ");

        // Act
        ResponseEntity<TokenIntrospectResponse> response = tokenIntrospectService.introspect(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().active());
    }

    @Test
    void introspect_emptyToken_returnsBadRequestWithActiveFalse() {
        // Arrange
        TokenIntrospectRequest request = new TokenIntrospectRequest("");

        // Act
        ResponseEntity<TokenIntrospectResponse> response = tokenIntrospectService.introspect(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().active());
    }

    // ── invalid token ─────────────────────────────────────────────────────────

    @Test
    void introspect_invalidToken_returnsOkWithActiveFalse() {
        // Arrange — non-blank token that fails validation
        TokenIntrospectRequest request = new TokenIntrospectRequest(INVALID_TOKEN);
        when(sessionService.isValid(INVALID_TOKEN)).thenReturn(false);

        // Act
        ResponseEntity<TokenIntrospectResponse> response = tokenIntrospectService.introspect(request);

        // Assert — HTTP 200 with active=false (not 4xx) per RFC 7662 style
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().active());
    }

    // ── valid token, full claims ───────────────────────────────────────────────

    @Test
    void introspect_validToken_returnsActiveTrueWithFullClaims() {
        // Arrange
        long expiresAtMs = System.currentTimeMillis() + 3_600_000L;
        long issuedAtMs = System.currentTimeMillis();
        TokenIntrospectRequest request = new TokenIntrospectRequest(VALID_TOKEN);
        when(sessionService.isValid(VALID_TOKEN)).thenReturn(true);
        when(sessionService.getTokenClaims(VALID_TOKEN)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(TEST_SUBJECT);
        when(claims.getIssuer()).thenReturn(TEST_ISSUER);
        when(claims.getAudience()).thenReturn(Set.of("test-audience"));
        when(claims.getExpiration()).thenReturn(new Date(expiresAtMs));
        when(claims.getIssuedAt()).thenReturn(new Date(issuedAtMs));
        when(claims.get("type")).thenReturn(SESSION_TYPE);
        when(claims.get("roles")).thenReturn("[ROLE_ADMIN, ROLE_USER]");

        // Act
        ResponseEntity<TokenIntrospectResponse> response = tokenIntrospectService.introspect(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().active());
        assertEquals(TEST_SUBJECT, response.getBody().subject());
        assertEquals(TEST_ISSUER, response.getBody().issuer());
        assertEquals("test-audience", response.getBody().audience());
        assertEquals(expiresAtMs, response.getBody().expiresAt());
        assertEquals(issuedAtMs, response.getBody().issuedAt());
        assertEquals(SESSION_TYPE, response.getBody().tokenType());
        assertNotNull(response.getBody().roles());
        assertEquals(2, response.getBody().roles().size());
        assertTrue(response.getBody().roles().contains("ROLE_ADMIN"));
        assertTrue(response.getBody().roles().contains("ROLE_USER"));
    }

    @Test
    void introspect_validToken_noRolesClaim_returnsEmptyRolesList() {
        // Arrange
        TokenIntrospectRequest request = new TokenIntrospectRequest(VALID_TOKEN);
        when(sessionService.isValid(VALID_TOKEN)).thenReturn(true);
        when(sessionService.getTokenClaims(VALID_TOKEN)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(TEST_SUBJECT);
        when(claims.getIssuer()).thenReturn(TEST_ISSUER);
        when(claims.getAudience()).thenReturn(null);
        when(claims.getExpiration()).thenReturn(null);
        when(claims.getIssuedAt()).thenReturn(null);
        when(claims.get("type")).thenReturn(null);
        when(claims.get("roles")).thenReturn(null);

        // Act
        ResponseEntity<TokenIntrospectResponse> response = tokenIntrospectService.introspect(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().active());
        assertNotNull(response.getBody().roles());
        assertTrue(response.getBody().roles().isEmpty());
        assertEquals(0L, response.getBody().expiresAt());
        assertEquals(0L, response.getBody().issuedAt());
        assertNull(response.getBody().tokenType());
        assertNull(response.getBody().audience());
    }

    @Test
    void introspect_validToken_emptyAudienceSet_nullAudienceInResponse() {
        // Arrange — empty Set<String> should yield null audience in response
        TokenIntrospectRequest request = new TokenIntrospectRequest(VALID_TOKEN);
        when(sessionService.isValid(VALID_TOKEN)).thenReturn(true);
        when(sessionService.getTokenClaims(VALID_TOKEN)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(TEST_SUBJECT);
        when(claims.getIssuer()).thenReturn(TEST_ISSUER);
        when(claims.getAudience()).thenReturn(Set.of());
        when(claims.getExpiration()).thenReturn(null);
        when(claims.getIssuedAt()).thenReturn(null);
        when(claims.get("type")).thenReturn(null);
        when(claims.get("roles")).thenReturn(null);

        // Act
        ResponseEntity<TokenIntrospectResponse> response = tokenIntrospectService.introspect(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().active());
        assertNull(response.getBody().audience());
    }

    @Test
    void introspect_validToken_emptyRolesClaim_returnsEmptyRolesList() {
        // Arrange — roles stored as "[]" bracket notation
        TokenIntrospectRequest request = new TokenIntrospectRequest(VALID_TOKEN);
        when(sessionService.isValid(VALID_TOKEN)).thenReturn(true);
        when(sessionService.getTokenClaims(VALID_TOKEN)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(TEST_SUBJECT);
        when(claims.getIssuer()).thenReturn(TEST_ISSUER);
        when(claims.getAudience()).thenReturn(null);
        when(claims.getExpiration()).thenReturn(null);
        when(claims.getIssuedAt()).thenReturn(null);
        when(claims.get("type")).thenReturn(null);
        when(claims.get("roles")).thenReturn("[]");

        // Act
        ResponseEntity<TokenIntrospectResponse> response = tokenIntrospectService.introspect(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().active());
        assertNotNull(response.getBody().roles());
        assertTrue(response.getBody().roles().isEmpty());
    }

    @Test
    void introspect_validToken_multipleAudiences_joinedWithComma() {
        // Arrange — JJWT returns Set<String> for aud; multiple values should be joined
        TokenIntrospectRequest request = new TokenIntrospectRequest(VALID_TOKEN);
        when(sessionService.isValid(VALID_TOKEN)).thenReturn(true);
        when(sessionService.getTokenClaims(VALID_TOKEN)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(TEST_SUBJECT);
        when(claims.getIssuer()).thenReturn(TEST_ISSUER);
        when(claims.getAudience()).thenReturn(Set.of("aud1", "aud2"));
        when(claims.getExpiration()).thenReturn(null);
        when(claims.getIssuedAt()).thenReturn(null);
        when(claims.get("type")).thenReturn(null);
        when(claims.get("roles")).thenReturn(null);

        // Act
        ResponseEntity<TokenIntrospectResponse> response = tokenIntrospectService.introspect(request);

        // Assert — audience is non-null and contains both values separated by comma
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().audience());
        assertTrue(response.getBody().audience().contains("aud1"));
        assertTrue(response.getBody().audience().contains("aud2"));
    }
}

