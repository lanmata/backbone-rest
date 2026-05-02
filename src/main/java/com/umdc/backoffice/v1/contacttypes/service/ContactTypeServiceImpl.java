/*
 *  @(#)ContactTypeServiceImpl.java
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

import com.umdc.backoffice.v1.contacttypes.mapper.ContactTypeMapper;
import com.umdc.backoffice.v1.contacttypes.api.to.ContactTypeRequest;
import com.umdc.commons.general.pojo.ContactType;
import com.umdc.persistence.general.repositories.ContactTypeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.umdc.backoffice.util.MessageUtil.MESSAGE_HEADER_STR;

/**
 * ContactTypeImpl.
 *
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
 * @version 1.0.0, 14-04-2022
 * @since 11
 */
@Service
public class ContactTypeServiceImpl implements ContactTypeService {
    private final ContactTypeMapper contactTypeMapper;
    private final ContactTypeRepository contactTypeRepository;

    public ContactTypeServiceImpl(ContactTypeMapper contactTypeMapper, ContactTypeRepository contactTypeRepository) {
        this.contactTypeMapper = contactTypeMapper;
        this.contactTypeRepository = contactTypeRepository;
    }

    @Override
    public ResponseEntity<List<ContactType>> list() {
        List<ContactType> contactTypes = new ArrayList<>();
        var result = contactTypeRepository.findAll();
        result.forEach(contactTypeEntity -> contactTypes.add(contactTypeMapper.toTarget(contactTypeEntity)));
        if(contactTypes.isEmpty()) {
            return ResponseEntity.notFound().header(MESSAGE_HEADER_STR, "Contact Type not found.").build();
        }
        return ResponseEntity.ok(contactTypes);
    }

    @Override
    public ResponseEntity<ContactType> create(ContactTypeRequest contactTypeRequest) {
        if(Objects.nonNull(contactTypeRequest) && Objects.nonNull(contactTypeRequest.getContactType())) {
            var contactTypeEntity = contactTypeMapper.toSource(contactTypeRequest.getContactType());
            return ResponseEntity.status(HttpStatus.CREATED).header(MESSAGE_HEADER_STR, "Contact Type created.")
                    .body(contactTypeMapper.toTarget(contactTypeRepository.save(contactTypeEntity)));
        }
        return ResponseEntity.badRequest().header(MESSAGE_HEADER_STR, "Contact Type couldn't be created.").build();
    }

    @Override
    public ResponseEntity<ContactType> update(UUID contactTypeId, ContactType contactType) {
        if(Objects.nonNull(contactTypeId) && Objects.nonNull(contactType)) {
            var contactTypePrevious = this.contactTypeRepository.findById(contactTypeId);
            if(contactTypePrevious.isPresent()) {
                var contactTypeEntity = contactTypePrevious.get();
                contactTypeEntity.setName(contactType.getName());
                contactTypeEntity.setActive(contactType.getActive());
                contactTypeEntity.setDescription(contactType.getDescription());
                return ResponseEntity.status(HttpStatus.ACCEPTED).header(MESSAGE_HEADER_STR, "Contact Type updated.")
                        .body(contactTypeMapper.toTarget(contactTypeRepository.save(contactTypeEntity)));
            }
            return ResponseEntity.notFound().header(MESSAGE_HEADER_STR, "Contact Type not found.").build();
        }
        return ResponseEntity.badRequest().header(MESSAGE_HEADER_STR, "Contact Type couldn't be updated.").build();
    }

    @Override
    public ResponseEntity<ContactType> findById(UUID contactTypeId) {
        if(Objects.nonNull(contactTypeId)) {
            var contactTypeEntity = contactTypeRepository.findById(contactTypeId);
            return contactTypeEntity.map(entity -> ResponseEntity.status(HttpStatus.OK)
                            .header(MESSAGE_HEADER_STR, "Contact Type found.")
                            .body(contactTypeMapper.toTarget(contactTypeEntity.get())))
                    .orElseGet(() -> ResponseEntity.notFound().header(MESSAGE_HEADER_STR, "Contact Type not found.").build());
        }
        return ResponseEntity.badRequest().header(MESSAGE_HEADER_STR, "Contact Type ID not valid.").build();
    }
}
