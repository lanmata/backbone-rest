/*
 *  @(#)AddressServiceImpl.java
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
package com.umdc.backoffice.v1.addresses.service;

import com.umdc.backoffice.v1.addresses.api.to.Address;
import com.umdc.backoffice.v1.addresses.mapper.AddressMapper;
import com.umdc.persistence.general.domains.AddressEntity;
import com.umdc.persistence.general.domains.PersonEntity;
import com.umdc.persistence.general.repositories.AddressRepository;
import com.umdc.persistence.general.repositories.PersonRepository;
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

/**
 * Service implementation for address operations.
 */
@Service
public class AddressServiceImpl implements AddressService {

    private static final Logger log = LoggerFactory.getLogger(AddressServiceImpl.class);

    private static final String NOT_FOUND_MSG = "Address not found.";
    private static final String FOUND_MSG = "Address found.";
    private static final String CREATED_MSG = "Address created successfully.";
    private static final String UPDATED_MSG = "Address updated successfully.";
    private static final String DELETED_MSG = "Address deleted successfully.";
    private static final String BAD_REQUEST_MSG = "Invalid request. The 'address' body is required and must include a non-blank 'address' and a 'personId'.";
    private static final String PERSON_NOT_FOUND_MSG = "Person not found.";
    private static final String NO_ADDRESSES_MSG = "No addresses found for the given person.";

    private final AddressRepository addressRepository;
    private final PersonRepository personRepository;
    private final AddressMapper addressMapper;

    /**
     * Constructor for AddressServiceImpl.
     *
     * @param addressRepository the address repository
     * @param personRepository  the person repository
     * @param addressMapper     the address mapper
     */
    public AddressServiceImpl(AddressRepository addressRepository,
                               PersonRepository personRepository,
                               AddressMapper addressMapper) {
        this.addressRepository = addressRepository;
        this.personRepository = personRepository;
        this.addressMapper = addressMapper;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public ResponseEntity<Address> create(Address address) {
        if (Objects.isNull(address) || Objects.isNull(address.getContent()) || address.getContent().isBlank()
                || Objects.isNull(address.getPersonId())) {
            log.debug("create called with an invalid address payload");
            return ResponseEntity.badRequest().header(MESSAGE_HEADER_STR, BAD_REQUEST_MSG).build();
        }
        Optional<PersonEntity> person = personRepository.findById(address.getPersonId());
        if (person.isEmpty()) {
            log.debug("Person not found for address creation: personId={}", address.getPersonId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).header(MESSAGE_HEADER_STR, PERSON_NOT_FOUND_MSG).build();
        }
        address.setId(null);
        AddressEntity entity = addressMapper.toSource(address);
        entity.setPerson(person.get());
        AddressEntity saved = addressRepository.save(entity);
        Address created = addressMapper.toTarget(saved);
        log.debug("Address created: id={}, personId={}", created.getId(), created.getPersonId());
        return ResponseEntity.status(HttpStatus.CREATED).header(MESSAGE_HEADER_STR, CREATED_MSG).body(created);
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Address> find(UUID id) {
        Optional<Address> result = addressRepository.findById(id).map(addressMapper::toTarget);
        return result.map(found -> ResponseEntity.ok().header(MESSAGE_HEADER_STR, FOUND_MSG).body(found))
                .orElseGet(() -> ResponseEntity.notFound().header(MESSAGE_HEADER_STR, NOT_FOUND_MSG).build());
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public ResponseEntity<Address> update(UUID id, Address address) {
        if (Objects.isNull(address) || Objects.isNull(address.getContent()) || address.getContent().isBlank()
                || Objects.isNull(address.getPersonId())) {
            log.debug("update called with an invalid address payload: id={}", id);
            return ResponseEntity.badRequest().header(MESSAGE_HEADER_STR, BAD_REQUEST_MSG).build();
        }
        Optional<AddressEntity> existing = addressRepository.findById(id);
        if (existing.isEmpty()) {
            log.debug("Address not found for update: id={}", id);
            return ResponseEntity.notFound().header(MESSAGE_HEADER_STR, NOT_FOUND_MSG).build();
        }
        Optional<PersonEntity> person = personRepository.findById(address.getPersonId());
        if (person.isEmpty()) {
            log.debug("Person not found for address update: personId={}", address.getPersonId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).header(MESSAGE_HEADER_STR, PERSON_NOT_FOUND_MSG).build();
        }
        AddressEntity entity = existing.get();
        entity.setAddress(address.getContent());
        entity.setZipcode(address.getZipcode());
        entity.setPerson(person.get());
        AddressEntity saved = addressRepository.save(entity);
        log.debug("Address updated: id={}", saved.getId());
        return ResponseEntity.ok().header(MESSAGE_HEADER_STR, UPDATED_MSG).body(addressMapper.toTarget(saved));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public ResponseEntity<Address> delete(UUID id) {
        Optional<AddressEntity> existing = addressRepository.findById(id);
        if (existing.isEmpty()) {
            log.debug("Address not found for delete: id={}", id);
            return ResponseEntity.notFound().header(MESSAGE_HEADER_STR, NOT_FOUND_MSG).build();
        }
        Address deleted = addressMapper.toTarget(existing.get());
        addressRepository.deleteById(id);
        log.debug("Address deleted: id={}", id);
        return ResponseEntity.ok().header(MESSAGE_HEADER_STR, DELETED_MSG).body(deleted);
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Address>> listByPerson(UUID personId) {
        if (!personRepository.existsById(personId)) {
            log.debug("Person not found for address listing: personId={}", personId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).header(MESSAGE_HEADER_STR, PERSON_NOT_FOUND_MSG).build();
        }
        List<Address> addresses = new ArrayList<>();
        addressRepository.findByPersonId(personId).forEach(entity -> addresses.add(addressMapper.toTarget(entity)));
        if (addresses.isEmpty()) {
            log.debug("Person exists but has no addresses registered: personId={}", personId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).header(MESSAGE_HEADER_STR, NO_ADDRESSES_MSG).build();
        }
        return ResponseEntity.ok().header(MESSAGE_HEADER_STR, FOUND_MSG).body(addresses);
    }
}
