/*
 *  @(#)PermissionCheckController.java
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
package com.umdc.backoffice.v1.iam.permissions.api.controller;

import com.umdc.backoffice.v1.iam.permissions.api.to.PermissionCheckRequest;
import com.umdc.backoffice.v1.iam.permissions.api.to.PermissionCheckResponse;
import com.umdc.backoffice.v1.iam.permissions.service.PermissionCheckService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

/// Thin REST controller for IAM permission-check operations.
/// Delegates all business logic to {@link PermissionCheckService}.
@RestController
@CrossOrigin(origins = "*")
public class PermissionCheckController implements PermissionCheckApi {

    private final PermissionCheckService permissionCheckService;

    /// Constructs a new {@code PermissionCheckController}.
    ///
    /// @param permissionCheckService the service that handles permission evaluation
    public PermissionCheckController(PermissionCheckService permissionCheckService) {
        this.permissionCheckService = permissionCheckService;
    }

    @Override
    public PermissionCheckService getService() {
        return this.permissionCheckService;
    }

    @Override
    public ResponseEntity<PermissionCheckResponse> check(PermissionCheckRequest request) {
        return permissionCheckService.check(request);
    }
}

