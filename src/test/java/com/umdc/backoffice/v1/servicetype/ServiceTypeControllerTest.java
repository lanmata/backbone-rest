/*
 *  @(#)ServiceTypeControllerTest.java
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
package com.umdc.backoffice.v1.servicetype;

import com.umdc.backoffice.v1.servicetype.api.controller.ServiceTypeController;
import com.umdc.backoffice.v1.servicetype.api.to.ServiceTypeRequest;
import com.umdc.backoffice.v1.servicetype.service.ServiceTypeService;
import com.umdc.commons.general.pojo.ServiceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

/// Unit tests for {@link ServiceTypeController} verifying correct delegation to service layer.
@ExtendWith(MockitoExtension.class)
class ServiceTypeControllerTest {

    private static final String SERVICE_TYPE_NAME = "test-service-type";

    @Mock
    private ServiceTypeService serviceTypeService;

    private ServiceTypeController controller;

    @BeforeEach
    void setUp() {
        controller = new ServiceTypeController(serviceTypeService);
    }

    @Test
    @DisplayName("GET /service-types — delegates listAll and returns 200")
    void listAll_delegates_returns200() {
        doReturn(ResponseEntity.ok(List.of(buildPojo())))
                .when(serviceTypeService).listAll();

        ResponseEntity<List<ServiceType>> response = controller.listAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(serviceTypeService).listAll();
    }

    @Test
    @DisplayName("GET /service-types/{active} — delegates listByStatus and returns 200")
    void listByStatus_delegates_returns200() {
        doReturn(ResponseEntity.ok(List.of(buildPojo())))
                .when(serviceTypeService).listByStatus(true);

        ResponseEntity<List<ServiceType>> response = controller.listByStatus(true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(serviceTypeService).listByStatus(true);
    }

    @Test
    @DisplayName("GET /service-types/find/{id} — delegates find and returns 200")
    void findById_delegates_returns200() {
        UUID id = UUID.randomUUID();
        doReturn(ResponseEntity.ok(buildPojo()))
                .when(serviceTypeService).find(id);

        ResponseEntity<ServiceType> response = controller.find(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(serviceTypeService).find(id);
    }

    @Test
    @DisplayName("GET /service-types/find/{id} — delegates find and returns 404")
    void findById_delegates_returns404() {
        UUID id = UUID.randomUUID();
        doReturn(ResponseEntity.notFound().build())
                .when(serviceTypeService).find(id);

        ResponseEntity<ServiceType> response = controller.find(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(serviceTypeService).find(id);
    }

    @Test
    @DisplayName("POST /service-types/ — delegates create and returns 201")
    void create_delegates_returns201() {
        ServiceTypeRequest request = buildRequest();
        doReturn(ResponseEntity.status(HttpStatus.CREATED).body(buildPojo()))
                .when(serviceTypeService).create(request.getServiceType());

        ResponseEntity<ServiceType> response = controller.create(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(serviceTypeService).create(request.getServiceType());
    }

    @Test
    @DisplayName("PUT /service-types/{id} — delegates update and returns 202")
    void update_delegates_returns202() {
        UUID id = UUID.randomUUID();
        ServiceTypeRequest request = buildRequest();
        doReturn(ResponseEntity.status(HttpStatus.ACCEPTED).body(buildPojo()))
                .when(serviceTypeService).update(id, request.getServiceType());

        ResponseEntity<ServiceType> response = controller.update(id, request);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        verify(serviceTypeService).update(id, request.getServiceType());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private ServiceType buildPojo() {
        ServiceType st = new ServiceType();
        st.setId(UUID.randomUUID());
        st.setName(SERVICE_TYPE_NAME);
        st.setDescription("A test service type");
        st.setActive(true);
        return st;
    }

    private ServiceTypeRequest buildRequest() {
        ServiceTypeRequest request = new ServiceTypeRequest();
        request.setServiceType(buildPojo());
        return request;
    }
}
