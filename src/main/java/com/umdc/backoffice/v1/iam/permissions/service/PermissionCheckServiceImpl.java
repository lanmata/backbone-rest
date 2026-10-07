/*
 *  @(#)PermissionCheckServiceImpl.java
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
package com.umdc.backoffice.v1.iam.permissions.service;

import com.umdc.backoffice.constant.keys.AuthKey;
import com.umdc.backoffice.security.util.RolesClaimParser;
import com.umdc.backoffice.v1.iam.permissions.api.to.PermissionCheckRequest;
import com.umdc.backoffice.v1.iam.permissions.api.to.PermissionCheckResponse;
import com.umdc.backoffice.v1.session.services.SessionService;
import com.umdc.backoffice.v1.users.service.ApplicationRoleUserGraphLookupService;
import com.umdc.persistence.general.domains.ApplicationRoleUserEntity;
import com.umdc.persistence.general.domains.FeatureEntity;
import com.umdc.persistence.general.domains.RoleEntity;
import com.umdc.persistence.general.domains.RoleFeatureEntity;
import com.umdc.persistence.general.repositories.ApplicationRoleUserRepository;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of {@link PermissionCheckService}.
 * <p>
 * Validates the provided session token, then evaluates the requested permission one of
 * two ways:
 * </p>
 * <ul>
 *   <li><strong>Application-scoped</strong> (when {@code applicationId} is present on the
 *   request <em>and</em> the token's {@code uid} claim resolves to a user): looks up the
 *   caller's single {@code ApplicationRoleUserEntity} row for that specific application
 *   (the existing {@code general.application_role_user} ACL — see
 *   {@link ApplicationRoleUserRepository}) and grants only if that row is active and its
 *   role name, or one of the role's active granted features, matches {@code permission}.</li>
 *   <li><strong>Global fallback</strong> (no {@code applicationId}, or the token carries no
 *   resolvable {@code uid}): preserves the original behavior — checks the flattened
 *   {@code roles} claim on the token, which is not scoped to any single application.</li>
 * </ul>
 */
