package com.umdc.backoffice.v1.session.services;

import com.umdc.backoffice.constant.keys.AuthKey;
import com.umdc.backoffice.security.bruteforce.LoginAttemptService;
import com.umdc.backoffice.security.jwt.JwtConfigProperties;
import com.umdc.backoffice.util.MessageUtil;
import com.umdc.backoffice.util.RequestContextUtil;
import com.umdc.backoffice.v1.iam.audit.service.AuditEventService;
import com.umdc.backoffice.v1.session.mapper.UserAliasMapper;
import com.umdc.backoffice.v1.session.to.SessionEmailRequest;
import com.umdc.backoffice.v1.session.to.SessionRequest;
import com.umdc.backoffice.v1.session.to.SessionResponse;
import com.umdc.backoffice.v1.session.to.UserAliasTO;
import com.umdc.backoffice.v1.users.api.to.UserTO;
import com.umdc.backoffice.v1.users.mapper.UserMapper;
import com.umdc.commons.general.pojo.AuditEventType;
import com.umdc.persistence.general.domains.ApplicationEntity;
import com.umdc.persistence.general.domains.ApplicationRoleUserEntity;
import com.umdc.persistence.general.domains.UserEntity;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A session belongs to ONE application: the tokens carry it ({@code aid}), renewals keep it, a user removed from
 * the application loses the session, the front end's managed client must match it, and logout revokes it.
 */
class SessionApplicationBindingTest {

    private static final String SECRET = "dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tdGVzdGluZy1wdXJwb3Nlcy1vbmx5LW1pbmltdW0tMjU2LWJpdHM=";
    private static final String AID = AuthKey.APPLICATION_ID.value;

    @Mock private JwtConfigProperties jwtConfigProperties;
    @Mock private MessageUtil messageUtil;
    @Mock private UserMapper userMapper;
    @Mock private UserAliasMapper userAliasMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JtiDenyListService jtiDenyListService;
    @Mock private LoginAttemptService loginAttemptService;
    @Mock private AuditEventService auditEventService;
    @Mock private SessionUserLookupService lookup;
    @Mock private RequestContextUtil requestContextUtil;
    @Mock private SessionClientGuard clientGuard;

    private SessionServiceImpl service;
    private SessionTokenServiceImpl tokens;

