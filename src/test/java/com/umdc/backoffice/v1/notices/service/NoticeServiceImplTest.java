/*
 *  @(#)NoticeServiceImplTest.java
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
package com.umdc.backoffice.v1.notices.service;

import com.umdc.backoffice.v1.notices.api.to.Notice;
import com.umdc.backoffice.v1.notices.mapper.NoticeMapper;
import com.umdc.persistence.general.domains.NoticeEntity;
import com.umdc.persistence.general.domains.NoticeId;
import com.umdc.persistence.general.domains.NoticeTypeEntity;
import com.umdc.persistence.general.repositories.ApplicationRepository;
import com.umdc.persistence.general.repositories.NoticeRepository;
import com.umdc.persistence.general.repositories.NoticeTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/// Unit tests for {@link NoticeServiceImpl}.
@ExtendWith(MockitoExtension.class)
class NoticeServiceImplTest {

    @Mock
    private NoticeRepository noticeRepository;

    @Mock
    private NoticeTypeRepository noticeTypeRepository;

    @Mock
    private NoticeMapper noticeMapper;

    @Mock
    private ApplicationRepository applicationRepository;

    private NoticeServiceImpl noticeService;

    @BeforeEach
    void setUp() {
        noticeService = new NoticeServiceImpl(noticeRepository, noticeTypeRepository, noticeMapper, applicationRepository);
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create: returns 201 when notice is valid and notice type exists")
    void create_returnsCreated_whenValid() {
        UUID userId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID noticeTypeId = UUID.randomUUID();

        Notice notice = buildNotice(userId, applicationId, noticeTypeId);
        NoticeTypeEntity noticeType = buildNoticeTypeEntity(noticeTypeId);
        NoticeEntity entity = buildEntity(userId, applicationId, noticeTypeId);
        NoticeEntity saved = buildEntity(userId, applicationId, noticeTypeId);
        Notice created = buildNotice(userId, applicationId, noticeTypeId);

        doReturn(Optional.of(noticeType)).when(noticeTypeRepository).findById(noticeTypeId);
        doReturn(entity).when(noticeMapper).toSource(notice);
        doReturn(saved).when(noticeRepository).save(any(NoticeEntity.class));
        doReturn(created).when(noticeMapper).toTarget(saved);

        ResponseEntity<Notice> response = noticeService.create(notice);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().getUserId());
        assertEquals(applicationId, response.getBody().getApplicationId());
        assertEquals(noticeTypeId, response.getBody().getNoticeTypeId());
        verify(noticeRepository).save(entity);
    }

    @Test
    @DisplayName("create: returns 400 when notice is null")
    void create_returnsBadRequest_whenNoticeNull() {
        ResponseEntity<Notice> response = noticeService.create(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(noticeTypeRepository, noticeRepository, noticeMapper);
    }

    @Test
    @DisplayName("create: returns 400 when userId is null")
    void create_returnsBadRequest_whenUserIdNull() {
        Notice notice = buildNotice(null, UUID.randomUUID(), UUID.randomUUID());

        ResponseEntity<Notice> response = noticeService.create(notice);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(noticeTypeRepository, noticeRepository, noticeMapper);
    }

    @Test
    @DisplayName("create: returns 400 when applicationId is null")
    void create_returnsBadRequest_whenApplicationIdNull() {
        Notice notice = buildNotice(UUID.randomUUID(), null, UUID.randomUUID());

        ResponseEntity<Notice> response = noticeService.create(notice);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(noticeTypeRepository, noticeRepository, noticeMapper);
    }

    @Test
    @DisplayName("create: returns 400 when noticeTypeId is null")
    void create_returnsBadRequest_whenNoticeTypeIdNull() {
        Notice notice = buildNotice(UUID.randomUUID(), UUID.randomUUID(), null);

        ResponseEntity<Notice> response = noticeService.create(notice);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(noticeTypeRepository, noticeRepository, noticeMapper);
    }

    @Test
    @DisplayName("create: returns 400 when the referenced notice type does not exist")
    void create_returnsBadRequest_whenNoticeTypeNotFound() {
        UUID userId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID noticeTypeId = UUID.randomUUID();
        Notice notice = buildNotice(userId, applicationId, noticeTypeId);

        doReturn(Optional.empty()).when(noticeTypeRepository).findById(noticeTypeId);

        ResponseEntity<Notice> response = noticeService.create(notice);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(noticeRepository, noticeMapper);
    }

    // ── listByApplication ────────────────────────────────────────────────────────

    @Test
    @DisplayName("listByApplication: returns 200 with populated list when notices exist")
    void listByApplication_returnsOk_whenNoticesExist() {
        UUID applicationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID noticeTypeId = UUID.randomUUID();
        NoticeEntity entity = buildEntity(userId, applicationId, noticeTypeId);
        Notice notice = buildNotice(userId, applicationId, noticeTypeId);

        doReturn(true).when(applicationRepository).existsById(applicationId);
        doReturn(List.of(entity)).when(noticeRepository).findByIdApplicationId(applicationId);
        doReturn(notice).when(noticeMapper).toTarget(entity);

        ResponseEntity<List<Notice>> response = noticeService.listByApplication(applicationId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(notice, response.getBody().get(0));
    }

    @Test
    @DisplayName("listByApplication: returns 200 with empty list when no notices found for the application")
    void listByApplication_returnsOkWithEmptyList_whenNoneFound() {
        UUID applicationId = UUID.randomUUID();

        doReturn(true).when(applicationRepository).existsById(applicationId);
        doReturn(List.of()).when(noticeRepository).findByIdApplicationId(applicationId);

        ResponseEntity<List<Notice>> response = noticeService.listByApplication(applicationId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verifyNoInteractions(noticeMapper);
    }

    @Test
    @DisplayName("listByApplication: returns 404 when the application does not exist")
    void listByApplication_returnsNotFound_whenApplicationAbsent() {
        UUID applicationId = UUID.randomUUID();

        doReturn(false).when(applicationRepository).existsById(applicationId);

        ResponseEntity<List<Notice>> response = noticeService.listByApplication(applicationId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(noticeRepository, never()).findByIdApplicationId(any(UUID.class));
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete: returns 200 when the notice exists for the composite key")
    void delete_returnsOk_whenExists() {
        UUID userId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID noticeTypeId = UUID.randomUUID();
        NoticeId id = buildNoticeId(userId, applicationId, noticeTypeId);

        doReturn(true).when(noticeRepository).existsById(id.getNoticeTypeId());

        ResponseEntity<?> response = noticeService.delete(userId, applicationId, noticeTypeId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(noticeRepository).deleteById(id.getNoticeTypeId());
    }

    @Test
    @DisplayName("delete: returns 404 when no notice matches the composite key")
    void delete_returnsNotFound_whenAbsent() {
        UUID userId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID noticeTypeId = UUID.randomUUID();
        NoticeId id = buildNoticeId(userId, applicationId, noticeTypeId);

        doReturn(false).when(noticeRepository).existsById(id.getNoticeTypeId());

        ResponseEntity<?> response = noticeService.delete(userId, applicationId, noticeTypeId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(noticeRepository, never()).deleteById(any(UUID.class));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Notice buildNotice(UUID userId, UUID applicationId, UUID noticeTypeId) {
        Notice notice = new Notice();
        notice.setUserId(userId);
        notice.setApplicationId(applicationId);
        notice.setNoticeTypeId(noticeTypeId);
        return notice;
    }

    private NoticeId buildNoticeId(UUID userId, UUID applicationId, UUID noticeTypeId) {
        NoticeId id = new NoticeId();
        id.setUserId(userId);
        id.setApplicationId(applicationId);
        id.setNoticeTypeId(noticeTypeId);
        return id;
    }

    private NoticeEntity buildEntity(UUID userId, UUID applicationId, UUID noticeTypeId) {
        NoticeEntity entity = new NoticeEntity();
        entity.setId(buildNoticeId(userId, applicationId, noticeTypeId));
        entity.setCreatedAt(Instant.now());
        return entity;
    }

    private NoticeTypeEntity buildNoticeTypeEntity(UUID id) {
        NoticeTypeEntity entity = new NoticeTypeEntity();
        entity.setId(id);
        entity.setName("test-notice-type");
        entity.setDescription("A test notice type");
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        entity.setActive(true);
        return entity;
    }
}
