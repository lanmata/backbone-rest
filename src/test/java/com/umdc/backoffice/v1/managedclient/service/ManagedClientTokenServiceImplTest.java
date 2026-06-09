package com.umdc.backoffice.v1.managedclient.service;

import com.umdc.backoffice.constant.types.AuditEventType;
import com.umdc.backoffice.jpa.domain.ManagedClientEntity;
import com.umdc.backoffice.jpa.repository.ManagedClientRepository;
import com.umdc.backoffice.property.ManagementAuthenticatorProperties;
import com.umdc.backoffice.property.SecurityProperties;
import com.umdc.backoffice.security.exception.CertificateSecurityException;
import com.umdc.backoffice.util.KeystoreUtil;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientTokenIntrospectResponse;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientTokenRequest;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientTokenResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/// Unit tests for {@link ManagedClientTokenServiceImpl} covering M2M token issuance,
/// revocation, and introspection (AC-TOK-01 – AC-TOK-04, AC-REV-01, AC-INT-01 – AC-INT-02).
@ExtendWith(MockitoExtension.class)
class ManagedClientTokenServiceImplTest {

    // PMD AvoidDuplicateLiterals constants
    private static final String TEST_SECRET  = "rawClientSecret";
    private static final String CURR_HASH    = "current-hash";
    private static final String GRACE_HASH   = "grace-hash";
    private static final String SCOPE_READ   = "read:data";
    private static final String ERR_INVALID  = "invalid_client";
    private static final String ERR_SCOPE    = "invalid_scope";
    private static final long   TOKEN_TTL    = 3600L;
    private static final int    RATE_LIMIT   = 100;
    private static final String ISSUER       = "backbone-rest";
    private static final String TYPE_M2M     = "M2M";

    @Mock
    private ManagedClientRepository repository;

    @Mock
    private ManagedClientSecretHashService secretHashService;

    @Mock
    private ManagedClientRedisService redisService;

    @Mock
    private ManagedClientAuditService auditService;

    @Mock
    private SecurityProperties securityProperties;

    @Mock
    private ManagementAuthenticatorProperties mcamProps;

    @Mock
    private KeystoreUtil keystoreUtil;

    private ManagedClientTokenServiceImpl service;

    private KeyPair testKeyPair;

    @BeforeEach
    void setUp() throws CertificateSecurityException, NoSuchAlgorithmException {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        testKeyPair = gen.generateKeyPair();

        when(securityProperties.getManagementAuthenticator()).thenReturn(mcamProps);
        when(keystoreUtil.loadPrivateKey(any(), any())).thenReturn(testKeyPair.getPrivate());
        when(keystoreUtil.loadPublicKey(any(), any())).thenReturn(testKeyPair.getPublic());

        service = new ManagedClientTokenServiceImpl(
                repository, secretHashService, redisService, auditService, securityProperties, keystoreUtil);
        service.init();
    }

