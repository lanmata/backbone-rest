/*
 *  @(#)FeatureGraphLookupServiceImpl.java
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

package com.umdc.backoffice.v1.features.service;

import com.umdc.persistence.general.domains.FeatureEntity;
import com.umdc.persistence.general.domains.RoleFeatureEntity;
import com.umdc.persistence.general.repositories.FeatureRepository;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Subgraph;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * {@code FeatureEntity.rolFeatures} is {@code @OneToMany(fetch = EAGER)} whose entries
 * ({@code RoleFeatureEntity.role}) are {@code @ManyToOne(fetch = LAZY)} to a genuinely different
 * entity — and that {@code RoleEntity} carries its OWN {@code application} LAZY to-one, plus its
 * own EAGER {@code roleFeatures}/{@code applicationRoleUser} collections, a real cycle back to
 * this same {@code FeatureEntity} type (see {@code com.umdc.persistence} — that module owns the
 * entities, not this one). {@link FeatureRepository#findById} (plain {@code JpaRepository}
 * method) JOIN FETCHes none of these.
 * <p>
 * Under the JVM this is harmless: every LAZY to-one gets backed by a runtime-generated
 * {@code HibernateProxy} the instant its owning row is hydrated — there's no "stay truly
 * uninitialized until accessed" option without build-time bytecode enhancement (not configured
 * here); a non-enhanced lazy to-one is always populated with either real data or a proxy at
 * hydration time, whether or not application code ever reads it — and every EAGER collection is
 * populated too. Under native-image, Spring Boot sets {@code hibernate.bytecode.provider=none}
 * (runtime bytecode generation is impossible in a closed-world native binary), so proxy creation
 * throws {@code HibernateException: "Generation of HibernateProxy instances at runtime is not
 * allowed"} instead — during row/collection hydration, before any application code runs.
 * <p>
 * The fix is a {@code jakarta.persistence.fetchgraph} {@link EntityGraph}: per the JPA spec,
 * attributes named in the graph are eager and EVERYTHING ELSE — on every entity type the graph
 * reaches, not just the root — is forced to LAZY, regardless of its own mapping. A LAZY
 * *collection* left genuinely untouched is safe under native-image (Hibernate represents it with
 * an already-compiled {@code PersistentSet} wrapper and defers the SELECT until first access — no
 * bytecode-generated proxy class needed, unlike a LAZY to-one). So the graph below lists
 * {@code rolFeatures}/{@code .role}/{@code .role.application} — but deliberately stops there: it
 * does NOT also list the reached {@code RoleEntity}'s own {@code roleFeatures}/
 * {@code applicationRoleUser}, since {@code FeatureMapper} never reads that far. Left unlisted,
 * they revert to LAZY-and-untouched, which is what breaks the cycle.
 */
@Service
public class FeatureGraphLookupServiceImpl implements FeatureGraphLookupService {

    private static final String FETCH_GRAPH_HINT = "jakarta.persistence.fetchgraph";

    private static final String SELECT_DISTINCT_FEATURE = "SELECT DISTINCT f FROM FeatureEntity f ";

    private static final String FIND_FEATURE_BY_ID = SELECT_DISTINCT_FEATURE + "WHERE f.id = :featureId";

    private static final String FIND_FEATURE_BY_NAME =
            SELECT_DISTINCT_FEATURE + "WHERE f.name = :name ORDER BY f.name DESC";

    private static final String FIND_ALL_FEATURES = SELECT_DISTINCT_FEATURE;

    private static final String FIND_FEATURES_BY_IDS_AND_STATUS =
            SELECT_DISTINCT_FEATURE + "WHERE f.id IN :ids AND f.active = :active ORDER BY f.name DESC";

    private static final String FIND_FEATURES_BY_ROLE_ID =
            SELECT_DISTINCT_FEATURE + "JOIN f.rolFeatures rf "
            + "WHERE rf.role.id = :roleId ORDER BY f.id ASC";

    private final EntityManager entityManager;

    /**
     * @param entityManager used to run the lookup queries
     */
    public FeatureGraphLookupServiceImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<FeatureEntity> findByIdWithGraph(UUID featureId) {
        return entityManager.createQuery(FIND_FEATURE_BY_ID, FeatureEntity.class)
                .setParameter("featureId", featureId)
                .setHint(FETCH_GRAPH_HINT, buildGraph())
                .getResultStream()
                .findFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<FeatureEntity> findByNameWithGraph(String name) {
        return entityManager.createQuery(FIND_FEATURE_BY_NAME, FeatureEntity.class)
                .setParameter("name", name)
                .setHint(FETCH_GRAPH_HINT, buildGraph())
                .getResultStream()
                .findFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FeatureEntity> findAllWithGraph() {
        return entityManager.createQuery(FIND_ALL_FEATURES, FeatureEntity.class)
                .setHint(FETCH_GRAPH_HINT, buildGraph())
                .getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FeatureEntity> findByIdsAndStatusWithGraph(List<UUID> ids, boolean active) {
        return entityManager.createQuery(FIND_FEATURES_BY_IDS_AND_STATUS, FeatureEntity.class)
                .setParameter("ids", ids)
                .setParameter("active", active)
                .setHint(FETCH_GRAPH_HINT, buildGraph())
                .getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FeatureEntity> findByRoleIdWithGraph(UUID roleId) {
        return entityManager.createQuery(FIND_FEATURES_BY_ROLE_ID, FeatureEntity.class)
                .setParameter("roleId", roleId)
                .setHint(FETCH_GRAPH_HINT, buildGraph())
                .getResultList();
    }

    private EntityGraph<FeatureEntity> buildGraph() {
        EntityGraph<FeatureEntity> graph = entityManager.createEntityGraph(FeatureEntity.class);
        Subgraph<RoleFeatureEntity> rfGraph = graph.addSubgraph("rolFeatures");
        rfGraph.addAttributeNodes("feature");
        rfGraph.addSubgraph("role").addAttributeNodes("application");
        return graph;
    }
}
