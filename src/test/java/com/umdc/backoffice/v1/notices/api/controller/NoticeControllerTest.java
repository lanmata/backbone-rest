/*
 *  @(#)NoticeControllerTest.java
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
package com.umdc.backoffice.v1.notices.api.controller;

import com.umdc.backoffice.v1.notices.api.to.Notice;
import com.umdc.backoffice.v1.notices.api.to.NoticeRequest;
import com.umdc.backoffice.v1.notices.service.NoticeService;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

/// Unit tests for {@link NoticeController} verifying correct delegation to the service layer.
@ExtendWith(MockitoExtension.class)
class NoticeControllerTest {

    @Mock
    private NoticeService noticeService;

    private NoticeController noticeController;

    @BeforeEach
    void setUp() {
        noticeController = new NoticeController(noticeService);
    }

    // ── createNotice ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /notices — delegates create and returns 201")
    void createNotice_delegates_returns201() {
        Notice notice = buildNotice(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        NoticeRequest request = buildRequest(notice);

        doReturn(ResponseEntity.status(HttpStatus.CREATED).body(notice))
                .when(noticeService).create(notice);

        ResponseEntity<Notice> response = noticeController.createNotice(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(noticeService).create(notice);
    }

    @Test
    @DisplayName("POST /notices — delegates create and returns 400")
    void createNotice_delegates_returns400() {
        NoticeRequest request = buildRequest(null);

        doReturn(ResponseEntity.badRequest().build())
                .when(noticeService).create(null);

        ResponseEntity<Notice> response = noticeController.createNotice(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(noticeService).create(null);
    }

    // ── listNoticesByApplication ─────────────────────────────────────────────────

    @Test
    @DisplayName("GET /notices/application/{applicationId} — delegates listByApplication and returns 200")
    void listNoticesByApplication_delegates_returns200() {
        UUID applicationId = UUID.randomUUID();
        Notice notice = buildNotice(UUID.randomUUID(), applicationId, UUID.randomUUID());

        doReturn(ResponseEntity.ok(List.of(notice)))
                .when(noticeService).listByApplication(applicationId);

        ResponseEntity<List<Notice>> response = noticeController.listNoticesByApplication(applicationId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(noticeService).listByApplication(applicationId);
    }

    // ── deleteNotice ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /notices/user/{userId}/application/{applicationId}/notice-type/{noticeTypeId} — delegates delete and returns 200")
    void deleteNotice_delegates_returns200() {
        UUID userId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID noticeTypeId = UUID.randomUUID();

        doReturn(ResponseEntity.ok().build())
                .when(noticeService).delete(userId, applicationId, noticeTypeId);

        ResponseEntity<?> response = noticeController.deleteNotice(userId, applicationId, noticeTypeId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(noticeService).delete(userId, applicationId, noticeTypeId);
    }

    @Test
    @DisplayName("DELETE /notices/user/{userId}/application/{applicationId}/notice-type/{noticeTypeId} — delegates delete and returns 404")
    void deleteNotice_delegates_returns404() {
        UUID userId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID noticeTypeId = UUID.randomUUID();

        doReturn(ResponseEntity.notFound().build())
                .when(noticeService).delete(userId, applicationId, noticeTypeId);

        ResponseEntity<?> response = noticeController.deleteNotice(userId, applicationId, noticeTypeId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(noticeService).delete(userId, applicationId, noticeTypeId);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Notice buildNotice(UUID userId, UUID applicationId, UUID noticeTypeId) {
        Notice notice = new Notice();
        notice.setUserId(userId);
        notice.setApplicationId(applicationId);
        notice.setNoticeTypeId(noticeTypeId);
        return notice;
    }

    private NoticeRequest buildRequest(Notice notice) {
        NoticeRequest request = new NoticeRequest();
        request.setNotice(notice);
        return request;
    }
}
