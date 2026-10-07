/*
 *  @(#)UserGraphLookupServiceImpl.java
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

package com.umdc.backoffice.v1.users.service;

import com.umdc.backoffice.v1.roles.service.RoleGraphLookupService;
import com.umdc.persistence.general.domains.ApplicationRoleUserEntity;
import com.umdc.persistence.general.domains.ContactEntity;
import com.umdc.persistence.general.domains.RoleEntity;
import com.umdc.persistence.general.domains.UserEntity;
import com.umdc.persistence.general.repositories.UserRepository;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Subgraph;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * {@code UserEntity.application} is a {@code @ManyToOne(fetch = LAZY, optional = false)} field
 * directly on the entity (see {@code com.umdc.persistence} — that module owns the entity, not
 * this one), separate from the {@code applicationRoleUser} collection. {@link UserRepository
 * #findById} (plain {@code JpaRepository} method) doesn't JOIN FETCH it, or the nested
 * {@code role} / {@code application} on each {@code applicationRoleUser} entry either.
 * <p>
 * Under the JVM this is harmless: every LAZY to-one gets backed by a runtime-generated
 * {@code HibernateProxy} the instant its owning row is hydrated — there's no "stay truly
 * uninitialized until accessed" option without build-time bytecode enhancement (not configured
 * here); a non-enhanced lazy to-one is always populated with either real data or a proxy at
 * hydration time, whether or not application code ever reads it — and every EAGER collection is
 * populated too. Under native-image, Spring Boot sets {@code hibernate.bytecode.provider=none}
 * (runtime bytecode generation is impossible in a closed-world native binary), so proxy creation
 * throws {@code HibernateException: "Generation of HibernateProxy instances at runtime is not
 * allowed"} instead — during row/collection hydration, before any application code runs. This is
 * the same root cause as {@code SessionUserLookupServiceImpl} (see that class), just reached
 * through {@code UserServiceImpl}'s update/unlink/roleLink/delete paths instead of login.
 * <p>
 * A plain JOIN FETCH list can't win against this: JOIN FETCHing a LAZY to-one (say,
 * {@code u.application}) loads real data for THAT field, but {@code u.application} is a real,
 * managed {@code ApplicationEntity} now, and {@code ApplicationEntity.applicationRoleUser} is
 * EAGER too (a per-entity-instance mapping; a single query's JOIN FETCH list has no say over an
 * association it doesn't ask for), whose rows need yet more LAZY to-ones resolved — rows that are
 * frequently some entity not already loaded elsewhere in this query. Chasing this down with more
 * JOIN FETCH never terminates: {@code Role.applicationRoleUser} → {@code User} →
 * {@code User.applicationRoleUser} → {@code Role} → ... is a real cycle in this entity graph.
 * <p>
 * The fix is a {@code jakarta.persistence.fetchgraph} {@link EntityGraph} instead of JOIN FETCH:
 * per the JPA spec, attributes named in the graph are eager and EVERYTHING ELSE — on every entity
 * type the graph reaches, not just the root — is forced to LAZY, regardless of its own mapping. A
 * LAZY *collection* left genuinely untouched is safe under native-image (Hibernate represents it
 * with an already-compiled {@code PersistentSet}/{@code PersistentBag} wrapper and defers the
 * SELECT until first access — no bytecode-generated proxy class needed, unlike a LAZY to-one). So
 * each graph below lists every to-one actually needed as real data, but deliberately stops before
 * listing a reached {@code RoleEntity}'s own {@code roleFeatures}/{@code applicationRoleUser} (see
 * {@link #fetchRoleSubGraph}) or a reached {@code UserEntity}'s own {@code applicationRoleUser} —
 * nothing here reads that far, and leaving it unlisted is what breaks the cycle.
 * <p>
 * The two collections ({@code person.contacts}, a {@code List}/bag, and
 * {@code applicationRoleUser}, a {@code Set}) are still loaded with <strong>two separate
 * queries</strong> rather than one: JOIN FETCHing a bag and another collection together in the
 * same query produces a SQL cartesian product — Hibernate only collapses duplicate root
 * {@code UserEntity} rows ({@code SELECT DISTINCT}), it does not deduplicate the bag itself, so a
 * user with 2 contacts and 3 role links would come back with 6 contact entries. Running two
 * queries against the same {@link EntityManager} inside one {@code @Transactional} boundary avoids
 * this: each query fetches only one to-many collection at a time, and Hibernate's
 * persistence-context identity map attaches the second query's result to the exact same managed
 * {@code UserEntity} instance the first query already returned.
 */
@Service
public class UserGraphLookupServiceImpl implements UserGraphLookupService {

