/*
 *  @(#)ManagedClientSecretRotateResponse.java
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
import java.util.UUID;

/// Response DTO returned after a successful secret rotation.
/// <p>
/// Contains the new plaintext {@code clientSecret} — returned ONCE ONLY.
/// The caller must store it immediately. The old secret remains valid for
/// {@code gracePeriodSeconds} to allow in-flight requests to complete.
/// </p>
///
/// @author Luis Antonio Mata
public class ManagedClientSecretRotateResponse {

    /// UUID of the managed client whose secret was rotated.
    private UUID clientId;

    /// New plaintext client secret. Returned exactly once — store immediately.
    private String clientSecret;

    /// Number of seconds the old secret remains valid (grace period).
    private long gracePeriodSeconds;

    /// UTC timestamp when the rotation occurred.
    private LocalDateTime rotatedAt;

    /// Default constructor.
    public ManagedClientSecretRotateResponse() {
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

    /// Returns the new plaintext client secret (once only).
    ///
    /// @return the clientSecret
    public String getClientSecret() {
        return clientSecret;
    }

    /// Sets the new plaintext client secret.
    ///
    /// @param clientSecret the clientSecret
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    /// Returns the grace period duration in seconds.
    ///
    /// @return the gracePeriodSeconds
    public long getGracePeriodSeconds() {
        return gracePeriodSeconds;
    }

    /// Sets the grace period duration in seconds.
    ///
    /// @param gracePeriodSeconds the gracePeriodSeconds
    public void setGracePeriodSeconds(long gracePeriodSeconds) {
        this.gracePeriodSeconds = gracePeriodSeconds;
    }

    /// Returns the rotation timestamp.
    ///
    /// @return the rotatedAt
    public LocalDateTime getRotatedAt() {
        return rotatedAt;
    }

    /// Sets the rotation timestamp.
    ///
    /// @param rotatedAt the rotatedAt
    public void setRotatedAt(LocalDateTime rotatedAt) {
        this.rotatedAt = rotatedAt;
    }
}

