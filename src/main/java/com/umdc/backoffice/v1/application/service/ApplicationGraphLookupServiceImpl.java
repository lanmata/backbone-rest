/*
 *  @(#)ApplicationGraphLookupServiceImpl.java
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

package com.umdc.backoffice.v1.application.service;

import com.umdc.persistence.general.domains.ApplicationEntity;
import com.umdc.persistence.general.repositories.ApplicationRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * {@code ApplicationEntity.applicationRoleUser} is {@code @OneToMany(fetch = EAGER)} (see
 * {@code com.umdc.persistence} — that module owns the entity, not this one), so any plain
 * {@code applicationRepository} lookup that hydrates a real {@code ApplicationEntity} makes
 * Hibernate unconditionally initialize it — and each entry needs its own LAZY
 * {@code user}/{@code role} resolved, entities that are frequently not already loaded elsewhere
 * in the same request. {@link ApplicationRepository#findById} (and {@code findAll}/
 * {@code findAllById}) JOIN FETCHes none of this.
 * <p>
 * Under the JVM a runtime-generated {@code HibernateProxy} backs each of those, harmlessly; under
 * native-image (Spring Boot sets {@code hibernate.bytecode.provider=none} — runtime bytecode
 * generation is impossible in a closed-world native binary) that throws
 * {@code HibernateException: "Generation of HibernateProxy instances at runtime is not allowed"},
 * during collection hydration, before any application code runs — none of
 * {@code ApplicationServiceImpl}/{@code RoleServiceImpl} (the callers) ever read
 * {@code applicationRoleUser} from an entity loaded this way.
 * <p>
 * An empty {@code jakarta.persistence.fetchgraph} forces every attribute not named in it —
 * {@code applicationRoleUser} is the only one {@code ApplicationEntity} has — to LAZY regardless
 * of its own mapping. Left genuinely untouched, a LAZY collection is safe under native-image:
 * Hibernate represents it with an already-compiled {@code PersistentSet} wrapper and defers the
 * SELECT until first access, unlike a LAZY to-one, which always needs either real data or a
 * bytecode-generated proxy the moment its owning row is read.
 */
@Service
public class ApplicationGraphLookupServiceImpl implements ApplicationGraphLookupService {

    private static final String FETCH_GRAPH_HINT = "jakarta.persistence.fetchgraph";

    private static final String FIND_BY_ID = "SELECT a FROM ApplicationEntity a WHERE a.id = :id";

    private static final String FIND_BY_IDS = "SELECT a FROM ApplicationEntity a WHERE a.id IN :ids";

    private static final String FIND_ALL = "SELECT a FROM ApplicationEntity a";

    private final EntityManager entityManager;

    /**
     * @param entityManager used to run the lookup queries
     */
    public ApplicationGraphLookupServiceImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ApplicationEntity> findByIdSafe(UUID applicationId) {
        return entityManager.createQuery(FIND_BY_ID, ApplicationEntity.class)
                .setParameter("id", applicationId)
                .setHint(FETCH_GRAPH_HINT, buildEmptyGraph())
                .getResultStream()
                .findFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ApplicationEntity> findByIdsSafe(List<UUID> ids) {
        return entityManager.createQuery(FIND_BY_IDS, ApplicationEntity.class)
                .setParameter("ids", ids)
                .setHint(FETCH_GRAPH_HINT, buildEmptyGraph())
                .getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ApplicationEntity> findAllSafe() {
        return entityManager.createQuery(FIND_ALL, ApplicationEntity.class)
                .setHint(FETCH_GRAPH_HINT, buildEmptyGraph())
                .getResultList();
    }

    private jakarta.persistence.EntityGraph<ApplicationEntity> buildEmptyGraph() {
        return entityManager.createEntityGraph(ApplicationEntity.class);
    }
}
