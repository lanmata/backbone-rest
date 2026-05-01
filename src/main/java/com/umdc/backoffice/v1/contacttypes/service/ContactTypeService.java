/*
 *  @(#)ContactTypeService.java
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

package com.umdc.backoffice.v1.contacttypes.service;

import com.umdc.backoffice.v1.contacttypes.api.to.ContactTypeRequest;
import com.umdc.commons.general.pojo.ContactType;
import com.umdc.commons.services.CrudService;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

/**
 * ContactType.
 *
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
 * @version 1.0.0, 14-04-2022
 * @since 11
 */
public interface ContactTypeService extends CrudService<UUID, ContactType> {

    default ResponseEntity<List<ContactType>> list() {
        throw new NotImplementedException();
    }

    default ResponseEntity<ContactType> create(ContactTypeRequest contactTypeRequest) {
        throw new NotImplementedException();
    }

    default ResponseEntity<ContactType> findById(UUID contactTypeId) {
        throw new NotImplementedException();
    }

    default ResponseEntity<List<ContactType>> listById(List<UUID> contactTypeIds) {
        throw new NotImplementedException();
    }

    default ResponseEntity<ContactType> delete(UUID contactTypeId) {
        throw new NotImplementedException();
    }

    @Override
    default ResponseEntity<ContactType> update(UUID contactTypeId, ContactType contactType) {
        throw new NotImplementedException();
    }
}
