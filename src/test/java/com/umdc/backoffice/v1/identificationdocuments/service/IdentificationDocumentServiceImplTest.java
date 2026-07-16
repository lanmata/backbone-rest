/*
 *  @(#)IdentificationDocumentServiceImplTest.java
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
package com.umdc.backoffice.v1.identificationdocuments.service;

import com.umdc.backoffice.v1.identificationdocuments.api.to.IdentificationDocument;
import com.umdc.backoffice.util.MessageUtil;
import com.umdc.commons.constants.types.IdentificationType;
import com.umdc.persistence.general.domains.IdentificationDocumentEntity;
import com.umdc.persistence.general.domains.PersonEntity;
import com.umdc.persistence.general.repositories.IdentificationDocumentRepository;
import com.umdc.persistence.general.repositories.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/// Unit tests for {@link IdentificationDocumentServiceImpl}.
@ExtendWith(MockitoExtension.class)
class IdentificationDocumentServiceImplTest {

    private static final UUID PERSON_ID = UUID.randomUUID();
    private static final UUID DOCUMENT_ID = UUID.randomUUID();
    private static final String NUMBER = "123456";
    private static final LocalDate EXPIRATION_DATE = LocalDate.now().plusYears(5);

    @Mock
    private IdentificationDocumentRepository identificationDocumentRepository;

    @Mock
    private PersonRepository personRepository;

    private IdentificationDocumentServiceImpl identificationDocumentService;

    @BeforeEach
    void setUp() {
        identificationDocumentService =
                new IdentificationDocumentServiceImpl(identificationDocumentRepository, personRepository);
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create: returns 201 when the document and its owning person are valid")
    void create_returnsCreated_whenValid() {
        IdentificationDocument pojo = buildPojo();
        PersonEntity person = buildPersonEntity(PERSON_ID);
        IdentificationDocumentEntity saved = buildEntity();

        doReturn(Optional.of(person)).when(personRepository).findById(PERSON_ID);
        doReturn(saved).when(identificationDocumentRepository).save(any(IdentificationDocumentEntity.class));

        ResponseEntity<IdentificationDocument> response = identificationDocumentService.create(pojo);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(NUMBER, response.getBody().getNumber());
        assertEquals(EXPIRATION_DATE, response.getBody().getExpirationDate());
        assertEquals(0, response.getBody().getIdentificationType());
        assertEquals(PERSON_ID, response.getBody().getPersonId());
    }

    @Test
    @DisplayName("create: returns 400 when the identification document is null")
    void create_returnsBadRequest_whenDocumentNull() {
        ResponseEntity<IdentificationDocument> response = identificationDocumentService.create(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(identificationDocumentRepository, personRepository);
    }

    @Test
    @DisplayName("create: returns 400 when personId is null")
    void create_returnsBadRequest_whenPersonIdNull() {
        IdentificationDocument pojo = buildPojo();
        pojo.setPersonId(null);

        ResponseEntity<IdentificationDocument> response = identificationDocumentService.create(pojo);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(identificationDocumentRepository, personRepository);
    }

    @Test
    @DisplayName("create: returns 400 when number is non-numeric")
    void create_returnsBadRequest_whenNumberNonNumeric() {
        IdentificationDocument pojo = buildPojo();
        pojo.setNumber("abc");

        ResponseEntity<IdentificationDocument> response = identificationDocumentService.create(pojo);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(identificationDocumentRepository, personRepository);
    }

    @Test
    @DisplayName("create: returns 400 when number is blank")
    void create_returnsBadRequest_whenNumberBlank() {
        IdentificationDocument pojo = buildPojo();
        pojo.setNumber("   ");

        ResponseEntity<IdentificationDocument> response = identificationDocumentService.create(pojo);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(identificationDocumentRepository, personRepository);
    }

    @Test
    @DisplayName("create: returns 400 when identificationType is above the valid range (boundary = 2)")
    void create_returnsBadRequest_whenIdentificationTypeAboveRange() {
        IdentificationDocument pojo = buildPojo();
        pojo.setIdentificationType(2);

        ResponseEntity<IdentificationDocument> response = identificationDocumentService.create(pojo);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(identificationDocumentRepository, personRepository);
    }

    @Test
    @DisplayName("create: returns 400 when identificationType is below the valid range (boundary = -1)")
    void create_returnsBadRequest_whenIdentificationTypeBelowRange() {
        IdentificationDocument pojo = buildPojo();
        pojo.setIdentificationType(-1);

        ResponseEntity<IdentificationDocument> response = identificationDocumentService.create(pojo);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(identificationDocumentRepository, personRepository);
    }

    @Test
    @DisplayName("create: returns 400 when the owning person does not exist")
    void create_returnsBadRequest_whenPersonNotFound() {
        IdentificationDocument pojo = buildPojo();

        doReturn(Optional.empty()).when(personRepository).findById(PERSON_ID);

        ResponseEntity<IdentificationDocument> response = identificationDocumentService.create(pojo);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(identificationDocumentRepository, never()).save(any());
    }

    @Test
    @DisplayName("create: returns 400 when expirationDate is null")
    void create_returnsBadRequest_whenExpirationDateNull() {
        IdentificationDocument pojo = buildPojo();
        pojo.setExpirationDate(null);

        ResponseEntity<IdentificationDocument> response = identificationDocumentService.create(pojo);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(identificationDocumentRepository, personRepository);
    }

    @Test
    @DisplayName("create: canonicalizes the number through int round-trip (e.g. leading zeros/whitespace are stripped)")
    void create_canonicalizesNumber_whenLeadingZerosOrWhitespace() {
        IdentificationDocument pojo = buildPojo();
        pojo.setNumber(" 0042 ");
        PersonEntity person = buildPersonEntity(PERSON_ID);
        IdentificationDocumentEntity saved = buildEntity();
        ArgumentCaptor<IdentificationDocumentEntity> captor = ArgumentCaptor.forClass(IdentificationDocumentEntity.class);

        doReturn(Optional.of(person)).when(personRepository).findById(PERSON_ID);
        doReturn(saved).when(identificationDocumentRepository).save(captor.capture());

        ResponseEntity<IdentificationDocument> response = identificationDocumentService.create(pojo);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("42", captor.getValue().getNumber());
    }

    // ── find ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("find: returns 200 when the identification document is found")
    void find_returnsOk_whenFound() {
        IdentificationDocumentEntity entity = buildEntity();

        doReturn(Optional.of(entity)).when(identificationDocumentRepository).findById(DOCUMENT_ID);

        ResponseEntity<IdentificationDocument> response = identificationDocumentService.find(DOCUMENT_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(DOCUMENT_ID, response.getBody().getId());
    }

    @Test
    @DisplayName("find: returns 404 when the identification document is absent")
    void find_returnsNotFound_whenAbsent() {
        doReturn(Optional.empty()).when(identificationDocumentRepository).findById(DOCUMENT_ID);

        ResponseEntity<IdentificationDocument> response = identificationDocumentService.find(DOCUMENT_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("find: returns 400 when id is null")
    void find_returnsBadRequest_whenIdNull() {
        ResponseEntity<IdentificationDocument> response = identificationDocumentService.find(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(identificationDocumentRepository);
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update: returns 200 when found and personId is unchanged (no person lookup)")
    void update_returnsOk_whenFoundAndPersonUnchanged() {
        IdentificationDocument pojo = buildPojo();
        IdentificationDocumentEntity existing = buildEntity();
        IdentificationDocumentEntity saved = buildEntity();

        doReturn(Optional.of(existing)).when(identificationDocumentRepository).findById(DOCUMENT_ID);
        doReturn(saved).when(identificationDocumentRepository).save(existing);

        ResponseEntity<IdentificationDocument> response = identificationDocumentService.update(DOCUMENT_ID, pojo);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verifyNoInteractions(personRepository);
    }

    @Test
    @DisplayName("update: returns 404 when the identification document is absent")
    void update_returnsNotFound_whenAbsent() {
        IdentificationDocument pojo = buildPojo();

        doReturn(Optional.empty()).when(identificationDocumentRepository).findById(DOCUMENT_ID);

        ResponseEntity<IdentificationDocument> response = identificationDocumentService.update(DOCUMENT_ID, pojo);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("update: returns 400 when id is null")
    void update_returnsBadRequest_whenIdNull() {
        ResponseEntity<IdentificationDocument> response = identificationDocumentService.update(null, buildPojo());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(identificationDocumentRepository, personRepository);
    }

    @Test
    @DisplayName("update: returns 400 when identificationDocument is null")
    void update_returnsBadRequest_whenDocumentNull() {
        ResponseEntity<IdentificationDocument> response =
                identificationDocumentService.update(DOCUMENT_ID, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(identificationDocumentRepository, personRepository);
    }

    @Test
    @DisplayName("update: returns 400 when number is invalid (existing document is looked up first)")
    void update_returnsBadRequest_whenNumberInvalid() {
        IdentificationDocument pojo = buildPojo();
        pojo.setNumber("not-a-number");
        IdentificationDocumentEntity existing = buildEntity();

        doReturn(Optional.of(existing)).when(identificationDocumentRepository).findById(DOCUMENT_ID);

        ResponseEntity<IdentificationDocument> response = identificationDocumentService.update(DOCUMENT_ID, pojo);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(identificationDocumentRepository, never()).save(any());
    }

    @Test
    @DisplayName("update: returns 400 when identificationType is out of range")
    void update_returnsBadRequest_whenIdentificationTypeInvalid() {
        IdentificationDocument pojo = buildPojo();
        pojo.setIdentificationType(2);
        IdentificationDocumentEntity existing = buildEntity();

        doReturn(Optional.of(existing)).when(identificationDocumentRepository).findById(DOCUMENT_ID);

        ResponseEntity<IdentificationDocument> response = identificationDocumentService.update(DOCUMENT_ID, pojo);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(identificationDocumentRepository, never()).save(any());
    }

    @Test
    @DisplayName("update: returns 400 when expirationDate is null (existing document is looked up first)")
    void update_returnsBadRequest_whenExpirationDateNull() {
        IdentificationDocument pojo = buildPojo();
        pojo.setExpirationDate(null);
        IdentificationDocumentEntity existing = buildEntity();

        doReturn(Optional.of(existing)).when(identificationDocumentRepository).findById(DOCUMENT_ID);

        ResponseEntity<IdentificationDocument> response = identificationDocumentService.update(DOCUMENT_ID, pojo);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(identificationDocumentRepository, never()).save(any());
        verifyNoInteractions(personRepository);
    }

    @Test
    @DisplayName("update: returns 400 when a new personId is requested and that person does not exist")
    void update_returnsBadRequest_whenNewPersonNotFound() {
        UUID newPersonId = UUID.randomUUID();
        IdentificationDocument pojo = buildPojo();
        pojo.setPersonId(newPersonId);
        IdentificationDocumentEntity existing = buildEntity();

        doReturn(Optional.of(existing)).when(identificationDocumentRepository).findById(DOCUMENT_ID);
        doReturn(Optional.empty()).when(personRepository).findById(newPersonId);

        ResponseEntity<IdentificationDocument> response = identificationDocumentService.update(DOCUMENT_ID, pojo);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(identificationDocumentRepository, never()).save(any());
    }

    @Test
    @DisplayName("update: reassigns the owning person when a different, existing personId is requested")
    void update_changesPerson_whenNewPersonIdProvided() {
        UUID newPersonId = UUID.randomUUID();
        IdentificationDocument pojo = buildPojo();
        pojo.setPersonId(newPersonId);
        IdentificationDocumentEntity existing = buildEntity();
        PersonEntity newPerson = buildPersonEntity(newPersonId);

        doReturn(Optional.of(existing)).when(identificationDocumentRepository).findById(DOCUMENT_ID);
        doReturn(Optional.of(newPerson)).when(personRepository).findById(newPersonId);
        doReturn(existing).when(identificationDocumentRepository).save(existing);

        ResponseEntity<IdentificationDocument> response = identificationDocumentService.update(DOCUMENT_ID, pojo);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(newPersonId, response.getBody().getPersonId());
    }

    @Test
    @DisplayName("update: keeps the existing owning person when personId is null in the request")
    void update_keepsExistingPerson_whenPersonIdNull() {
        IdentificationDocument pojo = buildPojo();
        pojo.setPersonId(null);
        IdentificationDocumentEntity existing = buildEntity();

        doReturn(Optional.of(existing)).when(identificationDocumentRepository).findById(DOCUMENT_ID);
        doReturn(existing).when(identificationDocumentRepository).save(existing);

        ResponseEntity<IdentificationDocument> response = identificationDocumentService.update(DOCUMENT_ID, pojo);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(PERSON_ID, response.getBody().getPersonId());
        verifyNoInteractions(personRepository);
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete: returns 200 with the deleted document when found")
    void delete_returnsOk_whenFound() {
        IdentificationDocumentEntity existing = buildEntity();

        doReturn(Optional.of(existing)).when(identificationDocumentRepository).findById(DOCUMENT_ID);

        ResponseEntity<IdentificationDocument> response =
                identificationDocumentService.delete(DOCUMENT_ID, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(DOCUMENT_ID, response.getBody().getId());
        verify(identificationDocumentRepository).deleteById(DOCUMENT_ID);
    }

    @Test
    @DisplayName("delete: returns 404 when the identification document is absent")
    void delete_returnsNotFound_whenAbsent() {
        doReturn(Optional.empty()).when(identificationDocumentRepository).findById(DOCUMENT_ID);

        ResponseEntity<IdentificationDocument> response =
                identificationDocumentService.delete(DOCUMENT_ID, null);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(identificationDocumentRepository, never()).deleteById(any());
    }

    // ── listByPerson ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("listByPerson: returns 200 with the list when the person has documents")
    void listByPerson_returnsOk_whenDocumentsExist() {
        IdentificationDocumentEntity entity = buildEntity();

        doReturn(true).when(personRepository).existsById(PERSON_ID);
        doReturn(List.of(entity)).when(identificationDocumentRepository).findByPersonId(PERSON_ID);

        ResponseEntity<List<IdentificationDocument>> response = identificationDocumentService.listByPerson(PERSON_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("listByPerson: returns 404 with a person-not-found message when the person does not exist")
    void listByPerson_returnsNotFound_whenPersonAbsent() {
        doReturn(false).when(personRepository).existsById(PERSON_ID);

        ResponseEntity<List<IdentificationDocument>> response = identificationDocumentService.listByPerson(PERSON_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Person not found.", response.getHeaders().getFirst(MessageUtil.MESSAGE_HEADER_STR));
        verify(identificationDocumentRepository, never()).findByPersonId(any());
    }

    @Test
    @DisplayName("listByPerson: returns 404 with a distinct no-documents message when the person exists but has none")
    void listByPerson_returnsNotFound_whenPersonHasNoDocuments() {
        doReturn(true).when(personRepository).existsById(PERSON_ID);
        doReturn(List.of()).when(identificationDocumentRepository).findByPersonId(PERSON_ID);

        ResponseEntity<List<IdentificationDocument>> response = identificationDocumentService.listByPerson(PERSON_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("No identification documents found for the given person.",
                response.getHeaders().getFirst(MessageUtil.MESSAGE_HEADER_STR));
    }

    @Test
    @DisplayName("listByPerson: returns 400 when personId is null")
    void listByPerson_returnsBadRequest_whenPersonIdNull() {
        ResponseEntity<List<IdentificationDocument>> response = identificationDocumentService.listByPerson(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(identificationDocumentRepository, personRepository);
    }

    // ── conversion helpers observable behavior ───────────────────────────────

    @Test
    @DisplayName("find: expirationDate round-trips unchanged from LocalDate through the entity and back")
    void find_expirationDateRoundTripsUnchanged() {
        IdentificationDocumentEntity entity = buildEntity();
        entity.setExpirationDate(EXPIRATION_DATE);

        doReturn(Optional.of(entity)).when(identificationDocumentRepository).findById(DOCUMENT_ID);

        ResponseEntity<IdentificationDocument> response = identificationDocumentService.find(DOCUMENT_ID);

        assertNotNull(response.getBody());
        assertEquals(EXPIRATION_DATE, response.getBody().getExpirationDate());
        assertTrue(response.getBody().getExpirationDate().isEqual(EXPIRATION_DATE));
    }

    @Test
    @DisplayName("find: null person on the entity yields a null personId on the DTO")
    void find_returnsNullPersonId_whenEntityPersonIsNull() {
        IdentificationDocumentEntity entity = buildEntity();
        entity.setPerson(null);

        doReturn(Optional.of(entity)).when(identificationDocumentRepository).findById(DOCUMENT_ID);

        ResponseEntity<IdentificationDocument> response = identificationDocumentService.find(DOCUMENT_ID);

        assertNotNull(response.getBody());
        assertNull(response.getBody().getPersonId());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private IdentificationDocument buildPojo() {
        IdentificationDocument dto = new IdentificationDocument();
        dto.setId(DOCUMENT_ID);
        dto.setNumber(NUMBER);
        dto.setExpirationDate(EXPIRATION_DATE);
        dto.setIdentificationType(0);
        dto.setPersonId(PERSON_ID);
        return dto;
    }

    private IdentificationDocumentEntity buildEntity() {
        IdentificationDocumentEntity entity = new IdentificationDocumentEntity();
        entity.setId(DOCUMENT_ID);
        entity.setNumber(NUMBER);
        entity.setExpirationDate(EXPIRATION_DATE);
        entity.setIdentificationType(IdentificationType.PASSPORT);
        entity.setPerson(buildPersonEntity(PERSON_ID));
        return entity;
    }

    private PersonEntity buildPersonEntity(UUID id) {
        PersonEntity person = new PersonEntity();
        person.setId(id);
        return person;
    }
}