    private static final String FETCH_GRAPH_HINT = "jakarta.persistence.fetchgraph";

    private static final String APPLICATION_ATTR = "application";

    private static final String APPLICATION_ID_PARAM = "applicationId";

    private static final String FIND_USER_BY_ID = "SELECT u FROM UserEntity u WHERE u.id = :userId";

    private static final String FETCH_APPLICATION_ROLE_USER_FOR_USER =
            "SELECT DISTINCT u FROM UserEntity u LEFT JOIN FETCH u.applicationRoleUser WHERE u.id = :userId";

    private static final String FIND_USER_BY_ALIAS_AND_APPLICATION =
            "SELECT u FROM UserEntity u "
            + "JOIN u.applicationRoleUser ar "
            + "WHERE u.alias = :alias AND ar.application.id = :applicationId";

    private static final String FIND_USER_BY_EMAIL_AND_APPLICATION =
            "SELECT u FROM UserEntity u "
            + "JOIN u.applicationRoleUser ar "
            + "WHERE u.email = :email AND ar.application.id = :applicationId";

    private static final String FIND_USER_BY_ALIAS = "SELECT u FROM UserEntity u WHERE u.alias = :alias";

    private static final String FIND_USERS_BY_APPLICATION =
            "SELECT u FROM UserEntity u JOIN u.applicationRoleUser ar WHERE ar.application.id = :applicationId";

    private final EntityManager entityManager;
    private final RoleGraphLookupService roleGraphLookupService;

