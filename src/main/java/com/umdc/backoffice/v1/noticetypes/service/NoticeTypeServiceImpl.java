/*
 *  @(#)NoticeTypeServiceImpl.java
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
 * Service implementation for notice type operations.
 */
@Service
public class NoticeTypeServiceImpl implements NoticeTypeService {

    private static final Logger log = LoggerFactory.getLogger(NoticeTypeServiceImpl.class);

    private static final String NOT_FOUND_MSG = "Notice type not found.";
    private static final String FOUND_MSG = "Notice type found.";
    private static final String CREATED_MSG = "Notice type created.";
    private static final String UPDATED_MSG = "Notice type updated.";
    private static final String DELETED_MSG = "Notice type deleted.";
    private static final String BAD_REQUEST_MSG =
            "Invalid request. The 'noticeType' body is required and must include a non-blank 'name'.";

    private final NoticeTypeRepository noticeTypeRepository;
    private final NoticeTypeMapper noticeTypeMapper;

    /**
     * Constructor for NoticeTypeServiceImpl.
     *
     * @param noticeTypeRepository the notice type repository
     * @param noticeTypeMapper     the notice type mapper
     */
    public NoticeTypeServiceImpl(NoticeTypeRepository noticeTypeRepository, NoticeTypeMapper noticeTypeMapper) {
        this.noticeTypeRepository = noticeTypeRepository;
        this.noticeTypeMapper = noticeTypeMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<NoticeType>> listAll() {
        log.debug("Listing all notice types");
        List<NoticeType> result = new ArrayList<>();
        noticeTypeRepository.findAll().forEach(entity -> result.add(noticeTypeMapper.toTarget(entity)));
        return ResponseEntity.ok().header(MESSAGE_HEADER_STR, FOUND_MSG).body(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<NoticeType> find(UUID id) {
        if (Objects.isNull(id)) {
            log.debug("find called with null id");
            return ResponseEntity.badRequest().header(MESSAGE_HEADER_STR, BAD_REQUEST_MSG).build();
        }
        Optional<NoticeTypeEntity> entity = noticeTypeRepository.findById(id);
        return entity.map(e -> ResponseEntity.ok()
                        .header(MESSAGE_HEADER_STR, FOUND_MSG)
                        .body(noticeTypeMapper.toTarget(e)))
                .orElseGet(() -> ResponseEntity.notFound().header(MESSAGE_HEADER_STR, NOT_FOUND_MSG).build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ResponseEntity<NoticeType> create(NoticeType noticeType) {
        if (Objects.isNull(noticeType) || Objects.isNull(noticeType.getName()) || noticeType.getName().isBlank()) {
            log.debug("create called with null or nameless noticeType");
            return ResponseEntity.badRequest().header(MESSAGE_HEADER_STR, BAD_REQUEST_MSG).build();
        }
        NoticeTypeEntity entity = noticeTypeMapper.toSource(noticeType);
        if (Objects.isNull(entity.getId())) {
            entity.setId(UUID.randomUUID());
        }
        Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        if (Objects.isNull(entity.getActive())) {
            entity.setActive(Boolean.TRUE);
        }
        NoticeTypeEntity saved = noticeTypeRepository.save(entity);
        log.debug("Notice type created: id={}", saved.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(MESSAGE_HEADER_STR, CREATED_MSG)
                .body(noticeTypeMapper.toTarget(saved));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ResponseEntity<NoticeType> update(UUID id, NoticeType noticeType) {
        if (Objects.isNull(id) || Objects.isNull(noticeType)
                || Objects.isNull(noticeType.getName()) || noticeType.getName().isBlank()) {
            log.debug("update called with null id, null noticeType, or nameless noticeType");
            return ResponseEntity.badRequest().header(MESSAGE_HEADER_STR, BAD_REQUEST_MSG).build();
        }
        Optional<NoticeTypeEntity> existing = noticeTypeRepository.findById(id);
        if (existing.isEmpty()) {
            log.debug("Notice type not found for update: id={}", id);
            return ResponseEntity.notFound().header(MESSAGE_HEADER_STR, NOT_FOUND_MSG).build();
        }
        NoticeTypeEntity entity = existing.get();
        entity.setName(noticeType.getName());
        entity.setDescription(noticeType.getDescription());
        if (!Objects.isNull(noticeType.getActive())) {
            entity.setActive(noticeType.getActive());
        }
        entity.setUpdatedAt(Instant.now());
        NoticeTypeEntity saved = noticeTypeRepository.save(entity);
        log.debug("Notice type updated: id={}", saved.getId());
        return ResponseEntity.ok()
                .header(MESSAGE_HEADER_STR, UPDATED_MSG)
                .body(noticeTypeMapper.toTarget(saved));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ResponseEntity<NoticeType> delete(UUID id, NoticeType noticeType) {
        if (Objects.isNull(id)) {
            log.debug("delete called with null id");
            return ResponseEntity.badRequest().header(MESSAGE_HEADER_STR, BAD_REQUEST_MSG).build();
        }
        Optional<NoticeTypeEntity> existing = noticeTypeRepository.findById(id);
        if (existing.isEmpty()) {
            log.debug("Notice type not found for delete: id={}", id);
            return ResponseEntity.notFound().header(MESSAGE_HEADER_STR, NOT_FOUND_MSG).build();
        }
        NoticeType deleted = noticeTypeMapper.toTarget(existing.get());
        noticeTypeRepository.deleteById(id);
        log.debug("Notice type deleted: id={}", id);
        return ResponseEntity.ok().header(MESSAGE_HEADER_STR, DELETED_MSG).body(deleted);
    }
}
