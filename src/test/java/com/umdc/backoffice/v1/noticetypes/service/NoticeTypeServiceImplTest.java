/*
 *  @(#)NoticeTypeServiceImplTest.java
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
package com.umdc.backoffice.v1.noticetypes.service;

import com.umdc.backoffice.v1.noticetypes.api.to.NoticeType;
import com.umdc.backoffice.v1.noticetypes.mapper.NoticeTypeMapper;
import com.umdc.persistence.general.domains.NoticeTypeEntity;
import com.umdc.persistence.general.repositories.NoticeTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/// Unit tests for {@link NoticeTypeServiceImpl}.
@ExtendWith(MockitoExtension.class)
class NoticeTypeServiceImplTest {

    private static final String NOTICE_TYPE_NAME = "test-notice-type";
    private static final Instant EXISTING_CREATED_AT = Instant.parse("2024-01-01T00:00:00Z");
    private static final Instant EXISTING_UPDATED_AT = Instant.parse("2024-02-01T00:00:00Z");

    @Mock
    private NoticeTypeRepository noticeTypeRepository;

    @Mock
    private NoticeTypeMapper noticeTypeMapper;

    private NoticeTypeServiceImpl noticeTypeService;

    @BeforeEach
    void setUp() {
        noticeTypeService = new NoticeTypeServiceImpl(noticeTypeRepository, noticeTypeMapper);
    }

    // ── listAll ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("listAll: returns 200 with populated list when notice types exist")
    void listAll_returnsOkWithList() {
        NoticeTypeEntity entity = buildEntity();
        NoticeType pojo = buildPojo();

        doReturn(List.of(entity)).when(noticeTypeRepository).findAll();
        doReturn(pojo).when(noticeTypeMapper).toTarget(entity);

        ResponseEntity<List<NoticeType>> response = noticeTypeService.listAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("listAll: returns 200 with empty list when no notice types found")
    void listAll_returnsOkWithEmptyList_whenNoneExist() {
        doReturn(List.of()).when(noticeTypeRepository).findAll();

        ResponseEntity<List<NoticeType>> response = noticeTypeService.listAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    // ── find ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("find: returns 200 when notice type is found")
    void find_returnsOk_whenFound() {
        UUID id = UUID.randomUUID();
        NoticeTypeEntity entity = buildEntity();
        NoticeType pojo = buildPojo();

        doReturn(Optional.of(entity)).when(noticeTypeRepository).findById(id);
        doReturn(pojo).when(noticeTypeMapper).toTarget(entity);

        ResponseEntity<NoticeType> response = noticeTypeService.find(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(noticeTypeRepository).findById(id);
    }

    @Test
    @DisplayName("find: returns 404 when notice type is absent")
    void find_returnsNotFound_whenAbsent() {
        UUID id = UUID.randomUUID();

        doReturn(Optional.empty()).when(noticeTypeRepository).findById(id);

        ResponseEntity<NoticeType> response = noticeTypeService.find(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("find: returns 400 when id is null")
    void find_returnsBadRequest_whenIdNull() {
        ResponseEntity<NoticeType> response = noticeTypeService.find(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create: returns 201 and stamps createdAt/updatedAt server-side")
    void create_returnsCreated_andStampsTimestamps() {
        NoticeType pojo = buildPojo();
        pojo.setCreatedAt(EXISTING_CREATED_AT);
        pojo.setUpdatedAt(EXISTING_UPDATED_AT);

        NoticeTypeEntity mappedEntity = new NoticeTypeEntity();
        mappedEntity.setId(UUID.randomUUID());
        mappedEntity.setName(NOTICE_TYPE_NAME);
        mappedEntity.setDescription("A test notice type");
        mappedEntity.setActive(true);
        mappedEntity.setCreatedAt(EXISTING_CREATED_AT);
        mappedEntity.setUpdatedAt(EXISTING_UPDATED_AT);

        NoticeTypeEntity saved = buildEntity();

        doReturn(mappedEntity).when(noticeTypeMapper).toSource(pojo);
        doReturn(saved).when(noticeTypeRepository).save(any(NoticeTypeEntity.class));
        doReturn(pojo).when(noticeTypeMapper).toTarget(saved);

        ResponseEntity<NoticeType> response = noticeTypeService.create(pojo);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        ArgumentCaptor<NoticeTypeEntity> captor = ArgumentCaptor.forClass(NoticeTypeEntity.class);
        verify(noticeTypeRepository).save(captor.capture());
        NoticeTypeEntity persisted = captor.getValue();

        assertNotNull(persisted.getCreatedAt());
        assertNotNull(persisted.getUpdatedAt());
        assertEquals(persisted.getCreatedAt(), persisted.getUpdatedAt());
        assertNotEquals(EXISTING_CREATED_AT, persisted.getCreatedAt());
        assertNotEquals(EXISTING_UPDATED_AT, persisted.getUpdatedAt());
    }

    @Test
    @DisplayName("create: assigns a random id when the mapped entity has none")
    void create_assignsRandomId_whenEntityIdMissing() {
        NoticeType pojo = buildPojo();
        pojo.setId(null);

        NoticeTypeEntity mappedEntity = new NoticeTypeEntity();
        mappedEntity.setName(NOTICE_TYPE_NAME);
        mappedEntity.setDescription("A test notice type");
        mappedEntity.setActive(true);

        NoticeTypeEntity saved = buildEntity();

        doReturn(mappedEntity).when(noticeTypeMapper).toSource(pojo);
        doReturn(saved).when(noticeTypeRepository).save(any(NoticeTypeEntity.class));
        doReturn(pojo).when(noticeTypeMapper).toTarget(saved);

        noticeTypeService.create(pojo);

        ArgumentCaptor<NoticeTypeEntity> captor = ArgumentCaptor.forClass(NoticeTypeEntity.class);
        verify(noticeTypeRepository).save(captor.capture());
        assertNotNull(captor.getValue().getId());
    }

    @Test
    @DisplayName("create: defaults active to true when not supplied")
    void create_defaultsActiveToTrue_whenNotSupplied() {
        NoticeType pojo = buildPojo();
        pojo.setActive(null);

        NoticeTypeEntity mappedEntity = new NoticeTypeEntity();
        mappedEntity.setId(UUID.randomUUID());
        mappedEntity.setName(NOTICE_TYPE_NAME);
        mappedEntity.setDescription("A test notice type");
        mappedEntity.setActive(null);

        NoticeTypeEntity saved = buildEntity();

        doReturn(mappedEntity).when(noticeTypeMapper).toSource(pojo);
        doReturn(saved).when(noticeTypeRepository).save(any(NoticeTypeEntity.class));
        doReturn(pojo).when(noticeTypeMapper).toTarget(saved);

        noticeTypeService.create(pojo);

        ArgumentCaptor<NoticeTypeEntity> captor = ArgumentCaptor.forClass(NoticeTypeEntity.class);
        verify(noticeTypeRepository).save(captor.capture());
        assertEquals(Boolean.TRUE, captor.getValue().getActive());
    }

    @Test
    @DisplayName("create: returns 400 when notice type is null")
    void create_returnsBadRequest_whenNull() {
        ResponseEntity<NoticeType> response = noticeTypeService.create(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("create: returns 400 when name is null")
    void create_returnsBadRequest_whenNameNull() {
        NoticeType pojo = buildPojo();
        pojo.setName(null);

        ResponseEntity<NoticeType> response = noticeTypeService.create(pojo);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(noticeTypeRepository, never()).save(any(NoticeTypeEntity.class));
    }

    @Test
    @DisplayName("create: returns 400 when name is blank")
    void create_returnsBadRequest_whenNameBlank() {
        NoticeType pojo = buildPojo();
        pojo.setName("   ");

        ResponseEntity<NoticeType> response = noticeTypeService.create(pojo);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(noticeTypeRepository, never()).save(any(NoticeTypeEntity.class));
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update: returns 200, refreshes updatedAt, and preserves createdAt")
    void update_returnsOk_refreshesUpdatedAt_preservesCreatedAt() {
        UUID id = UUID.randomUUID();
        NoticeType pojo = buildPojo();
        pojo.setName("updated-name");
        pojo.setDescription("updated description");
        pojo.setActive(false);

        NoticeTypeEntity existing = buildEntity();
        existing.setCreatedAt(EXISTING_CREATED_AT);
        existing.setUpdatedAt(EXISTING_UPDATED_AT);

        NoticeTypeEntity saved = buildEntity();

        doReturn(Optional.of(existing)).when(noticeTypeRepository).findById(id);
        doReturn(saved).when(noticeTypeRepository).save(existing);
        doReturn(pojo).when(noticeTypeMapper).toTarget(saved);

        ResponseEntity<NoticeType> response = noticeTypeService.update(id, pojo);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        ArgumentCaptor<NoticeTypeEntity> captor = ArgumentCaptor.forClass(NoticeTypeEntity.class);
        verify(noticeTypeRepository).save(captor.capture());
        NoticeTypeEntity persisted = captor.getValue();

        assertEquals(EXISTING_CREATED_AT, persisted.getCreatedAt());
        assertNotEquals(EXISTING_UPDATED_AT, persisted.getUpdatedAt());
        assertEquals("updated-name", persisted.getName());
        assertEquals("updated description", persisted.getDescription());
        assertEquals(Boolean.FALSE, persisted.getActive());
    }

    @Test
    @DisplayName("update: preserves existing active flag when request omits it")
    void update_preservesActive_whenNotSupplied() {
        UUID id = UUID.randomUUID();
        NoticeType pojo = buildPojo();
        pojo.setActive(null);

        NoticeTypeEntity existing = buildEntity();
        existing.setActive(true);

        NoticeTypeEntity saved = buildEntity();

        doReturn(Optional.of(existing)).when(noticeTypeRepository).findById(id);
        doReturn(saved).when(noticeTypeRepository).save(existing);
        doReturn(pojo).when(noticeTypeMapper).toTarget(saved);

        noticeTypeService.update(id, pojo);

        ArgumentCaptor<NoticeTypeEntity> captor = ArgumentCaptor.forClass(NoticeTypeEntity.class);
        verify(noticeTypeRepository).save(captor.capture());
        assertEquals(Boolean.TRUE, captor.getValue().getActive());
    }

    @Test
    @DisplayName("update: returns 404 when notice type is absent")
    void update_returnsNotFound_whenAbsent() {
        UUID id = UUID.randomUUID();
        NoticeType pojo = buildPojo();

        doReturn(Optional.empty()).when(noticeTypeRepository).findById(id);

        ResponseEntity<NoticeType> response = noticeTypeService.update(id, pojo);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("update: returns 400 when id, noticeType, or name is invalid")
    void update_returnsBadRequest_whenInvalidInputs() {
        ResponseEntity<NoticeType> responseNullId = noticeTypeService.update(null, buildPojo());
        assertEquals(HttpStatus.BAD_REQUEST, responseNullId.getStatusCode());

        ResponseEntity<NoticeType> responseNullNoticeType = noticeTypeService.update(UUID.randomUUID(), null);
        assertEquals(HttpStatus.BAD_REQUEST, responseNullNoticeType.getStatusCode());

        NoticeType blankName = buildPojo();
        blankName.setName(" ");
        ResponseEntity<NoticeType> responseBlankName = noticeTypeService.update(UUID.randomUUID(), blankName);
        assertEquals(HttpStatus.BAD_REQUEST, responseBlankName.getStatusCode());
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete: returns 200 with deleted body when found")
    void delete_returnsOk_whenFound() {
        UUID id = UUID.randomUUID();
        NoticeTypeEntity existing = buildEntity();
        NoticeType pojo = buildPojo();

        doReturn(Optional.of(existing)).when(noticeTypeRepository).findById(id);
        doReturn(pojo).when(noticeTypeMapper).toTarget(existing);

        ResponseEntity<NoticeType> response = noticeTypeService.delete(id, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pojo, response.getBody());
        verify(noticeTypeRepository).deleteById(id);
    }

    @Test
    @DisplayName("delete: returns 404 when notice type is absent")
    void delete_returnsNotFound_whenAbsent() {
        UUID id = UUID.randomUUID();

        doReturn(Optional.empty()).when(noticeTypeRepository).findById(id);

        ResponseEntity<NoticeType> response = noticeTypeService.delete(id, null);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(noticeTypeRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    @DisplayName("delete: returns 400 when id is null")
    void delete_returnsBadRequest_whenIdNull() {
        ResponseEntity<NoticeType> response = noticeTypeService.delete(null, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(noticeTypeRepository, never()).deleteById(any(UUID.class));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private NoticeType buildPojo() {
        NoticeType noticeType = new NoticeType();
        noticeType.setId(UUID.randomUUID());
        noticeType.setName(NOTICE_TYPE_NAME);
        noticeType.setDescription("A test notice type");
        noticeType.setActive(true);
        return noticeType;
    }

    private NoticeTypeEntity buildEntity() {
        NoticeTypeEntity entity = new NoticeTypeEntity();
        entity.setId(UUID.randomUUID());
        entity.setName(NOTICE_TYPE_NAME);
        entity.setDescription("A test notice type");
        entity.setActive(true);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return entity;
    }
}
