/*
 *  @(#)RoleGraphLookupServiceImpl.java
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

package com.umdc.backoffice.v1.roles.service;

import com.umdc.persistence.general.domains.ApplicationRoleUserEntity;
import com.umdc.persistence.general.domains.RoleEntity;
import com.umdc.persistence.general.domains.RoleFeatureEntity;
import com.umdc.persistence.general.domains.UserEntity;
import com.umdc.persistence.general.repositories.RoleRepository;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Subgraph;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * {@code RoleEntity.application} is a {@code @ManyToOne(fetch = LAZY)} field, and
 * {@code RoleEntity.roleFeatures} / {@code RoleEntity.applicationRoleUser} are each
 * {@code @OneToMany(fetch = EAGER)} whose entries ({@code RoleFeatureEntity.feature} /
 * {@code ApplicationRoleUserEntity.user}) are themselves {@code @ManyToOne(fetch = LAZY)} to a
 * genuinely different entity — and {@code UserEntity} reached that way carries its OWN EAGER
 * {@code applicationRoleUser} collection, a real cycle (see {@code com.umdc.persistence} — that
 * module owns the entities, not this one). {@link RoleRepository#findById} (plain
 * {@code JpaRepository} method) JOIN FETCHes none of these.
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
 * A plain JOIN FETCH list can't win against this: JOIN FETCHing a LAZY to-one loads real data for
 * THAT field, but the real entity it points to may carry its own EAGER collection — e.g.
 * {@code applicationRoleUser.user} is a real, managed {@code UserEntity}, and
 * {@code UserEntity.applicationRoleUser} is EAGER too (a per-entity-instance mapping; a single
 * query's JOIN FETCH list has no say over an association it doesn't ask for), whose rows need yet
 * more LAZY to-ones resolved. Chasing this by adding more JOIN FETCH never terminates:
 * {@code Role.applicationRoleUser} → {@code User} → {@code User.applicationRoleUser} →
 * {@code Role} → ... is a real cycle.
 * <p>
 * The fix is a {@code jakarta.persistence.fetchgraph} {@link EntityGraph}: per the JPA spec,
 * attributes named in the graph are eager and EVERYTHING ELSE — on every entity type the graph
 * reaches, not just the root — is forced to LAZY, regardless of its own mapping. A LAZY
 * *collection* left genuinely untouched is safe under native-image (Hibernate represents it with
 * an already-compiled {@code PersistentSet} wrapper and defers the SELECT until first access — no
 * bytecode-generated proxy class needed, unlike a LAZY to-one). So the graph below lists every
 * to-one actually needed as real data ({@code application} at every level reached;
 * {@code roleFeatures.feature}; {@code applicationRoleUser.user} and its own {@code application}/
 * {@code person}) — but deliberately stops there: it does NOT also list the nested
 * {@code user}'s own {@code applicationRoleUser}, since nothing here ({@code RoleMapper}) ever
 * reads that far. Left unlisted, it reverts to LAZY-and-untouched, which is exactly what breaks
 * the cycle — not because the graph is deep enough to cover it, but because it deliberately stops
 * one level before it would otherwise have to.
 * <p>
 * {@code roleFeatures} and {@code applicationRoleUser} are still fetched via two SEPARATE queries
 * (two separate graphs), rather than both in one: both are {@code @OneToMany} collections, and
 * Hibernate only collapses duplicate root {@code RoleEntity} rows ({@code SELECT DISTINCT}) — it
 * does not deduplicate the collections themselves, so fetching two to-many collections in a single
 * query produces a SQL cartesian product (mirrors {@code UserGraphLookupServiceImpl}).
 */
@Service
public class RoleGraphLookupServiceImpl implements RoleGraphLookupService {

    private static final String FETCH_GRAPH_HINT = "jakarta.persistence.fetchgraph";

    private static final String APPLICATION_ATTR = "application";

    private static final String SELECT_DISTINCT_ROLE = "SELECT DISTINCT r FROM RoleEntity r ";

    private static final String FIND_ROLE_BY_ID = SELECT_DISTINCT_ROLE + "WHERE r.id = :roleId";

    private static final String FIND_ALL_ROLES = SELECT_DISTINCT_ROLE;

    private static final String FIND_ROLES_BY_IDS =
            SELECT_DISTINCT_ROLE + "WHERE r.id IN :ids ORDER BY r.id ASC";

    private static final String FIND_ROLES_BY_STATUS =
            SELECT_DISTINCT_ROLE + "WHERE r.active = :status ORDER BY r.id ASC";

    private static final String FIND_ROLES_BY_STATUS_AND_IDS =
            SELECT_DISTINCT_ROLE + "WHERE r.id IN :ids AND r.active = :active";

    private static final String FIND_ROLES_BY_USER_ID =
            "SELECT DISTINCT r FROM ApplicationRoleUserEntity aru "
            + "JOIN aru.role r "
            + "WHERE aru.user.id = :userId ORDER BY r.id ASC";

    private static final String FIND_ROLES_BY_APPLICATION_ID =
            SELECT_DISTINCT_ROLE + "WHERE r.application.id = :applicationId ORDER BY r.id ASC";

    private static final String FETCH_APPLICATION_ROLE_USER_FOR_ROLES =
            SELECT_DISTINCT_ROLE + "LEFT JOIN FETCH r.applicationRoleUser aru WHERE r.id IN :ids";

    private final EntityManager entityManager;

