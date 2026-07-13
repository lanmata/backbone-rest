/*
 *  @(#)ApplicationController.java
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * REST controller for managing applications.
 */
@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController implements ApplicationApi {

    private final ApplicationService applicationService;

    /**
     * Constructor for ApplicationController.
     *
     * @param applicationService the application service
     */
    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Override
    public ResponseEntity<List<Application>> listAll() {
        return applicationService.listAll();
    }

    @Override
    public ResponseEntity<List<Application>> list(List<UUID> ids) {
        return applicationService.list(ids.toArray(new UUID[0]));
    }

    @Override
    public ResponseEntity<Application> create(ApplicationCreateRequest applicationCreateRequest) {
        if (Objects.isNull(applicationCreateRequest.getApplication())) {
            return ResponseEntity.badRequest().build();
        }
        return applicationService.create(applicationCreateRequest.getApplication());
    }

    @Override
    public ResponseEntity<Application> find(UUID id) {
        return applicationService.find(id);
    }

    @Override
    public ResponseEntity<Application> update(UUID id, ApplicationUpdateRequest request) {
        if (Objects.isNull(request.getApplication())) {
            return ResponseEntity.badRequest().build();
        }
        return applicationService.update(id, request.getApplication());
    }

    @Override
    public ResponseEntity<Application> delete(UUID id) {
        return applicationService.delete(id, null);
    }
}
