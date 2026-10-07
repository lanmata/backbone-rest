/*
 *  @(#)IdentificationDocumentGraphLookupServiceImpl.java
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

package com.umdc.backoffice.v1.identificationdocuments.service;

import com.umdc.persistence.general.domains.IdentificationDocumentEntity;
import com.umdc.persistence.general.repositories.IdentificationDocumentRepository;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * {@code IdentificationDocumentEntity.person} is {@code @ManyToOne(fetch = LAZY)}, and the
 * {@code PersonEntity} it points to carries its own {@code @OneToMany(fetch = EAGER)}
 * {@code contacts} collection (see {@code com.umdc.persistence} — that module owns the entities,
 * not this one). {@link IdentificationDocumentRepository#findById} (plain {@code JpaRepository}
 * method) JOIN FETCHes neither.
 * <p>
 * Under the JVM a runtime-generated {@code HibernateProxy} backs the LAZY {@code person}
 * reference, harmlessly; under native-image (Spring Boot sets
 * {@code hibernate.bytecode.provider=none} — runtime bytecode generation is impossible in a
 * closed-world native binary) that throws {@code HibernateException: "Generation of
 * HibernateProxy instances at runtime is not allowed"} during row hydration, before
 * {@code IdentificationDocumentServiceImpl} (which reads {@code person.id} but never
 * {@code person.contacts}) runs.
 * <p>
 * A {@code jakarta.persistence.fetchgraph} listing {@code person} as a bare leaf (no subgraph)
 * loads it as real data while forcing its OWN {@code contacts} to LAZY regardless of its mapped
 * fetch type (unlisted attributes are forced to LAZY on every entity type the graph reaches, not
 * just the root). Left genuinely untouched here, that LAZY collection is safe under native-image:
 * Hibernate represents it with an already-compiled {@code PersistentList} wrapper and defers the
 * SELECT until first access, no bytecode-generated proxy needed (unlike the LAZY to-one this whole
 * class exists to avoid).
 */
@Service
public class IdentificationDocumentGraphLookupServiceImpl implements IdentificationDocumentGraphLookupService {

    private static final String FETCH_GRAPH_HINT = "jakarta.persistence.fetchgraph";

    private static final String FIND_BY_ID = "SELECT d FROM IdentificationDocumentEntity d WHERE d.id = :id";

    private static final String FIND_BY_PERSON_ID =
            "SELECT d FROM IdentificationDocumentEntity d WHERE d.person.id = :personId";

    private final EntityManager entityManager;

    /**
     * @param entityManager used to run the lookup queries
     */
    public IdentificationDocumentGraphLookupServiceImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<IdentificationDocumentEntity> findByIdWithGraph(UUID id) {
        return entityManager.createQuery(FIND_BY_ID, IdentificationDocumentEntity.class)
                .setParameter("id", id)
                .setHint(FETCH_GRAPH_HINT, buildGraph())
                .getResultStream()
                .findFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IdentificationDocumentEntity> findByPersonIdWithGraph(UUID personId) {
        return entityManager.createQuery(FIND_BY_PERSON_ID, IdentificationDocumentEntity.class)
                .setParameter("personId", personId)
                .setHint(FETCH_GRAPH_HINT, buildGraph())
                .getResultList();
    }

    private EntityGraph<IdentificationDocumentEntity> buildGraph() {
        EntityGraph<IdentificationDocumentEntity> graph = entityManager.createEntityGraph(IdentificationDocumentEntity.class);
        graph.addAttributeNodes("person");
        return graph;
    }
}
