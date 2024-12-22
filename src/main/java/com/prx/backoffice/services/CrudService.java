/*
 *  @(#)CrudService.java
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

package com.prx.backoffice.services;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Interface for CRUD operations.
 * Provides default methods for creating, updating, deleting, finding, and listing entities.
 *
 * @param <T> the type of the entity
 * @version 1.0.0, 20-10-2020
 */
public interface CrudService<T> {

    /**
     * Creates a new entity.
     *
     * @param t the entity to create
     * @return the created entity wrapped in a ResponseEntity
     * @throws NotImplementedException if the method is not implemented
     */
    default ResponseEntity<T> create(T t) {
        throw new NotImplementedException();
    }

    /**
     * Updates an existing entity.
     *
     * @param id the ID of the entity to update
     * @param t the entity with updated information
     * @return the updated entity wrapped in a ResponseEntity
     * @throws NotImplementedException if the method is not implemented
     */
    default ResponseEntity<T> update(String id, T t) {
        throw new NotImplementedException();
    }

    /**
     * Deletes an entity.
     *
     * @param id the ID of the entity to delete
     * @param t the entity to delete
     * @return the deleted entity wrapped in a ResponseEntity
     * @throws NotImplementedException if the method is not implemented
     */
    default ResponseEntity<T> delete(String id, T t) {
        throw new NotImplementedException();
    }

    /**
     * Finds an entity by its ID.
     *
     * @param id the ID of the entity to find
     * @return the found entity wrapped in a ResponseEntity
     * @throws NotImplementedException if the method is not implemented
     */
    default ResponseEntity<T> find(String id) {
        throw new NotImplementedException();
    }

    /**
     * Lists entities by their IDs.
     *
     * @param id the IDs of the entities to list
     * @return the list of entities wrapped in a ResponseEntity
     * @throws NotImplementedException if the method is not implemented
     */
    default ResponseEntity<List<T>> list(String... id) {
        throw new NotImplementedException();
    }
}
