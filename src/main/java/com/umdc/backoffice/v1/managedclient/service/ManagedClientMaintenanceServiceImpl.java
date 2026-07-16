/*
 *  @(#)ManagedClientMaintenanceServiceImpl.java
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

import com.umdc.backoffice.property.SecurityProperties;
import com.umdc.persistence.general.domains.ManagedClientEntity;
import com.umdc.persistence.general.repositories.ManagedClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Scheduled service that clears stale {@code prevSecretHash} values from
 * managed-client rows after the rotation grace period has elapsed (R-05 mitigation).
 */
@Service
public class ManagedClientMaintenanceServiceImpl implements ManagedClientMaintenanceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagedClientMaintenanceServiceImpl.class);

    private final ManagedClientRepository repository;
    private final SecurityProperties securityProperties;

    /**
     * Constructs a new {@code ManagedClientMaintenanceServiceImpl}.
     *
     * @param repository         the managed client JPA repository
     * @param securityProperties the security configuration properties
     */
    public ManagedClientMaintenanceServiceImpl(ManagedClientRepository repository,
                                               SecurityProperties securityProperties) {
        this.repository = repository;
        this.securityProperties = securityProperties;
    }

    /**
     * {@inheritDoc}
     *
     * Runs every 10 minutes by default ({@code MCAM_MAINTENANCE_INTERVAL_MS}).
     * Clears {@code prevSecretHash} on clients where {@code secretLastRotatedAt}
     * plus the grace period is in the past.
     */
    @Scheduled(fixedDelayString = "${MCAM_MAINTENANCE_INTERVAL_MS:600000}")
    @Override
    public void clearExpiredPrevSecretHashes() {
        long gracePeriodSeconds = securityProperties.getManagementAuthenticator()
                .getRotationGracePeriodSeconds();
        LocalDateTime graceCutoff = LocalDateTime.now(ZoneId.systemDefault()).minusSeconds(gracePeriodSeconds);
        List<ManagedClientEntity> staleEntities = repository.findWithExpiredPrevSecretHash(graceCutoff);
        int count = staleEntities.size();
        for (ManagedClientEntity entity : staleEntities) {
            entity.setPrevSecretHash(null);
            repository.save(entity);
        }
        if (count > 0) {
            LOGGER.info("Cleared prevSecretHash from {} managed client(s) with expired grace period", count);
        } else {
            LOGGER.debug("Maintenance pass complete — no stale prevSecretHash entries found");
        }
    }
}
