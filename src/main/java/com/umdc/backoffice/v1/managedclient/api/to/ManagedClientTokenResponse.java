/*
 *  @(#)ManagedClientTokenResponse.java
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

/// Response DTO returned after successful M2M token issuance.
/// <p>
/// Returned by {@code POST /api/v1/managed-clients/token} on success (HTTP 200).
/// The {@code accessToken} is a signed RS256 JWT with {@code type=M2M}.
/// </p>
///
/// @author Luis Antonio Mata
public class ManagedClientTokenResponse {

    /// The signed RS256 M2M access token.
    private String accessToken;

    /// Token type — always "Bearer".
    private String tokenType;

    /// Token TTL in seconds from the time of issuance.
    private long expiresIn;

    /// The granted OAuth2 scopes for this token.
    private List<String> scopes;

    /// UTC timestamp when the token was issued.
    private LocalDateTime issuedAt;

    /// Default constructor.
    public ManagedClientTokenResponse() {
        // Default constructor
    }

    /// Returns the signed access token.
    ///
    /// @return the accessToken
    public String getAccessToken() {
        return accessToken;
    }

    /// Sets the signed access token.
    ///
    /// @param accessToken the accessToken
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /// Returns the token type (always "Bearer").
    ///
    /// @return the tokenType
    public String getTokenType() {
        return tokenType;
    }

    /// Sets the token type.
    ///
    /// @param tokenType the tokenType
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    /// Returns the token TTL in seconds.
    ///
    /// @return the expiresIn
    public long getExpiresIn() {
        return expiresIn;
    }

    /// Sets the token TTL in seconds.
    ///
    /// @param expiresIn the expiresIn
    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    /// Returns the granted scopes list.
    ///
    /// @return the scopes
    public List<String> getScopes() {
        return scopes;
    }

    /// Sets the granted scopes list.
    ///
    /// @param scopes the scopes
    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    /// Returns the token issuance timestamp.
    ///
    /// @return the issuedAt
    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    /// Sets the token issuance timestamp.
    ///
    /// @param issuedAt the issuedAt
    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }
}

