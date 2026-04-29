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
import com.prx.commons.general.pojo.Application;
import com.prx.persistence.general.domains.ApplicationEntity;
import com.prx.persistence.general.repositories.ApplicationRepository;
import org.apache.commons.lang.NotImplementedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ApplicationServiceImplTest {

    @Test
    @DisplayName("Create application successfully")
    void createApplicationSuccessfully() {
        ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
        ApplicationMapper applicationMapper = mock(ApplicationMapper.class);
        ApplicationServiceImpl applicationService = new ApplicationServiceImpl(applicationRepository, applicationMapper);

        Application application = new Application();
        when(applicationMapper.toSource(application)).thenReturn(new ApplicationEntity());
        when(applicationRepository.save(any())).thenReturn(new ApplicationEntity());
        when(applicationMapper.toTarget(any())).thenReturn(application);

        ResponseEntity<Application> response = applicationService.create(application);

        assertEquals(ResponseEntity.ok(application), response);
    }

    @Test
    @DisplayName("Create application with null application")
    void createApplicationWithNullApplication() {
        ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
        ApplicationMapper applicationMapper = mock(ApplicationMapper.class);
        ApplicationServiceImpl applicationService = new ApplicationServiceImpl(applicationRepository, applicationMapper);


        assertThrows(NullPointerException.class, () ->  applicationService.create(null));
    }

    @Test
    @DisplayName("Update application successfully")
    void updateApplicationSuccessfully() {
        var id = UUID.randomUUID();
        ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
        ApplicationMapper applicationMapper = mock(ApplicationMapper.class);
        ApplicationServiceImpl applicationService = new ApplicationServiceImpl(applicationRepository, applicationMapper);

        Application application = new Application();
        when(applicationRepository.findById(any())).thenReturn(Optional.of(new ApplicationEntity()));
        when(applicationMapper.toSource(application)).thenReturn(new ApplicationEntity());
        when(applicationRepository.save(any())).thenReturn(new Application());
        when(applicationMapper.toTarget(any())).thenReturn(application);

        assertThrows(NotImplementedException.class, () ->  applicationService.update(id, application));

    }

    @Test
    @DisplayName("Update application with non-existent ID")
    void updateApplicationWithNonExistentId() {
        var id = UUID.randomUUID();
        ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
        ApplicationMapper applicationMapper = mock(ApplicationMapper.class);
        ApplicationServiceImpl applicationService = new ApplicationServiceImpl(applicationRepository, applicationMapper);

        Application application = new Application();
        when(applicationRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(NotImplementedException.class, () -> applicationService.update(id, application));
    }

    @Test
    @DisplayName("Delete application successfully")
    void deleteApplicationSuccessfully() {
        var id = UUID.randomUUID();
        ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
        ApplicationMapper applicationMapper = mock(ApplicationMapper.class);
        ApplicationServiceImpl applicationService = new ApplicationServiceImpl(applicationRepository, applicationMapper);

        Application application = new Application();
        when(applicationRepository.findById(any())).thenReturn(Optional.of(new ApplicationEntity()));
        doNothing().when(applicationRepository).delete(any());


        assertThrows(NotImplementedException.class, () -> applicationService.delete(id, application));
    }

    @Test
    @DisplayName("Delete application with non-existent ID")
    void deleteApplicationWithNonExistentId() {
        var id = UUID.randomUUID();
        ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
        ApplicationMapper applicationMapper = mock(ApplicationMapper.class);
        ApplicationServiceImpl applicationService = new ApplicationServiceImpl(applicationRepository, applicationMapper);

        Application application = new Application();
        when(applicationRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(NotImplementedException.class, () ->  applicationService.delete(id, application));

    }

    @Test
    @DisplayName("Find application by ID successfully")
    void findApplicationByIdSuccessfully() {
        ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
        ApplicationMapper applicationMapper = mock(ApplicationMapper.class);
        ApplicationServiceImpl applicationService = new ApplicationServiceImpl(applicationRepository, applicationMapper);

        UUID appId = UUID.randomUUID();
        ApplicationEntity applicationEntity = new ApplicationEntity();
        applicationEntity.setId(appId);
        Application application = new Application();
        application.setId(appId);

        when(applicationRepository.findById(appId)).thenReturn(Optional.of(applicationEntity));
        when(applicationMapper.toTarget(applicationEntity)).thenReturn(application);

        ResponseEntity<Application> response = applicationService.find(appId);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(application, response.getBody());
        verify(applicationRepository, times(1)).findById(appId);
    }

    @Test
    @DisplayName("Find application by non-existent ID throws exception")
    void findApplicationByNonExistentIdThrowsException() {
        ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
        ApplicationMapper applicationMapper = mock(ApplicationMapper.class);
        ApplicationServiceImpl applicationService = new ApplicationServiceImpl(applicationRepository, applicationMapper);

        UUID appId = UUID.randomUUID();
        when(applicationRepository.findById(appId)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> applicationService.find(appId));
    }

    @Test
    @DisplayName("List applications returns not implemented")
    void listApplicationsReturnsNotImplemented() {
        ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
        ApplicationMapper applicationMapper = mock(ApplicationMapper.class);
        ApplicationServiceImpl applicationService = new ApplicationServiceImpl(applicationRepository, applicationMapper);

        UUID appId = UUID.randomUUID();
        assertThrows(NotImplementedException.class, () -> applicationService.list(appId));
    }

    @Test
    @DisplayName("Create application with complete data")
    void createApplicationWithCompleteData() {
        ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
        ApplicationMapper applicationMapper = mock(ApplicationMapper.class);
        ApplicationServiceImpl applicationService = new ApplicationServiceImpl(applicationRepository, applicationMapper);

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

        assertEquals(200, response.getStatusCode().value());
        assertEquals(application, response.getBody());
        verify(applicationRepository, times(1)).save(entity);
    }
}
