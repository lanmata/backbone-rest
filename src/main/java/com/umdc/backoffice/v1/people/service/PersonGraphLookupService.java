/*
 *  @(#)PersonGraphLookupService.java
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

import com.umdc.persistence.general.domains.PersonEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Person lookups with {@code contacts} either suppressed to lazy (for callers that only need the
 * person's own id/fields) or eagerly loaded with its own graph (for callers that read contact
 * data). See {@link PersonGraphLookupServiceImpl} for why this exists instead of the plain
 * {@code PersonRepository} query methods.
 */
public interface PersonGraphLookupService {

    /**
     * @param personId the person ID
     * @return the matching person, with {@code contacts} suppressed to lazy-and-untouched — safe
     *         for callers that only need the person's own fields (e.g. to resolve the relation
     *         before persisting a different entity)
     */
    Optional<PersonEntity> findByIdSafe(UUID personId);

    /**
     * @param personId the person ID
     * @return the matching person, with {@code contacts}/{@code .contactType}/{@code .person}/
     *         {@code .application} all eagerly loaded
     */
    Optional<PersonEntity> findByIdWithContactsGraph(UUID personId);

    /**
     * @param ids the person IDs
     * @return the matching people, with the same graph as {@link #findByIdWithContactsGraph}
     */
    List<PersonEntity> findByIdsWithContactsGraph(List<UUID> ids);

    /**
     * @return every person, with the same graph as {@link #findByIdWithContactsGraph}
     */
    List<PersonEntity> findAllWithContactsGraph();
}