    /**
     * @param entityManager used to run the lookup queries
     */
    public RoleGraphLookupServiceImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<RoleEntity> findByIdWithGraph(UUID roleId) {
        Optional<RoleEntity> role = entityManager.createQuery(FIND_ROLE_BY_ID, RoleEntity.class)
                .setParameter("roleId", roleId)
                .setHint(FETCH_GRAPH_HINT, buildRoleFeaturesGraph())
                .getResultStream()
                .findFirst();
        role.map(List::of).ifPresent(this::fetchApplicationRoleUser);
        return role;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<List<RoleEntity>> findAllWithGraph() {
        return Optional.of(fetchApplicationRoleUser(
                entityManager.createQuery(FIND_ALL_ROLES, RoleEntity.class)
                        .setHint(FETCH_GRAPH_HINT, buildRoleFeaturesGraph())
                        .getResultList()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<List<RoleEntity>> findByIdsWithGraph(List<UUID> ids) {
        return Optional.of(fetchApplicationRoleUser(
                entityManager.createQuery(FIND_ROLES_BY_IDS, RoleEntity.class)
                        .setParameter("ids", ids)
                        .setHint(FETCH_GRAPH_HINT, buildRoleFeaturesGraph())
                        .getResultList()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<List<RoleEntity>> findByStatusWithGraph(Boolean status) {
        return Optional.of(fetchApplicationRoleUser(
                entityManager.createQuery(FIND_ROLES_BY_STATUS, RoleEntity.class)
                        .setParameter("status", status)
                        .setHint(FETCH_GRAPH_HINT, buildRoleFeaturesGraph())
                        .getResultList()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<List<RoleEntity>> findByStatusAndIdsWithGraph(boolean active, List<UUID> ids) {
        return Optional.of(fetchApplicationRoleUser(
                entityManager.createQuery(FIND_ROLES_BY_STATUS_AND_IDS, RoleEntity.class)
                        .setParameter("active", active)
                        .setParameter("ids", ids)
                        .setHint(FETCH_GRAPH_HINT, buildRoleFeaturesGraph())
                        .getResultList()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<List<RoleEntity>> findByUserIdWithGraph(UUID userId) {
        return Optional.of(fetchApplicationRoleUser(
                entityManager.createQuery(FIND_ROLES_BY_USER_ID, RoleEntity.class)
                        .setParameter("userId", userId)
                        .setHint(FETCH_GRAPH_HINT, buildRoleFeaturesGraph())
                        .getResultList()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<List<RoleEntity>> findByApplicationIdWithGraph(UUID applicationId) {
        return Optional.of(fetchApplicationRoleUser(
                entityManager.createQuery(FIND_ROLES_BY_APPLICATION_ID, RoleEntity.class)
                        .setParameter("applicationId", applicationId)
                        .setHint(FETCH_GRAPH_HINT, buildRoleFeaturesGraph())
                        .getResultList()));
    }

    /**
     * Populates {@code applicationRoleUser}/{@code .user}/{@code .user.application}/
     * {@code .user.person} on the same managed {@link RoleEntity} instances {@code roles} already
     * holds (same {@link EntityManager}, same persistence context) — see the class Javadoc for why
     * this must be a second query/graph rather than folded into the primary one.
     *
     * @param roles the already-loaded roles to populate
     * @return {@code roles}, unchanged, for call-site chaining
     */
    private List<RoleEntity> fetchApplicationRoleUser(List<RoleEntity> roles) {
        if (roles.isEmpty()) {
            return roles;
        }
        entityManager.createQuery(FETCH_APPLICATION_ROLE_USER_FOR_ROLES, RoleEntity.class)
                .setParameter("ids", roles.stream().map(RoleEntity::getId).toList())
                .setHint(FETCH_GRAPH_HINT, buildApplicationRoleUserGraph())
                .getResultList();
        return roles;
    }

    /**
     * Graph for the primary role query: {@code application} (the role's own, LAZY, non-null FK)
     * and {@code roleFeatures}/{@code roleFeatures.feature} (feature has no further to-one of its
     * own to chase). {@code roleFeatures.role} is the exact same row as the root for every entry
     * — listed anyway since even a self-reference still needs resolving like any other.
     */
    private EntityGraph<RoleEntity> buildRoleFeaturesGraph() {
        EntityGraph<RoleEntity> graph = entityManager.createEntityGraph(RoleEntity.class);
        graph.addAttributeNodes(APPLICATION_ATTR);
        Subgraph<RoleFeatureEntity> rfGraph = graph.addSubgraph("roleFeatures");
        rfGraph.addAttributeNodes("role", "feature");
        return graph;
    }

    /**
     * Graph for the {@code applicationRoleUser} follow-up query: {@code application} (the link's
     * own) and {@code user}/{@code user.application}/{@code user.person} — {@code user}'s OWN
     * {@code applicationRoleUser} is deliberately NOT listed (nothing here ever reads that far),
     * which is what breaks the {@code Role} → {@code User} → {@code Role} cycle: left unlisted, it
     * reverts to LAZY-and-untouched instead of cascading into another full graph.
     * {@code applicationRoleUser.role} is the exact same row as the root for every entry — listed
     * anyway since even a self-reference still needs resolving like any other.
     */
    private EntityGraph<RoleEntity> buildApplicationRoleUserGraph() {
        EntityGraph<RoleEntity> graph = entityManager.createEntityGraph(RoleEntity.class);
        Subgraph<ApplicationRoleUserEntity> aruGraph = graph.addSubgraph("applicationRoleUser");
        aruGraph.addAttributeNodes("role", APPLICATION_ATTR);
        Subgraph<UserEntity> userGraph = aruGraph.addSubgraph("user");
        userGraph.addAttributeNodes(APPLICATION_ATTR, "person");
        return graph;
    }
}
