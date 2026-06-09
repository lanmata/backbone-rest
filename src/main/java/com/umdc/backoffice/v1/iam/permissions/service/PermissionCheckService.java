/*
 *  @(#)PermissionCheckService.java
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

import com.umdc.backoffice.v1.iam.permissions.api.to.PermissionCheckRequest;
import com.umdc.backoffice.v1.iam.permissions.api.to.PermissionCheckResponse;
import org.springframework.http.ResponseEntity;

/// Service contract for evaluating whether a session token carries a given permission.
public interface PermissionCheckService {

    /// Evaluates the permission check request.
    ///
    /// @param request the check request containing the session token and the permission to evaluate
    /// @return a {@link ResponseEntity} wrapping a {@link PermissionCheckResponse}
    ResponseEntity<PermissionCheckResponse> check(PermissionCheckRequest request);
}

