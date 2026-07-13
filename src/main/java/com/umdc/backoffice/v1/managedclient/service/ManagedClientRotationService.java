/*
 *  @(#)ManagedClientRotationService.java
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

import org.springframework.http.ResponseEntity;

import java.util.UUID;

/**
 * Service interface for MCAM client secret rotation operations.
 */
public interface ManagedClientRotationService {

    /**
     * Rotates the secret for the specified managed client.
     * <p>
     * The old secret hash is stored in Redis for the configured grace period so that
     * in-flight token requests using the previous secret continue to succeed.
     * Returns the new plaintext secret exactly once in the response body.
     * </p>
     *
     * @param clientId    the UUID of the managed client
     * @param requestorIp the IP address of the caller (recorded in the audit trail)
     * @return HTTP 200 with {@code ManagedClientSecretRotateResponse}, or HTTP 404 if client not found
     */
    ResponseEntity<?> rotateSecret(UUID clientId, String requestorIp);
}
