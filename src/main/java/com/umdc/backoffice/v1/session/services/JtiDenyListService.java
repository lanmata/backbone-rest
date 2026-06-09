/*
 *  @(#)JtiDenyListService.java
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
package com.umdc.backoffice.v1.session.services;

/**
 * Service interface for managing a JWT ID (JTI) deny-list backed by Redis.
 * Provides token revocation capability by storing denied JTI values with TTL.
 */
public interface JtiDenyListService {

    /**
     * Adds the given JTI to the deny-list with the specified TTL.
     *
     * @param jti        the JWT ID to deny
     * @param ttlSeconds time-to-live in seconds; entry is auto-removed after expiry
     */
    void denyJti(String jti, long ttlSeconds);

    /**
     * Checks whether the given JTI is present in the deny-list.
     *
     * @param jti the JWT ID to check
     * @return {@code true} if the JTI has been denied, {@code false} otherwise
     */
    boolean isDenied(String jti);
}

