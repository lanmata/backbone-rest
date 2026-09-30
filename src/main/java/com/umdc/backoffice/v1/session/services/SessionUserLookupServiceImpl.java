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

import com.umdc.persistence.general.domains.UserEntity;
import com.umdc.persistence.general.repositories.UserRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * {@link UserRepository#findByAliasAndApplication} and
 * {@link UserRepository#findByEmailAndApplication} only plain-JOIN {@code applicationRoleUser}
 * (needed for their WHERE filter) — they don't JOIN FETCH it or its nested {@code role} /
 * {@code application} associations. {@code applicationRoleUser} is mapped {@code fetch = EAGER}
 * while {@code role} and {@code application} are each {@code fetch = LAZY @ManyToOne} (see
 * {@code com.umdc.persistence} — that module owns the entities, not this one).
 * <p>
 * Under the JVM this is harmless: the moment the EAGER collection is materialized, Hibernate
 * backs each LAZY reference with a runtime-generated {@code HibernateProxy}, and nothing in this
 * codebase calls a role/application getter through that proxy for it to matter. Under
 * native-image, Spring Boot sets {@code hibernate.bytecode.provider=none} (runtime bytecode
 * generation is impossible in a closed-world native binary), so that same proxy-creation attempt
 * throws {@code HibernateException: "Generation of HibernateProxy instances at runtime is not
 * allowed"} — during row materialization, before any application code runs.
 * <p>
 * JOIN FETCHing {@code role} and {@code application} directly (mirroring the already-working
 * {@link UserRepository#findUserInfo} query) loads them as real data instead of a lazy reference,
 * so Hibernate never attempts the proxy path. Kept as its own collaborator, rather than inlined
 * into {@code SessionServiceImpl}, to keep that class's own responsibilities (and PMD's
 * GodClass/WMC metrics on it) in check.
 */
@Service
public class SessionUserLookupServiceImpl implements SessionUserLookupService {

    private static final String FIND_USER_BY_ALIAS_AND_APPLICATION =
            "SELECT u FROM UserEntity u "
            + "JOIN FETCH u.applicationRoleUser ar "
            + "LEFT JOIN FETCH ar.role "
            + "LEFT JOIN FETCH ar.application "
            + "WHERE u.alias = :alias AND ar.application.id = :applicationId";

    private static final String FIND_USER_BY_EMAIL_AND_APPLICATION =
            "SELECT u FROM UserEntity u "
            + "JOIN FETCH u.applicationRoleUser ar "
            + "LEFT JOIN FETCH ar.role "
            + "LEFT JOIN FETCH ar.application "
            + "WHERE u.email = :email AND ar.application.id = :applicationId";

    private final EntityManager entityManager;

    /**
     * @param entityManager used to run the JOIN FETCH lookups
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
                .getResultStream()
                .findFirst();
    }
}
