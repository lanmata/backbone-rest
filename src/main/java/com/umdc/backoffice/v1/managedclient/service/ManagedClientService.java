/*
 *  @(#)ManagedClientService.java
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

import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientCreateRequest;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientUpdateRequest;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

/**
 * Service interface for managed client CRUD operations.
 */
public interface ManagedClientService {

    /**
     * Registers a new managed client. Returns 201 with plaintext clientSecret on success.
     *
     * @param request the registration request
     * @return 201 with {@code ManagedClientCreateResponse}, or 409 on duplicate name/application
     */
    ResponseEntity<?> registerClient(ManagedClientCreateRequest request);

    /**
     * Lists managed clients with optional filters. Returns 204 if no results.
     *
     * @param applicationId optional filter by owning application UUID
     * @param active        optional filter by active flag
     * @param page          zero-based page index
     * @param size          page size
     * @return 200 with client list, or 204 if no clients match
     */
    ResponseEntity<?> listClients(UUID applicationId, Boolean active, int page, int size);

    /**
     * Returns a single managed client by ID. Never exposes secret material.
     *
     * @param clientId the client UUID
     * @return 200 with {@code ManagedClientTO}, or 404 if not found
     */
    ResponseEntity<?> getClient(UUID clientId);

    /**
     * Partially updates a managed client. Returns 200 on success.
     *
     * @param clientId the client UUID
     * @param request  the partial update request
     * @return 200 with updated {@code ManagedClientTO}, or 404 if not found
     */
    ResponseEntity<?> updateClient(UUID clientId, ManagedClientUpdateRequest request);

    /**
     * Deletes a managed client. Revokes all active tokens before deletion.
     *
     * @param clientId the client UUID
     * @return 204 on success, or 404 if not found
     */
    ResponseEntity<?> deleteClient(UUID clientId);
}
