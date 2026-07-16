/*
 *  @(#)NoticeController.java
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing notices.
 */
@RestController
@RequestMapping("/api/v1/notices")
public class NoticeController implements NoticeApi {

    private final NoticeService noticeService;

    /**
     * Constructor for NoticeController.
     *
     * @param noticeService the notice service
     */
    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @Override
    public ResponseEntity<Notice> createNotice(NoticeRequest noticeRequest) {
        return noticeService.create(noticeRequest.getNotice());
    }

    @Override
    public ResponseEntity<List<Notice>> listNoticesByApplication(UUID applicationId) {
        return noticeService.listByApplication(applicationId);
    }

    @Override
    public ResponseEntity<?> deleteNotice(UUID userId, UUID applicationId, UUID noticeTypeId) {
        return noticeService.delete(userId, applicationId, noticeTypeId);
    }
}
