/*
 *  @(#)NoticeServiceImpl.java
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.umdc.backoffice.util.MessageUtil.MESSAGE_HEADER_STR;

/**
 * Service implementation for notice operations.
 */
@Service
public class NoticeServiceImpl implements NoticeService {

    private static final Logger log = LoggerFactory.getLogger(NoticeServiceImpl.class);

    private static final String NOT_FOUND_MSG = "Notice not found.";
    private static final String APPLICATION_NOT_FOUND_MSG = "Application not found.";
    private static final String FOUND_MSG = "Notice list returned.";
    private static final String CREATED_MSG = "Notice created.";
    private static final String DELETED_MSG = "Notice deleted.";
    private static final String BAD_REQUEST_MSG =
            "Invalid request. The 'notice' body is required and must include 'userId', "
                    + "'applicationId' and a valid 'noticeTypeId'.";

    private final NoticeRepository noticeRepository;
    private final NoticeTypeRepository noticeTypeRepository;
    private final NoticeMapper noticeMapper;
    private final ApplicationRepository applicationRepository;

    /**
     * Constructor for NoticeServiceImpl.
     *
     * @param noticeRepository      the notice repository
     * @param noticeTypeRepository  the notice type repository
     * @param noticeMapper          the notice mapper
     * @param applicationRepository the application repository
     */
    public NoticeServiceImpl(NoticeRepository noticeRepository,
                              NoticeTypeRepository noticeTypeRepository,
                              NoticeMapper noticeMapper,
                              ApplicationRepository applicationRepository) {
        this.noticeRepository = noticeRepository;
        this.noticeTypeRepository = noticeTypeRepository;
        this.noticeMapper = noticeMapper;
        this.applicationRepository = applicationRepository;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public ResponseEntity<Notice> create(Notice notice) {
        if (Objects.isNull(notice) || Objects.isNull(notice.getUserId()) || Objects.isNull(notice.getApplicationId())
                || Objects.isNull(notice.getNoticeTypeId())) {
            log.debug("create called with an invalid notice payload");
            return ResponseEntity.badRequest().header(MESSAGE_HEADER_STR, BAD_REQUEST_MSG).build();
        }
        Optional<NoticeTypeEntity> noticeType = noticeTypeRepository.findById(notice.getNoticeTypeId());
        if (noticeType.isEmpty()) {
            log.debug("Notice type not found for notice creation: noticeTypeId={}", notice.getNoticeTypeId());
            return ResponseEntity.badRequest().header(MESSAGE_HEADER_STR, BAD_REQUEST_MSG).build();
        }
        NoticeEntity entity = noticeMapper.toSource(notice);
        entity.setNoticeType(noticeType.get());
        entity.setCreatedAt(Instant.now());
        NoticeEntity saved = noticeRepository.save(entity);
        Notice created = noticeMapper.toTarget(saved);
        log.debug("Notice created: userId={}, applicationId={}, noticeTypeId={}",
                created.getUserId(), created.getApplicationId(), created.getNoticeTypeId());
        return ResponseEntity.status(HttpStatus.CREATED).header(MESSAGE_HEADER_STR, CREATED_MSG).body(created);
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Notice>> listByApplication(UUID applicationId) {
        if (!applicationRepository.existsById(applicationId)) {
            log.debug("Application not found for notice listing: applicationId={}", applicationId);
            return ResponseEntity.notFound().header(MESSAGE_HEADER_STR, APPLICATION_NOT_FOUND_MSG).build();
        }
        List<Notice> notices = new ArrayList<>();
        noticeRepository.findByIdApplicationId(applicationId).forEach(entity -> notices.add(noticeMapper.toTarget(entity)));
        return ResponseEntity.ok().header(MESSAGE_HEADER_STR, FOUND_MSG).body(notices);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public ResponseEntity<?> delete(UUID userId, UUID applicationId, UUID noticeTypeId) {
        NoticeId id = new NoticeId();
        id.setUserId(userId);
        id.setApplicationId(applicationId);
        id.setNoticeTypeId(noticeTypeId);
        if (!noticeRepository.existsById(id.getNoticeTypeId())) {
            log.debug("Notice not found for delete: userId={}, applicationId={}, noticeTypeId={}",
                    userId, applicationId, noticeTypeId);
            return ResponseEntity.notFound().header(MESSAGE_HEADER_STR, NOT_FOUND_MSG).build();
        }
        noticeRepository.deleteById(id.getNoticeTypeId());
        log.debug("Notice deleted: userId={}, applicationId={}, noticeTypeId={}", userId, applicationId, noticeTypeId);
        return ResponseEntity.ok().header(MESSAGE_HEADER_STR, DELETED_MSG).build();
    }
}