@Service
public class PermissionCheckServiceImpl implements PermissionCheckService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionCheckServiceImpl.class);

    private static final String INVALID_TOKEN_MSG = "Invalid session token";
    private static final String NO_ROLES_MSG = "No roles present in token";
    private static final String NO_APPLICATION_ACCESS_MSG = "User has no active role in this application";
    private static final String PERMISSION_GRANTED_MSG = "Permission granted";
    private static final String PERMISSION_DENIED_PREFIX = "Required permission '";
    private static final String PERMISSION_DENIED_SUFFIX = "' not present in token";

    private final SessionService sessionService;
    private final ApplicationRoleUserGraphLookupService applicationRoleUserGraphLookupService;

    /**
     * Constructs a new {@code PermissionCheckServiceImpl}.
     *
     * @param sessionService                         the session service used for token validation and claims extraction
     * @param applicationRoleUserGraphLookupService safe application-role-user lookups (user,
     *                                               application and role — including the role's own
     *                                               granted features — eagerly loaded); see
     *                                               {@link ApplicationRoleUserGraphLookupService}
     *                                               for why this replaces
     *                                               {@link ApplicationRoleUserRepository#findByUserAndApplication}
     */
    public PermissionCheckServiceImpl(SessionService sessionService,
                                      ApplicationRoleUserGraphLookupService applicationRoleUserGraphLookupService) {
        this.sessionService = sessionService;
        this.applicationRoleUserGraphLookupService = applicationRoleUserGraphLookupService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<PermissionCheckResponse> check(PermissionCheckRequest request) {
        String token = request.sessionToken();
        String permission = request.permission();

        LOGGER.debug("Permission check requested — permission='{}', applicationId={}", permission, request.applicationId());

        if (!sessionService.isValid(token)) {
            LOGGER.warn("Permission check rejected — invalid session token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new PermissionCheckResponse(false, permission, INVALID_TOKEN_MSG));
        }

        Claims claims = sessionService.getTokenClaims(token);

        if (Objects.nonNull(request.applicationId())) {
            UUID userId = extractUserId(claims);
            if (Objects.nonNull(userId)) {
                return checkApplicationScoped(userId, request.applicationId(), permission);
            }
            LOGGER.debug("applicationId requested but token carries no resolvable uid — falling back to global roles claim");
        }

        return checkGlobalRolesClaim(claims, permission);
    }

    /**
     * Evaluates the permission against the caller's application-specific role, using the
     * existing {@code general.application_role_user} ACL — a real, per-application answer,
     * as opposed to the token's flattened cross-application {@code roles} claim.
     */
    private ResponseEntity<PermissionCheckResponse> checkApplicationScoped(UUID userId, UUID applicationId, String permission) {
        Optional<ApplicationRoleUserEntity> linkOpt =
                applicationRoleUserGraphLookupService.findByUserAndApplicationWithGraph(userId, applicationId);

        if (linkOpt.isEmpty() || !Boolean.TRUE.equals(linkOpt.get().getActive())) {
            LOGGER.debug("No active application_role_user link — userId={}, applicationId={}", userId, applicationId);
            return ResponseEntity.ok(new PermissionCheckResponse(false, permission, NO_APPLICATION_ACCESS_MSG));
        }

        boolean granted = roleGrants(linkOpt.get().getRole(), permission);
        String reason = granted ? PERMISSION_GRANTED_MSG : deniedReason(permission);

        LOGGER.debug("Application-scoped permission check result — permission='{}', applicationId={}, granted={}",
                permission, applicationId, granted);
        return ResponseEntity.ok(new PermissionCheckResponse(granted, permission, reason));
    }

    /**
     * Preserves the original global behavior: checks the token's flattened {@code roles}
     * claim, which is not scoped to any single application.
     */
    private ResponseEntity<PermissionCheckResponse> checkGlobalRolesClaim(Claims claims, String permission) {
        Object rolesObj = claims.get(AuthKey.ROLES_ID.value);

        if (Objects.isNull(rolesObj)) {
            LOGGER.debug("No roles claim found in token — permission '{}' denied", permission);
            return ResponseEntity.ok(new PermissionCheckResponse(false, permission, NO_ROLES_MSG));
        }

        boolean granted = RolesClaimParser.parseRoles(rolesObj).contains(permission);
        String reason = granted ? PERMISSION_GRANTED_MSG : deniedReason(permission);

        LOGGER.debug("Global permission check result — permission='{}', granted={}", permission, granted);
        return ResponseEntity.ok(new PermissionCheckResponse(granted, permission, reason));
    }

    /**
     * A role grants a permission when the role itself is active and either its own name,
     * or the name of one of its active granted features, matches (case-insensitively).
     */
    private boolean roleGrants(RoleEntity role, String permission) {
        if (Objects.isNull(role) || !role.isActive()) {
            return false;
        }
        return permission.equalsIgnoreCase(role.getName())
                || activeFeatureNames(role).stream().anyMatch(permission::equalsIgnoreCase);
    }

    private Set<String> activeFeatureNames(RoleEntity role) {
        Set<RoleFeatureEntity> roleFeatures = role.getRoleFeatures();
        if (Objects.isNull(roleFeatures)) {
            return Set.of();
        }
        return roleFeatures.stream()
                .filter(rf -> Boolean.TRUE.equals(rf.getActive()))
                .map(RoleFeatureEntity::getFeature)
                .filter(Objects::nonNull)
                .map(FeatureEntity::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private String deniedReason(String permission) {
        return PERMISSION_DENIED_PREFIX + permission + PERMISSION_DENIED_SUFFIX;
    }

    /**
     * Resolves the {@code uid} claim into a {@link UUID}, returning {@code null} when the
     * claim is absent or not a well-formed UUID (e.g. a token minted before this claim was added).
     */
    private UUID extractUserId(Claims claims) {
        Object raw = claims.get(AuthKey.USER_ID.value);
        if (Objects.isNull(raw)) {
            return null;
        }
        try {
            return UUID.fromString(raw.toString());
        } catch (IllegalArgumentException _) {
            LOGGER.debug("uid claim is not a well-formed UUID: '{}'", raw);
            return null;
        }
    }
}
