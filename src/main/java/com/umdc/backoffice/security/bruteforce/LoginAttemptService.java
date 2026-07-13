/*
 *  @(#)LoginAttemptService.java
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
package com.umdc.backoffice.security.bruteforce;

import java.util.UUID;

/**
 * Service for tracking failed login attempts and enforcing brute-force protection.
 * <p>
 * After {@link #MAX_FAILURES} consecutive failures the account is locked for
 * {@link #LOCK_TTL_MINUTES} minutes. A successful login clears the counter immediately.
 * </p>
 */
public interface LoginAttemptService {

    /**
     * Maximum number of consecutive failures before the account is locked.
     */
    int MAX_FAILURES = 5;

    /**
     * Lock duration in minutes applied after {@link #MAX_FAILURES} failures.
     */
    long LOCK_TTL_MINUTES = 15L;

    /**
     * Records a failed login attempt for the given alias and application.
     * Increments the failure counter and sets the lock TTL if not already present.
     *
     * @param alias         the user alias (login identifier)
     * @param applicationId the application identifier; may be {@code null} for alias-only logins
     */
    void recordFailure(String alias, UUID applicationId);

    /**
     * Clears the failure counter for the given alias and application after a successful login.
     *
     * @param alias         the user alias
     * @param applicationId the application identifier; may be {@code null}
     */
    void recordSuccess(String alias, UUID applicationId);

    /**
     * Returns {@code true} if the alias+application combination is currently locked
     * due to too many consecutive failures.
     *
     * @param alias         the user alias
     * @param applicationId the application identifier; may be {@code null}
     * @return {@code true} if the account is locked; {@code false} otherwise
     */
    boolean isLocked(String alias, UUID applicationId);
}

