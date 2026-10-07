/*
 *  @(#)NoticeGraphLookupServiceImpl.java
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

package com.umdc.backoffice.v1.notices.service;

import com.umdc.persistence.general.domains.NoticeEntity;
import com.umdc.persistence.general.repositories.NoticeRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * {@code NoticeEntity.noticeTypeEntity} is {@code @ManyToOne(fetch = LAZY)} (see
 * {@code com.umdc.persistence} — that module owns the entity, not this one).
 * {@link NoticeRepository#findByIdApplicationId} (a plain Spring Data derived query method) JOIN
 * FETCHes none of this. Unlike {@code RoleEntity}/{@code UserEntity}/{@code ApplicationEntity},
 * {@code NoticeTypeEntity} itself has no further associations of its own — no cycle to worry
 * about here, just the one LAZY to-one.
 * <p>
 * Under the JVM a runtime-generated {@code HibernateProxy} backs that LAZY reference, harmlessly;
 * under native-image (Spring Boot sets {@code hibernate.bytecode.provider=none} — runtime
 * bytecode generation is impossible in a closed-world native binary) that throws
 * {@code HibernateException: "Generation of HibernateProxy instances at runtime is not allowed"}
 * during row hydration, before any application code runs — {@code NoticeMapper} never even reads
 * {@code noticeTypeEntity} (the DTO's {@code noticeTypeId} comes from the embedded
 * {@code NoticeId} instead), so the crash happens regardless of use, exactly like every other
 * LAZY to-one in this codebase (see {@code SessionUserLookupServiceImpl}'s class Javadoc for the
 * full explanation).
 * <p>
 * A plain {@code LEFT JOIN FETCH} loads {@code noticeTypeEntity} as real data instead of a lazy
 * reference, so Hibernate never attempts the proxy path.
 */
@Service
public class NoticeGraphLookupServiceImpl implements NoticeGraphLookupService {

    private static final String FIND_BY_APPLICATION_ID =
            "SELECT n FROM NoticeEntity n LEFT JOIN FETCH n.noticeTypeEntity WHERE n.id.applicationId = :applicationId";

    private final EntityManager entityManager;

    /**
     * @param entityManager used to run the JOIN FETCH lookup
     */
    public NoticeGraphLookupServiceImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<NoticeEntity> findByApplicationIdWithGraph(UUID applicationId) {
        return entityManager.createQuery(FIND_BY_APPLICATION_ID, NoticeEntity.class)
                .setParameter("applicationId", applicationId)
                .getResultList();
    }
}
