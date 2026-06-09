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
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/// Implementation of {@link PermissionCheckService}.
/// <p>
/// Validates the provided session token and checks whether the {@code roles} claim
/// contains the requested permission string.
/// </p>
@Service
public class PermissionCheckServiceImpl implements PermissionCheckService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionCheckServiceImpl.class);

    private static final String INVALID_TOKEN_MSG = "Invalid session token";
    private static final String NO_ROLES_MSG = "No roles present in token";
    private static final String PERMISSION_GRANTED_MSG = "Permission granted";
    private static final String PERMISSION_DENIED_PREFIX = "Required permission '";
    private static final String PERMISSION_DENIED_SUFFIX = "' not present in token";

    private final SessionService sessionService;

    /// Constructs a new {@code PermissionCheckServiceImpl}.
    ///
    /// @param sessionService the session service used for token validation and claims extraction
    public PermissionCheckServiceImpl(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    /// {@inheritDoc}
    @Override
    public ResponseEntity<PermissionCheckResponse> check(PermissionCheckRequest request) {
        String token = request.sessionToken();
        String permission = request.permission();

        LOGGER.debug("Permission check requested — permission='{}'", permission);

        if (!sessionService.isValid(token)) {
            LOGGER.warn("Permission check rejected — invalid session token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new PermissionCheckResponse(false, permission, INVALID_TOKEN_MSG));
        }

        Claims claims = sessionService.getTokenClaims(token);
        Object rolesObj = claims.get(AuthKey.ROLES_ID.value);

        if (rolesObj == null) {
            LOGGER.debug("No roles claim found in token — permission '{}' denied", permission);
            return ResponseEntity.ok(
                    new PermissionCheckResponse(false, permission, NO_ROLES_MSG));
        }

        boolean granted = RolesClaimParser.parseRoles(rolesObj).contains(permission);

        String reason = granted
                ? PERMISSION_GRANTED_MSG
                : PERMISSION_DENIED_PREFIX + permission + PERMISSION_DENIED_SUFFIX;

        LOGGER.debug("Permission check result — permission='{}', granted={}", permission, granted);
        return ResponseEntity.ok(new PermissionCheckResponse(granted, permission, reason));
    }
}
