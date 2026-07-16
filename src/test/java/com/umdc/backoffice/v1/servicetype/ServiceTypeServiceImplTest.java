/*
 *  @(#)ServiceTypeServiceImplTest.java
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

import com.umdc.backoffice.v1.servicetype.mapper.ServiceTypeMapper;
import com.umdc.backoffice.v1.servicetype.service.ServiceTypeServiceImpl;
import com.umdc.commons.general.pojo.ServiceType;
import com.umdc.persistence.general.domains.ServiceTypeEntity;
import com.umdc.persistence.general.repositories.ServiceTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

/// Unit tests for {@link ServiceTypeServiceImpl}.
@ExtendWith(MockitoExtension.class)
class ServiceTypeServiceImplTest {

    private static final String SERVICE_TYPE_NAME = "test-service-type";

    @Mock
    private ServiceTypeRepository serviceTypeRepository;

    @Mock
    private ServiceTypeMapper serviceTypeMapper;

    private ServiceTypeServiceImpl serviceTypeService;

    @BeforeEach
    void setUp() {
        serviceTypeService = new ServiceTypeServiceImpl(serviceTypeRepository, serviceTypeMapper);
    }

    // ── listAll ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("listAll: returns 200 when service types exist")
    void listAll_returnsOkWithList() {
        ServiceTypeEntity entity = buildEntity();
        ServiceType pojo = buildPojo();

        doReturn(List.of(entity)).when(serviceTypeRepository).findAll();
        doReturn(pojo).when(serviceTypeMapper).toTarget(entity);

        ResponseEntity<List<ServiceType>> response = serviceTypeService.listAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("listAll: returns 404 when no service types found")
    void listAll_returnsNotFound_whenEmpty() {
        doReturn(List.of()).when(serviceTypeRepository).findAll();

        ResponseEntity<List<ServiceType>> response = serviceTypeService.listAll();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ── listByStatus ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("listByStatus(true): returns 200 when active service types exist")
    void listByStatus_active_returnsOk() {
        ServiceTypeEntity entity = buildEntity();
        ServiceType pojo = buildPojo();

        doReturn(List.of(entity)).when(serviceTypeRepository).findByActive(true);
        doReturn(pojo).when(serviceTypeMapper).toTarget(entity);

        ResponseEntity<List<ServiceType>> response = serviceTypeService.listByStatus(true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("listByStatus(false): returns 404 when no inactive service types found")
    void listByStatus_inactive_returnsNotFound_whenEmpty() {
        doReturn(List.of()).when(serviceTypeRepository).findByActive(false);

        ResponseEntity<List<ServiceType>> response = serviceTypeService.listByStatus(false);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ── find ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("find: returns 200 when service type is found")
    void find_returnsOk_whenFound() {
        UUID id = UUID.randomUUID();
        ServiceTypeEntity entity = buildEntity();
        ServiceType pojo = buildPojo();

        doReturn(Optional.of(entity)).when(serviceTypeRepository).findById(id);
        doReturn(pojo).when(serviceTypeMapper).toTarget(entity);

        ResponseEntity<ServiceType> response = serviceTypeService.find(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(serviceTypeRepository).findById(id);
    }

    @Test
    @DisplayName("find: returns 404 when service type is absent")
    void find_returnsNotFound_whenAbsent() {
        UUID id = UUID.randomUUID();

        doReturn(Optional.empty()).when(serviceTypeRepository).findById(id);

        ResponseEntity<ServiceType> response = serviceTypeService.find(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("find: returns 400 when id is null")
    void find_returnsBadRequest_whenIdNull() {
        ResponseEntity<ServiceType> response = serviceTypeService.find(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create: returns 201 when service type is valid and name is unique")
    void create_returnsCreated_whenValid() {
        ServiceType pojo = buildPojo();
        ServiceTypeEntity entity = buildEntity();
        ServiceTypeEntity saved = buildEntity();

        doReturn(false).when(serviceTypeRepository).existsByName(SERVICE_TYPE_NAME);
        doReturn(entity).when(serviceTypeMapper).toSource(pojo);
        doReturn(saved).when(serviceTypeRepository).save(any(ServiceTypeEntity.class));
        doReturn(pojo).when(serviceTypeMapper).toTarget(saved);

        ResponseEntity<ServiceType> response = serviceTypeService.create(pojo);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("create: returns 400 when service type is null")
    void create_returnsBadRequest_whenNull() {
        ResponseEntity<ServiceType> response = serviceTypeService.create(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("create: returns 409 when service type name already exists")
    void create_returnsConflict_whenNameExists() {
        ServiceType pojo = buildPojo();

        doReturn(true).when(serviceTypeRepository).existsByName(SERVICE_TYPE_NAME);

        ResponseEntity<ServiceType> response = serviceTypeService.create(pojo);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update: returns 202 when service type is found and updated")
    void update_returnsAccepted_whenFound() {
        UUID id = UUID.randomUUID();
        ServiceType pojo = buildPojo();
        ServiceTypeEntity existing = buildEntity();
        ServiceTypeEntity saved = buildEntity();

        doReturn(Optional.of(existing)).when(serviceTypeRepository).findById(id);
        doReturn(saved).when(serviceTypeRepository).save(existing);
        doReturn(pojo).when(serviceTypeMapper).toTarget(saved);

        ResponseEntity<ServiceType> response = serviceTypeService.update(id, pojo);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("update: returns 404 when service type is absent")
    void update_returnsNotFound_whenAbsent() {
        UUID id = UUID.randomUUID();
        ServiceType pojo = buildPojo();

        doReturn(Optional.empty()).when(serviceTypeRepository).findById(id);

        ResponseEntity<ServiceType> response = serviceTypeService.update(id, pojo);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("update: returns 400 when id or serviceType is null")
    void update_returnsBadRequest_whenNullInputs() {
        ResponseEntity<ServiceType> responseNullId = serviceTypeService.update(null, buildPojo());
        assertEquals(HttpStatus.BAD_REQUEST, responseNullId.getStatusCode());

        ResponseEntity<ServiceType> responseNullEntity = serviceTypeService.update(UUID.randomUUID(), null);
        assertEquals(HttpStatus.BAD_REQUEST, responseNullEntity.getStatusCode());
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

    private ServiceTypeEntity buildEntity() {
        ServiceTypeEntity entity = new ServiceTypeEntity();
        entity.setId(UUID.randomUUID());
        entity.setName(SERVICE_TYPE_NAME);
        entity.setDescription("A test service type");
        entity.setActive(true);
        return entity;
    }
}
