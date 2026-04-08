package com.prx.backoffice.v1.session.services;

import com.prx.backoffice.security.jwt.JwtConfigProperties;
import com.prx.backoffice.util.MessageUtil;
import com.prx.backoffice.v1.session.mapper.UserAliasMapper;
import com.prx.backoffice.v1.session.to.SessionEmailRequest;
import com.prx.backoffice.v1.session.to.SessionRequest;
import com.prx.backoffice.v1.session.to.SessionResponse;
import com.prx.backoffice.v1.session.to.UserAliasTO;
import com.prx.backoffice.v1.users.api.to.UserTO;
import com.prx.backoffice.v1.users.mapper.UserMapper;
import com.prx.persistence.general.domains.ApplicationEntity;
import com.prx.persistence.general.domains.ApplicationRoleUserEntity;
import com.prx.persistence.general.domains.UserEntity;
import com.prx.persistence.general.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class SessionServiceImplTest {

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

    private SessionServiceImpl sessionService;

    private static final String TEST_SECRET = "dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tdGVzdGluZy1wdXJwb3Nlcy1vbmx5LW1pbmltdW0tMjU2LWJpdHM=";
    private static final Long EXPIRATION_MS = 3600000L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(jwtConfigProperties.getSecret()).thenReturn(TEST_SECRET);
        when(jwtConfigProperties.getExpirationMs()).thenReturn(EXPIRATION_MS);
        sessionService = new SessionServiceImpl(jwtConfigProperties, messageUtil, userMapper, userAliasMapper, userRepository);
    }

    @Test
    void testLoadSession_NullRequest() {
        when(messageUtil.getUserSolicitudNulaVacia()).thenReturn("Request is null or empty");

        ResponseEntity<SessionResponse> response = sessionService.loadSession((SessionRequest) null);

        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Request is null or empty", response.getBody().getToken());
    }

    @Test
    void testLoadSession_EmptyAlias() {
        SessionRequest request = new SessionRequest();
        request.setAlias("");
        request.setPassword("password");

        when(messageUtil.getUserAliasNuloVacio()).thenReturn("Alias is null or empty");

        ResponseEntity<SessionResponse> response = sessionService.loadSession(request);

        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Alias is null or empty", response.getBody().getToken());
    }

    @Test
    void testLoadSession_EmptyPassword() {
        SessionRequest request = new SessionRequest();
        request.setAlias("testAlias");
        request.setPassword("");

        when(messageUtil.getUserClaveNulaVacia()).thenReturn("Password is null or empty");

        ResponseEntity<SessionResponse> response = sessionService.loadSession(request);

        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Password is null or empty", response.getBody().getToken());
    }

    @Test
    void testLoadSession_UserNotFound() {
        SessionRequest request = new SessionRequest();
        request.setAlias("testAlias");
        request.setPassword("password");

        when(userRepository.findByAlias("testAlias")).thenReturn(null);

        ResponseEntity<SessionResponse> response = sessionService.loadSession(request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testLoadSession_UserInactive() {
        SessionRequest request = new SessionRequest();
        request.setAlias("testAlias");
        request.setPassword("password");

        UserEntity userEntity = new UserEntity();
        userEntity.setAlias("testAlias");
        userEntity.setPassword("password");
        userEntity.setActive(false);

        when(userRepository.findByAlias("testAlias")).thenReturn(userEntity);

        ResponseEntity<SessionResponse> response = sessionService.loadSession(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testLoadSession_WrongPassword() {
        SessionRequest request = new SessionRequest();
        request.setAlias("testAlias");
        request.setPassword("wrongPassword");

        UserEntity userEntity = new UserEntity();
        userEntity.setAlias("testAlias");
        userEntity.setPassword("correctPassword");
        userEntity.setActive(true);
        userEntity.setId(UUID.randomUUID());

        when(userRepository.findByAlias("testAlias")).thenReturn(userEntity);

        ResponseEntity<SessionResponse> response = sessionService.loadSession(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testLoadSession_Success() {
        SessionRequest request = new SessionRequest();
        request.setAlias("testAlias");
        request.setPassword("password");

        UUID userId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setAlias("testAlias");
        userEntity.setPassword("password");
        userEntity.setActive(true);

        UserEntity userInfo = new UserEntity();
        UserTO userTO = new UserTO();
        UserAliasTO userAliasTO = new UserAliasTO();
        userAliasTO.setRoles(Set.of(UUID.randomUUID()));
        userAliasTO.setFirstname("John");
        userAliasTO.setLastname("Doe");

        when(userRepository.findByAlias("testAlias")).thenReturn(userEntity);
        when(userRepository.findUserInfo(userId)).thenReturn(userInfo);
        when(userMapper.toTarget(userInfo)).thenReturn(userTO);
        when(userAliasMapper.toTarget(userTO)).thenReturn(userAliasTO);

        ResponseEntity<SessionResponse> response = sessionService.loadSession(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getToken());
    }

    @Test
    void testLoadSession_UserAliasNull() {
        SessionRequest request = new SessionRequest();
        request.setAlias("testAlias");
        request.setPassword("password");

        UUID userId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setAlias("testAlias");
        userEntity.setPassword("password");
        userEntity.setActive(true);

        when(userRepository.findByAlias("testAlias")).thenReturn(userEntity);
        when(userRepository.findUserInfo(userId)).thenReturn(null);

        ResponseEntity<SessionResponse> response = sessionService.loadSession(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testLoadSessionEmail_NullRequest() {
        when(messageUtil.getUserSolicitudNulaVacia()).thenReturn("Request is null or empty");

        ResponseEntity<SessionResponse> response = sessionService.loadSession((SessionEmailRequest) null);

        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Request is null or empty", response.getBody().getToken());
    }

    @Test
    void testLoadSessionEmail_EmptyEmail() {
        SessionEmailRequest request = new SessionEmailRequest("", "password", UUID.randomUUID());

        when(messageUtil.getUserCorreoNoValido()).thenReturn("Invalid email");

        ResponseEntity<SessionResponse> response = sessionService.loadSession(request);

        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid email", response.getBody().getToken());
    }

    @Test
    void testLoadSessionEmail_NullApplicationId() {
        SessionEmailRequest request = new SessionEmailRequest("test@example.com", "password", null);

        ResponseEntity<SessionResponse> response = sessionService.loadSession(request);

        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid application ID", response.getBody().getToken());
    }

    @Test
    void testLoadSessionEmail_EmptyPassword() {
        SessionEmailRequest request = new SessionEmailRequest("test@example.com", "", UUID.randomUUID());

        when(messageUtil.getUserClaveNulaVacia()).thenReturn("Password is null or empty");

        ResponseEntity<SessionResponse> response = sessionService.loadSession(request);

        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Password is null or empty", response.getBody().getToken());
    }

    @Test
    void testLoadSessionEmail_UserNotFound() {
        UUID appId = UUID.randomUUID();
        SessionEmailRequest request = new SessionEmailRequest("test@example.com", "password", appId);

        when(userRepository.findByEmailAndApplication("test@example.com", appId)).thenReturn(Optional.empty());

        ResponseEntity<SessionResponse> response = sessionService.loadSession(request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testLoadSessionEmail_UserInactive() {
        UUID appId = UUID.randomUUID();
        SessionEmailRequest request = new SessionEmailRequest("test@example.com", "password", appId);

        UserEntity userEntity = new UserEntity();
        userEntity.setEmail("test@example.com");
        userEntity.setPassword("password");
        userEntity.setActive(false);

        when(userRepository.findByEmailAndApplication("test@example.com", appId)).thenReturn(Optional.of(userEntity));

        ResponseEntity<SessionResponse> response = sessionService.loadSession(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testLoadSessionEmail_Success() {
        UUID appId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        SessionEmailRequest request = new SessionEmailRequest("test@example.com", "password", appId);

        ApplicationEntity applicationEntity = new ApplicationEntity();
        applicationEntity.setId(appId);

        ApplicationRoleUserEntity applicationRoleUser = new ApplicationRoleUserEntity();
        applicationRoleUser.setApplication(applicationEntity);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setEmail("test@example.com");
        userEntity.setPassword("password");
        userEntity.setActive(true);
        userEntity.setApplicationRoleUser(Set.of(applicationRoleUser));

        UserEntity userInfo = new UserEntity();
        UserTO userTO = new UserTO();
        UserAliasTO userAliasTO = new UserAliasTO();
        userAliasTO.setRoles(Set.of(UUID.randomUUID()));
        userAliasTO.setFirstname("John");
        userAliasTO.setLastname("Doe");
        userAliasTO.setAlias("testAlias");
        userAliasTO.setUserId(userId);

        when(userRepository.findByEmailAndApplication("test@example.com", appId)).thenReturn(Optional.of(userEntity));
        when(userRepository.findUserInfo(userId)).thenReturn(userInfo);
        when(userMapper.toTarget(userInfo)).thenReturn(userTO);
        when(userAliasMapper.toTarget(userTO)).thenReturn(userAliasTO);

        ResponseEntity<SessionResponse> response = sessionService.loadSession(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getToken());
    }

    @Test
    void testLoadSessionEmail_WrongPassword() {
        UUID appId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        SessionEmailRequest request = new SessionEmailRequest("test@example.com", "wrongPassword", appId);

        ApplicationEntity applicationEntity = new ApplicationEntity();
        applicationEntity.setId(appId);

        ApplicationRoleUserEntity applicationRoleUser = new ApplicationRoleUserEntity();
        applicationRoleUser.setApplication(applicationEntity);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setEmail("test@example.com");
        userEntity.setPassword("correctPassword");
        userEntity.setActive(true);
        userEntity.setApplicationRoleUser(Set.of(applicationRoleUser));

        when(userRepository.findByEmailAndApplication("test@example.com", appId)).thenReturn(Optional.of(userEntity));

        ResponseEntity<SessionResponse> response = sessionService.loadSession(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testGenerateSessionToken() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("roles_id", "[123]");
        parameters.put("firstname", "John");
        parameters.put("lastname", "Doe");

        String token = sessionService.generateSessionToken("testUser", parameters);

        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void testGenerateSessionToken_NullParameters() {
        String token = sessionService.generateSessionToken("testUser", null);

        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void testGenerateSessionToken_EmptyParameters() {
        String token = sessionService.generateSessionToken("testUser", new HashMap<>());

        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void testGetTokenClaims() {
        String token = sessionService.generateSessionToken("testUser", null);
        Claims claims = sessionService.getTokenClaims(token);

        assertNotNull(claims);
        assertEquals("testUser", claims.getSubject());
    }

    @Test
    void testGetUsernameFromToken() {
        String token = sessionService.generateSessionToken("testUser", null);
        String username = sessionService.getUsernameFromToken(token);

        assertEquals("testUser", username);
    }

    @Test
    void testRenewToken_EmptyToken() {
        ResponseEntity<SessionResponse> response = sessionService.renewToken("");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testRenewToken_NullToken() {
        when(messageUtil.getUserInvalido()).thenReturn("Invalid user");

        ResponseEntity<SessionResponse> response = sessionService.renewToken(null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testRenewToken_InvalidToken() {
        when(messageUtil.getUserClaveNoPermitida()).thenReturn("Invalid token");

        ResponseEntity<SessionResponse> response = sessionService.renewToken("invalid.token.here");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testLoadSession_WithRolesNull() {
        SessionRequest request = new SessionRequest();
        request.setAlias("testAlias");
        request.setPassword("password");

        UUID userId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setAlias("testAlias");
        userEntity.setPassword("password");
        userEntity.setActive(true);

        UserEntity userInfo = new UserEntity();
        UserTO userTO = new UserTO();
        UserAliasTO userAliasTO = new UserAliasTO();
        userAliasTO.setRoles(null); // Null roles
        userAliasTO.setFirstname("John");
        userAliasTO.setLastname("Doe");

        when(userRepository.findByAlias("testAlias")).thenReturn(userEntity);
        when(userRepository.findUserInfo(userId)).thenReturn(userInfo);
        when(userMapper.toTarget(userInfo)).thenReturn(userTO);
        when(userAliasMapper.toTarget(userTO)).thenReturn(userAliasTO);

        ResponseEntity<SessionResponse> response = sessionService.loadSession(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testLoadSessionEmail_WrongEmail() {
        UUID appId = UUID.randomUUID();
        SessionEmailRequest request = new SessionEmailRequest("wrong@example.com", "password", appId);

        when(userRepository.findByEmailAndApplication("wrong@example.com", appId)).thenReturn(Optional.empty());

        ResponseEntity<SessionResponse> response = sessionService.loadSession(request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testLoadSessionEmail_ApplicationMismatch() {
        UUID appId = UUID.randomUUID();
        UUID differentAppId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        SessionEmailRequest request = new SessionEmailRequest("test@example.com", "password", appId);

        ApplicationEntity applicationEntity = new ApplicationEntity();
        applicationEntity.setId(differentAppId); // Different app ID

        ApplicationRoleUserEntity applicationRoleUser = new ApplicationRoleUserEntity();
        applicationRoleUser.setApplication(applicationEntity);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setEmail("test@example.com");
        userEntity.setPassword("password");
        userEntity.setActive(true);
        userEntity.setApplicationRoleUser(Set.of(applicationRoleUser));

        when(userRepository.findByEmailAndApplication("test@example.com", appId)).thenReturn(Optional.of(userEntity));

        ResponseEntity<SessionResponse> response = sessionService.loadSession(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testGenerateSessionToken_WithAllParameters() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("roles_id", "[UUID1,UUID2]");
        parameters.put("firstname", "Jane");
        parameters.put("lastname", "Smith");
        parameters.put("alias", "jsmith");
        parameters.put("user_id", UUID.randomUUID().toString());

        String token = sessionService.generateSessionToken("jsmith", parameters);

        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertTrue(token.split("\\.").length == 3);

        Claims claims = sessionService.getTokenClaims(token);
        assertEquals("jsmith", claims.getSubject());
        assertTrue(claims.containsKey("roles_id"));
        assertTrue(claims.containsKey("firstname"));
    }

    @Test
    void testGetUsernameFromToken_ValidToken() {
        Map<String, String> params = new HashMap<>();
        params.put("test", "value");
        String token = sessionService.generateSessionToken("testuser123", params);

        String username = sessionService.getUsernameFromToken(token);

        assertEquals("testuser123", username);
    }

}










