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
import com.umdc.backoffice.security.jwt.JwtConfigProperties;
import com.umdc.backoffice.util.MessageUtil;
import com.umdc.backoffice.v1.session.mapper.UserAliasMapper;
import com.umdc.backoffice.v1.session.to.SessionEmailRequest;
import com.umdc.backoffice.v1.session.to.SessionRequest;
import com.umdc.backoffice.v1.session.to.SessionResponse;
import com.umdc.backoffice.v1.session.to.UserAliasTO;
import com.umdc.backoffice.v1.users.mapper.UserMapper;
import com.umdc.commons.util.ValidatorCommonsUtil;
import com.umdc.persistence.general.domains.UserEntity;
import com.umdc.persistence.general.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service class for handling JWT operations related to sessions.
 */
@Service
public class SessionServiceImpl implements SessionService {

    private final JwtConfigProperties jwtConfigProperties;
    private final MessageUtil messageUtil;
    private final UserMapper userMapper;
    private final UserAliasMapper userAliasMapper;
    private final UserRepository userRepository;
    private final SecretKey key;

    /**
     * Constructs a new SessionServiceImpl with the specified dependencies.
     *
     * @param jwtConfigProperties the JWT configuration properties
     * @param messageUtil         the message utility
     * @param userMapper          the user mapper
     * @param userAliasMapper     the user alias mapper
     * @param userRepository      the user repository
     */
    public SessionServiceImpl(JwtConfigProperties jwtConfigProperties, MessageUtil messageUtil,
                              UserMapper userMapper, UserAliasMapper userAliasMapper,
                              UserRepository userRepository) {
        this.jwtConfigProperties = jwtConfigProperties;
        this.userMapper = userMapper;
        this.userAliasMapper = userAliasMapper;
        this.userRepository = userRepository;
        this.messageUtil = messageUtil;
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
        UserEntity userEntity;
        String messageError = "";
        Map<String, String> parameters;

        // IF User and Service linked
        if (ValidatorCommonsUtil.esNulo(sessionRequest)) {
            messageError = messageUtil.getUserSolicitudNulaVacia();
            isFieldsInvalid = true;
        } else if (ValidatorCommonsUtil.esVacio(sessionRequest.getAlias())) {
            messageError = messageUtil.getUserAliasNuloVacio();
            isFieldsInvalid = true;
        } else if (ValidatorCommonsUtil.esVacio(sessionRequest.getPassword())) {
            messageError = messageUtil.getUserClaveNulaVacia();
            isFieldsInvalid = true;
        }

        if (isFieldsInvalid) {
            responseEntity = new ResponseEntity<>(new SessionResponse(messageError), HttpStatus.NOT_ACCEPTABLE);
            return responseEntity;
        }

        // IF User is active
        // To next: I have to include the Service ID validation
        userEntity = userRepository.findByAlias(sessionRequest.getAlias());
        if (Objects.isNull(userEntity)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if (!userEntity.getActive()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // IF User and Password validated
        if (userEntity.getAlias().equals(sessionRequest.getAlias()) && userEntity.getPassword().equals(sessionRequest.getPassword())) {
            var userAlias = loadUserAlias(userEntity.getId());
            if (Objects.nonNull(userAlias) && Objects.nonNull(userAlias.getRoles())) {
                parameters = new ConcurrentHashMap<>();
                parameters.put(AuthKey.ROLES_ID.value, userAlias.getRoles().toString());
                parameters.put(AuthKey.FIRSTNAME.value, userAlias.getFirstname());
                parameters.put(AuthKey.LASTNAME.value, userAlias.getLastname());
                sessionToken = generateSessionToken(sessionId, parameters);
                return ResponseEntity.ok(new SessionResponse(sessionToken));
            }
        }
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
            messageError = "Invalid application ID";
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

        var applicationId = userEntity.getApplicationRoleUser().stream()
                .filter(applicationRoleUser ->
                        applicationRoleUser.getApplication().getId().equals(sessionEmailRequest.applicationId())
                ).map(applicationRoleUser -> applicationRoleUser.getApplication().getId()).findFirst();
        if (applicationId.isPresent() && applicationId.get().equals(sessionEmailRequest.applicationId())
                && userEntity.getEmail().equals(sessionEmailRequest.email())
                && userEntity.getPassword().equals(sessionEmailRequest.password())) {

            // IF User and Password validated
            return ResponseEntity.ok(new SessionResponse(generateSessionToken(userEntity.getId())));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    /**
     * Generates a session token with the specified username and parameters.
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

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtConfigProperties.getExpirationMs()))
                .signWith(key)
                .compact();
    }

    /**
     * Retrieves the claims from the specified token.
     *
     * @param token the token
     * @return the claims
     */
    @Override
    public Claims getTokenClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
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

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new SessionResponse(messageUtil.getUserInvalido()));
        }
    }
}
