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

import com.prx.backoffice.MockLoaderBase;
import com.prx.backoffice.v1.contacttype.mapper.ContactTypeMapper;
import com.prx.commons.pojo.ContactType;
import com.prx.persistence.general.domains.ContactTypeEntity;
import com.prx.persistence.general.repositories.ContactTypeRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigInteger;
import java.util.ArrayList;

/**
 * ContactTypeServiceImplTest.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 16-04-2022
 * @since
 */
class ContactTypeServiceImplTest extends MockLoaderBase {

    @Mock
    ContactTypeRepository contactTypeRepository;

    @Mock
    ContactTypeMapper contactTypeMapper;

    @InjectMocks
    ContactTypeServiceImpl contactTypeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void list() {
        var result = new ArrayList<ContactTypeEntity>();
        var contactTypeEntity = new ContactTypeEntity();
        contactTypeEntity.setId(BigInteger.ONE);
        contactTypeEntity.setName("Messenger");
        contactTypeEntity.setActive(true);
        contactTypeEntity.setDescription("Instant message app");
        var contactType = new ContactType();
        contactType.setId(1L);
        contactType.setName("Messenger");
        contactType.setActive(true);
        contactType.setDescription("Instant message app");
        result.add(contactTypeEntity);

        Mockito.doReturn(contactTypeEntity).when(contactTypeMapper).toSource(ArgumentMatchers.any(ContactType.class));
        Mockito.when(contactTypeRepository.findAll()).thenReturn(result);
        var responseResult = contactTypeRepository.findAll();
        Assertions.assertNotNull(responseResult);
    }
}
