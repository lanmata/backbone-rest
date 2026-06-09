/*
 *  @(#)ManagedClientSecretHashServiceImpl.java
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

import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/// Implementation of {@link ManagedClientSecretHashService} using BCrypt.
@Service
public class ManagedClientSecretHashServiceImpl implements ManagedClientSecretHashService {

    private final PasswordEncoder passwordEncoder;

    /// Constructs a new {@code ManagedClientSecretHashServiceImpl}.
    ///
    /// @param passwordEncoder the BCrypt password encoder bean (strength 12)
    public ManagedClientSecretHashServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /// {@inheritDoc}
    /// <p>
    /// Runs on the {@code mcamHashExecutor} thread pool to prevent BCrypt from
    /// blocking Tomcat I/O threads under load (NFR-P-03).
    /// </p>
    @Async("mcamHashExecutor")
    @Override
    public CompletableFuture<String> hashSecret(String rawSecret) {
        return CompletableFuture.completedFuture(passwordEncoder.encode(rawSecret));
    }

    /// {@inheritDoc}
    /// <p>
    /// {@code BCryptPasswordEncoder.matches()} internally uses a constant-time
    /// comparison algorithm, preventing timing-oracle attacks (AC-TOK-02).
    /// </p>
    @Override
    public boolean matchesWithConstantTime(String rawSecret, String storedHash) {
        return passwordEncoder.matches(rawSecret, storedHash);
    }
}
