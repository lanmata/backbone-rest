/*
 *  @(#)ApplicationServiceImpl.java
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

package com.umdc.backoffice.v1.application.service;

import com.umdc.backoffice.v1.application.mapper.ApplicationMapper;
import com.umdc.commons.general.pojo.Application;
import com.umdc.persistence.general.repositories.ApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.umdc.backoffice.util.MessageUtil.MESSAGE_HEADER_STR;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationServiceImpl.class);

    private static final String NOT_FOUND_MSG = "Application not found.";
    private static final String FOUND_MSG = "Application found.";
    private static final String CREATED_MSG = "Application created successfully.";
    private static final String BAD_REQUEST_MSG = "Invalid request. The 'application' body is required and must include a non-blank 'name'.";
    private static final String NO_DATA_MSG = "No applications found.";

    private final ApplicationRepository applicationRepository;
    private final ApplicationMapper applicationMapper;

    public ApplicationServiceImpl(ApplicationRepository applicationRepository, ApplicationMapper applicationMapper) {
        this.applicationRepository = applicationRepository;
        this.applicationMapper = applicationMapper;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public ResponseEntity<Application> create(Application application) {
        if (Objects.isNull(application) || Objects.isNull(application.getName()) || application.getName().isBlank()) {
            LOGGER.debug("create called with null or nameless application");
            return ResponseEntity.badRequest().header(MESSAGE_HEADER_STR, BAD_REQUEST_MSG).build();
        }
        application.setId(null);
        var entity = applicationMapper.toSource(application);
        if (Objects.isNull(entity.getCodeName())) {
            String raw = application.getName().toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9]+", "_");
            entity.setCodeName(raw.length() > 8 ? raw.substring(0, 8) : raw);
        }
        var saved = applicationRepository.save(entity);
        Application created = applicationMapper.toTarget(saved);
        created.setCreatedDate(LocalDateTime.now(ZoneOffset.UTC));
        LOGGER.debug("Application created: id={}, name={}", created.getId(), created.getName());
        return ResponseEntity.status(HttpStatus.CREATED).header(MESSAGE_HEADER_STR, CREATED_MSG).body(created);
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Application> find(UUID id) {
        Optional<Application> result = applicationRepository.findById(id)
                .map(applicationMapper::toTarget);
        return result.map(app -> ResponseEntity.ok().header(MESSAGE_HEADER_STR, FOUND_MSG).body(app))
                     .orElseGet(() -> ResponseEntity.notFound().header(MESSAGE_HEADER_STR, NOT_FOUND_MSG).build());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Application> update(UUID id, Application application) {
        return ApplicationService.super.update(id, application);
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Application> delete(UUID id, Application application) {
        return ApplicationService.super.delete(id, application);
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Application>> list(UUID... id) {
        return ApplicationService.super.list(id);
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Application>> listAll() {
        LOGGER.debug("Listing all applications");
        List<Application> applications = new ArrayList<>();
        applicationRepository.findAll().forEach(entity -> applications.add(applicationMapper.toTarget(entity)));
        if (applications.isEmpty()) {
            LOGGER.debug("No applications found");
            return ResponseEntity.notFound().header(MESSAGE_HEADER_STR, NO_DATA_MSG).build();
        }
        return ResponseEntity.ok().header(MESSAGE_HEADER_STR, FOUND_MSG).body(applications);
    }
}
