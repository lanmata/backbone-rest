/*
 *  @(#)ManagedClientSecretHashService.java
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

import java.util.concurrent.CompletableFuture;

/// Service interface for hashing and comparing M2M client secrets.
public interface ManagedClientSecretHashService {

    /// Hashes a raw secret asynchronously using BCrypt on the dedicated
    /// {@code mcamHashExecutor} thread pool (NFR-P-03).
    ///
    /// @param rawSecret the plaintext secret to hash
    /// @return a {@link CompletableFuture} resolving to the BCrypt hash string
    CompletableFuture<String> hashSecret(String rawSecret);

    /// Performs a constant-time comparison of a raw secret against a stored BCrypt hash.
    /// <p>
    /// Delegates to {@code BCryptPasswordEncoder.matches()}, which is inherently
    /// constant-time and prevents timing-oracle attacks (AC-TOK-02).
    /// </p>
    ///
    /// @param rawSecret   the candidate plaintext secret
    /// @param storedHash  the persisted BCrypt hash
    /// @return {@code true} if the raw secret matches the hash
    boolean matchesWithConstantTime(String rawSecret, String storedHash);
}
