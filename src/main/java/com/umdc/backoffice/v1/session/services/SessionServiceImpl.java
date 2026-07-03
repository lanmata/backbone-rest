/*
 *  @(#)SessionServiceImpl.java
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
import com.umdc.backoffice.security.bruteforce.LoginAttemptService;
import com.umdc.backoffice.security.jwt.JwtConfigProperties;
import com.umdc.backoffice.util.MessageUtil;
import com.umdc.backoffice.constant.types.AuditEventType;
import com.umdc.backoffice.v1.iam.audit.service.AuditEventService;
import com.umdc.backoffice.v1.session.mapper.UserAliasMapper;
import com.umdc.backoffice.v1.session.to.SessionEmailRequest;
import com.umdc.backoffice.v1.session.to.SessionRequest;
import com.umdc.backoffice.v1.session.to.SessionResponse;
import com.umdc.backoffice.v1.session.to.UserAliasTO;
import com.umdc.backoffice.v1.users.mapper.UserMapper;
import com.umdc.commons.exception.StandardException;
import com.umdc.commons.util.ValidatorCommonsUtil;
import com.umdc.persistence.general.domains.UserEntity;
import com.umdc.persistence.general.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service class for handling JWT operations related to sessions.
 */
@Service
@SuppressWarnings("PMD.GodClass") // Class has grown during Phase 1 security hardening; refactor recommended
public class SessionServiceImpl implements SessionService {

    private final JwtConfigProperties jwtConfigProperties;
    private final MessageUtil messageUtil;
    private final UserMapper userMapper;
    private final UserAliasMapper userAliasMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JtiDenyListService jtiDenyListService;
    private final LoginAttemptService loginAttemptService;
    private final AuditEventService auditEventService;
    private final SecretKey key;

    private static final String INVALID_APPLICATION_ID_MSG = "Invalid application ID";
    private static final String ACCOUNT_LOCKED_MSG = "Account temporarily locked due to too many failed attempts";
    private static final String INVALID_TOKEN_TYPE_MSG = "Token type is not valid for refresh";
    private static final String USER_INACTIVE_MSG = "User not found or account is inactive";
    private static final long REFRESH_TOKEN_MULTIPLIER = 7L;
    private static final long REFRESH_GRACE_SECONDS = 604_800L; // 7 days

    /**
     * Constructs a new SessionServiceImpl with the specified dependencies.
     *
     * @param jwtConfigProperties the JWT configuration properties
     * @param messageUtil         the message utility
     * @param userMapper          the user mapper
     * @param userAliasMapper     the user alias mapper
     * @param userRepository      the user repository
     * @param passwordEncoder     the password encoder for BCrypt verification
     * @param jtiDenyListService  the JTI deny-list service for token revocation
     * @param loginAttemptService the brute-force login attempt tracking service
     * @param auditEventService   the audit event service for recording security events
     */
    public SessionServiceImpl(JwtConfigProperties jwtConfigProperties, MessageUtil messageUtil,
                              UserMapper userMapper, UserAliasMapper userAliasMapper,
                              UserRepository userRepository, PasswordEncoder passwordEncoder,
                              JtiDenyListService jtiDenyListService,
                              LoginAttemptService loginAttemptService,
                              AuditEventService auditEventService) {
        this.jwtConfigProperties = jwtConfigProperties;
        this.userMapper = userMapper;
        this.userAliasMapper = userAliasMapper;
        this.userRepository = userRepository;
        this.messageUtil = messageUtil;
        this.passwordEncoder = passwordEncoder;
        this.jtiDenyListService = jtiDenyListService;
        this.loginAttemptService = loginAttemptService;
        this.auditEventService = auditEventService;
        this.key = generateKey();
    }

