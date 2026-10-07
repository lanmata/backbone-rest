/*
 *  @(#)PersonGraphLookupServiceImpl.java
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

package com.umdc.backoffice.v1.people.service;

import com.umdc.persistence.general.domains.ContactEntity;
import com.umdc.persistence.general.domains.PersonEntity;
import com.umdc.persistence.general.repositories.PersonRepository;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Subgraph;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * {@code PersonEntity.contacts} is {@code @OneToMany(fetch = EAGER)} whose entries
 * ({@code ContactEntity.contactType}/{@code .person}/{@code .application}) are each a to-one
 * association (see {@code com.umdc.persistence} — that module owns the entities, not this one).
 * {@link PersonRepository#findById} (plain {@code JpaRepository} method) JOIN FETCHes none of
 * these, so Hibernate unconditionally initializes {@code contacts} the moment any
 * {@code PersonEntity} is loaded by any path, and each row then needs its own to-ones resolved.
 * <p>
 * Under the JVM a runtime-generated {@code HibernateProxy} backs each of those, harmlessly; under
 * native-image (Spring Boot sets {@code hibernate.bytecode.provider=none} — runtime bytecode
 * generation is impossible in a closed-world native binary) that throws
 * {@code HibernateException: "Generation of HibernateProxy instances at runtime is not allowed"},
 * during collection/row hydration, before any application code runs.
 * <p>
 * Two different {@code jakarta.persistence.fetchgraph}s cover the two real needs here:
 * {@link #findByIdSafe} uses an EMPTY graph — forcing {@code contacts} (the only attribute
 * {@code PersonEntity} has) to LAZY regardless of its own mapping, which is safe as long as it's
 * left genuinely untouched (a LAZY *collection* needs no bytecode-generated proxy, unlike a LAZY
 * to-one — Hibernate represents it with an already-compiled {@code PersistentList} wrapper and
 * defers the SELECT until first access). {@link #findByIdWithContactsGraph} and its bulk variants
 * use a graph that DOES list {@code contacts}/{@code .contactType}/{@code .person}/
 * {@code .application} as real data, for callers ({@code PersonMapper}) that actually read contact
 * details.
 */
@Service
public class PersonGraphLookupServiceImpl implements PersonGraphLookupService {

    private static final String FETCH_GRAPH_HINT = "jakarta.persistence.fetchgraph";

    private static final String FIND_BY_ID = "SELECT p FROM PersonEntity p WHERE p.id = :id";

    private static final String FIND_BY_IDS = "SELECT DISTINCT p FROM PersonEntity p WHERE p.id IN :ids";

    private static final String FIND_ALL = "SELECT DISTINCT p FROM PersonEntity p";

    private final EntityManager entityManager;

    /**
     * @param entityManager used to run the lookup queries
     */
    public PersonGraphLookupServiceImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<PersonEntity> findByIdSafe(UUID personId) {
        return entityManager.createQuery(FIND_BY_ID, PersonEntity.class)
                .setParameter("id", personId)
                .setHint(FETCH_GRAPH_HINT, entityManager.createEntityGraph(PersonEntity.class))
                .getResultStream()
                .findFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<PersonEntity> findByIdWithContactsGraph(UUID personId) {
        return entityManager.createQuery(FIND_BY_ID, PersonEntity.class)
                .setParameter("id", personId)
                .setHint(FETCH_GRAPH_HINT, buildContactsGraph())
                .getResultStream()
                .findFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PersonEntity> findByIdsWithContactsGraph(List<UUID> ids) {
        return entityManager.createQuery(FIND_BY_IDS, PersonEntity.class)
                .setParameter("ids", ids)
                .setHint(FETCH_GRAPH_HINT, buildContactsGraph())
                .getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PersonEntity> findAllWithContactsGraph() {
        return entityManager.createQuery(FIND_ALL, PersonEntity.class)
                .setHint(FETCH_GRAPH_HINT, buildContactsGraph())
                .getResultList();
    }

    private EntityGraph<PersonEntity> buildContactsGraph() {
        EntityGraph<PersonEntity> graph = entityManager.createEntityGraph(PersonEntity.class);
        Subgraph<ContactEntity> contactsGraph = graph.addSubgraph("contacts");
        contactsGraph.addAttributeNodes("contactType", "person", "application");
        return graph;
    }
}