    // ── issueToken ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("issueToken — valid credentials return 200 JWT with M2M type claim (AC-TOK-01)")
    void issueToken_validCredentials_returns200WithJwt() {
        UUID clientId = UUID.randomUUID();
        ManagedClientEntity entity = buildActiveEntity(clientId, List.of(SCOPE_READ), CURR_HASH);
        ManagedClientTokenRequest request = buildTokenRequest(clientId, TEST_SECRET, List.of(SCOPE_READ));

        when(repository.findById(clientId)).thenReturn(Optional.of(entity));
        when(secretHashService.matchesWithConstantTime(TEST_SECRET, CURR_HASH)).thenReturn(true);
        when(redisService.getGraceSecret(clientId)).thenReturn(Optional.empty());
        when(mcamProps.getRateLimitRpm()).thenReturn(RATE_LIMIT);
        when(mcamProps.getTokenTtlSeconds()).thenReturn(TOKEN_TTL);
        when(redisService.checkAndIncrementRateLimit(clientId, RATE_LIMIT)).thenReturn(true);

        ResponseEntity<?> response = service.issueToken(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ManagedClientTokenResponse body = (ManagedClientTokenResponse) response.getBody();
        assertNotNull(body);
        assertNotNull(body.getAccessToken());

        Claims claims = Jwts.parser()
                .verifyWith(testKeyPair.getPublic())
                .build()
                .parseSignedClaims(body.getAccessToken())
                .getPayload();
        assertEquals(clientId.toString(), claims.getSubject());
        assertEquals(ISSUER, claims.getIssuer());
        assertEquals(TYPE_M2M, claims.get("type"));
        assertNotNull(claims.getId());
        assertNotNull(claims.getExpiration());
    }

    @Test
    @DisplayName("issueToken — wrong secret returns 401 (AC-TOK-02)")
    void issueToken_wrongSecret_returns401() {
        UUID clientId = UUID.randomUUID();
        ManagedClientEntity entity = buildActiveEntity(clientId, List.of(SCOPE_READ), CURR_HASH);
        ManagedClientTokenRequest request = buildTokenRequest(clientId, "wrong", List.of(SCOPE_READ));

        when(repository.findById(clientId)).thenReturn(Optional.of(entity));
        when(secretHashService.matchesWithConstantTime(eq("wrong"), eq(CURR_HASH))).thenReturn(false);
        when(redisService.getGraceSecret(clientId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = service.issueToken(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(auditService).record(eq(clientId), eq(AuditEventType.CLIENT_TOKEN_ISSUE_FAILED),
                any(), eq("INVALID_SECRET"), any());
    }

    @Test
    @DisplayName("issueToken — inactive client returns 401 (AC-TOK-03)")
    void issueToken_inactiveClient_returns401() {
        UUID clientId = UUID.randomUUID();
        ManagedClientEntity entity = buildActiveEntity(clientId, List.of(SCOPE_READ), CURR_HASH);
        entity.setActive(false);
        ManagedClientTokenRequest request = buildTokenRequest(clientId, TEST_SECRET, List.of(SCOPE_READ));

        when(repository.findById(clientId)).thenReturn(Optional.of(entity));

        ResponseEntity<?> response = service.issueToken(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(auditService).record(eq(clientId), eq(AuditEventType.CLIENT_TOKEN_ISSUE_FAILED),
                any(), eq("INVALID_CLIENT"), any());
    }

    @Test
    @DisplayName("issueToken — requested scope exceeds registered scopes returns 400 (AC-TOK-04)")
    void issueToken_scopeOverflow_returns400() {
        UUID clientId = UUID.randomUUID();
        ManagedClientEntity entity = buildActiveEntity(clientId, List.of(SCOPE_READ), CURR_HASH);
        ManagedClientTokenRequest request = buildTokenRequest(clientId, TEST_SECRET, List.of("admin:all"));

        when(repository.findById(clientId)).thenReturn(Optional.of(entity));
        when(secretHashService.matchesWithConstantTime(TEST_SECRET, CURR_HASH)).thenReturn(true);
        when(redisService.getGraceSecret(clientId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = service.issueToken(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(auditService).record(eq(clientId), eq(AuditEventType.CLIENT_TOKEN_ISSUE_FAILED),
                any(), eq("INVALID_SCOPE"), any());
    }

    @Test
    @DisplayName("issueToken — rate limit exceeded returns 429")
    void issueToken_rateLimitExceeded_returns429() {
        UUID clientId = UUID.randomUUID();
        ManagedClientEntity entity = buildActiveEntity(clientId, List.of(SCOPE_READ), CURR_HASH);
        ManagedClientTokenRequest request = buildTokenRequest(clientId, TEST_SECRET, List.of(SCOPE_READ));

        when(repository.findById(clientId)).thenReturn(Optional.of(entity));
        when(secretHashService.matchesWithConstantTime(TEST_SECRET, CURR_HASH)).thenReturn(true);
        when(redisService.getGraceSecret(clientId)).thenReturn(Optional.empty());
        when(mcamProps.getRateLimitRpm()).thenReturn(RATE_LIMIT);
        when(redisService.checkAndIncrementRateLimit(clientId, RATE_LIMIT)).thenReturn(false);

        ResponseEntity<?> response = service.issueToken(request);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
    }

    @Test
    @DisplayName("issueToken — grace secret matches when current hash fails (AC-ROT-01 partial)")
    void issueToken_graceSecretMatches_returns200() {
        UUID clientId = UUID.randomUUID();
        ManagedClientEntity entity = buildActiveEntity(clientId, List.of(SCOPE_READ), CURR_HASH);
        ManagedClientTokenRequest request = buildTokenRequest(clientId, TEST_SECRET, List.of(SCOPE_READ));

        when(repository.findById(clientId)).thenReturn(Optional.of(entity));
        when(secretHashService.matchesWithConstantTime(TEST_SECRET, CURR_HASH)).thenReturn(false);
        when(redisService.getGraceSecret(clientId)).thenReturn(Optional.of(GRACE_HASH));
        when(secretHashService.matchesWithConstantTime(TEST_SECRET, GRACE_HASH)).thenReturn(true);
        when(mcamProps.getRateLimitRpm()).thenReturn(RATE_LIMIT);
        when(mcamProps.getTokenTtlSeconds()).thenReturn(TOKEN_TTL);
        when(redisService.checkAndIncrementRateLimit(clientId, RATE_LIMIT)).thenReturn(true);

        ResponseEntity<?> response = service.issueToken(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ── revokeAllTokens ───────────────────────────────────────────────────────

    @Test
    @DisplayName("revokeAllTokens — revokes each JTI and returns 204 (AC-REV-01)")
    void revokeAllTokens_withActiveTokens_returns204AndRevokes() {
        UUID clientId = UUID.randomUUID();
        Set<String> jtis = Set.of("jti1", "jti2", "jti3");

        when(repository.existsById(clientId)).thenReturn(true);
        when(mcamProps.getTokenTtlSeconds()).thenReturn(TOKEN_TTL);
        when(redisService.getClientTokenJtis(clientId)).thenReturn(jtis);

        ResponseEntity<?> response = service.revokeAllTokens(clientId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(redisService, times(jtis.size())).revokeToken(any(), eq(TOKEN_TTL));
        verify(auditService).record(eq(clientId), eq(AuditEventType.CLIENT_TOKEN_REVOKED),
                any(), eq("SUCCESS"), any());
    }

    @Test
    @DisplayName("revokeAllTokens — no active tokens still returns 204")
    void revokeAllTokens_noActiveTokens_returns204() {
        UUID clientId = UUID.randomUUID();

        when(repository.existsById(clientId)).thenReturn(true);
        when(mcamProps.getTokenTtlSeconds()).thenReturn(TOKEN_TTL);
        when(redisService.getClientTokenJtis(clientId)).thenReturn(Set.of());

        ResponseEntity<?> response = service.revokeAllTokens(clientId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(redisService, times(0)).revokeToken(any(), any(Long.class));
    }

    // ── introspectToken ───────────────────────────────────────────────────────

    @Test
    @DisplayName("introspectToken — valid token returns active=true with all claims (AC-INT-01)")
    void introspectToken_validToken_returnsActiveTrue() {
        UUID clientId = UUID.randomUUID();
        ManagedClientEntity entity = buildActiveEntity(clientId, List.of(SCOPE_READ), CURR_HASH);
        entity.setName("svc-client");

        String jwt = buildSignedJwt(clientId, List.of(SCOPE_READ), TOKEN_TTL);
        Claims claims = Jwts.parser().verifyWith(testKeyPair.getPublic()).build()
                .parseSignedClaims(jwt).getPayload();
        String jti = claims.getId();

        when(redisService.isTokenStored(jti)).thenReturn(true);
        when(redisService.isRevoked(jti)).thenReturn(false);
        when(repository.findById(clientId)).thenReturn(Optional.of(entity));

        ResponseEntity<?> response = service.introspectToken(jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ManagedClientTokenIntrospectResponse body = (ManagedClientTokenIntrospectResponse) response.getBody();
        assertNotNull(body);
        assertTrue(body.isActive());
        assertEquals(clientId.toString(), body.getClientId());
        assertNotNull(body.getJti());
        assertNotNull(body.getExp());
    }

    @Test
    @DisplayName("introspectToken — revoked token returns active=false (AC-REV-01)")
    void introspectToken_revokedToken_returnsActiveFalse() {
        UUID clientId = UUID.randomUUID();
        String jwt = buildSignedJwt(clientId, List.of(SCOPE_READ), TOKEN_TTL);
        Claims claims = Jwts.parser().verifyWith(testKeyPair.getPublic()).build()
                .parseSignedClaims(jwt).getPayload();
        String jti = claims.getId();

        when(redisService.isTokenStored(jti)).thenReturn(true);
        when(redisService.isRevoked(jti)).thenReturn(true);

        ResponseEntity<?> response = service.introspectToken(jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(((ManagedClientTokenIntrospectResponse) response.getBody()).isActive());
    }

    @Test
    @DisplayName("introspectToken — expired token returns active=false (AC-INT-02)")
    void introspectToken_expiredToken_returnsActiveFalse() {
        String expiredJwt = buildExpiredJwt(UUID.randomUUID(), List.of(SCOPE_READ));

        ResponseEntity<?> response = service.introspectToken(expiredJwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(((ManagedClientTokenIntrospectResponse) response.getBody()).isActive());
    }

    @Test
    @DisplayName("introspectToken — malformed token returns active=false, never 4xx (AC-INT-02)")
    void introspectToken_malformedToken_returnsActiveFalse() {
        ResponseEntity<?> response = service.introspectToken("not.a.valid.jwt.at.all");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(((ManagedClientTokenIntrospectResponse) response.getBody()).isActive());
    }

    // ── isTokenActive ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("isTokenActive — stored and not revoked returns true")
    void isTokenActive_activeJti_returnsTrue() {
        UUID clientId = UUID.randomUUID();
        String jwt = buildSignedJwt(clientId, List.of(SCOPE_READ), TOKEN_TTL);
        Claims claims = Jwts.parser().verifyWith(testKeyPair.getPublic()).build()
                .parseSignedClaims(jwt).getPayload();
        String jti = claims.getId();

        when(redisService.isTokenStored(jti)).thenReturn(true);
        when(redisService.isRevoked(jti)).thenReturn(false);

        assertTrue(service.isTokenActive(jwt));
    }

    @Test
    @DisplayName("isTokenActive — revoked JTI returns false")
    void isTokenActive_revokedJti_returnsFalse() {
        UUID clientId = UUID.randomUUID();
        String jwt = buildSignedJwt(clientId, List.of(SCOPE_READ), TOKEN_TTL);
        Claims claims = Jwts.parser().verifyWith(testKeyPair.getPublic()).build()
                .parseSignedClaims(jwt).getPayload();
        String jti = claims.getId();

        when(redisService.isTokenStored(jti)).thenReturn(true);
        when(redisService.isRevoked(jti)).thenReturn(true);

        assertFalse(service.isTokenActive(jwt));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private ManagedClientEntity buildActiveEntity(UUID id, List<String> scopes, String hash) {
        ManagedClientEntity entity = new ManagedClientEntity();
        entity.setId(id);
        entity.setName("svc-" + id);
        entity.setApplicationId(UUID.randomUUID());
        entity.setScopes(scopes);
        entity.setActive(true);
        entity.setSecretHash(hash);
        return entity;
    }

    private ManagedClientTokenRequest buildTokenRequest(UUID clientId, String secret, List<String> scopes) {
        ManagedClientTokenRequest req = new ManagedClientTokenRequest();
        req.setClientId(clientId);
        req.setClientSecret(secret);
        req.setScopes(scopes);
        return req;
    }

    private String buildSignedJwt(UUID clientId, List<String> scopes, long ttlSeconds) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + ttlSeconds * 1000L);
        return Jwts.builder()
                .subject(clientId.toString())
                .issuer(ISSUER)
                .issuedAt(now)
                .expiration(exp)
                .id(UUID.randomUUID().toString())
                .claim("scopes", scopes)
                .claim("type", TYPE_M2M)
                .signWith(testKeyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();
    }

    private String buildExpiredJwt(UUID clientId, List<String> scopes) {
        Date past = new Date(System.currentTimeMillis() - 5000L);
        return Jwts.builder()
                .subject(clientId.toString())
                .issuer(ISSUER)
                .issuedAt(past)
                .expiration(past)
                .id(UUID.randomUUID().toString())
                .claim("scopes", scopes)
                .claim("type", TYPE_M2M)
                .signWith(testKeyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();
    }
}
