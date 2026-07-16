/*
 *  @(#)NoticeTypeService.java
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
package com.umdc.backoffice.v1.noticetypes.service;

import com.umdc.backoffice.v1.noticetypes.api.to.NoticeType;
import com.umdc.commons.services.CrudService;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

/**
 * Interface for notice type operations.
 * Extends the CrudService interface to provide CRUD operations for notice types.
 */
public interface NoticeTypeService extends CrudService<UUID, NoticeType> {

    /**
     * Creates a new notice type.
     *
     * @param noticeType the notice type to create
     * @return the created notice type wrapped in a ResponseEntity
     * @throws NotImplementedException if the method is not implemented
     */
    @Override
    default ResponseEntity<NoticeType> create(NoticeType noticeType) {
        throw new NotImplementedException();
    }

    /**
     * Finds a notice type by its ID.
     *
     * @param id the ID of the notice type to find
     * @return the found notice type wrapped in a ResponseEntity
     * @throws NotImplementedException if the method is not implemented
     */
    @Override
    default ResponseEntity<NoticeType> find(UUID id) {
        throw new NotImplementedException();
    }

    /**
     * Updates an existing notice type.
     *
     * @param id         the ID of the notice type to update
     * @param noticeType the notice type with updated information
     * @return the updated notice type wrapped in a ResponseEntity
     * @throws NotImplementedException if the method is not implemented
     */
    @Override
    default ResponseEntity<NoticeType> update(UUID id, NoticeType noticeType) {
        throw new NotImplementedException();
    }

    /**
     * Deletes a notice type.
     *
     * @param id         the ID of the notice type to delete
     * @param noticeType the notice type to delete
     * @return the deleted notice type wrapped in a ResponseEntity
     * @throws NotImplementedException if the method is not implemented
     */
    @Override
    default ResponseEntity<NoticeType> delete(UUID id, NoticeType noticeType) {
        throw new NotImplementedException();
    }

    /**
     * Lists notice types by their IDs.
     *
     * @param id the IDs of the notice types to list
     * @return a list of notice types wrapped in a ResponseEntity
     * @throws NotImplementedException if the method is not implemented
     */
    @Override
    default ResponseEntity<List<NoticeType>> list(UUID... id) {
        throw new NotImplementedException();
    }

    /**
     * Returns all registered notice types.
     *
     * @return all notice types wrapped in a ResponseEntity
     */
    default ResponseEntity<List<NoticeType>> listAll() {
        throw new NotImplementedException();
    }
}