    private final UUID appId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(jwtConfigProperties.getSecret()).thenReturn(SECRET);
        when(jwtConfigProperties.getExpirationMs()).thenReturn(3_600_000L);
        tokens = new SessionTokenServiceImpl(jwtConfigProperties, jtiDenyListService);
        service = new SessionServiceImpl(messageUtil, userMapper, userAliasMapper, passwordEncoder, loginAttemptService,
                auditEventService, tokens, lookup, requestContextUtil, clientGuard);
        when(clientGuard.reject(any())).thenReturn(Optional.empty());
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
    }

    /** An active user linked to the given applications, resolvable by id with the alias data a token needs. */
    private UserEntity user(UUID... applications) {
        Set<ApplicationRoleUserEntity> links = new HashSet<>();
        for (UUID application : applications) {
            ApplicationEntity entity = new ApplicationEntity();
            entity.setId(application);
            ApplicationRoleUserEntity link = new ApplicationRoleUserEntity();
            link.setApplication(entity);
            links.add(link);
        }
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setAlias("ana");
        user.setEmail("ana@example.com");
        user.setPassword("hash");
        user.setActive(true);
        user.setApplicationRoleUser(links);

        UserTO to = new UserTO();
        UserAliasTO alias = new UserAliasTO();
        alias.setUserId(userId);
        alias.setAlias("ana");
        alias.setFirstname("Ana");
        alias.setLastname("Diaz");
        alias.setRoles(Set.of(UUID.randomUUID()));
        when(lookup.findByIdWithGraph(userId)).thenReturn(Optional.of(user));
        when(userMapper.toTarget(user)).thenReturn(to);
        when(userAliasMapper.toTarget(to)).thenReturn(alias);
        return user;
    }

    private Claims claims(String token) {
        return tokens.getTokenClaims(token);
    }

    // ── login ────────────────────────────────────────────────────────────────

    @Test
    void emailLoginPutsTheApplicationInBothTokens() {
        UserEntity ana = user(appId);
        when(lookup.findByEmailAndApplication("ana@example.com", appId)).thenReturn(Optional.of(ana));

        SessionResponse body = service.loadSession(new SessionEmailRequest("ana@example.com", "pw", appId)).getBody();

        assertNotNull(body);
        assertEquals(appId.toString(), claims(body.getToken()).get(AID));
        assertEquals(appId.toString(), claims(body.getRefreshToken()).get(AID));
        assertEquals(userId.toString(), claims(body.getToken()).get(AuthKey.USER_ID.value));
    }

    @Test
    void aliasLoginPutsTheApplicationInBothTokens() {
        UserEntity ana = user(appId);
        when(lookup.findByAliasAndApplication("ana", appId)).thenReturn(Optional.of(ana));

        SessionResponse body = service.loadSession(new SessionRequest("ana", "pw", appId)).getBody();

        assertNotNull(body);
        assertEquals(appId.toString(), claims(body.getToken()).get(AID));
        assertEquals(appId.toString(), claims(body.getRefreshToken()).get(AID));
    }

    @Test
    void loginIsRefusedWhenTheApplicationNeedsAClientAndNoneIsPresented() {
        when(clientGuard.reject(appId)).thenReturn(Optional.of(HttpStatus.UNAUTHORIZED));

        assertEquals(HttpStatus.UNAUTHORIZED,
                service.loadSession(new SessionEmailRequest("ana@example.com", "pw", appId)).getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED, service.loadSession(new SessionRequest("ana", "pw", appId)).getStatusCode());
        // refused before the user is even looked up: no user enumeration and no lock-out through a foreign client
        verify(lookup, never()).findByEmailAndApplication(anyString(), any());
        verify(lookup, never()).findByAliasAndApplication(anyString(), any());
        verify(loginAttemptService, never()).recordFailure(anyString(), any());
    }

    @Test
    void loginIsForbiddenForAClientOfAnotherApplication() {
        when(clientGuard.reject(appId)).thenReturn(Optional.of(HttpStatus.FORBIDDEN));

        assertEquals(HttpStatus.FORBIDDEN,
                service.loadSession(new SessionEmailRequest("ana@example.com", "pw", appId)).getStatusCode());
    }

    // ── refresh ──────────────────────────────────────────────────────────────

    @Test
    void refreshKeepsTheApplication() {
        user(appId);

        SessionResponse body = service.refreshSession(tokens.generateRefreshToken(userId, appId)).getBody();

        assertNotNull(body);
        assertEquals(appId.toString(), claims(body.getToken()).get(AID));
        assertEquals(appId.toString(), claims(body.getRefreshToken()).get(AID));
        verify(clientGuard).reject(appId);
    }

    @Test
    void refreshEndsTheSessionOfAUserRemovedFromTheApplication() {
        user(UUID.randomUUID());   // still a user, but of another application

        assertEquals(HttpStatus.UNAUTHORIZED, service.refreshSession(tokens.generateRefreshToken(userId, appId)).getStatusCode());
    }

    @Test
    void refreshOfALegacyTokenWithoutApplicationStillWorksAndStaysUnbound() {
        user(appId);

        SessionResponse body = service.refreshSession(tokens.generateRefreshToken(userId)).getBody();

        assertNotNull(body);
        assertNull(claims(body.getToken()).get(AID));
        verify(clientGuard).reject(null);
    }

    @Test
    void refreshIsRefusedForARevokedRefreshToken() {
        user(appId);
        when(jtiDenyListService.isDenied(anyString())).thenReturn(true);

        assertEquals(HttpStatus.UNAUTHORIZED, service.refreshSession(tokens.generateRefreshToken(userId, appId)).getStatusCode());
    }

    @Test
    void refreshIsRefusedWithoutTheClientOfTheApplication() {
        user(appId);
        when(clientGuard.reject(appId)).thenReturn(Optional.of(HttpStatus.FORBIDDEN));

        assertEquals(HttpStatus.FORBIDDEN, service.refreshSession(tokens.generateRefreshToken(userId, appId)).getStatusCode());
    }

    // ── renew ────────────────────────────────────────────────────────────────

    private String sessionToken(UUID application) {
        UserEntity ana = user(application);
        when(lookup.findByEmailAndApplication("ana@example.com", application)).thenReturn(Optional.of(ana));
        return service.loadSession(new SessionEmailRequest("ana@example.com", "pw", application)).getBody().getToken();
    }

    @Test
    void renewKeepsTheApplication() {
        String token = sessionToken(appId);

        ResponseEntity<SessionResponse> renewed = service.renewToken(token);

        assertEquals(HttpStatus.OK, renewed.getStatusCode());
        assertEquals(appId.toString(), claims(renewed.getBody().getToken()).get(AID));
    }

    @Test
    void renewEndsTheSessionOfAUserRemovedFromTheApplication() {
        String token = sessionToken(appId);
        user(UUID.randomUUID());

        assertEquals(HttpStatus.UNAUTHORIZED, service.renewToken(token).getStatusCode());
    }

    @Test
    void renewIsRefusedWithoutTheClientOfTheApplication() {
        String token = sessionToken(appId);
        when(clientGuard.reject(appId)).thenReturn(Optional.of(HttpStatus.UNAUTHORIZED));

        assertEquals(HttpStatus.UNAUTHORIZED, service.renewToken(token).getStatusCode());
    }

    // ── logout ───────────────────────────────────────────────────────────────

    @Test
    void logoutRevokesTheSessionAndItsRefreshTokenAndIsAudited() {
        String token = sessionToken(appId);
        String refresh = tokens.generateRefreshToken(userId, appId);

        assertEquals(HttpStatus.NO_CONTENT, service.logout(token, refresh).getStatusCode());

        verify(jtiDenyListService).denyJti(eq(claims(token).get(AuthKey.JTI.value, String.class)), anyLong());
        verify(jtiDenyListService).denyJti(eq(claims(refresh).get(AuthKey.JTI.value, String.class)), anyLong());
        verify(auditEventService).saveRecord(eq(userId), eq(appId), eq(AuditEventType.LOGOUT), any(), any(), anyString());
    }

    @Test
    void logoutWithoutRefreshTokenRevokesOnlyTheSession() {
        String token = sessionToken(appId);

        assertEquals(HttpStatus.NO_CONTENT, service.logout(token, " ").getStatusCode());

        verify(jtiDenyListService, times(1)).denyJti(anyString(), anyLong());
    }

    @Test
    void logoutNeverRevokesTheRefreshTokenOfAnotherUser() {
        String token = sessionToken(appId);
        String foreign = tokens.generateRefreshToken(UUID.randomUUID(), appId);

        assertEquals(HttpStatus.NO_CONTENT, service.logout(token, foreign).getStatusCode());

        verify(jtiDenyListService, never()).denyJti(eq(claims(foreign).get(AuthKey.JTI.value, String.class)), anyLong());
    }

    @Test
    void logoutIgnoresAnUnusableRefreshToken() {
        String token = sessionToken(appId);

        assertEquals(HttpStatus.NO_CONTENT, service.logout(token, "not-a-jwt").getStatusCode());
    }

    @Test
    void logoutNeedsATokenOfThisService() {
        assertEquals(HttpStatus.BAD_REQUEST, service.logout(null, null).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, service.logout(" ", null).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, service.logout("not-a-jwt", null).getStatusCode());
        verify(jtiDenyListService, never()).denyJti(anyString(), anyLong());
    }

    @Test
    void logoutOfALegacyTokenWithoutUidStillRevokesIt() {
        String legacy = tokens.generateSessionToken("someone", null);

        assertEquals(HttpStatus.NO_CONTENT, service.logout(legacy, null).getStatusCode());

        verify(jtiDenyListService).denyJti(anyString(), anyLong());
        verify(auditEventService, never()).saveRecord(any(), any(), any(), any(), any(), anyString());
    }
}
