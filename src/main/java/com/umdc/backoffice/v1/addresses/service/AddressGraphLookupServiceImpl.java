/*
 *  @(#)AddressGraphLookupServiceImpl.java
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

package com.umdc.backoffice.v1.addresses.service;

import com.umdc.persistence.general.domains.AddressEntity;
import com.umdc.persistence.general.repositories.AddressRepository;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * {@code AddressEntity.person} is {@code @ManyToOne(fetch = LAZY)}, and the
 * {@code PersonEntity} it points to carries its own {@code @OneToMany(fetch = EAGER)}
 * {@code contacts} collection (see {@code com.umdc.persistence} — that module owns the entities,
 * not this one). {@link AddressRepository#findById} (plain {@code JpaRepository} method) JOIN
 * FETCHes neither.
 * <p>
 * Under the JVM a runtime-generated {@code HibernateProxy} backs the LAZY {@code person}
 * reference, harmlessly; under native-image (Spring Boot sets
 * {@code hibernate.bytecode.provider=none} — runtime bytecode generation is impossible in a
 * closed-world native binary) that throws {@code HibernateException: "Generation of
 * HibernateProxy instances at runtime is not allowed"} during row hydration, before
 * {@link AddressMapper} (which reads {@code person.id} but never {@code person.contacts}) runs.
 * <p>
 * A {@code jakarta.persistence.fetchgraph} listing {@code person} as a bare leaf (no subgraph)
 * loads it as real data — satisfying {@code AddressMapper} — while forcing its OWN
 * {@code contacts} to LAZY regardless of its mapped fetch type (unlisted attributes are forced to
 * LAZY on every entity type the graph reaches, not just the root). Left genuinely untouched here,
 * that LAZY collection is safe under native-image: Hibernate represents it with an
 * already-compiled {@code PersistentList} wrapper and defers the SELECT until first access, no
 * bytecode-generated proxy needed (unlike the LAZY to-one this whole class exists to avoid).
 */
@Service
public class AddressGraphLookupServiceImpl implements AddressGraphLookupService {

    private static final String FETCH_GRAPH_HINT = "jakarta.persistence.fetchgraph";

    private static final String FIND_BY_ID = "SELECT a FROM AddressEntity a WHERE a.id = :id";

    private static final String FIND_BY_PERSON_ID =
            "SELECT a FROM AddressEntity a WHERE a.person.id = :personId";

    private final EntityManager entityManager;

    /**
     * @param entityManager used to run the lookup queries
     */
    public AddressGraphLookupServiceImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<AddressEntity> findByIdWithGraph(UUID addressId) {
        return entityManager.createQuery(FIND_BY_ID, AddressEntity.class)
                .setParameter("id", addressId)
                .setHint(FETCH_GRAPH_HINT, buildGraph())
                .getResultStream()
                .findFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AddressEntity> findByPersonIdWithGraph(UUID personId) {
        return entityManager.createQuery(FIND_BY_PERSON_ID, AddressEntity.class)
                .setParameter("personId", personId)
                .setHint(FETCH_GRAPH_HINT, buildGraph())
                .getResultList();
    }

    private EntityGraph<AddressEntity> buildGraph() {
        EntityGraph<AddressEntity> graph = entityManager.createEntityGraph(AddressEntity.class);
        graph.addAttributeNodes("person");
        return graph;
    }
}
