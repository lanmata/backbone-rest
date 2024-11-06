package com.prx.backoffice.v1.contacttypes.service;

import com.prx.backoffice.v1.contacttypes.mapper.ContactTypeMapper;
import com.prx.backoffice.v1.contacttypes.to.ContactTypeRequest;
import com.prx.commons.pojo.ContactType;
import com.prx.persistence.general.domains.ContactTypeEntity;
import com.prx.persistence.general.repositories.ContactTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class ContactTypeServiceImplTest {

    @InjectMocks
    private ContactTypeServiceImpl contactTypeServiceImpl;

    @Mock
    private ContactTypeMapper contactTypeMapper;

    @Mock
    private ContactTypeRepository contactTypeRepository;

    @Test
    @DisplayName("Test listing all contact types - Not Found")
    void list_not_found() {
        when(contactTypeRepository.findAll()).thenReturn((Iterable<ContactTypeEntity>) new ArrayList<ContactTypeEntity>());
        var result = contactTypeServiceImpl.list();
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        verify(contactTypeRepository).findAll();
    }

    @Test
    @DisplayName("Test creating a contact type")
    void create() {
        var contactTypeUUID = UUID.randomUUID();
        var contactTypeEntity = new ContactTypeEntity();
        contactTypeEntity.setId(contactTypeUUID);
        contactTypeEntity.setName("Contact type description 001");
        contactTypeEntity.setDescription("Contact type description");
        contactTypeEntity.setActive(true);
        var contactType = getContactType();
        var response = getContactTypeRequest(contactType);

        when(contactTypeRepository.save(Mockito.any(ContactTypeEntity.class))).thenReturn(contactTypeEntity);
        when(contactTypeMapper.toSource(any(ContactType.class))).thenReturn(contactTypeEntity);
        when(contactTypeMapper.toTarget(any(ContactTypeEntity.class))).thenReturn(contactType);
        var result = contactTypeServiceImpl.create(response);
        assertNotNull(result);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        verify(contactTypeRepository).save(Mockito.<ContactTypeEntity>any());
    }

    @Test
    @DisplayName("Test creating a contact type - Bad Request")
    void create_bad_request() {
        var contactTypeRequest = new ContactTypeRequest();
        contactTypeRequest.setContactType(null);
        var result = contactTypeServiceImpl.create(contactTypeRequest);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    @DisplayName("Test creating a contact type with null request - Bad Request")
    void create_null_request_bad_request() {
        var result = contactTypeServiceImpl.create(null);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    @DisplayName("Test updating a contact type")
    void testUpdate() {
        var contactTypeUUID = UUID.randomUUID();
        var contactTypeEntity = new ContactTypeEntity();
        contactTypeEntity.setId(contactTypeUUID);
        contactTypeEntity.setName("Contact type description 001");
        contactTypeEntity.setDescription("Contact type description");
        contactTypeEntity.setActive(true);
        when(contactTypeMapper.toSource(Mockito.any(ContactType.class))).thenReturn(contactTypeEntity);
        when(contactTypeMapper.toTarget(Mockito.any(ContactTypeEntity.class))).thenReturn(getContactType());
        when(contactTypeRepository.findById(Mockito.any(UUID.class))).thenReturn(Optional.of(contactTypeEntity));
        when(contactTypeRepository.save(Mockito.any(ContactTypeEntity.class))).thenReturn(contactTypeEntity);
        var result = contactTypeServiceImpl.update(contactTypeUUID.toString(), getContactType());
        assertNotNull(result);
        assertEquals(HttpStatus.ACCEPTED, result.getStatusCode());
        verify(contactTypeRepository).findById(Mockito.<UUID>any());
        verify(contactTypeRepository).save(Mockito.<ContactTypeEntity>any());
    }

    @Test
    @DisplayName("Test updating a contact type - Not Found")
    void testUpdate_not_found() {
        var contactTypeUUID = UUID.randomUUID();
        when(contactTypeRepository.findById(Mockito.any(UUID.class))).thenReturn(Optional.empty());
        var result = contactTypeServiceImpl.update(contactTypeUUID.toString(), getContactType());
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        verify(contactTypeRepository).findById(Mockito.<UUID>any());
    }

    @Test
    @DisplayName("Test updating a contact type - Bad Request")
    void testUpdate_bad_request() {
        var result = contactTypeServiceImpl.update(null, getContactType());
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    @DisplayName("Test updating a contact type with null contact type - Bad Request")
    void testUpdate_bad_request_contactType_null() {
        var result = contactTypeServiceImpl.update(UUID.randomUUID().toString(), null);
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    @DisplayName("Test updating a contact type with null parameters - Bad Request")
    void testUpdate_bad_request_parameters_null() {
        var result = contactTypeServiceImpl.update(null, null);
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    @DisplayName("Test finding a contact type by ID")
    void testFindById() {
        final var contactType = getContactType();
        final var uuidValue = contactType.getId();
        var contactTypeEntity = new ContactTypeEntity();
        contactTypeEntity.setId(UUID.fromString(uuidValue));
        contactTypeEntity.setName("Contact type description 001");
        contactTypeEntity.setDescription("Contact type description");
        contactTypeEntity.setActive(true);
        when(contactTypeRepository.findById(Mockito.<UUID>any())).thenReturn(Optional.of(contactTypeEntity));
        when(contactTypeMapper.toTarget(Mockito.<ContactTypeEntity>any())).thenReturn(getContactType());
        var result = contactTypeServiceImpl.findById(uuidValue);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(uuidValue, contactType.getId());
        verify(contactTypeRepository).findById(Mockito.<UUID>any());
        verify(contactTypeMapper).toTarget(Mockito.<ContactTypeEntity>any());
    }

    @Test
    @DisplayName("Test finding a contact type by ID - Bad Request")
    void testFindById_bad_request() {
        when(contactTypeRepository.findById(Mockito.<UUID>any())).thenReturn(Optional.empty());
        var result = contactTypeServiceImpl.findById(null);
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    @DisplayName("Test finding a contact type by ID - Not Found")
    void testFindById_not_found() {
        when(contactTypeRepository.findById(Mockito.<UUID>any())).thenReturn(Optional.empty());
        var result = contactTypeServiceImpl.findById("efafc19c-b4a7-4d97-a911-bca3a7436c20");
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    private static ContactType getContactType() {
        final var contactTypeUUID = UUID.randomUUID();
        ContactType contactType = new ContactType();
        contactType.setActive(true);
        contactType.setDescription("The characteristics of someone or something");
        contactType.setId(contactTypeUUID.toString());
        contactType.setName("Name");
        return contactType;
    }

    private static ContactTypeRequest getContactTypeRequest(ContactType contactType) {
        var contactTypeRequest = new ContactTypeRequest();
        contactTypeRequest.setContactType(contactType);
        contactTypeRequest.setAppName("TST-001");
        contactTypeRequest.setAppToken("12536");
        contactTypeRequest.setDateTime(LocalDateTime.now());
        return contactTypeRequest;
    }
}
