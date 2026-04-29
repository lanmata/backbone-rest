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
import com.umdc.backoffice.v1.application.service.ApplicationService;
import com.prx.commons.general.pojo.Application;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplicationControllerTest {

    @Test
    @DisplayName("Create application successfully")
    void createApplicationSuccessfully() {
        ApplicationService applicationService = mock(ApplicationService.class);
        ApplicationController applicationController = new ApplicationController(applicationService);

        ApplicationCreateRequest request = new ApplicationCreateRequest();
        Application application = new Application();
        request.setApplication(application);
        when(applicationService.create(request.getApplication())).thenReturn(ResponseEntity.ok(application));

        ResponseEntity<Application> response = applicationController.create(request);

        assertEquals(ResponseEntity.ok(application), response);
    }

    @Test
    @DisplayName("Create application with null request")
    void createApplicationWithNullRequest() {
        ApplicationService applicationService = mock(ApplicationService.class);
        ApplicationController applicationController = new ApplicationController(applicationService);

        assertThrows(NullPointerException.class, () -> applicationController.create(null));
    }

    @Test
    @DisplayName("Create application with null application in request")
    void createApplicationWithNullApplicationInRequest() {
        ApplicationService applicationService = mock(ApplicationService.class);
        ApplicationController applicationController = new ApplicationController(applicationService);

        ApplicationCreateRequest request = new ApplicationCreateRequest();

        ResponseEntity<Application> response = applicationController.create(request);

        assertEquals(ResponseEntity.badRequest().build(), response);
    }
}
