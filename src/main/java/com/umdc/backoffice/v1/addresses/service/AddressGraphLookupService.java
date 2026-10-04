/*
 *  @(#)AddressGraphLookupService.java
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Address lookups with {@code person} eagerly loaded (its own {@code contacts} suppressed to
 * lazy). See {@link AddressGraphLookupServiceImpl} for why this exists instead of the plain
 * {@code AddressRepository} query methods.
 */
public interface AddressGraphLookupService {

    /**
     * @param addressId the address ID
     * @return the matching address, with {@code person} eagerly loaded
     */
    Optional<AddressEntity> findByIdWithGraph(UUID addressId);

    /**
     * @param personId the person ID
     * @return the addresses belonging to that person, with {@code person} eagerly loaded
     */
    List<AddressEntity> findByPersonIdWithGraph(UUID personId);
}
