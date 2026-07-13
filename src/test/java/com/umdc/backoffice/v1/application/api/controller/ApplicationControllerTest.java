/*
 *  @(#)ApplicationControllerTest.java
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

package com.umdc.backoffice.v1.application.api.controller;

import com.umdc.backoffice.v1.application.api.to.ApplicationCreateRequest;
import com.umdc.backoffice.v1.application.api.to.ApplicationUpdateRequest;
import com.umdc.backoffice.v1.application.service.ApplicationService;
import com.umdc.commons.general.pojo.Application;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApplicationControllerTest {

    private ApplicationService applicationService;
    private ApplicationController applicationController;

    @BeforeEach
    void setUp() {
        applicationService = mock(ApplicationService.class);
        applicationController = new ApplicationController(applicationService);
    }

    // ── create ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create: delegates to service and returns its response")
    void createApplicationSuccessfully() {
        ApplicationCreateRequest request = new ApplicationCreateRequest();
        Application application = new Application();
        request.setApplication(application);
        when(applicationService.create(application)).thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(application));

        ResponseEntity<Application> response = applicationController.create(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(applicationService).create(application);
    }

    @Test
    @DisplayName("create: null request throws NullPointerException")
    void createApplicationWithNullRequest() {
        assertThrows(NullPointerException.class, () -> applicationController.create(null));
    }

    @Test
    @DisplayName("create: null application in request returns 400 without calling service")
    void createApplicationWithNullApplicationInRequest() {
        ApplicationCreateRequest request = new ApplicationCreateRequest();

        ResponseEntity<Application> response = applicationController.create(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(applicationService);
    }

    // ── find ────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("find: delegates to service and returns its response")
    void findApplicationByIdSuccessfully() {
        UUID appId = UUID.randomUUID();
        Application application = new Application();
        application.setId(appId);
        when(applicationService.find(appId)).thenReturn(ResponseEntity.ok(application));

        ResponseEntity<Application> response = applicationController.find(appId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(application, response.getBody());
        verify(applicationService).find(appId);
    }

    @Test
    @DisplayName("find: non-existent ID delegates 404 from service")
    void findApplicationNotFoundDelegates404() {
        UUID appId = UUID.randomUUID();
        when(applicationService.find(appId)).thenReturn(ResponseEntity.notFound().build());

        ResponseEntity<Application> response = applicationController.find(appId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(applicationService).find(appId);
    }

    // ── update ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update: delegates to service and returns its response")
    void updateApplicationSuccessfully() {
        UUID appId = UUID.randomUUID();
        Application application = new Application();
        application.setName("Updated");
        ApplicationUpdateRequest request = new ApplicationUpdateRequest();
        request.setApplication(application);
        when(applicationService.update(appId, application)).thenReturn(ResponseEntity.ok(application));

        ResponseEntity<Application> response = applicationController.update(appId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(applicationService).update(appId, application);
    }

    @Test
    @DisplayName("update: null application in request returns 400 without calling service")
    void updateApplicationWithNullApplicationReturns400() {
        ApplicationUpdateRequest request = new ApplicationUpdateRequest();

        ResponseEntity<Application> response = applicationController.update(UUID.randomUUID(), request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(applicationService);
    }

    @Test
    @DisplayName("update: non-existent ID delegates 404 from service")
    void updateApplicationNotFoundDelegates404() {
        UUID appId = UUID.randomUUID();
        Application application = new Application();
        application.setName("Updated");
        ApplicationUpdateRequest request = new ApplicationUpdateRequest();
        request.setApplication(application);
        when(applicationService.update(appId, application)).thenReturn(ResponseEntity.notFound().build());

        ResponseEntity<Application> response = applicationController.update(appId, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(applicationService).update(appId, application);
    }

    // ── delete ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete: delegates to service and returns its response")
    void deleteApplicationSuccessfully() {
        UUID appId = UUID.randomUUID();
        when(applicationService.delete(appId, null)).thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Application> response = applicationController.delete(appId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(applicationService).delete(appId, null);
    }

    @Test
    @DisplayName("delete: non-existent ID delegates 404 from service")
    void deleteApplicationNotFoundDelegates404() {
        UUID appId = UUID.randomUUID();
        when(applicationService.delete(appId, null)).thenReturn(ResponseEntity.notFound().build());

        ResponseEntity<Application> response = applicationController.delete(appId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(applicationService).delete(appId, null);
    }

    // ── list(ids) ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("list: delegates to service and returns its response")
    void listApplicationsByIdsSuccessfully() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        List<UUID> ids = List.of(id1, id2);
        Application app1 = new Application();
        Application app2 = new Application();
        when(applicationService.list(id1, id2)).thenReturn(ResponseEntity.ok(List.of(app1, app2)));

        ResponseEntity<List<Application>> response = applicationController.list(ids);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(applicationService).list(id1, id2);
    }

    @Test
    @DisplayName("list: no matches delegates 404 from service")
    void listApplicationsByIdsNoMatchDelegates404() {
        UUID id1 = UUID.randomUUID();
        List<UUID> ids = List.of(id1);
        when(applicationService.list(id1)).thenReturn(ResponseEntity.notFound().build());

        ResponseEntity<List<Application>> response = applicationController.list(ids);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(applicationService).list(id1);
    }

    // ── listAll ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("listAll: delegates to service and returns its response")
    void listAllApplicationsSuccessfully() {
        Application app = new Application();
        when(applicationService.listAll()).thenReturn(ResponseEntity.ok(List.of(app)));

        ResponseEntity<List<Application>> response = applicationController.listAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(applicationService).listAll();
    }
}
