package com.umdc.backoffice.v1.session.services;

import com.umdc.backoffice.security.bruteforce.LoginAttemptService;
import com.umdc.backoffice.security.jwt.JwtConfigProperties;
import com.umdc.backoffice.util.MessageUtil;
import com.umdc.backoffice.v1.iam.audit.service.AuditEventService;
import com.umdc.backoffice.v1.session.mapper.UserAliasMapper;
import com.umdc.backoffice.v1.session.to.SessionResponse;
import com.umdc.backoffice.v1.session.to.UserAliasTO;
import com.umdc.backoffice.v1.users.api.to.UserTO;
import com.umdc.backoffice.v1.users.mapper.UserMapper;
import com.umdc.persistence.general.domains.UserEntity;
import com.umdc.persistence.general.repositories.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests focused on {@link SessionServiceImpl#refreshSession(String)}
 * and the refresh-token field emitted by {@code loadSession(SessionEmailRequest)}.
 */
class SessionServiceImplRefreshTest {

    // JWT config used by both the service under test and the test helper
    private static final String TEST_SECRET   =
            "dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tdGVzdGluZy1wdXJwb3Nlcy1vbmx5LW1pbmltdW0tMjU2LWJpdHM=";
    private static final long   EXPIRATION_MS = 3_600_000L;

    // Claim key constants (mirror production AuthKey enum values)
    private static final String CLAIM_TYPE   = "type";
    private static final String CLAIM_UID    = "uid";
    private static final String REFRESH_TYPE = "refresh-token";

    @Mock
    private JwtConfigProperties jwtConfigProperties;
    @Mock
    private MessageUtil messageUtil;
    @Mock
    private UserMapper userMapper;
    @Mock
    private UserAliasMapper userAliasMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JtiDenyListService jtiDenyListService;
    @Mock
    private LoginAttemptService loginAttemptService;
    @Mock
    private AuditEventService auditEventService;

    private SessionServiceImpl sessionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(jwtConfigProperties.getSecret()).thenReturn(TEST_SECRET);
        when(jwtConfigProperties.getExpirationMs()).thenReturn(EXPIRATION_MS);
        sessionService = new SessionServiceImpl(
                jwtConfigProperties, messageUtil, userMapper, userAliasMapper,
                userRepository, passwordEncoder, jtiDenyListService,
                loginAttemptService, auditEventService);
    }

    // ── null / blank token ─────────────────────────────────────────────────────

    @Test
    @DisplayName("null refresh token returns 400 Bad Request")
    void refreshSession_nullToken_returns400() {
        ResponseEntity<SessionResponse> response = sessionService.refreshSession(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("blank refresh token returns 400 Bad Request")
    void refreshSession_blankToken_returns400() {
        ResponseEntity<SessionResponse> response = sessionService.refreshSession("   ");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ── malformed / wrong-signature token ─────────────────────────────────────

    @Test
    @DisplayName("malformed JWT string returns 401 Unauthorized")
    void refreshSession_invalidJwtString_returns401() {
        ResponseEntity<SessionResponse> response =
                sessionService.refreshSession("not.a.valid.jwt");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // ── user state checks ──────────────────────────────────────────────────────

    @Test
    @DisplayName("valid refresh token for an inactive user returns 401 Unauthorized")
    void refreshSession_inactiveUser_returns401() {
        UUID userId = UUID.randomUUID();
        String token = buildRefreshToken(userId, EXPIRATION_MS);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setActive(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        ResponseEntity<SessionResponse> response = sessionService.refreshSession(token);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("valid refresh token but user not found returns 401 Unauthorized")
    void refreshSession_userNotFound_returns401() {
        UUID userId = UUID.randomUUID();
        String token = buildRefreshToken(userId, EXPIRATION_MS);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResponseEntity<SessionResponse> response = sessionService.refreshSession(token);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // ── happy path ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("valid refresh token for active user returns 200 OK with new tokens")
    void refreshSession_activeUser_returns200WithNewTokens() {
        UUID userId = UUID.randomUUID();
        String token = buildRefreshToken(userId, EXPIRATION_MS);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setActive(true);

        UserAliasTO userAliasTO = new UserAliasTO();
        userAliasTO.setUserId(userId);
        userAliasTO.setAlias("testuser");
        userAliasTO.setFirstname("Test");
        userAliasTO.setLastname("User");
        userAliasTO.setRoles(Set.of(UUID.randomUUID()));

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userRepository.findUserInfo(userId)).thenReturn(new UserEntity());
        when(userMapper.toTarget(any())).thenReturn(new UserTO());
        when(userAliasMapper.toTarget(any())).thenReturn(userAliasTO);

        ResponseEntity<SessionResponse> response = sessionService.refreshSession(token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getToken());
        assertNotNull(response.getBody().getRefreshToken());
    }

    // ── helper ─────────────────────────────────────────────────────────────────

    /**
     * Builds a compact, signed refresh token that mirrors the structure
     * produced by {@code SessionServiceImpl#generateRefreshToken(UUID)}.
     *
     * @param userId   the user ID to embed in the {@code uid} claim and subject
     * @param expiryMs token time-to-live in milliseconds
     * @return compact JWT string
     */
    private String buildRefreshToken(UUID userId, long expiryMs) {
        SecretKey k = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET));
        return Jwts.builder()
                .claim(CLAIM_TYPE, REFRESH_TYPE)
                .claim(CLAIM_UID, userId.toString())
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiryMs))
                .signWith(k)
                .compact();
    }
}

