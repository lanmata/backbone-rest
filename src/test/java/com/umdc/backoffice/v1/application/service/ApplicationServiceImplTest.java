/*
 *  @(#)ApplicationServiceImplTest.java
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

package com.umdc.backoffice.v1.application.service;

import com.umdc.backoffice.v1.application.mapper.ApplicationMapper;
import com.umdc.commons.general.pojo.Application;
import com.umdc.persistence.general.domains.ApplicationEntity;
import com.umdc.persistence.general.repositories.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.umdc.backoffice.util.MessageUtil.MESSAGE_HEADER_STR;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ApplicationServiceImplTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationMapper applicationMapper;

    private ApplicationServiceImpl applicationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        applicationService = new ApplicationServiceImpl(applicationRepository, applicationMapper);
    }

    // ── create ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create: valid request returns 201 with Message-header")
    void createApplicationSuccessfully() {
        Application application = new Application();
        application.setName("My App");
        ApplicationEntity entity = new ApplicationEntity();

        when(applicationMapper.toSource(application)).thenReturn(entity);
        when(applicationRepository.save(entity)).thenReturn(entity);
        when(applicationMapper.toTarget(entity)).thenReturn(application);

        ResponseEntity<Application> response = applicationService.create(application);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getCreatedDate());
        assertNotNull(response.getHeaders().getFirst(MESSAGE_HEADER_STR));
        verify(applicationRepository).save(entity);
    }

    @Test
    @DisplayName("create: with complete application data returns 201 and correct body")
    void createApplicationWithCompleteData() {
        Application application = new Application();
        application.setId(UUID.randomUUID());
        application.setName("Test App");
        application.setDescription("Test Description");
        application.setActive(true);

        ApplicationEntity entity = new ApplicationEntity();
        entity.setName(application.getName());

        when(applicationMapper.toSource(application)).thenReturn(entity);
        when(applicationRepository.save(entity)).thenReturn(entity);
        when(applicationMapper.toTarget(entity)).thenReturn(application);

        ResponseEntity<Application> response = applicationService.create(application);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(application, response.getBody());
        verify(applicationRepository, times(1)).save(entity);
    }

    @Test
    @DisplayName("create: derives codeName from name when codeName is null")
    void createApplicationDerivesCodeNameFromName() {
        Application application = new Application();
        application.setName("My App");
        ApplicationEntity entity = new ApplicationEntity();

        when(applicationMapper.toSource(application)).thenReturn(entity);
        when(applicationRepository.save(entity)).thenReturn(entity);
        when(applicationMapper.toTarget(entity)).thenReturn(application);

        applicationService.create(application);

        assertEquals("my_app", entity.getCodeName());
    }

    @Test
    @DisplayName("create: codeName is truncated to 8 characters when name is long")
    void createApplicationTruncatesCodeNameToEightChars() {
        Application application = new Application();
        application.setName("My Very Long Application Name");
        ApplicationEntity entity = new ApplicationEntity();

        when(applicationMapper.toSource(application)).thenReturn(entity);
        when(applicationRepository.save(entity)).thenReturn(entity);
        when(applicationMapper.toTarget(entity)).thenReturn(application);

        applicationService.create(application);

        assertNotNull(entity.getCodeName());
        assertTrue(entity.getCodeName().length() <= 8);
        assertEquals("my_very_", entity.getCodeName());
    }

    @Test
    @DisplayName("create: null application returns 400 with Message-header")
    void createApplicationWithNullApplicationReturnsBadRequest() {
        ResponseEntity<Application> response = applicationService.create(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getHeaders().getFirst(MESSAGE_HEADER_STR));
        verifyNoInteractions(applicationRepository);
    }

    @Test
    @DisplayName("create: blank name returns 400 with Message-header")
    void createApplicationWithBlankNameReturnsBadRequest() {
        Application application = new Application();
        application.setName("   ");

        ResponseEntity<Application> response = applicationService.create(application);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getHeaders().getFirst(MESSAGE_HEADER_STR));
        verifyNoInteractions(applicationRepository);
    }

    @Test
    @DisplayName("create: null name returns 400 with Message-header")
    void createApplicationWithNullNameReturnsBadRequest() {
        ResponseEntity<Application> response = applicationService.create(new Application());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getHeaders().getFirst(MESSAGE_HEADER_STR));
        verifyNoInteractions(applicationRepository);
    }

    // ── find ────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("find: existing ID returns 200 with Message-header and body")
    void findApplicationByIdSuccessfully() {
        UUID appId = UUID.randomUUID();
        ApplicationEntity entity = new ApplicationEntity();
        entity.setId(appId);
        Application application = new Application();
        application.setId(appId);

        when(applicationRepository.findById(appId)).thenReturn(Optional.of(entity));
        when(applicationMapper.toTarget(entity)).thenReturn(application);

        ResponseEntity<Application> response = applicationService.find(appId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(application, response.getBody());
        assertNotNull(response.getHeaders().getFirst(MESSAGE_HEADER_STR));
        verify(applicationRepository).findById(appId);
    }

    @Test
    @DisplayName("find: non-existent ID returns 404 with Message-header")
    void findApplicationByNonExistentIdReturnsNotFound() {
        UUID appId = UUID.randomUUID();
        when(applicationRepository.findById(appId)).thenReturn(Optional.empty());

        ResponseEntity<Application> response = applicationService.find(appId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getHeaders().getFirst(MESSAGE_HEADER_STR));
        verify(applicationRepository).findById(appId);
        verifyNoInteractions(applicationMapper);
    }

    // ── update ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update: existing application returns 200 with updated body and Message-header")
    void updateApplicationSuccessfully() {
        UUID appId = UUID.randomUUID();
        Application incoming = new Application();
        incoming.setName("Updated Name");
        incoming.setActive(true);

        ApplicationEntity entity = new ApplicationEntity();
        entity.setId(appId);
        Application updated = new Application();
        updated.setId(appId);
        updated.setName("Updated Name");

        when(applicationRepository.findById(appId)).thenReturn(Optional.of(entity));
        when(applicationRepository.save(entity)).thenReturn(entity);
        when(applicationMapper.toTarget(entity)).thenReturn(updated);

        ResponseEntity<Application> response = applicationService.update(appId, incoming);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updated, response.getBody());
        assertNotNull(response.getHeaders().getFirst(MESSAGE_HEADER_STR));
        verify(applicationRepository).save(entity);
    }

    @Test
    @DisplayName("update: non-existent ID returns 404 with Message-header")
    void updateApplicationNotFoundReturns404() {
        UUID appId = UUID.randomUUID();
        Application incoming = new Application();
        incoming.setName("Updated Name");

        when(applicationRepository.findById(appId)).thenReturn(Optional.empty());

        ResponseEntity<Application> response = applicationService.update(appId, incoming);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getHeaders().getFirst(MESSAGE_HEADER_STR));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("update: null application returns 400 with Message-header")
    void updateApplicationWithNullBodyReturnsBadRequest() {
        ResponseEntity<Application> response = applicationService.update(UUID.randomUUID(), null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getHeaders().getFirst(MESSAGE_HEADER_STR));
        verifyNoInteractions(applicationRepository);
    }

    @Test
    @DisplayName("update: blank name returns 400 with Message-header")
    void updateApplicationWithBlankNameReturnsBadRequest() {
        Application incoming = new Application();
        incoming.setName("");

        ResponseEntity<Application> response = applicationService.update(UUID.randomUUID(), incoming);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getHeaders().getFirst(MESSAGE_HEADER_STR));
        verifyNoInteractions(applicationRepository);
    }

    @Test
    @DisplayName("update: codeName is recalculated and truncated to 8 chars")
    void updateApplicationRecalculatesCodeName() {
        UUID appId = UUID.randomUUID();
        Application incoming = new Application();
        incoming.setName("New Long App Name");

        ApplicationEntity entity = new ApplicationEntity();
        entity.setId(appId);
        Application result = new Application();

        when(applicationRepository.findById(appId)).thenReturn(Optional.of(entity));
        when(applicationRepository.save(entity)).thenReturn(entity);
        when(applicationMapper.toTarget(entity)).thenReturn(result);

        applicationService.update(appId, incoming);

        assertNotNull(entity.getCodeName());
        assertTrue(entity.getCodeName().length() <= 8);
        assertEquals("new_long", entity.getCodeName());
    }

    // ── delete ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete: existing application returns 200 with Message-header")
    void deleteApplicationSuccessfully() {
        UUID appId = UUID.randomUUID();
        when(applicationRepository.existsById(appId)).thenReturn(true);

        ResponseEntity<Application> response = applicationService.delete(appId, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getHeaders().getFirst(MESSAGE_HEADER_STR));
        verify(applicationRepository).deleteById(appId);
    }

    @Test
    @DisplayName("delete: non-existent ID returns 404 with Message-header")
    void deleteApplicationNotFoundReturns404() {
        UUID appId = UUID.randomUUID();
        when(applicationRepository.existsById(appId)).thenReturn(false);

        ResponseEntity<Application> response = applicationService.delete(appId, null);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getHeaders().getFirst(MESSAGE_HEADER_STR));
        verify(applicationRepository, never()).deleteById(any());
    }

    // ── list(ids) ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("list: returns 200 with matched applications and Message-header")
    void listApplicationsByIdsSuccessfully() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        ApplicationEntity entity1 = new ApplicationEntity();
        ApplicationEntity entity2 = new ApplicationEntity();
        Application app1 = new Application();
        Application app2 = new Application();

        when(applicationRepository.findAllById(List.of(id1, id2))).thenReturn(List.of(entity1, entity2));
        when(applicationMapper.toTarget(entity1)).thenReturn(app1);
        when(applicationMapper.toTarget(entity2)).thenReturn(app2);

        ResponseEntity<List<Application>> response = applicationService.list(id1, id2);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertNotNull(response.getHeaders().getFirst(MESSAGE_HEADER_STR));
    }

    @Test
    @DisplayName("list: returns 404 with Message-header when no IDs match")
    void listApplicationsByIdsNoMatchReturns404() {
        UUID id1 = UUID.randomUUID();
        when(applicationRepository.findAllById(List.of(id1))).thenReturn(List.of());

        ResponseEntity<List<Application>> response = applicationService.list(id1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getHeaders().getFirst(MESSAGE_HEADER_STR));
        verifyNoInteractions(applicationMapper);
    }

    // ── listAll ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("listAll: returns 200 with Message-header and all applications")
    void listAllApplicationsSuccessfully() {
        ApplicationEntity entity1 = new ApplicationEntity();
        ApplicationEntity entity2 = new ApplicationEntity();
        Application app1 = new Application();
        Application app2 = new Application();

        when(applicationRepository.findAll()).thenReturn(List.of(entity1, entity2));
        when(applicationMapper.toTarget(entity1)).thenReturn(app1);
        when(applicationMapper.toTarget(entity2)).thenReturn(app2);

        ResponseEntity<List<Application>> response = applicationService.listAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().containsAll(List.of(app1, app2)));
        assertNotNull(response.getHeaders().getFirst(MESSAGE_HEADER_STR));
        verify(applicationRepository).findAll();
    }

    @Test
    @DisplayName("listAll: returns 404 with Message-header when no applications exist")
    void listAllApplicationsWhenNoneExistReturnsNotFound() {
        when(applicationRepository.findAll()).thenReturn(List.of());

        ResponseEntity<List<Application>> response = applicationService.listAll();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getHeaders().getFirst(MESSAGE_HEADER_STR));
        verify(applicationRepository).findAll();
        verifyNoInteractions(applicationMapper);
    }
}
