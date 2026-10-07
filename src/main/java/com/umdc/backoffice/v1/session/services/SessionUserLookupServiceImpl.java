/*
 *  @(#)SessionUserLookupServiceImpl.java
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

import com.umdc.persistence.general.domains.ApplicationRoleUserEntity;
import com.umdc.persistence.general.domains.UserEntity;
import com.umdc.persistence.general.repositories.UserRepository;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Subgraph;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * {@link UserRepository#findByAliasAndApplication} and
 * {@link UserRepository#findByEmailAndApplication} only plain-JOIN {@code applicationRoleUser}
 * (needed for their WHERE filter) — they don't JOIN FETCH anything. {@code applicationRoleUser}
 * is mapped {@code @OneToMany(fetch = EAGER)} on {@code UserEntity}; {@code application} (on both
 * {@code UserEntity} and {@code ApplicationRoleUserEntity}) and {@code role} (on
 * {@code ApplicationRoleUserEntity}) are each {@code @ManyToOne(fetch = LAZY)} (see
 * {@code com.umdc.persistence} — that module owns the entities, not this one). Both
 * {@code ApplicationEntity} and {@code RoleEntity} in turn carry their OWN
 * {@code @OneToMany(fetch = EAGER)} collections ({@code applicationRoleUser} again, plus
 * {@code roleFeatures} on {@code RoleEntity}) — a cyclical graph.
 * <p>
 * Under the JVM this is harmless: every LAZY to-one gets backed by a runtime-generated
 * {@code HibernateProxy} the instant its owning row is hydrated (there's no "stay truly
 * uninitialized until accessed" option without build-time bytecode enhancement, not configured
 * here — a non-enhanced lazy to-one is always populated with either real data or a proxy at
 * hydration time, whether or not application code ever reads it), and every EAGER collection is
 * populated too — none of it breaks anything on the JVM. Under native-image, Spring Boot sets
 * {@code hibernate.bytecode.provider=none} (runtime bytecode generation is impossible in a
 * closed-world native binary), so proxy creation throws {@code HibernateException: "Generation of
 * HibernateProxy instances at runtime is not allowed"} instead — during row/collection hydration,
 * before any application code runs.
 * <p>
 * A plain JOIN FETCH list can't win against this: JOIN FETCHing a LAZY to-one (say,
 * {@code u.application}) does load real data instead of a proxy for THAT field — but
 * {@code u.application} is a real, managed {@code ApplicationEntity} now, and Hibernate
 * unconditionally initializes ITS OWN EAGER {@code applicationRoleUser} collection too (EAGER is a
 * per-entity-instance mapping; a single query's JOIN FETCH list has no say over an association it
 * doesn't ask for), whose rows need yet more LAZY to-ones resolved — rows that are frequently NOT
 * some entity already loaded elsewhere in this query. Chasing each one down this way never
 * terminates: {@code Role.applicationRoleUser} → {@code User} → {@code User.applicationRoleUser}
 * → {@code Role} → ... and {@code Feature.rolFeatures} → {@code Role} → {@code Role.roleFeatures}
 * → {@code Feature} → ... are both real cycles in this entity graph.
 * <p>
 * The fix is a {@code jakarta.persistence.fetchgraph} {@link EntityGraph} instead of (or alongside
 * — see below) JOIN FETCH: per the JPA spec, attributes named in the graph are eager and
 * EVERYTHING ELSE — on every entity type the graph reaches, not just the root — is forced to LAZY,
 * REGARDLESS of its own {@code @OneToMany}/{@code @ManyToOne} mapping. A LAZY *collection* left
 * genuinely untouched is safe under native-image: Hibernate represents it with a hand-written,
 * already-compiled {@code PersistentSet} wrapper and defers the SELECT until first access — no
 * bytecode-generated proxy class needed (that mechanism is specific to LAZY to-one associations).
 * So a graph covering exactly {@code application}, {@code applicationRoleUser},
 * {@code applicationRoleUser.role} and {@code applicationRoleUser.application} — each as a bare
 * leaf node, no further subgraph — loads every to-one this method's callers actually need as real
 * data, while every EAGER collection on every entity reached (the root {@code UserEntity}'s own
 * {@code applicationRoleUser} aside, which the graph DOES cover) reverts to LAZY-and-untouched,
 * breaking both cycles above without needing to know how deep they go.
 * <p>
 * {@code ar} is still also plain-JOINed (not fetched) in the JPQL text itself, separately from the
 * graph: the WHERE clause filters on {@code ar.application.id}, and a path expression in WHERE
 * needs that join regardless of what the graph fetches.
 */
