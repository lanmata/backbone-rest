/*
 *  @(#)ManagedClientMaintenanceService.java
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

/// Service interface for scheduled MCAM maintenance tasks.
public interface ManagedClientMaintenanceService {

    /// Clears stale {@code prevSecretHash} values from managed-client rows where
    /// the rotation grace period has expired.
    /// <p>
    /// Run periodically to prevent stale previous-secret hashes from persisting
    /// in the database after the Redis grace-period key has already expired.
    /// </p>
    void clearExpiredPrevSecretHashes();
}
