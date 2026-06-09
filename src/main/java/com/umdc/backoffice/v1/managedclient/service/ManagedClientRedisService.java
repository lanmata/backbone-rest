/*
 *  @(#)ManagedClientRedisService.java
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

import java.util.List;
import java.util.Set;
import java.util.UUID;

/// Redis operations for the MCAM M2M token lifecycle.
/// <p>
/// Key schema:
/// <ul>
///   <li>{@code mcam:token:{jti}} — token payload with TTL</li>
///   <li>{@code mcam:revoked:{jti}} — revocation marker with TTL</li>
///   <li>{@code mcam:client-tokens:{clientId}} — per-client JTI index (set)</li>
///   <li>{@code mcam:grace:{clientId}} — secret rotation grace marker with TTL</li>
///   <li>{@code mcam:ratelimit:{clientId}} — issuance rate counter (60 s window)</li>
/// </ul>
/// </p>
public interface ManagedClientRedisService {

    /// Stores the token payload for a newly issued JTI.
    ///
    /// @param jti        the unique token identifier
    /// @param clientId   the issuing managed client
    /// @param scopes     the granted scopes
    /// @param ttlSeconds time-to-live in seconds
    void storeToken(String jti, UUID clientId, List<String> scopes, long ttlSeconds);

    /// Returns {@code true} if the token entry for the given JTI exists (not yet expired).
    ///
    /// @param jti the unique token identifier
    /// @return {@code true} if the token is stored
    boolean isTokenStored(String jti);

    /// Marks the given JTI as revoked.
    ///
    /// @param jti        the unique token identifier
    /// @param ttlSeconds the TTL for the revocation marker
    void revokeToken(String jti, long ttlSeconds);

    /// Returns {@code true} if the given JTI has been explicitly revoked.
    ///
    /// @param jti the unique token identifier
    /// @return {@code true} if revoked
    boolean isRevoked(String jti);

    /// Adds the given JTI to the per-client token index.
    ///
    /// @param jti      the unique token identifier
    /// @param clientId the owning managed client
    void addClientTokenJti(String jti, UUID clientId);

    /// Returns all JTIs stored in the per-client token index.
    ///
    /// @param clientId the managed client
    /// @return set of JTI strings; never {@code null}
    Set<String> getClientTokenJtis(UUID clientId);

    /// Increments the per-client rate-limit counter (60 s sliding window).
    /// Returns {@code true} if the request is within the allowed rate.
    ///
    /// @param clientId the managed client
    /// @param maxRpm   the maximum allowed requests per minute
    /// @return {@code true} if the client is within the rate limit
    boolean checkAndIncrementRateLimit(UUID clientId, int maxRpm);

    /// Sets the secret-rotation grace period marker for the given client.
    ///
    /// @param clientId   the managed client
    /// @param ttlSeconds the grace period TTL in seconds
    void setGracePeriod(UUID clientId, long ttlSeconds);

    /// Returns {@code true} if the given client is currently in a secret-rotation grace period.
    ///
    /// @param clientId the managed client
    /// @return {@code true} if in grace period
    boolean isInGracePeriod(UUID clientId);

    /// Stores the previous secret hash in Redis for the grace period.
    /// Enables in-flight token requests using the old secret to continue succeeding.
    ///
    /// @param clientId          the managed client
    /// @param prevHash          the BCrypt hash of the previous secret
    /// @param graceTtlSeconds   the TTL for the grace-period entry
    void storeGraceSecret(UUID clientId, String prevHash, long graceTtlSeconds);

    /// Returns the previous secret hash stored during a grace period, if present.
    ///
    /// @param clientId the managed client
    /// @return an {@link java.util.Optional} containing the prevHash, or empty if not in grace period
    java.util.Optional<String> getGraceSecret(UUID clientId);

    /// Removes the grace-period secret hash for the given client.
    ///
    /// @param clientId the managed client
    void removeGraceSecret(UUID clientId);
}
