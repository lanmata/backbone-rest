/*
 *  @(#)ContactGraphLookupService.java
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Contact lookups with {@code contactType}, {@code person} and {@code application} all eagerly
 * loaded. See {@link ContactGraphLookupServiceImpl} for why this exists instead of the plain
 * {@code ContactRepository} query methods.
 */
public interface ContactGraphLookupService {

    /**
     * @param contactId the contact ID
     * @return the matching contact, with {@code contactType}, {@code person} and
     *         {@code application} all eagerly loaded
     */
    Optional<ContactEntity> findByIdWithGraph(UUID contactId);

    /**
     * @param personId the person ID
     * @return the contacts belonging to that person, ordered by contact type name, with
     *         {@code contactType}, {@code person} and {@code application} all eagerly loaded
     */
    List<ContactEntity> findByPersonIdWithGraph(UUID personId);
}
