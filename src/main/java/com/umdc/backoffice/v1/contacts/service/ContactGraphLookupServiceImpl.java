/*
 *  @(#)ContactGraphLookupServiceImpl.java
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

package com.umdc.backoffice.v1.contacts.service;

import com.umdc.persistence.general.domains.ContactEntity;
import com.umdc.persistence.general.repositories.ContactRepository;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * {@code ContactEntity.application} is a {@code @ManyToOne(fetch = LAZY)} field (see
 * {@code com.umdc.persistence} — that module owns the entity, not this one); {@code contactType}
 * is {@code @ManyToOne(fetch = EAGER)} and {@code person} defaults to {@code EAGER} (the JPA
 * default for {@code @ManyToOne} when no {@code fetch} is specified). Neither
 * {@link ContactRepository#findById} nor {@link ContactRepository#listByPersonId} JOIN FETCHes
 * any of them.
 * <p>
 * Under the JVM this is harmless — see the identical explanation on
 * {@code SessionUserLookupServiceImpl}. Under native-image, Spring Boot sets
 * {@code hibernate.bytecode.provider=none}, so resolving {@code application} (a LAZY to-one)
 * throws {@code HibernateException: "Generation of HibernateProxy instances at runtime is not
 * allowed"} during row hydration, before any application code runs — regardless of whether the
 * caller reads that field. {@code contactType} and {@code person}, despite being mapped EAGER,
 * still need listing in the {@code jakarta.persistence.fetchgraph} below: per the JPA spec, every
 * attribute not named in the graph is forced to LAZY regardless of its own mapped fetch type.
 * <p>
 * None of {@code ContactTypeEntity} (no associations of its own), {@code PersonEntity} (only a
 * {@code contacts} collection, safe to leave lazy) or {@code ApplicationEntity} (only its own
 * {@code applicationRoleUser} EAGER collection, also safe to leave lazy — a LAZY collection needs
 * no bytecode-generated proxy, unlike a LAZY to-one) introduces a further cycle, so each is listed
 * as a bare leaf attribute node with no subgraph needed.
 */
@Service
public class ContactGraphLookupServiceImpl implements ContactGraphLookupService {

    private static final String FETCH_GRAPH_HINT = "jakarta.persistence.fetchgraph";

    private static final String FIND_BY_ID = "SELECT ce FROM ContactEntity ce WHERE ce.id = :contactId";

    private static final String FIND_BY_PERSON_ID =
            "SELECT ce FROM ContactEntity ce WHERE ce.person.id = :personId ORDER BY ce.contactType.name";

    private final EntityManager entityManager;

    /**
     * @param entityManager used to run the lookup queries
     */
    public ContactGraphLookupServiceImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ContactEntity> findByIdWithGraph(UUID contactId) {
        return entityManager.createQuery(FIND_BY_ID, ContactEntity.class)
                .setParameter("contactId", contactId)
                .setHint(FETCH_GRAPH_HINT, buildGraph())
                .getResultStream()
                .findFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ContactEntity> findByPersonIdWithGraph(UUID personId) {
        return entityManager.createQuery(FIND_BY_PERSON_ID, ContactEntity.class)
                .setParameter("personId", personId)
                .setHint(FETCH_GRAPH_HINT, buildGraph())
                .getResultList();
    }

    private EntityGraph<ContactEntity> buildGraph() {
        EntityGraph<ContactEntity> graph = entityManager.createEntityGraph(ContactEntity.class);
        graph.addAttributeNodes("contactType", "person", "application");
        return graph;
    }
}
