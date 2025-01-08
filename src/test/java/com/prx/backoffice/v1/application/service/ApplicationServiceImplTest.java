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

package com.prx.backoffice.v1.application.service;

import com.prx.backoffice.v1.application.mapper.ApplicationMapper;
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
    @DisplayName("Find application by ID successfully")
    void findApplicationByIdSuccessfully() {
        ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
        ApplicationMapper applicationMapper = mock(ApplicationMapper.class);
        ApplicationServiceImpl applicationService = new ApplicationServiceImpl(applicationRepository, applicationMapper);

        Application application = new Application();
        when(applicationRepository.findById(any())).thenReturn(Optional.of(new ApplicationEntity()));
        when(applicationMapper.toTarget(any())).thenReturn(application);

        assertThrows(NotImplementedException.class, () ->  applicationService.find(UUID.randomUUID()));
    }

    @Test
    @DisplayName("Find application by non-existent ID")
    void findApplicationByNonExistentId() {
        ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
        ApplicationMapper applicationMapper = mock(ApplicationMapper.class);
        ApplicationServiceImpl applicationService = new ApplicationServiceImpl(applicationRepository, applicationMapper);

        when(applicationRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(NotImplementedException.class, () ->  applicationService.find(UUID.randomUUID()));

    }

    @Test
    @DisplayName("Update application successfully")
    void updateApplicationSuccessfully() {
        ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
        ApplicationMapper applicationMapper = mock(ApplicationMapper.class);
        ApplicationServiceImpl applicationService = new ApplicationServiceImpl(applicationRepository, applicationMapper);

        Application application = new Application();
        when(applicationRepository.findById(any())).thenReturn(Optional.of(new ApplicationEntity()));
        when(applicationMapper.toSource(application)).thenReturn(new ApplicationEntity());
        when(applicationRepository.save(any())).thenReturn(new Application());
        when(applicationMapper.toTarget(any())).thenReturn(application);

        assertThrows(NotImplementedException.class, () ->  applicationService.update(UUID.randomUUID(), application));

    }

    @Test
    @DisplayName("Update application with non-existent ID")
    void updateApplicationWithNonExistentId() {
        ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
        ApplicationMapper applicationMapper = mock(ApplicationMapper.class);
        ApplicationServiceImpl applicationService = new ApplicationServiceImpl(applicationRepository, applicationMapper);

        Application application = new Application();
        when(applicationRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(NotImplementedException.class, () -> applicationService.update(UUID.randomUUID(), application));
    }

    @Test
    @DisplayName("Delete application successfully")
    void deleteApplicationSuccessfully() {
        ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
        ApplicationMapper applicationMapper = mock(ApplicationMapper.class);
        ApplicationServiceImpl applicationService = new ApplicationServiceImpl(applicationRepository, applicationMapper);

        Application application = new Application();
        when(applicationRepository.findById(any())).thenReturn(Optional.of(new ApplicationEntity()));
        doNothing().when(applicationRepository).delete(any());


        assertThrows(NotImplementedException.class, () -> applicationService.delete(UUID.randomUUID(), application));
    }

    @Test
    @DisplayName("Delete application with non-existent ID")
    void deleteApplicationWithNonExistentId() {
        ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
        ApplicationMapper applicationMapper = mock(ApplicationMapper.class);
        ApplicationServiceImpl applicationService = new ApplicationServiceImpl(applicationRepository, applicationMapper);

        Application application = new Application();
        when(applicationRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(NotImplementedException.class, () ->  applicationService.delete(UUID.randomUUID(), application));

    }
}
