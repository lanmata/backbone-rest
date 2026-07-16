/*
 *  @(#)NoticeTypeControllerTest.java
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
package com.umdc.backoffice.v1.noticetypes.api.controller;

import com.umdc.backoffice.v1.noticetypes.api.to.NoticeType;
import com.umdc.backoffice.v1.noticetypes.api.to.NoticeTypeRequest;
import com.umdc.backoffice.v1.noticetypes.service.NoticeTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/// Unit tests for {@link NoticeTypeController}.
class NoticeTypeControllerTest {

    private static final String NOTICE_TYPE_NAME = "test-notice-type";

    private NoticeTypeService noticeTypeService;
    private NoticeTypeController noticeTypeController;

    @BeforeEach
    void setUp() {
        noticeTypeService = mock(NoticeTypeService.class);
        noticeTypeController = new NoticeTypeController(noticeTypeService);
    }

    // ── create ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("createNoticeType: delegates to service and returns its response")
    void createNoticeType_delegatesToService() {
        NoticeType noticeType = buildPojo();
        NoticeTypeRequest request = new NoticeTypeRequest();
        request.setNoticeType(noticeType);

        when(noticeTypeService.create(noticeType))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(noticeType));

        ResponseEntity<NoticeType> response = noticeTypeController.createNoticeType(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(noticeType, response.getBody());
        verify(noticeTypeService).create(noticeType);
    }

    // ── find ────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findNoticeTypeById: delegates to service and returns its response")
    void findNoticeTypeById_delegatesToService() {
        UUID id = UUID.randomUUID();
        NoticeType noticeType = buildPojo();

        when(noticeTypeService.find(id)).thenReturn(ResponseEntity.ok(noticeType));

        ResponseEntity<NoticeType> response = noticeTypeController.findNoticeTypeById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(noticeType, response.getBody());
        verify(noticeTypeService).find(id);
    }

    @Test
    @DisplayName("findNoticeTypeById: delegates 404 from service")
    void findNoticeTypeById_delegatesNotFound() {
        UUID id = UUID.randomUUID();

        when(noticeTypeService.find(id)).thenReturn(ResponseEntity.notFound().build());

        ResponseEntity<NoticeType> response = noticeTypeController.findNoticeTypeById(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(noticeTypeService).find(id);
    }

    // ── update ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateNoticeType: delegates to service and returns its response")
    void updateNoticeType_delegatesToService() {
        UUID id = UUID.randomUUID();
        NoticeType noticeType = buildPojo();
        NoticeTypeRequest request = new NoticeTypeRequest();
        request.setNoticeType(noticeType);

        when(noticeTypeService.update(id, noticeType)).thenReturn(ResponseEntity.ok(noticeType));

        ResponseEntity<NoticeType> response = noticeTypeController.updateNoticeType(id, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(noticeType, response.getBody());
        verify(noticeTypeService).update(id, noticeType);
    }

    @Test
    @DisplayName("updateNoticeType: delegates 404 from service")
    void updateNoticeType_delegatesNotFound() {
        UUID id = UUID.randomUUID();
        NoticeType noticeType = buildPojo();
        NoticeTypeRequest request = new NoticeTypeRequest();
        request.setNoticeType(noticeType);

        when(noticeTypeService.update(id, noticeType)).thenReturn(ResponseEntity.notFound().build());

        ResponseEntity<NoticeType> response = noticeTypeController.updateNoticeType(id, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(noticeTypeService).update(id, noticeType);
    }

    // ── delete ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteNoticeType: delegates to service and returns its response")
    void deleteNoticeType_delegatesToService() {
        UUID id = UUID.randomUUID();
        NoticeType noticeType = buildPojo();

        when(noticeTypeService.delete(id, null)).thenReturn(ResponseEntity.ok(noticeType));

        ResponseEntity<NoticeType> response = noticeTypeController.deleteNoticeType(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(noticeType, response.getBody());
        verify(noticeTypeService).delete(id, null);
    }

    @Test
    @DisplayName("deleteNoticeType: delegates 404 from service")
    void deleteNoticeType_delegatesNotFound() {
        UUID id = UUID.randomUUID();

        when(noticeTypeService.delete(id, null)).thenReturn(ResponseEntity.notFound().build());

        ResponseEntity<NoticeType> response = noticeTypeController.deleteNoticeType(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(noticeTypeService).delete(id, null);
    }

    // ── listAll ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("listAllNoticeTypes: delegates to service and returns its response")
    void listAllNoticeTypes_delegatesToService() {
        NoticeType noticeType = buildPojo();

        when(noticeTypeService.listAll()).thenReturn(ResponseEntity.ok(List.of(noticeType)));

        ResponseEntity<List<NoticeType>> response = noticeTypeController.listAllNoticeTypes();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(noticeTypeService).listAll();
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
}
