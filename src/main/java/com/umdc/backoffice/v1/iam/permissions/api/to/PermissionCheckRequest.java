/*
 *  @(#)PermissionCheckRequest.java
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
package com.umdc.backoffice.v1.iam.permissions.api.to;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

/**
 * Request body for the permission check endpoint.
 *
 * @param permission    the permission string to check (e.g. {@code ROLE_ADMIN})
 * @param applicationId the application context for the check; may be {@code null}
 * @param sessionToken  the session-token JWT of the requesting user
 */
public record PermissionCheckRequest(
        @NotBlank String permission,
        UUID applicationId,
        @NotBlank String sessionToken
) {
}

