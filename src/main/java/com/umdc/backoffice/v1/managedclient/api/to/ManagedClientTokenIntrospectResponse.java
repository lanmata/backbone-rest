/*
 *  @(#)ManagedClientTokenIntrospectResponse.java
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

import java.util.List;

/// Response DTO for the M2M token introspection endpoint.
/// <p>
/// Returned by {@code POST /api/v1/managed-clients/introspect}.
/// Per RFC 7662, this endpoint always returns HTTP 200. When the token is
/// invalid, expired, or revoked, {@code active} is {@code false} and all
/// other fields are {@code null}.
/// </p>
///
/// @author Luis Antonio Mata
public class ManagedClientTokenIntrospectResponse {

    /// Whether the token is currently active (valid, not expired, not revoked).
    private boolean active;

    /// Subject claim — the managed client UUID (null when active=false).
    private String clientId;

    /// Human-readable client name (null when active=false).
    private String clientName;

    /// Granted scopes for the token (null when active=false).
    private List<String> scopes;

    /// Issuer claim from the token (null when active=false).
    private String issuer;

    /// Expiry time as Unix epoch seconds (null when active=false).
    private Long exp;

    /// Issued-at time as Unix epoch seconds (null when active=false).
    private Long iat;

    /// Unique token identifier (null when active=false).
    private String jti;

    /// Default constructor.
    public ManagedClientTokenIntrospectResponse() {
        // Default constructor
    }

    /// Returns whether the token is active.
    ///
    /// @return {@code true} if the token is valid, not expired, and not revoked
    public boolean isActive() {
        return active;
    }

    /// Sets the active flag.
    ///
    /// @param active the active flag
    public void setActive(boolean active) {
        this.active = active;
    }

    /// Returns the client UUID (subject claim).
    ///
    /// @return the clientId
    public String getClientId() {
        return clientId;
    }

    /// Sets the client UUID.
    ///
    /// @param clientId the clientId
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /// Returns the client name.
    ///
    /// @return the clientName
    public String getClientName() {
        return clientName;
    }

    /// Sets the client name.
    ///
    /// @param clientName the clientName
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    /// Returns the granted scopes.
    ///
    /// @return the scopes
    public List<String> getScopes() {
        return scopes;
    }

    /// Sets the granted scopes.
    ///
    /// @param scopes the scopes
    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    /// Returns the issuer claim.
    ///
    /// @return the issuer
    public String getIssuer() {
        return issuer;
    }

    /// Sets the issuer claim.
    ///
    /// @param issuer the issuer
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    /// Returns the expiry time as Unix epoch seconds.
    ///
    /// @return the exp
    public Long getExp() {
        return exp;
    }

    /// Sets the expiry time as Unix epoch seconds.
    ///
    /// @param exp the exp
    public void setExp(Long exp) {
        this.exp = exp;
    }

    /// Returns the issued-at time as Unix epoch seconds.
    ///
    /// @return the iat
    public Long getIat() {
        return iat;
    }

    /// Sets the issued-at time as Unix epoch seconds.
    ///
    /// @param iat the iat
    public void setIat(Long iat) {
        this.iat = iat;
    }

    /// Returns the unique token identifier.
    ///
    /// @return the jti
    public String getJti() {
        return jti;
    }

    /// Sets the unique token identifier.
    ///
    /// @param jti the jti
    public void setJti(String jti) {
        this.jti = jti;
    }
}

