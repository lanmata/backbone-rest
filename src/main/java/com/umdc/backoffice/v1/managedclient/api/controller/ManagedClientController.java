/*
 *  @(#)ManagedClientController.java
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
package com.umdc.backoffice.v1.managedclient.api.controller;

import com.umdc.backoffice.v1.iam.tokens.api.to.TokenIntrospectRequest;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientCreateRequest;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientTokenRequest;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientUpdateRequest;
import com.umdc.backoffice.v1.managedclient.service.ManagedClientRotationService;
import com.umdc.backoffice.v1.managedclient.service.ManagedClientService;
import com.umdc.backoffice.v1.managedclient.service.ManagedClientTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/// Thin REST controller delegate for managed client CRUD operations.
/// All business logic is handled by {@link ManagedClientService}.
@RestController
@RequestMapping("/api/v1/managed-clients")
public class ManagedClientController implements ManagedClientApi {

    private final ManagedClientService managedClientService;
    private final ManagedClientTokenService managedClientTokenService;
    private final ManagedClientRotationService managedClientRotationService;

    /// Constructs a new {@code ManagedClientController}.
    ///
    /// @param managedClientService      the service handling MCAM CRUD logic
    /// @param managedClientTokenService the service handling M2M token lifecycle
    /// @param managedClientRotationService the service handling secret rotation
    public ManagedClientController(ManagedClientService managedClientService,
                                   ManagedClientTokenService managedClientTokenService,
                                   ManagedClientRotationService managedClientRotationService) {
        this.managedClientService = managedClientService;
        this.managedClientTokenService = managedClientTokenService;
        this.managedClientRotationService = managedClientRotationService;
    }

    /// Package-visible accessor used by {@link ManagedClientApi} default methods.
    ManagedClientService getManagedClientService() {
        return managedClientService;
    }

    /// Package-visible accessor used by {@link ManagedClientApi} default methods.
    ManagedClientTokenService getManagedClientTokenService() {
        return managedClientTokenService;
    }

    /// Package-visible accessor used by {@link ManagedClientApi} default methods.
    ManagedClientRotationService getManagedClientRotationService() {
        return managedClientRotationService;
    }

    @Override
    public ResponseEntity<?> registerManagedClient(
            @Valid @RequestBody ManagedClientCreateRequest request) {
        return managedClientService.registerClient(request);
    }

    @Override
    public ResponseEntity<?> listManagedClients(UUID applicationId, Boolean active,
                                                  int page, int size) {
        return managedClientService.listClients(applicationId, active, page, size);
    }

    @Override
    public ResponseEntity<?> getManagedClient(UUID clientId) {
        return managedClientService.getClient(clientId);
    }

    @Override
    public ResponseEntity<?> updateManagedClient(UUID clientId,
                                                   @Valid @RequestBody ManagedClientUpdateRequest request) {
        return managedClientService.updateClient(clientId, request);
    }

    @Override
    public ResponseEntity<?> deleteManagedClient(UUID clientId) {
        return managedClientService.deleteClient(clientId);
    }

    @Override
    public ResponseEntity<?> issueToken(@Valid @RequestBody ManagedClientTokenRequest request) {
        return managedClientTokenService.issueToken(request);
    }

    @Override
    public ResponseEntity<?> revokeAllTokens(UUID clientId) {
        return managedClientTokenService.revokeAllTokens(clientId);
    }

    @Override
    public ResponseEntity<?> introspectToken(@Valid @RequestBody TokenIntrospectRequest request) {
        return managedClientTokenService.introspectToken(request.token());
    }

    @Override
    public ResponseEntity<?> rotateManagedClientSecret(UUID clientId, HttpServletRequest httpRequest) {
        return managedClientRotationService.rotateSecret(clientId, httpRequest.getRemoteAddr());
    }
}
