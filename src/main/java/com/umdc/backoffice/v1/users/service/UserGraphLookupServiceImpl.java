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
import jakarta.transaction.Transactional;
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
 * <p>
 * The two collections are deliberately loaded with <strong>two separate queries</strong> rather
 * than one: {@code person.contacts} is a {@code List} (a bag — {@code PersonEntity} declares no
 * {@code @OrderColumn}) and {@code applicationRoleUser} is a {@code Set}. JOIN FETCHing a bag
 * and another collection together in the same query produces a SQL cartesian product — Hibernate
 * only collapses duplicate root {@code UserEntity} rows ({@code SELECT DISTINCT}), it does not
 * deduplicate the bag itself, so a user with 2 contacts and 3 role links would come back with 6
 * contact entries. Running two queries against the same {@link EntityManager} inside one
 * {@code @Transactional} boundary avoids this: each query fetches only one to-many collection at
 * a time (safe — a single bag can't cartesian-product against itself), and Hibernate's
 * persistence-context identity map attaches the second query's result to the exact same managed
 * {@code UserEntity} instance the first query already returned.
 */
@Service
public class UserGraphLookupServiceImpl implements UserGraphLookupService {

    private static final String FIND_USER_BY_ID_WITH_APPLICATION_AND_CONTACTS =
            "SELECT DISTINCT u FROM UserEntity u "
            + "LEFT JOIN FETCH u.application "
            + "LEFT JOIN FETCH u.person p "
            + "LEFT JOIN FETCH p.contacts "
            + "WHERE u.id = :userId";

    private static final String FETCH_APPLICATION_ROLE_USER_FOR_USER =
            "SELECT DISTINCT u FROM UserEntity u "
            + "LEFT JOIN FETCH u.applicationRoleUser ar "
            + "LEFT JOIN FETCH ar.role "
            + "LEFT JOIN FETCH ar.application "
            + "WHERE u.id = :userId";

    private final EntityManager entityManager;

    /**
     * @param entityManager used to run the JOIN FETCH lookups
     */
    public UserGraphLookupServiceImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public Optional<UserEntity> findByIdWithGraph(UUID userId) {
        Optional<UserEntity> user = entityManager
                .createQuery(FIND_USER_BY_ID_WITH_APPLICATION_AND_CONTACTS, UserEntity.class)
                .setParameter("userId", userId)
                .getResultStream()
                .findFirst();
        if (user.isEmpty()) {
            return Optional.empty();
        }
        // Populates applicationRoleUser/role/application on the same managed instance `user`
        // already holds — same EntityManager, same persistence context, same transaction.
        entityManager.createQuery(FETCH_APPLICATION_ROLE_USER_FOR_USER, UserEntity.class)
                .setParameter("userId", userId)
                .getResultStream()
                .findFirst();
        return user;
    }
}
