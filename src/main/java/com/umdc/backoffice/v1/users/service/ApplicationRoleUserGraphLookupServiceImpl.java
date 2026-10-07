/*
 *  @(#)ApplicationRoleUserGraphLookupServiceImpl.java
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
import com.umdc.persistence.general.domains.RoleEntity;
import com.umdc.persistence.general.domains.UserEntity;
import com.umdc.persistence.general.repositories.ApplicationRoleUserRepository;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Subgraph;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * {@code ApplicationRoleUserEntity.user}, {@code .role} and {@code .application} are each
 * {@code @ManyToOne(fetch = LAZY, optional = false)} (see {@code com.umdc.persistence} — that
 * module owns the entity, not this one). {@link ApplicationRoleUserRepository
 * #findByUserAndApplication} (a plain {@code @Query} derived method) doesn't JOIN FETCH any of
 * them.
 * <p>
 * Under the JVM this is harmless — see the identical explanation on
 * {@code SessionUserLookupServiceImpl}. Under native-image, Spring Boot sets
 * {@code hibernate.bytecode.provider=none}, so resolving any of these three non-null LAZY
 * to-ones throws {@code HibernateException: "Generation of HibernateProxy instances at runtime
 * is not allowed"} during row hydration, before any application code runs — regardless of
 * whether the caller (e.g. {@code ProfileImageServiceImpl}, {@code PermissionCheckServiceImpl})
 * actually reads that field.
 * <p>
 * {@code role} needs special handling beyond a bare fetchgraph leaf: {@code RoleEntity.roleFeatures}
 * is itself {@code @OneToMany(fetch = EAGER)} (see {@code RoleGraphLookupServiceImpl}), so merely
 * reaching a role here — even without this class reading its features — reintroduces the same
 * crash one level deeper the instant Hibernate initializes that collection. Delegating to
 * {@link RoleGraphLookupService#findByIdWithGraph}, on the same persistence context, populates
 * the role's own graph on the exact managed instance already attached to this entity's
 * {@code role} field.
 */
@Service
public class ApplicationRoleUserGraphLookupServiceImpl implements ApplicationRoleUserGraphLookupService {

    private static final String FETCH_GRAPH_HINT = "jakarta.persistence.fetchgraph";

    private static final String APPLICATION_ATTR = "application";

    private static final String FIND_BY_USER_AND_APPLICATION =
            "SELECT aru FROM ApplicationRoleUserEntity aru "
            + "WHERE aru.user.id = :userId AND aru.application.id = :applicationId";

    private final EntityManager entityManager;
    private final RoleGraphLookupService roleGraphLookupService;

    /**
     * @param entityManager          used to run the lookup query
     * @param roleGraphLookupService populates the reached role's own
     *                               {@code roleFeatures}/{@code roleFeatures.feature} graph — see
     *                               the class Javadoc for why that's needed on top of this
     *                               class's own query
     */
    public ApplicationRoleUserGraphLookupServiceImpl(EntityManager entityManager,
                                                      RoleGraphLookupService roleGraphLookupService) {
        this.entityManager = entityManager;
        this.roleGraphLookupService = roleGraphLookupService;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public Optional<ApplicationRoleUserEntity> findByUserAndApplicationWithGraph(UUID userId, UUID applicationId) {
        Optional<ApplicationRoleUserEntity> link = entityManager
                .createQuery(FIND_BY_USER_AND_APPLICATION, ApplicationRoleUserEntity.class)
                .setParameter("userId", userId)
                .setParameter("applicationId", applicationId)
                .setHint(FETCH_GRAPH_HINT, buildGraph())
                .getResultStream()
                .findFirst();
        link.map(ApplicationRoleUserEntity::getRole)
                .map(RoleEntity::getId)
                .filter(Objects::nonNull)
                .ifPresent(roleGraphLookupService::findByIdWithGraph);
        return link;
    }

    /**
     * {@code application} is a bare leaf — {@code ApplicationEntity}'s own
     * {@code applicationRoleUser} EAGER collection is left unlisted and safely reverts to LAZY.
     * {@code user}'s own {@code application}/{@code person} to-ones are listed for the same
     * reason {@code SessionUserLookupServiceImpl} lists them; its own {@code applicationRoleUser}
     * collection is deliberately left unlisted. {@code role}'s own {@code application} is listed
     * the same way; its {@code roleFeatures}/{@code applicationRoleUser} collections are
     * deliberately left unlisted — {@link RoleGraphLookupService#findByIdWithGraph} populates
     * {@code roleFeatures} separately, after this query runs.
     */
    private EntityGraph<ApplicationRoleUserEntity> buildGraph() {
        EntityGraph<ApplicationRoleUserEntity> graph = entityManager.createEntityGraph(ApplicationRoleUserEntity.class);
        graph.addAttributeNodes(APPLICATION_ATTR);
        Subgraph<UserEntity> userGraph = graph.addSubgraph("user");
        userGraph.addAttributeNodes(APPLICATION_ATTR, "person");
        graph.addSubgraph("role").addAttributeNodes(APPLICATION_ATTR);
        return graph;
    }
}
