/*
 *  @(#)ManagedClientCreateResponse.java
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
package com.umdc.backoffice.v1.managedclient.api.to;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO returned after successful managed client registration.
 * <p>
 * Contains the plaintext {@code clientSecret} — this is the ONLY time the
 * secret is ever returned. The caller must store it immediately and securely.
 * Subsequent reads via {@code GET /api/v1/managed-clients/{clientId}} will
 * never return any secret material.
 * </p>
 *
 * @author Luis Antonio Mata
 */
public class ManagedClientCreateResponse {

    /**
     * UUID assigned to the newly registered client.
     */
    private UUID clientId;

    /**
     * Plaintext client secret. Returned exactly once — store immediately.
     */
    private String clientSecret;

    /**
     * Human-readable name of the registered client.
     */
    private String name;

    /**
     * UUID of the owning application.
     */
    private UUID applicationId;

    /**
     * Authorised OAuth2 scopes for this client.
     */
    private List<String> scopes;

    /**
     * Whether the client is active.
     */
    private boolean active;

    /**
     * UTC timestamp when the client was created.
     */
    private LocalDateTime createdAt;

    /**
     * Default constructor.
     */
    public ManagedClientCreateResponse() {
        // Default constructor
    }

    /**
     * Returns the client UUID.
     *
     * @return the clientId
     */
    public UUID getClientId() {
        return clientId;
    }

    /**
     * Sets the client UUID.
     *
     * @param clientId the clientId
     */
    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    /**
     * Returns the plaintext client secret (once only).
     *
     * @return the clientSecret
     */
    public String getClientSecret() {
        return clientSecret;
    }

    /**
     * Sets the plaintext client secret.
     *
     * @param clientSecret the clientSecret
     */
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    /**
     * Returns the client name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the client name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the owning application UUID.
     *
     * @return the applicationId
     */
    public UUID getApplicationId() {
        return applicationId;
    }

    /**
     * Sets the owning application UUID.
     *
     * @param applicationId the applicationId
     */
    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * Returns the authorised scopes list.
     *
     * @return the scopes
     */
    public List<String> getScopes() {
        return scopes;
    }

    /**
     * Sets the authorised scopes list.
     *
     * @param scopes the scopes
     */
    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    /**
     * Returns whether the client is active.
     *
     * @return {@code true} if active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the active flag.
     *
     * @param active {@code true} if active
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Returns the creation timestamp.
     *
     * @return the createdAt
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp.
     *
     * @param createdAt the createdAt
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

