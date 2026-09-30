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

import com.umdc.persistence.general.domains.UserEntity;
import com.umdc.persistence.general.repositories.UserRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * {@code UserEntity.application} is a {@code @ManyToOne(fetch = LAZY, optional = false)} field
 * directly on the entity (see {@code com.umdc.persistence} — that module owns the entity, not
 * this one), separate from the {@code applicationRoleUser} collection. {@link UserRepository
 * #findById} (plain {@code JpaRepository} method) doesn't JOIN FETCH it, or the nested
 * {@code role} / {@code application} on each {@code applicationRoleUser} entry either.
 * <p>
 * Under the JVM this is harmless: Hibernate backs each LAZY reference with a
 * runtime-generated {@code HibernateProxy} the moment the owning row is hydrated, and nothing
 * breaks unless/until a getter is called through one. Under native-image, Spring Boot sets
 * {@code hibernate.bytecode.provider=none} (runtime bytecode generation is impossible in a
 * closed-world native binary), so that same proxy-creation attempt throws
 * {@code HibernateException: "Generation of HibernateProxy instances at runtime is not
 * allowed"} — during row hydration itself, before any application code runs. This is the same
 * root cause as {@code SessionUserLookupServiceImpl} (see that class), just reached through
 * {@code UserServiceImpl}'s update/unlink/roleLink/delete paths instead of login.
 * <p>
 * JOIN FETCHing {@code application}, {@code person}/{@code person.contacts} and
 * {@code applicationRoleUser}/{@code role}/{@code application} all directly loads them as real
 * data instead of a lazy reference, so Hibernate never attempts the proxy path. {@code person}
 * is fetched with a {@code LEFT} join (not {@code INNER}, unlike {@link UserRepository
 * #findUserInfo}) since it's an optional one-to-one — an inner join would silently exclude any
 * user without a person record.
 */
@Service
public class UserGraphLookupServiceImpl implements UserGraphLookupService {

    private static final String FIND_USER_BY_ID_WITH_GRAPH =
            "SELECT DISTINCT u FROM UserEntity u "
            + "LEFT JOIN FETCH u.application "
            + "LEFT JOIN FETCH u.person p "
            + "LEFT JOIN FETCH p.contacts "
            + "LEFT JOIN FETCH u.applicationRoleUser ar "
            + "LEFT JOIN FETCH ar.role "
            + "LEFT JOIN FETCH ar.application "
            + "WHERE u.id = :userId";

    private final EntityManager entityManager;

    /**
     * @param entityManager used to run the JOIN FETCH lookup
     */
    public UserGraphLookupServiceImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<UserEntity> findByIdWithGraph(UUID userId) {
        return entityManager.createQuery(FIND_USER_BY_ID_WITH_GRAPH, UserEntity.class)
                .setParameter("userId", userId)
                .getResultStream()
                .findFirst();
    }
}
