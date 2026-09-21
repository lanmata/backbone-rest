/*
 *  @(#)PermissionCheckControllerTest.java
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @author Luis Mata
 */
class PermissionCheckControllerTest {

    @Mock
    private PermissionCheckService permissionCheckService;

    private PermissionCheckController permissionCheckController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        permissionCheckController = new PermissionCheckController(permissionCheckService);
    }

    @Test
    @DisplayName("getService returns the injected PermissionCheckService")
    void getServiceReturnsInjectedService() {
        assertEquals(permissionCheckService, permissionCheckController.getService());
    }

    @Test
    @DisplayName("check delegates to PermissionCheckService.check")
    void checkDelegatesToService() {
        PermissionCheckRequest request = new PermissionCheckRequest("ROLE_ADMIN", UUID.randomUUID(), "a-session-token");
        ResponseEntity<PermissionCheckResponse> expected =
                ResponseEntity.ok(new PermissionCheckResponse(true, "ROLE_ADMIN", "granted"));
        when(permissionCheckService.check(request)).thenReturn(expected);

        var response = permissionCheckController.check(request);

        assertEquals(expected, response);
    }
}
