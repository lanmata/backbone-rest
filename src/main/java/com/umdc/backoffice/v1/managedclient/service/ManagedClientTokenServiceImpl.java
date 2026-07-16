/*
 *  @(#)ManagedClientTokenServiceImpl.java
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
package com.umdc.backoffice.v1.managedclient.service;

import com.umdc.backoffice.property.ManagementAuthenticatorProperties;
import com.umdc.backoffice.property.SecurityProperties;
import com.umdc.backoffice.security.exception.CertificateSecurityException;
import com.umdc.backoffice.util.KeystoreUtil;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientErrorResponse;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientTokenIntrospectResponse;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientTokenRequest;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientTokenResponse;
import com.umdc.commons.general.pojo.AuditEventType;
import com.umdc.persistence.general.domains.ManagedClientEntity;
import com.umdc.persistence.general.repositories.ManagedClientRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of {@link ManagedClientTokenService} providing RS256 M2M token issuance,
 * revocation, and introspection (MCAM Phase 3).
 */
@Service
public class ManagedClientTokenServiceImpl implements ManagedClientTokenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagedClientTokenServiceImpl.class);
    private static final String TOKEN_TYPE_M2M = "M2M";
    private static final String ISSUER = "backbone-rest";
    private static final String CLAIM_SCOPES = "scopes";
    private static final String CLAIM_TYPE = "type";
    private static final String CLAIM_SUCCESS = "SUCCESS";


    private final ManagedClientRepository repository;
    private final ManagedClientSecretHashService secretHashService;
    private final ManagedClientRedisService redisService;
    private final ManagedClientAuditService auditService;
    private final SecurityProperties securityProperties;
    private final KeystoreUtil keystoreUtil;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    /**
     * Constructs a new {@code ManagedClientTokenServiceImpl}.
     *
     * @param repository        the managed client repository
     * @param secretHashService the BCrypt hash comparison service
     * @param redisService      the Redis token lifecycle service
     * @param auditService      the audit event recording service
     * @param securityProperties the security configuration properties
     * @param keystoreUtil      the keystore utility for key extraction
     */
    public ManagedClientTokenServiceImpl(ManagedClientRepository repository,
                                         ManagedClientSecretHashService secretHashService,
                                         ManagedClientRedisService redisService,
                                         ManagedClientAuditService auditService,
                                         SecurityProperties securityProperties,
                                         KeystoreUtil keystoreUtil) {
        this.repository = repository;
        this.secretHashService = secretHashService;
        this.redisService = redisService;
        this.auditService = auditService;
        this.securityProperties = securityProperties;
        this.keystoreUtil = keystoreUtil;
    }

    /**
     * Loads the RS256 private and public keys from the MCAM keystore.
     * Fails fast at application startup if the keys cannot be loaded.
     *
     * @throws CertificateSecurityException if key extraction fails
     */
    @PostConstruct
    void init() throws CertificateSecurityException {
        ManagementAuthenticatorProperties mcam = securityProperties.getManagementAuthenticator();
        this.privateKey = keystoreUtil.loadPrivateKey(mcam.getKeystore(), mcam.getKeyAlias());
        this.publicKey = keystoreUtil.loadPublicKey(mcam.getKeystore(), mcam.getKeyAlias());
        LOGGER.info("MCAM RS256 keys loaded for alias='{}'", mcam.getKeyAlias());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<?> issueToken(ManagedClientTokenRequest request) {
        UUID clientId = request.getClientId();
        LOGGER.debug("Token request for clientId='{}'", clientId);

        Optional<ManagedClientEntity> found = repository.findById(clientId);
        if (found.isEmpty() || !found.get().isActive()) {
            auditService.save(clientId, AuditEventType.CLIENT_TOKEN_ISSUE_FAILED, null, "INVALID_CLIENT", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ManagedClientErrorResponse("invalid_client", "Client not found or inactive.", null));
        }

        ManagedClientEntity entity = found.get();
        boolean currentMatch = secretHashService.matchesWithConstantTime(
                request.getClientSecret(), entity.getSecretHash());
        boolean graceMatch = false;
        Optional<String> graceHash = redisService.getGraceSecret(clientId);
        if (graceHash.isPresent()) {
            graceMatch = secretHashService.matchesWithConstantTime(request.getClientSecret(), graceHash.get());
        }
        if (!currentMatch && !graceMatch) {
            auditService.save(clientId, AuditEventType.CLIENT_TOKEN_ISSUE_FAILED, null, "INVALID_SECRET", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ManagedClientErrorResponse("invalid_client", "Invalid client credentials.", null));
        }

        List<String> registeredScopes = entity.getScopes();
        List<String> requestedScopes = request.getScopes();
        if (registeredScopes == null || !registeredScopes.containsAll(requestedScopes)) {
            auditService.save(clientId, AuditEventType.CLIENT_TOKEN_ISSUE_FAILED, null, "INVALID_SCOPE", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ManagedClientErrorResponse("invalid_scope",
                            "Requested scopes exceed registered scopes.", null));
        }

        ManagementAuthenticatorProperties mcam = securityProperties.getManagementAuthenticator();
        if (!redisService.checkAndIncrementRateLimit(clientId, mcam.getRateLimitRpm())) {
            auditService.save(clientId, AuditEventType.CLIENT_TOKEN_ISSUE_FAILED, null, "RATE_LIMITED", null);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ManagedClientErrorResponse("rate_limit_exceeded",
                            "Token issuance rate limit exceeded.", null));
        }

        long ttlSeconds = mcam.getTokenTtlSeconds();
        Date now = new Date();
        Date expiry = new Date(now.getTime() + ttlSeconds * 1000L);
        String jti = UUID.randomUUID().toString();

        String rawToken = Jwts.builder()
                .subject(clientId.toString())
                .issuer(ISSUER)
                .issuedAt(now)
                .expiration(expiry)
                .id(jti)
                .claim(CLAIM_SCOPES, requestedScopes)
                .claim(CLAIM_TYPE, TOKEN_TYPE_M2M)
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();

        redisService.storeToken(jti, clientId, requestedScopes, ttlSeconds);
        redisService.addClientTokenJti(jti, clientId);
        auditService.save(clientId, AuditEventType.CLIENT_TOKEN_ISSUED, null, CLAIM_SUCCESS, null);

        ManagedClientTokenResponse response = new ManagedClientTokenResponse();
        response.setAccessToken(rawToken);
        response.setTokenType("Bearer");
        response.setExpiresIn(ttlSeconds);
        response.setScopes(requestedScopes);
        response.setIssuedAt(LocalDateTime.ofInstant(now.toInstant(), ZoneOffset.UTC));

        LOGGER.debug("M2M token issued — jti='{}' clientId='{}'", jti, clientId);
        return ResponseEntity.ok(response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<?> revokeAllTokens(UUID clientId) {
        if (!repository.existsById(clientId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ManagedClientErrorResponse("not_found", "Managed client not found.", clientId.toString()));
        }

        long ttl = securityProperties.getManagementAuthenticator().getTokenTtlSeconds();
        Set<String> jtis = redisService.getClientTokenJtis(clientId);
        for (String jti : jtis) {
            redisService.revokeToken(jti, ttl);
        }
        auditService.save(clientId, AuditEventType.CLIENT_TOKEN_REVOKED, null, CLAIM_SUCCESS, null);
        LOGGER.debug("Revoked {} tokens for clientId='{}'", jtis.size(), clientId);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<?> introspectToken(String rawToken) {
        ManagedClientTokenIntrospectResponse inactive = new ManagedClientTokenIntrospectResponse();
        inactive.setActive(false);

        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(rawToken)
                    .getPayload();
        } catch (JwtException e) {
            LOGGER.debug("Token introspection — invalid JWT: {}", e.getMessage());
            return ResponseEntity.ok(inactive);
        }

        String jti = claims.getId();
        if (!redisService.isTokenStored(jti) || redisService.isRevoked(jti)) {
            LOGGER.debug("Token introspection — jti='{}' not active in Redis", jti);
            return ResponseEntity.ok(inactive);
        }

        String clientIdStr = claims.getSubject();
        Optional<ManagedClientEntity> found = repository.findById(UUID.fromString(clientIdStr));
        if (found.isEmpty()) {
            return ResponseEntity.ok(inactive);
        }

        @SuppressWarnings("unchecked")
        List<String> scopes = claims.get(CLAIM_SCOPES, List.class);
        Date expDate = claims.getExpiration();
        Date iatDate = claims.getIssuedAt();

        ManagedClientTokenIntrospectResponse active = new ManagedClientTokenIntrospectResponse();
        active.setActive(true);
        active.setClientId(clientIdStr);
        active.setClientName(found.get().getName());
        active.setScopes(scopes);
        active.setIssuer(claims.getIssuer());
        active.setExp(expDate != null ? expDate.toInstant().getEpochSecond() : null);
        active.setIat(iatDate != null ? iatDate.toInstant().getEpochSecond() : null);
        active.setJti(jti);

        auditService.save(UUID.fromString(clientIdStr), AuditEventType.CLIENT_INTROSPECTION_CALLED,
                null, CLAIM_SUCCESS, null);

        return ResponseEntity.ok(active);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTokenActive(String rawToken) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(rawToken)
                    .getPayload();
            String jti = claims.getId();
            return redisService.isTokenStored(jti) && !redisService.isRevoked(jti);
        } catch (JwtException e) {
            LOGGER.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
}
