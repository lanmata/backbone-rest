/*
 *  @(#)ServiceTypeServiceImpl.java
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
package com.umdc.backoffice.v1.servicetype.service;

import com.umdc.backoffice.jpa.domain.ServiceTypeEntity;
import com.umdc.backoffice.jpa.repository.ServiceTypeRepository;
import com.umdc.backoffice.v1.servicetype.mapper.ServiceTypeMapper;
import com.umdc.commons.general.pojo.ServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.umdc.backoffice.util.MessageUtil.MESSAGE_HEADER_STR;

/// Service implementation for service type operations.
///
/// @version 1.0.0, 11-07-2026
@Service
public class ServiceTypeServiceImpl implements ServiceTypeService {

    private static final Logger log = LoggerFactory.getLogger(ServiceTypeServiceImpl.class);

    private static final String NOT_FOUND_MSG = "Service type not found.";
    private static final String FOUND_MSG = "Service type found.";
    private static final String CREATED_MSG = "Service type created.";
    private static final String UPDATED_MSG = "Service type updated.";
    private static final String BAD_REQUEST_MSG = "Invalid request.";
    private static final String ALREADY_EXISTS_MSG = "Service type name already in use.";

    private final ServiceTypeRepository serviceTypeRepository;
    private final ServiceTypeMapper serviceTypeMapper;

    /// Constructor for ServiceTypeServiceImpl.
    ///
    /// @param serviceTypeRepository the service type repository
    /// @param serviceTypeMapper     the service type mapper
    public ServiceTypeServiceImpl(ServiceTypeRepository serviceTypeRepository,
                                  ServiceTypeMapper serviceTypeMapper) {
        this.serviceTypeRepository = serviceTypeRepository;
        this.serviceTypeMapper = serviceTypeMapper;
    }

    /// {@inheritDoc}
    @Override
    public ResponseEntity<List<ServiceType>> listAll() {
        log.debug("Listing all service types");
        List<ServiceType> result = new ArrayList<>();
        serviceTypeRepository.findAll().forEach(entity -> result.add(serviceTypeMapper.toTarget(entity)));
        if (result.isEmpty()) {
            log.debug("No service types found");
            return ResponseEntity.notFound().header(MESSAGE_HEADER_STR, NOT_FOUND_MSG).build();
        }
        return ResponseEntity.ok().header(MESSAGE_HEADER_STR, FOUND_MSG).body(result);
    }

    /// {@inheritDoc}
    @Override
    public ResponseEntity<List<ServiceType>> listByStatus(boolean active) {
        log.debug("Listing service types by active={}", active);
        List<ServiceType> result = new ArrayList<>();
        serviceTypeRepository.findByActive(active).forEach(entity -> result.add(serviceTypeMapper.toTarget(entity)));
        if (result.isEmpty()) {
            log.debug("No service types found for active={}", active);
            return ResponseEntity.notFound().header(MESSAGE_HEADER_STR, NOT_FOUND_MSG).build();
        }
        return ResponseEntity.ok().header(MESSAGE_HEADER_STR, FOUND_MSG).body(result);
    }

    /// {@inheritDoc}
    @Override
    public ResponseEntity<ServiceType> find(UUID id) {
        if (Objects.isNull(id)) {
            log.debug("find called with null id");
            return ResponseEntity.badRequest().header(MESSAGE_HEADER_STR, BAD_REQUEST_MSG).build();
        }
        Optional<ServiceTypeEntity> entity = serviceTypeRepository.findById(id);
        return entity.map(e -> ResponseEntity.ok()
                        .header(MESSAGE_HEADER_STR, FOUND_MSG)
                        .body(serviceTypeMapper.toTarget(e)))
                .orElseGet(() -> ResponseEntity.notFound().header(MESSAGE_HEADER_STR, NOT_FOUND_MSG).build());
    }

    /// {@inheritDoc}
    @Override
    @Transactional
    public ResponseEntity<ServiceType> create(ServiceType serviceType) {
        if (Objects.isNull(serviceType)) {
            log.debug("create called with null serviceType");
            return ResponseEntity.badRequest().header(MESSAGE_HEADER_STR, BAD_REQUEST_MSG).build();
        }
        if (serviceTypeRepository.existsByName(serviceType.getName())) {
            log.debug("Service type name already exists: {}", serviceType.getName());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .header(MESSAGE_HEADER_STR, ALREADY_EXISTS_MSG).build();
        }
        ServiceTypeEntity entity = serviceTypeMapper.toSource(serviceType);
        if (Objects.isNull(entity.getId())) {
            entity.setId(UUID.randomUUID());
        }
        ServiceTypeEntity saved = serviceTypeRepository.save(entity);
        log.debug("Service type created: id={}", saved.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(MESSAGE_HEADER_STR, CREATED_MSG)
                .body(serviceTypeMapper.toTarget(saved));
    }

    /// {@inheritDoc}
    @Override
    @Transactional
    public ResponseEntity<ServiceType> update(UUID id, ServiceType serviceType) {
        if (Objects.isNull(id) || Objects.isNull(serviceType)) {
            log.debug("update called with null id or null serviceType");
            return ResponseEntity.badRequest().header(MESSAGE_HEADER_STR, BAD_REQUEST_MSG).build();
        }
        Optional<ServiceTypeEntity> existing = serviceTypeRepository.findById(id);
        if (existing.isEmpty()) {
            log.debug("Service type not found for update: id={}", id);
            return ResponseEntity.notFound().header(MESSAGE_HEADER_STR, NOT_FOUND_MSG).build();
        }
        ServiceTypeEntity entity = existing.get();
        entity.setName(serviceType.getName());
        entity.setDescription(serviceType.getDescription());
        entity.setActive(serviceType.isActive());
        ServiceTypeEntity saved = serviceTypeRepository.save(entity);
        log.debug("Service type updated: id={}", saved.getId());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .header(MESSAGE_HEADER_STR, UPDATED_MSG)
                .body(serviceTypeMapper.toTarget(saved));
    }
}