    /**
     * @param entityManager          used to run the lookup queries
     * @param roleGraphLookupService populates each reached role's own
     *                               {@code roleFeatures}/{@code applicationRoleUser} graph — see
     *                               {@link #fetchRoleSubGraph} for why that's needed on top of
     *                               this class's own queries
     */
    public UserGraphLookupServiceImpl(EntityManager entityManager, RoleGraphLookupService roleGraphLookupService) {
        this.entityManager = entityManager;
        this.roleGraphLookupService = roleGraphLookupService;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public Optional<UserEntity> findByIdWithGraph(UUID userId) {
        Optional<UserEntity> user = entityManager.createQuery(FIND_USER_BY_ID, UserEntity.class)
                .setParameter("userId", userId)
                .setHint(FETCH_GRAPH_HINT, buildApplicationAndContactsGraph())
                .getResultStream()
                .findFirst();
        if (user.isEmpty()) {
            return Optional.empty();
        }
        fetchApplicationRoleUser(userId);
        fetchRoleSubGraph(user.get());
        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public Optional<UserEntity> findByAliasAndApplicationWithGraph(String alias, UUID applicationId) {
        Optional<UserEntity> user = entityManager.createQuery(FIND_USER_BY_ALIAS_AND_APPLICATION, UserEntity.class)
                .setParameter("alias", alias)
                .setParameter(APPLICATION_ID_PARAM, applicationId)
                .setHint(FETCH_GRAPH_HINT, buildApplicationAndContactsGraph())
                .getResultStream()
                .findFirst();
        return finishLoadingGraph(user);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public Optional<UserEntity> findByEmailAndApplicationWithGraph(String email, UUID applicationId) {
        Optional<UserEntity> user = entityManager.createQuery(FIND_USER_BY_EMAIL_AND_APPLICATION, UserEntity.class)
                .setParameter("email", email)
                .setParameter(APPLICATION_ID_PARAM, applicationId)
                .setHint(FETCH_GRAPH_HINT, buildApplicationAndContactsGraph())
                .getResultStream()
                .findFirst();
        return finishLoadingGraph(user);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public Optional<UserEntity> findByAliasWithGraph(String alias) {
        Optional<UserEntity> user = entityManager.createQuery(FIND_USER_BY_ALIAS, UserEntity.class)
                .setParameter("alias", alias)
                .setHint(FETCH_GRAPH_HINT, buildApplicationAndContactsGraph())
                .getResultStream()
                .findFirst();
        return finishLoadingGraph(user);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public List<UserEntity> findByApplicationWithGraph(UUID applicationId) {
        List<UserEntity> users = entityManager.createQuery(FIND_USERS_BY_APPLICATION, UserEntity.class)
                .setParameter(APPLICATION_ID_PARAM, applicationId)
                .setHint(FETCH_GRAPH_HINT, buildApplicationAndContactsGraph())
                .getResultList();
        List<UUID> ids = users.stream().map(UserEntity::getId).toList();
        if (!ids.isEmpty()) {
            entityManager.createQuery("SELECT DISTINCT u FROM UserEntity u LEFT JOIN FETCH u.applicationRoleUser WHERE u.id IN :userIds", UserEntity.class)
                    .setParameter("userIds", ids)
                    .setHint(FETCH_GRAPH_HINT, buildApplicationRoleUserGraph())
                    .getResultList();
            var roleIds = users.stream().filter(u -> u.getApplicationRoleUser() != null)
                    .flatMap(u -> u.getApplicationRoleUser().stream())
                    .map(ApplicationRoleUserEntity::getRole).filter(Objects::nonNull)
                    .map(RoleEntity::getId).distinct().toList();
            if (!roleIds.isEmpty()) roleGraphLookupService.findByIdsWithGraph(roleIds);
        }
        return users;
    }

    private Optional<UserEntity> finishLoadingGraph(Optional<UserEntity> user) {
        if (user.isEmpty()) {
            return Optional.empty();
        }
        fetchApplicationRoleUser(user.get().getId());
        fetchRoleSubGraph(user.get());
        return user;
    }

    /**
     * Populates {@code applicationRoleUser}/{@code .role}/{@code .application}/{@code .user} on
     * the same managed instance the primary query already returned — same {@link EntityManager},
     * same persistence context, same transaction.
     */
    private void fetchApplicationRoleUser(UUID userId) {
        entityManager.createQuery(FETCH_APPLICATION_ROLE_USER_FOR_USER, UserEntity.class)
                .setParameter("userId", userId)
                .setHint(FETCH_GRAPH_HINT, buildApplicationRoleUserGraph())
                .getResultList();
    }

    /**
     * {@code ar.role} above loads real {@code RoleEntity} instances, not lazy references — but
     * {@code RoleEntity.roleFeatures} and {@code RoleEntity.applicationRoleUser} are themselves
     * {@code @OneToMany(fetch = EAGER)} (see {@code RoleGraphLookupServiceImpl}), so Hibernate
     * unconditionally initializes those too the moment any {@code RoleEntity} is loaded by any
     * path — merely reaching a role here, even without this class using its features/links,
     * reintroduces the exact same proxy-creation crash one level deeper. Delegating to
     * {@link RoleGraphLookupService#findByIdsWithGraph}, on the same transaction/persistence
     * context, populates every reached role's own graph on the exact managed instances already
     * attached to {@code user.getApplicationRoleUser()}.
     */
    private void fetchRoleSubGraph(UserEntity user) {
        if (Objects.isNull(user.getApplicationRoleUser())) {
            return;
        }
        var roleIds = user.getApplicationRoleUser().stream()
                .map(ApplicationRoleUserEntity::getRole)
                .filter(Objects::nonNull)
                .map(RoleEntity::getId)
                .distinct()
                .toList();
        if (!roleIds.isEmpty()) {
            roleGraphLookupService.findByIdsWithGraph(roleIds);
        }
    }

    /**
     * Graph for the primary query: {@code application} (the user's own) and
     * {@code person}/{@code person.contacts} — {@code person} is listed even though it's mapped
     * {@code @OneToOne(fetch = EAGER)} because {@code fetchgraph} forces every unlisted attribute
     * to LAZY regardless of its own mapped fetch type. Each {@code contacts} entry's own
     * {@code contactType} and {@code person}/{@code application} to-ones are listed too, for the
     * same reason ({@code PersonMapper} reads {@code contactType}).
     */
    private EntityGraph<UserEntity> buildApplicationAndContactsGraph() {
        EntityGraph<UserEntity> graph = entityManager.createEntityGraph(UserEntity.class);
        graph.addAttributeNodes(APPLICATION_ATTR);
        Subgraph<ContactEntity> contactsGraph = graph.addSubgraph("person").addSubgraph("contacts");
        contactsGraph.addAttributeNodes("contactType", "person", APPLICATION_ATTR);
        return graph;
    }

    /**
     * Graph for the {@code applicationRoleUser} follow-up query: {@code role}/{@code role
     * .application}, {@code application} and {@code user} (the exact same row as the root for
     * every entry here — listed anyway since even a self-reference still needs resolving like any
     * other). {@code role}'s own {@code roleFeatures}/{@code applicationRoleUser} are deliberately
     * NOT listed — {@link #fetchRoleSubGraph} populates those separately, on the same persistence
     * context, only for the roles actually reached.
     */
    private EntityGraph<UserEntity> buildApplicationRoleUserGraph() {
        EntityGraph<UserEntity> graph = entityManager.createEntityGraph(UserEntity.class);
        Subgraph<ApplicationRoleUserEntity> aruGraph = graph.addSubgraph("applicationRoleUser");
        aruGraph.addAttributeNodes(APPLICATION_ATTR, "user");
        aruGraph.addSubgraph("role").addAttributeNodes(APPLICATION_ATTR);
        return graph;
    }
}
