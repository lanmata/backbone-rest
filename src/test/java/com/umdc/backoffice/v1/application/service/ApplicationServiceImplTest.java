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
import org.apache.commons.lang.NotImplementedException;
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
    @DisplayName("create: persists application and returns 200 with createdDate populated")
    void createApplicationSuccessfully() {
        Application application = new Application();
        ApplicationEntity entity = new ApplicationEntity();

        when(applicationMapper.toSource(application)).thenReturn(entity);
        when(applicationRepository.save(entity)).thenReturn(entity);
        when(applicationMapper.toTarget(entity)).thenReturn(application);

        ResponseEntity<Application> response = applicationService.create(application);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getCreatedDate());
        verify(applicationRepository).save(entity);
    }

    @Test
    @DisplayName("create: with complete application data returns 200 and correct body")
    void createApplicationWithCompleteData() {
        Application application = new Application();
        application.setId(UUID.randomUUID());
        application.setName("Test App");
        application.setDescription("Test Description");
        application.setActive(true);

        ApplicationEntity entity = new ApplicationEntity();
        entity.setId(application.getId());
        entity.setName(application.getName());

        when(applicationMapper.toSource(application)).thenReturn(entity);
        when(applicationRepository.save(entity)).thenReturn(entity);
        when(applicationMapper.toTarget(entity)).thenReturn(application);

        ResponseEntity<Application> response = applicationService.create(application);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(application, response.getBody());
        verify(applicationRepository, times(1)).save(entity);
    }

    @Test
    @DisplayName("create: null application throws NullPointerException")
    void createApplicationWithNullApplicationThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> applicationService.create(null));
    }

    // ── find ────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("find: existing ID returns 200 with application body")
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
        verify(applicationRepository).findById(appId);
    }

    @Test
    @DisplayName("find: non-existent ID returns 404")
    void findApplicationByNonExistentIdReturnsNotFound() {
        UUID appId = UUID.randomUUID();
        when(applicationRepository.findById(appId)).thenReturn(Optional.empty());

        ResponseEntity<Application> response = applicationService.find(appId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(applicationRepository).findById(appId);
        verifyNoInteractions(applicationMapper);
    }

    // ── listAll ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("listAll: returns 200 with all applications when records exist")
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
        verify(applicationRepository).findAll();
    }

    @Test
    @DisplayName("listAll: returns 404 when no applications exist")
    void listAllApplicationsWhenNoneExistReturnsNotFound() {
        when(applicationRepository.findAll()).thenReturn(List.of());

        ResponseEntity<List<Application>> response = applicationService.listAll();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(applicationRepository).findAll();
        verifyNoInteractions(applicationMapper);
    }

    // ── update ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update: delegates to default interface method and throws NotImplementedException")
    void updateApplicationThrowsNotImplemented() {
        assertThrows(NotImplementedException.class,
                () -> applicationService.update(UUID.randomUUID(), new Application()));
    }

    @Test
    @DisplayName("update: with non-existent ID still throws NotImplementedException")
    void updateApplicationWithNonExistentIdThrowsNotImplemented() {
        when(applicationRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(NotImplementedException.class,
                () -> applicationService.update(UUID.randomUUID(), new Application()));
    }

    // ── delete ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete: delegates to default interface method and throws NotImplementedException")
    void deleteApplicationThrowsNotImplemented() {
        assertThrows(NotImplementedException.class,
                () -> applicationService.delete(UUID.randomUUID(), new Application()));
    }

    @Test
    @DisplayName("delete: with non-existent ID still throws NotImplementedException")
    void deleteApplicationWithNonExistentIdThrowsNotImplemented() {
        when(applicationRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(NotImplementedException.class,
                () -> applicationService.delete(UUID.randomUUID(), new Application()));
    }

    // ── list ────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("list: delegates to default interface method and throws NotImplementedException")
    void listApplicationsByIdThrowsNotImplemented() {
        assertThrows(NotImplementedException.class,
                () -> applicationService.list(UUID.randomUUID()));
    }
}
