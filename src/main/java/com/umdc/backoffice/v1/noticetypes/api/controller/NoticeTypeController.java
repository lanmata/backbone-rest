/*
 *  @(#)NoticeTypeController.java
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing notice types.
 * Provides endpoints for CRUD operations on notice types.
 */
@RestController
@RequestMapping("/api/v1/notice-types")
public class NoticeTypeController implements NoticeTypeApi {

    private final NoticeTypeService noticeTypeService;

    /**
     * Constructor for NoticeTypeController.
     *
     * @param noticeTypeService the notice type service
     */
    public NoticeTypeController(NoticeTypeService noticeTypeService) {
        this.noticeTypeService = noticeTypeService;
    }

    @Override
    public ResponseEntity<NoticeType> createNoticeType(NoticeTypeRequest noticeTypeRequest) {
        return noticeTypeService.create(noticeTypeRequest.getNoticeType());
    }

    @Override
    public ResponseEntity<NoticeType> findNoticeTypeById(UUID noticeTypeId) {
        return noticeTypeService.find(noticeTypeId);
    }

    @Override
    public ResponseEntity<NoticeType> updateNoticeType(UUID noticeTypeId, NoticeTypeRequest noticeTypeRequest) {
        return noticeTypeService.update(noticeTypeId, noticeTypeRequest.getNoticeType());
    }

    @Override
    public ResponseEntity<NoticeType> deleteNoticeType(UUID noticeTypeId) {
        return noticeTypeService.delete(noticeTypeId, null);
    }

    @Override
    public ResponseEntity<List<NoticeType>> listAllNoticeTypes() {
        return noticeTypeService.listAll();
    }
}