@Service
public class SessionUserLookupServiceImpl implements SessionUserLookupService {

    private static final String FETCH_GRAPH_HINT = "jakarta.persistence.fetchgraph";

    private static final String APPLICATION_ATTR = "application";

    private static final String FIND_USER_BY_ALIAS_AND_APPLICATION =
            "SELECT u FROM UserEntity u "
            + "JOIN u.applicationRoleUser ar "
            + "WHERE u.alias = :alias AND ar.application.id = :applicationId";

    private static final String FIND_USER_BY_EMAIL_AND_APPLICATION =
            "SELECT u FROM UserEntity u "
            + "JOIN u.applicationRoleUser ar "
            + "WHERE u.email = :email AND ar.application.id = :applicationId";

    private static final String FIND_USER_BY_ID = "SELECT u FROM UserEntity u WHERE u.id = :userId";

    private static final String FIND_USER_BY_ALIAS = "SELECT u FROM UserEntity u WHERE u.alias = :alias";

    private final EntityManager entityManager;

    /**
     * @param entityManager used to run the lookup queries
     */
    public SessionUserLookupServiceImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<UserEntity> findByAliasAndApplication(String alias, UUID applicationId) {
        return entityManager.createQuery(FIND_USER_BY_ALIAS_AND_APPLICATION, UserEntity.class)
                .setParameter("alias", alias)
                .setParameter("applicationId", applicationId)
                .setHint(FETCH_GRAPH_HINT, buildFetchGraph())
                .getResultStream()
                .findFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<UserEntity> findByEmailAndApplication(String email, UUID applicationId) {
        return entityManager.createQuery(FIND_USER_BY_EMAIL_AND_APPLICATION, UserEntity.class)
                .setParameter("email", email)
                .setParameter("applicationId", applicationId)
                .setHint(FETCH_GRAPH_HINT, buildFetchGraph())
                .getResultStream()
                .findFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<UserEntity> findByIdWithGraph(UUID userId) {
        return entityManager.createQuery(FIND_USER_BY_ID, UserEntity.class)
                .setParameter("userId", userId)
                .setHint(FETCH_GRAPH_HINT, buildFetchGraph())
                .getResultStream()
                .findFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<UserEntity> findByAliasWithGraph(String alias) {
        return entityManager.createQuery(FIND_USER_BY_ALIAS, UserEntity.class)
                .setParameter("alias", alias)
                .setHint(FETCH_GRAPH_HINT, buildFetchGraph())
                .getResultStream()
                .findFirst();
    }

    /**
     * Builds the {@code jakarta.persistence.fetchgraph} described in the class Javadoc: eager for
     * {@code application} and {@code applicationRoleUser}/{@code .role}/{@code .application},
     * LAZY (and, left untouched by every caller of this class, never initialized) for every
     * {@code @OneToMany(fetch = EAGER)} collection on any of those — the entity-level mapping
     * that's otherwise impossible to override per-query.
     */
    private EntityGraph<UserEntity> buildFetchGraph() {
        EntityGraph<UserEntity> graph = entityManager.createEntityGraph(UserEntity.class);
        // person is required here even though this class never reads it: UserEntity.person is
        // @OneToOne(fetch = EAGER) by mapping, but jakarta.persistence.fetchgraph forces EVERY
        // attribute not named in the graph to LAZY, regardless of its own mapped fetch type —
        // including ones that were originally EAGER. Left out, u.person becomes an unfetched LAZY
        // to-one with a non-null FK (every user has a person row), hitting the exact same
        // proxy-creation crash this whole class exists to avoid. The same reasoning is why every
        // node below lists every one of ITS OWN to-one associations too, not just the ones this
        // class happens to read — an unlisted non-null to-one crashes regardless of use.
        graph.addAttributeNodes(APPLICATION_ATTR, "person");
        Subgraph<ApplicationRoleUserEntity> aruGraph = graph.addSubgraph("applicationRoleUser");
        // user is always the same row as the root u for every entry in u's own applicationRoleUser
        // collection — listed anyway since a self-reference still needs resolving like any other.
        aruGraph.addAttributeNodes("user", APPLICATION_ATTR);
        var roleGraph = aruGraph.addSubgraph("role");
        roleGraph.addAttributeNodes(APPLICATION_ATTR);
        return graph;
    }
}
