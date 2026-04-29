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
import com.prx.commons.general.pojo.Application;
import com.prx.persistence.general.repositories.ApplicationRepository;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@org.springframework.stereotype.Service
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationMapper applicationMapper;

    public ApplicationServiceImpl(ApplicationRepository applicationRepository, ApplicationMapper applicationMapper) {
        this.applicationRepository = applicationRepository;
        this.applicationMapper = applicationMapper;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Application> create(Application application) {
        var result = Optional.of(applicationRepository.save(applicationMapper.toSource(application)));
        return result.map(entity -> ResponseEntity.ok(applicationMapper.toTarget(entity))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Application> find(UUID id) {
        var applicationEntity = applicationRepository.findById(id);
        return ResponseEntity.ok(applicationMapper.toTarget(applicationEntity.get()));
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
}
