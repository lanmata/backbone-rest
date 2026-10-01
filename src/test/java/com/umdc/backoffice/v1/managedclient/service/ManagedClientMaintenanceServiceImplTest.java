/*
 *  @(#)ManagedClientMaintenanceServiceImplTest.java
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

import com.umdc.backoffice.property.ManagementAuthenticatorProperties;
import com.umdc.backoffice.property.SecurityProperties;
import com.umdc.persistence.general.domains.ManagedClientEntity;
import com.umdc.persistence.general.repositories.ManagedClientRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ManagedClientMaintenanceServiceImplTest {

    @Mock
    private ManagedClientRepository repository;

    @Mock
    private SecurityProperties securityProperties;

    private ManagedClientMaintenanceServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ManagedClientMaintenanceServiceImpl(repository, securityProperties);

        var mcam = new ManagementAuthenticatorProperties();
        mcam.setRotationGracePeriodSeconds(600L);
        when(securityProperties.getManagementAuthenticator()).thenReturn(mcam);
    }

    @Test
    @DisplayName("clears prevSecretHash on every stale entity and saves it")
    void clearExpiredPrevSecretHashes_clearsAndSavesStaleEntities() {
        var entity = new ManagedClientEntity();
        entity.setPrevSecretHash("old-hash");
        when(repository.findWithExpiredPrevSecretHash(any(LocalDateTime.class)))
                .thenReturn(List.of(entity));

        service.clearExpiredPrevSecretHashes();

        Assertions.assertNull(entity.getPrevSecretHash());
        verify(repository, times(1)).save(entity);
    }

    @Test
    @DisplayName("does nothing but log when there are no stale entities")
    void clearExpiredPrevSecretHashes_noStaleEntities_noSaveCalls() {
        when(repository.findWithExpiredPrevSecretHash(any(LocalDateTime.class)))
                .thenReturn(List.of());

        service.clearExpiredPrevSecretHashes();

        verify(repository, never()).save(any());
    }
}
