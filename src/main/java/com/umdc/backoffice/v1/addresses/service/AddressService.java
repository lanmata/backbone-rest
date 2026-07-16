/*
 *  @(#)AddressService.java
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

import com.umdc.backoffice.v1.addresses.api.to.Address;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

/**
 * Interface for address operations.
 */
public interface AddressService {

    /**
     * Creates a new address.
     *
     * @param address the address to create
     * @return the created address wrapped in a ResponseEntity
     * @throws NotImplementedException if the method is not implemented
     */
    default ResponseEntity<Address> create(Address address) {
        throw new NotImplementedException();
    }

    /**
     * Finds an address by its ID.
     *
     * @param id the ID of the address to find
     * @return the found address wrapped in a ResponseEntity
     * @throws NotImplementedException if the method is not implemented
     */
    default ResponseEntity<Address> find(UUID id) {
        throw new NotImplementedException();
    }

    /**
     * Updates an existing address.
     *
     * @param id      the ID of the address to update
     * @param address the address with updated information
     * @return the updated address wrapped in a ResponseEntity
     * @throws NotImplementedException if the method is not implemented
     */
    default ResponseEntity<Address> update(UUID id, Address address) {
        throw new NotImplementedException();
    }

    /**
     * Deletes an address.
     *
     * @param id the ID of the address to delete
     * @return the deleted address wrapped in a ResponseEntity
     * @throws NotImplementedException if the method is not implemented
     */
    default ResponseEntity<Address> delete(UUID id) {
        throw new NotImplementedException();
    }

    /**
     * Lists addresses belonging to a given person.
     *
     * @param personId the person ID whose addresses are requested
     * @return the addresses wrapped in a ResponseEntity
     * @throws NotImplementedException if the method is not implemented
     */
    default ResponseEntity<List<Address>> listByPerson(UUID personId) {
        throw new NotImplementedException();
    }
}
