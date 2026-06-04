/*
 *  @(#)ManagedClientTokenRequest.java
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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/// Request DTO for issuing an M2M access token (client credentials grant).
/// <p>
/// Used as the request body for {@code POST /api/v1/managed-clients/token}.
/// This is a public endpoint — no bearer token required for this request.
/// </p>
///
/// @author Luis Antonio Mata
public class ManagedClientTokenRequest {

    /// UUID of the managed client requesting a token. Required.
    @NotNull
    private UUID clientId;

    /// Plaintext client secret. Required. Never log or store this value.
    @NotBlank
    private String clientSecret;

    /// Requested OAuth2 scopes. Required. Must be a subset of registered scopes.
    @NotEmpty
    private List<@NotBlank String> scopes;

    /// Default constructor.
    public ManagedClientTokenRequest() {
        // Default constructor
    }

    /// Returns the client UUID.
    ///
    /// @return the clientId
    public UUID getClientId() {
        return clientId;
    }

    /// Sets the client UUID.
    ///
    /// @param clientId the clientId
    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    /// Returns the plaintext client secret.
    ///
    /// @return the clientSecret
    public String getClientSecret() {
        return clientSecret;
    }

    /// Sets the plaintext client secret.
    ///
    /// @param clientSecret the clientSecret
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    /// Returns the requested scopes list.
    ///
    /// @return the scopes
    public List<String> getScopes() {
        return scopes;
    }

    /// Sets the requested scopes list.
    ///
    /// @param scopes the scopes
    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }
}

