/*
 *  @(#)ServiceTypeController.java
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
package com.umdc.backoffice.v1.servicetype.api.controller;

import com.umdc.backoffice.v1.servicetype.api.to.ServiceTypeRequest;
import com.umdc.backoffice.v1.servicetype.service.ServiceTypeService;
import com.umdc.commons.general.pojo.ServiceType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing service types.
 * Provides endpoints for CRUD operations on service types.
 */
@RestController
@RequestMapping("/api/v1/service-types")
public class ServiceTypeController implements ServiceTypeApi {

    private final ServiceTypeService serviceTypeService;

    /**
     * Constructor for ServiceTypeController.
     *
     * @param serviceTypeService the service type service
     */
    public ServiceTypeController(ServiceTypeService serviceTypeService) {
        this.serviceTypeService = serviceTypeService;
    }

    @Override
    public ResponseEntity<List<ServiceType>> listAll() {
        return serviceTypeService.listAll();
    }

    @Override
    public ResponseEntity<List<ServiceType>> listByStatus(boolean active) {
        return serviceTypeService.listByStatus(active);
    }

    @Override
    public ResponseEntity<ServiceType> find(UUID serviceTypeId) {
        return serviceTypeService.find(serviceTypeId);
    }

    @Override
    public ResponseEntity<ServiceType> create(ServiceTypeRequest serviceTypeRequest) {
        return serviceTypeService.create(serviceTypeRequest.getServiceType());
    }

    @Override
    public ResponseEntity<ServiceType> update(UUID serviceTypeId, ServiceTypeRequest serviceTypeRequest) {
        return serviceTypeService.update(serviceTypeId, serviceTypeRequest.getServiceType());
    }
}
