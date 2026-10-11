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
import org.springframework.data.domain.Page;

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

    /**
     * @param personId      the person ID
     * @param applicationId the application the contacts belong to; {@code null} for every application
     * @return the person's contacts for that application, ordered by contact type name
     */
    List<ContactEntity> findByPersonIdWithGraph(UUID personId, UUID applicationId);

    /**
     * @param ownerType     the kind of the external owner (e.g. {@code MERCHANT})
     * @param ownerId       the id of the external owner
     * @param applicationId the application the contacts belong to; {@code null} for every application
     * @return the owner's contacts, ordered by contact type name
     */
    List<ContactEntity> findByOwnerWithGraph(String ownerType, UUID ownerId, UUID applicationId);

    /**
     * Server-side paginated, filtered contact directory of one application.
     *
     * @param criteria the filters ({@code applicationId} is mandatory)
     * @param page     zero-based page index
     * @param size     page size
     * @return the requested page
     */
    Page<ContactEntity> search(ContactSearchCriteria criteria, int page, int size);
}
