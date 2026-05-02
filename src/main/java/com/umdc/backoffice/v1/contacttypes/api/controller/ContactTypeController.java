/*
 *  @(#)ContactTypeController.java
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
package com.umdc.backoffice.v1.contacttypes.api.controller;

import com.umdc.backoffice.v1.contacttypes.service.ContactTypeService;
import com.umdc.backoffice.v1.contacttypes.api.to.ContactTypeRequest;
import com.umdc.commons.general.pojo.ContactType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * ContactTypeApiController.
 *
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
 * @version 1.0.0, 10-04-2022
 * @since 11
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/contact-types")
public class ContactTypeController implements ContactTypeApi {
    private final ContactTypeService contactTypeService;

    public ContactTypeController(ContactTypeService contactTypeService) {
        this.contactTypeService = contactTypeService;
    }

    @Override
    public ResponseEntity<ContactType> create(final ContactTypeRequest contactTypeRequest) {
        return contactTypeService.create(contactTypeRequest);
    }

    @Override
    public ResponseEntity<ContactType> find(final UUID contactTypeId) {
        return contactTypeService.findById(contactTypeId);
    }

    @Override
    public ResponseEntity<ContactType> update(final UUID contactTypeId, @RequestBody ContactType contactType) {
        return contactTypeService.update(contactTypeId, contactType);
    }

    @Override
    public ResponseEntity<List<ContactType>> list(final List<UUID> contactTypeIds){
        return contactTypeService.listById(contactTypeIds);
    }

    @Override
    public ResponseEntity<List<ContactType>> list() {
        return contactTypeService.list();
    }

    @Override
    public ResponseEntity<ContactType> delete(final UUID contactTypeId) {
        return contactTypeService.delete(contactTypeId);
    }
}
