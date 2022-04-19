/*
 * @(#)$file.className.java.
 *
 * Copyright (c) Luis Antonio Mata Mata. All rights reserved.
 *
 * All rights to this product are owned by Luis Antonio Mata Mata and may only
 * be used under the terms of its associated license document. You may NOT
 * copy, modify, sublicense, or distribute this source file or portions of
 * it unless previously authorized in writing by Luis Antonio Mata Mata.
 * In any event, this notice and the above copyright must always be included
 * verbatim with this file.
 */

package com.prx.backoffice.v1.contacttype.service;

import com.prx.backoffice.v1.contact.mapper.ContactTypeMapper;
import com.prx.commons.pojo.ContactType;
import com.prx.persistence.general.repositories.ContactTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * ContactTypeImpl.
 *
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
 * @version 1.0.0, 14-04-2022
 * @since 11
 */
@Service
@RequiredArgsConstructor
public class ContactTypeServiceImpl implements ContactTypeService {
    private final ContactTypeMapper contactTypeMapper;
    private final ContactTypeRepository contactTypeRepository;

    @Override
    public ResponseEntity<List<ContactType>> list() {
        List<ContactType> contactTypes = new ArrayList<>();
        var result = contactTypeRepository.findAll();
        result.forEach(contactTypeEntity -> contactTypes.add(contactTypeMapper.toTarget(contactTypeEntity)));
        return ResponseEntity.ok(contactTypes);
    }
}
