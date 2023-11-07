package com.prx.backoffice.v1.contacttypes.service;

import com.prx.backoffice.v1.contacttypes.mapper.ContactTypeMapper;
import com.prx.backoffice.v1.contacttypes.to.ContactTypeRequest;
import com.prx.commons.pojo.ContactType;
import com.prx.persistence.general.domains.ContactTypeEntity;
import com.prx.persistence.general.repositories.ContactTypeRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {ContactTypeServiceImpl.class})
@ExtendWith(SpringExtension.class)
class ContactTypeServiceImplTest {

    @MockBean
    private ContactTypeMapper contactTypeMapper;

    @MockBean
    private ContactTypeRepository contactTypeRepository;

    @Autowired
    private ContactTypeServiceImpl contactTypeServiceImpl;

    @BeforeEach
    void setUp() {
    }

    @Test
    void list() {
    }

    @Test
    void create() {
        var contactTypeUUID = UUID.randomUUID();
        var contactType = getContactType();
        var contactTypeRequest = new ContactTypeRequest();
        contactTypeRequest.setContactType(contactType);
        contactTypeRequest.setAppName("TST-001");
        contactTypeRequest.setAppToken("12536");
        contactTypeRequest.setDateTime(LocalDateTime.now());
        var contactTypeEntity = new ContactTypeEntity();
        contactTypeEntity.setId(contactTypeUUID);
        contactTypeEntity.setName("Contact type description 001");
        contactTypeEntity.setDescription("Contact type description");
        contactTypeEntity.setActive(true);
        when(contactTypeRepository.save(Mockito.any(ContactTypeEntity.class))).thenReturn(contactTypeEntity);
        var result = contactTypeServiceImpl.create(contactTypeRequest);
        assertNotNull(result);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        verify(contactTypeRepository).save(Mockito.<ContactTypeEntity>any());
    }

    @Test
    void create_bad_request() {
        var contactType = getContactType();
        var contactTypeRequest = new ContactTypeRequest();
        contactTypeRequest.setContactType(null);
        var result = contactTypeServiceImpl.create(contactTypeRequest);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void create_null_request_bad_request() {
        var result = contactTypeServiceImpl.create(null);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    private static ContactType getContactType() {
        final var contactTypeUUID = UUID.randomUUID();
        final var contactType2UUID = UUID.randomUUID();
        ContactType contactType = new ContactType();
        contactType.setActive(true);
        contactType.setDescription("The characteristics of someone or something");
        contactType.setId(contactTypeUUID.toString());
        contactType.setName("Name");
        return contactType;
    }
}
