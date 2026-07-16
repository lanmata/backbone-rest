/*
 *  @(#)ManagedClientAuditServiceImpl.java
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

import com.umdc.commons.general.pojo.AuditEventType;
import com.umdc.persistence.general.domains.ManagedClientAuditEventEntity;
import com.umdc.persistence.general.repositories.ManagedClientAuditEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Implementation of {@link ManagedClientAuditService}.
 * <p>
 * {@link #save} is annotated with {@link Async} so that audit writes never
 * block the MCAM critical path.
 * </p>
 */
@Service
public class ManagedClientAuditServiceImpl implements ManagedClientAuditService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagedClientAuditServiceImpl.class);

    private final ManagedClientAuditEventRepository auditEventRepository;

    /**
     * Constructs a new {@code ManagedClientAuditServiceImpl}.
     *
     * @param auditEventRepository the repository used to persist MCAM audit events
     */
    public ManagedClientAuditServiceImpl(ManagedClientAuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Executed on a separate thread from the Spring async executor so that
     * database I/O does not delay the API response.
     * </p>
     */
    @Async
    @Override
    public void save(UUID clientId, AuditEventType eventType, String ipAddress,
                     String outcome, String details) {
        LOGGER.debug("Recording MCAM audit event — clientId='{}', eventType='{}'", clientId, eventType);
        try {
            ManagedClientAuditEventEntity entity = new ManagedClientAuditEventEntity();
            entity.setId(UUID.randomUUID());
            entity.setClientId(clientId);
            entity.setEventType(eventType);
            entity.setIpAddress(ipAddress);
            entity.setOutcome(outcome);
            entity.setDetails(details);
            auditEventRepository.save(entity);
            LOGGER.debug("MCAM audit event persisted — id='{}', eventType='{}'", entity.getId(), eventType);
        } catch (Exception ex) {
            LOGGER.error("Failed to persist MCAM audit event — clientId='{}', eventType='{}'",
                    clientId, eventType, ex);
        }
    }
}