    /**
     * Loads a session based on the provided session request.
     *
     * @param sessionRequest the session request
     * @return a ResponseEntity containing the session response
     */
    @Override
    public ResponseEntity<SessionResponse> loadSession(SessionRequest sessionRequest) {
        String sessionId = UUID.randomUUID().toString();
        String sessionToken;
        ResponseEntity<SessionResponse> responseEntity;
        boolean isFieldsInvalid = false;
        Optional<UserEntity> optionalUserEntity;
        UserEntity userEntity;
        String messageError = "";
        Map<String, String> parameters;
        String ipAddress = extractIpAddress();
        String userAgent = extractUserAgent();

        // IF User and Service linked
        if (ValidatorCommonsUtil.esNulo(sessionRequest)) {
            messageError = messageUtil.getUserSolicitudNulaVacia();
            isFieldsInvalid = true;
        } else if (ValidatorCommonsUtil.esVacio(sessionRequest.alias())) {
            messageError = messageUtil.getUserAliasNuloVacio();
            isFieldsInvalid = true;
        } else if (ValidatorCommonsUtil.esVacio(sessionRequest.password())) {
            messageError = messageUtil.getUserClaveNulaVacia();
            isFieldsInvalid = true;
        } else if (Objects.isNull(sessionRequest.applicationId())) {
            messageError = INVALID_APPLICATION_ID_MSG;
            isFieldsInvalid = true;
        }

        if (isFieldsInvalid) {
            responseEntity = new ResponseEntity<>(new SessionResponse(messageError), HttpStatus.NOT_ACCEPTABLE);
            return responseEntity;
        }

        // Lookup user scoped to the target application
        optionalUserEntity = userRepository.findByAliasAndApplication(sessionRequest.alias(), sessionRequest.applicationId());
        if (optionalUserEntity.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        userEntity = optionalUserEntity.get();
        if (!userEntity.getActive()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Brute-force protection — check before validating password
        if (loginAttemptService.isLocked(sessionRequest.alias(), sessionRequest.applicationId())) {
            auditEventService.saveRecord(userEntity.getId(), sessionRequest.applicationId(), AuditEventType.ACCOUNT_LOCKED,
                    ipAddress, userAgent, buildDescription("Account locked", sessionRequest.alias()));
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new SessionResponse(ACCOUNT_LOCKED_MSG));
        }

        // Verify application membership, alias, and password (BCrypt)
        var appIdMatched = userEntity.getApplicationRoleUser().stream()
                .filter(aru -> aru.getApplication().getId().equals(sessionRequest.applicationId()))
                .map(aru -> aru.getApplication().getId()).findFirst();

        if (appIdMatched.isPresent() && appIdMatched.get().equals(sessionRequest.applicationId())
                && userEntity.getAlias().equals(sessionRequest.alias())
                && passwordEncoder.matches(sessionRequest.password(), userEntity.getPassword())) {
            var userAlias = loadUserAlias(userEntity.getId());
            if (Objects.nonNull(userAlias) && Objects.nonNull(userAlias.getRoles())) {
                parameters = new ConcurrentHashMap<>();
                parameters.put(AuthKey.ROLES_ID.value, userAlias.getRoles().toString());
                parameters.put(AuthKey.FIRSTNAME.value, userAlias.getFirstname());
                parameters.put(AuthKey.LASTNAME.value, userAlias.getLastname());
                sessionToken = generateSessionToken(sessionId, parameters);
                loginAttemptService.recordSuccess(sessionRequest.alias(), sessionRequest.applicationId());
                auditEventService.saveRecord(userEntity.getId(), sessionRequest.applicationId(), AuditEventType.LOGIN_SUCCESS,
                        ipAddress, userAgent, buildDescription("Login successful", sessionRequest.alias()));
                String refreshToken = generateRefreshToken(userEntity.getId());
                return ResponseEntity.ok(new SessionResponse(sessionToken, refreshToken));
            }
        }
        loginAttemptService.recordFailure(sessionRequest.alias(), sessionRequest.applicationId());
        auditEventService.saveRecord(userEntity.getId(), sessionRequest.applicationId(), AuditEventType.LOGIN_FAILURE,
                ipAddress, userAgent, buildDescription("Login failed", sessionRequest.alias()));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @Override
    public ResponseEntity<SessionResponse> loadSession(SessionEmailRequest sessionEmailRequest) {
        ResponseEntity<SessionResponse> responseEntity;
        boolean isFieldsInvalid = false;
        Optional<UserEntity> optionalUserEntity;
        UserEntity userEntity;
        String messageError = "";

        // IF User and Service linked
        if (ValidatorCommonsUtil.esNulo(sessionEmailRequest)) {
            messageError = messageUtil.getUserSolicitudNulaVacia();
            isFieldsInvalid = true;
        } else if (ValidatorCommonsUtil.esVacio(sessionEmailRequest.email())) {
            messageError = messageUtil.getUserCorreoNoValido();
            isFieldsInvalid = true;
        } else if (Objects.isNull(sessionEmailRequest.applicationId())) {
            messageError = INVALID_APPLICATION_ID_MSG;
            isFieldsInvalid = true;
        } else if (ValidatorCommonsUtil.esVacio(sessionEmailRequest.password())) {
            messageError = messageUtil.getUserClaveNulaVacia();
            isFieldsInvalid = true;
        }

        if (isFieldsInvalid) {
            responseEntity = new ResponseEntity<>(new SessionResponse(messageError), HttpStatus.NOT_ACCEPTABLE);
            return responseEntity;
        }

        optionalUserEntity = userRepository.findByEmailAndApplication(sessionEmailRequest.email(), sessionEmailRequest.applicationId());
        if (optionalUserEntity.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        userEntity = optionalUserEntity.get();
        if (!userEntity.getActive()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Brute-force protection — check before validating password
        if (loginAttemptService.isLocked(sessionEmailRequest.email(), sessionEmailRequest.applicationId())) {
            auditEventService.saveRecord(userEntity.getId(), sessionEmailRequest.applicationId(),
                    AuditEventType.ACCOUNT_LOCKED, null, null, null);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new SessionResponse(ACCOUNT_LOCKED_MSG));
        }

        var applicationId = userEntity.getApplicationRoleUser().stream()
                .filter(applicationRoleUser ->
                        applicationRoleUser.getApplication().getId().equals(sessionEmailRequest.applicationId())
                ).map(applicationRoleUser -> applicationRoleUser.getApplication().getId()).findFirst();
        if (applicationId.isPresent() && applicationId.get().equals(sessionEmailRequest.applicationId())
                && userEntity.getEmail().equals(sessionEmailRequest.email())
                && passwordEncoder.matches(sessionEmailRequest.password(), userEntity.getPassword())) {

            // IF User and Password validated (BCrypt)
            loginAttemptService.recordSuccess(sessionEmailRequest.email(), sessionEmailRequest.applicationId());
            auditEventService.saveRecord(userEntity.getId(), sessionEmailRequest.applicationId(),
                    AuditEventType.LOGIN_SUCCESS, null, null, null);
            String newToken = generateSessionToken(userEntity.getId());
            if (Objects.isNull(newToken)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new SessionResponse(messageUtil.getSinDatos()));
            }
            String newRefreshToken = generateRefreshToken(userEntity.getId());
            return ResponseEntity.ok(new SessionResponse(newToken, newRefreshToken));
        }
        loginAttemptService.recordFailure(sessionEmailRequest.email(), sessionEmailRequest.applicationId());
        auditEventService.saveRecord(userEntity.getId(), sessionEmailRequest.applicationId(),
                AuditEventType.LOGIN_FAILURE, null, null, null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    /**
     * Generates a session token with the specified username and parameters.
     * Adds {@code iss} and {@code aud} claims when configured.
     *
     * @param username   the username
     * @param parameters the parameters
     * @return the generated session token
     */
    public String generateSessionToken(String username, Map<String, String> parameters) {
        Map<String, Object> claims = new ConcurrentHashMap<>();
        // Required
        claims.put(AuthKey.JTI.value, UUID.randomUUID().toString());
        claims.put(AuthKey.TYPE.value, SESSION_TOKEN_KEY);
        claims.put(AuthKey.IAT.value, new Date());
        // Optional
        if (Objects.nonNull(parameters) && !parameters.isEmpty()) {
            claims.putAll(parameters);
        }

        var builder = Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtConfigProperties.getExpirationMs()));

        String issuer = jwtConfigProperties.getIssuer();
        if (Objects.nonNull(issuer) && !issuer.isEmpty()) {
            builder.issuer(issuer);
        }
        String audience = jwtConfigProperties.getAudience();
        if (Objects.nonNull(audience) && !audience.isEmpty()) {
            builder.audience().add(audience).and();
        }

        return builder.signWith(key).compact();
    }

    /**
     * {@inheritDoc}
     * Overrides the default to also reject tokens whose JTI is in the deny-list.
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
     * Revokes the given session token by adding its JTI to the deny-list
     * with the remaining TTL so it is evicted automatically on natural expiry.
     *
     * @param token the session token to revoke
     * @return 204 No Content on success; 400 Bad Request on invalid token
     */
    @Override
    public ResponseEntity<Void> revokeToken(String token) {
        try {
            if (ValidatorCommonsUtil.esVacio(token)) {
                return ResponseEntity.badRequest().build();
            }
            Claims claims = getTokenClaims(token);
            String jti = (String) claims.get(AuthKey.JTI.value);
            if (Objects.isNull(jti)) {
                return ResponseEntity.badRequest().build();
            }
            long remainingTtlSeconds = (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000L;
            if (remainingTtlSeconds > 0) {
                jtiDenyListService.denyJti(jti, remainingTtlSeconds);
            }
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Retrieves the claims from the specified token.
     *
     * @param token the token
     * @return the claims
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
     * Retrieves the username from the specified token.
     *
     * @param token the token
     * @return the username
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

    /**
     * Loads user alias details by user ID.
     *
     * @param userId the user ID
     * @return the user alias transfer object
     */
    private UserAliasTO loadUserAlias(UUID userId) {
        final var userInfo = userRepository.findUserInfo(userId);
        return Objects.nonNull(userInfo) ? userAliasMapper.toTarget(userMapper.toTarget(userInfo)) : null;
    }

    private String generateSessionToken(UUID userId) {
        Map<String, String> parameters;
        String sessionId = UUID.randomUUID().toString();
        var userAlias = loadUserAlias(userId);
        if (Objects.nonNull(userAlias) && Objects.nonNull(userAlias.getRoles())) {
            parameters = new ConcurrentHashMap<>();
            parameters.put(AuthKey.ALIAS.value, userAlias.getAlias());
            parameters.put(AuthKey.USER_ID.value, userAlias.getUserId().toString());
            parameters.put(AuthKey.ROLES_ID.value, userAlias.getRoles().toString());
            parameters.put(AuthKey.FIRSTNAME.value, userAlias.getFirstname());
            parameters.put(AuthKey.LASTNAME.value, userAlias.getLastname());
            return generateSessionToken(sessionId, parameters);
        }
        return null;
    }

    /**
     * Renews a session token by validating the current token and generating a new one.
     *
     * @param currentToken the current session token to be renewed
     * @return a ResponseEntity containing the session response with the new token
     */
    @Override
    public ResponseEntity<SessionResponse> renewToken(String currentToken) {
        try {
            // Validate the current token
            if (ValidatorCommonsUtil.esVacio(currentToken)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new SessionResponse(""));
            }

            // Check if token is valid and not expired
            if (!isValid(currentToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new SessionResponse(messageUtil.getUserClaveNoPermitida()));
            }

            return getSessionResponseResponseEntity(currentToken);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new SessionResponse(messageUtil.getUserInvalido()));
        }
    }

    private @NonNull ResponseEntity<SessionResponse> getSessionResponseResponseEntity(String currentToken) {
        // Extract user information from the current token
        Claims claims = getTokenClaims(currentToken);
        String username = claims.getSubject();

        // Get user details to generate new token
        String userIdStr = (String) claims.get(AuthKey.USER_ID.value);
        if (ValidatorCommonsUtil.esVacio(userIdStr)) {
            // If USER_ID is not in claims, try to find user by username (subject)
            UserEntity userEntity = userRepository.findByAlias(username);
            if (Objects.isNull(userEntity)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new SessionResponse(messageUtil.getUserCorreoNoExiste()));
            }

            if (!userEntity.getActive()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new SessionResponse(messageUtil.getUserInvalido()));
            }

            String newToken = generateSessionToken(userEntity.getId());
            if (Objects.nonNull(newToken)) {
                return ResponseEntity.ok(new SessionResponse(newToken));
            }
        } else {
            // Use USER_ID from claims
            UUID userId = UUID.fromString(userIdStr);
            Optional<UserEntity> userEntity = userRepository.findById(userId);

            if (userEntity.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new SessionResponse(messageUtil.getSinDatos()));
            }

            if (!userEntity.get().getActive()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new SessionResponse(messageUtil.getUserInvalido()));
            }

            String newToken = generateSessionToken(userId);
            if (Objects.nonNull(newToken)) {
                return ResponseEntity.ok(new SessionResponse(newToken));
            }
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new SessionResponse());
    }

    /**
     * Exchanges a valid or recently-expired refresh token for a new access token
     * and a new refresh token.
     * <p>
     * Tokens of type {@code refresh-token} are accepted. Tokens of type {@code session-token}
     * are also accepted for backward compatibility. A grace window of
     * {@value #REFRESH_GRACE_SECONDS} seconds (7 days) beyond the {@code exp} claim is
     * allowed so that a client with a just-expired refresh token can still obtain new tokens
     * without forcing a full re-login.
     * </p>
     *
     * @param refreshToken the refresh token string
     * @return 200 with new access + refresh tokens; 400 on empty input;
     * 401 on invalid/expired/wrong-type token; 500 on generation failure
     */
    @Override
    public ResponseEntity<SessionResponse> refreshSession(String refreshToken) {
        UUID userId = null;
        Claims claims;
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().body(new SessionResponse());
        }
        try {
            claims = getTokenClaims(refreshToken);
            // Accept refresh-token or Autorization (backward compat)
            Object tokenType = claims.get(AuthKey.TYPE.value);
            if (!REFRESH_TOKEN_KEY.equals(tokenType) && !AUTHORIZATION_HEADER.equals(tokenType)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new SessionResponse(INVALID_TOKEN_TYPE_MSG));
            }
            userId = getUserId(claims);
        } catch (Exception e) {
            if (e instanceof StandardException se) {
                return ResponseEntity.status(se.getCode())
                        .body(new SessionResponse(se.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new SessionResponse(e.getMessage()));
        }

        assert userId != null;
        Optional<UserEntity> userEntityOpt = userRepository.findById(userId);
        if (userEntityOpt.isEmpty() || !userEntityOpt.get().getActive()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new SessionResponse(USER_INACTIVE_MSG));
        }

        String newAccessToken = generateSessionToken(userId);
        String newRefreshToken = generateRefreshToken(userId);

        if (Objects.isNull(newAccessToken)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SessionResponse());
        }

        return ResponseEntity.ok(new SessionResponse(newAccessToken, newRefreshToken));
    }

    private @Nullable UUID getUserId(Claims claims) {
        // Resolve userId from the uid claim first, then fall back to subject
        String userIdStr = (String) claims.get(AuthKey.USER_ID.value);
        UUID userId = null;

        if (userIdStr != null && !userIdStr.isBlank()) {
            try {
                userId = UUID.fromString(userIdStr);
            } catch (IllegalArgumentException ignored) {
                // will fall through to alias lookup below
            }
        }

        if (Objects.isNull(userId)) {
            // Fall back to subject — may be a session-id string or alias
            String subject = claims.getSubject();
            if (subject == null || subject.isBlank()) {
                throw new StandardException(JWTMessage.CANNOT_IDENTIFY_USER);
            }
            try {
                userId = UUID.fromString(subject);
            } catch (IllegalArgumentException ignored) {
                // subject is an alias; look up by alias
                UserEntity aliasEntity = userRepository.findByAlias(subject);
                if (Objects.isNull(aliasEntity) || !aliasEntity.getActive()) {
                    throw new StandardException(JWTMessage.USER_INACTIVE);
                }
                userId = aliasEntity.getId();
            }
        }
        return userId;
    }

    private String extractIpAddress() {
        try {
            var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;
            HttpServletRequest request = attrs.getRequest();
            String forwarded = request.getHeader("X-Forwarded-For");
            return (forwarded != null && !forwarded.isBlank())
                    ? forwarded.split(",")[0].trim()
                    : request.getRemoteAddr();
        } catch (Exception e) {
            return null;
        }
    }

    private String extractUserAgent() {
        try {
            var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;
            return attrs.getRequest().getHeader("User-Agent");
        } catch (Exception e) {
            return null;
        }
    }

    private String buildDescription(String action, String alias) {
        return "{\"action\":\"" + action + "\",\"alias\":\"" + alias + "\"}";
    }

    /**
     * Generates a refresh token for the given user ID.
     * The refresh token carries {@code type=refresh-token} and has a TTL of
     * {@value #REFRESH_TOKEN_MULTIPLIER}× the configured access-token expiry.
     *
     * @param userId the user whose ID is set as subject and {@code uid} claim
     * @return the compact refresh token string
     */
    private String generateRefreshToken(UUID userId) {
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

        String issuer = jwtConfigProperties.getIssuer();
        if (Objects.nonNull(issuer) && !issuer.isEmpty()) {
            builder.issuer(issuer);
        }
        String audience = jwtConfigProperties.getAudience();
        if (Objects.nonNull(audience) && !audience.isEmpty()) {
            builder.audience().add(audience).and();
        }

        return builder.signWith(key).compact();
    }
}

