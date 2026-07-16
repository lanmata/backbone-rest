/*
 *  @(#)AddressServiceImplTest.java
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.umdc.backoffice.util.MessageUtil.MESSAGE_HEADER_STR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/// Unit tests for {@link AddressServiceImpl}.
@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    private static final String ADDRESS_TEXT = "123 Main Street";
    private static final String ZIPCODE = "90210";

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private AddressMapper addressMapper;

    private AddressServiceImpl addressService;

    @BeforeEach
    void setUp() {
        addressService = new AddressServiceImpl(addressRepository, personRepository, addressMapper);
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create: returns 201 when address is valid and person exists")
    void create_returnsCreated_whenValid() {
        UUID personId = UUID.randomUUID();
        Address address = buildPojo(personId);
        PersonEntity person = buildPersonEntity(personId);
        AddressEntity mapped = new AddressEntity();
        AddressEntity saved = buildEntity(person);
        Address created = buildPojo(personId);

        doReturn(Optional.of(person)).when(personRepository).findById(personId);
        doReturn(mapped).when(addressMapper).toSource(address);
        doReturn(saved).when(addressRepository).save(mapped);
        doReturn(created).when(addressMapper).toTarget(saved);

        ResponseEntity<Address> response = addressService.create(address);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getHeaders().getFirst(MESSAGE_HEADER_STR));
        verify(addressRepository).save(mapped);
    }

    @Test
    @DisplayName("create: returns 400 when address is null")
    void create_returnsBadRequest_whenAddressNull() {
        ResponseEntity<Address> response = addressService.create(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(personRepository, addressRepository, addressMapper);
    }

    @Test
    @DisplayName("create: returns 400 when address text is null")
    void create_returnsBadRequest_whenAddressTextNull() {
        Address address = buildPojo(UUID.randomUUID());
        address.setContent(null);

        ResponseEntity<Address> response = addressService.create(address);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("create: returns 400 when address text is blank")
    void create_returnsBadRequest_whenAddressTextBlank() {
        Address address = buildPojo(UUID.randomUUID());
        address.setContent("   ");

        ResponseEntity<Address> response = addressService.create(address);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("create: returns 400 when personId is null")
    void create_returnsBadRequest_whenPersonIdNull() {
        Address address = buildPojo(null);

        ResponseEntity<Address> response = addressService.create(address);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("create: returns 404 when referenced person does not exist")
    void create_returnsNotFound_whenPersonAbsent() {
        UUID personId = UUID.randomUUID();
        Address address = buildPojo(personId);

        doReturn(Optional.empty()).when(personRepository).findById(personId);

        ResponseEntity<Address> response = addressService.create(address);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verifyNoInteractions(addressMapper);
    }

    // ── find ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("find: returns 200 when address is found")
    void find_returnsOk_whenFound() {
        UUID id = UUID.randomUUID();
        PersonEntity person = buildPersonEntity(UUID.randomUUID());
        AddressEntity entity = buildEntity(person);
        Address pojo = buildPojo(person.getId());

        doReturn(Optional.of(entity)).when(addressRepository).findById(id);
        doReturn(pojo).when(addressMapper).toTarget(entity);

        ResponseEntity<Address> response = addressService.find(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(addressRepository).findById(id);
    }

    @Test
    @DisplayName("find: returns 404 when address is absent")
    void find_returnsNotFound_whenAbsent() {
        UUID id = UUID.randomUUID();

        doReturn(Optional.empty()).when(addressRepository).findById(id);

        ResponseEntity<Address> response = addressService.find(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update: returns 200 when address and person are found")
    void update_returnsOk_whenValid() {
        UUID id = UUID.randomUUID();
        UUID personId = UUID.randomUUID();
        Address address = buildPojo(personId);
        PersonEntity person = buildPersonEntity(personId);
        AddressEntity existing = buildEntity(null);
        Address updated = buildPojo(personId);

        doReturn(Optional.of(existing)).when(addressRepository).findById(id);
        doReturn(Optional.of(person)).when(personRepository).findById(personId);
        doReturn(existing).when(addressRepository).save(existing);
        doReturn(updated).when(addressMapper).toTarget(existing);

        ResponseEntity<Address> response = addressService.update(id, address);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ADDRESS_TEXT, existing.getAddress());
        assertEquals(ZIPCODE, existing.getZipcode());
        assertEquals(person, existing.getPerson());
    }

    @Test
    @DisplayName("update: returns 400 when address is null")
    void update_returnsBadRequest_whenAddressNull() {
        ResponseEntity<Address> response = addressService.update(UUID.randomUUID(), null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(addressRepository, personRepository, addressMapper);
    }

    @Test
    @DisplayName("update: returns 400 when address text is blank")
    void update_returnsBadRequest_whenAddressTextBlank() {
        Address address = buildPojo(UUID.randomUUID());
        address.setContent(" ");

        ResponseEntity<Address> response = addressService.update(UUID.randomUUID(), address);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("update: returns 400 when personId is null")
    void update_returnsBadRequest_whenPersonIdNull() {
        Address address = buildPojo(null);

        ResponseEntity<Address> response = addressService.update(UUID.randomUUID(), address);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("update: returns 404 when address is absent")
    void update_returnsNotFound_whenAddressAbsent() {
        UUID id = UUID.randomUUID();
        Address address = buildPojo(UUID.randomUUID());

        doReturn(Optional.empty()).when(addressRepository).findById(id);

        ResponseEntity<Address> response = addressService.update(id, address);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verifyNoInteractions(personRepository);
    }

    @Test
    @DisplayName("update: returns 404 when referenced person does not exist")
    void update_returnsNotFound_whenPersonAbsent() {
        UUID id = UUID.randomUUID();
        UUID personId = UUID.randomUUID();
        Address address = buildPojo(personId);
        AddressEntity existing = buildEntity(null);

        doReturn(Optional.of(existing)).when(addressRepository).findById(id);
        doReturn(Optional.empty()).when(personRepository).findById(personId);

        ResponseEntity<Address> response = addressService.update(id, address);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete: returns 200 with deleted body when address is found")
    void delete_returnsOk_whenFound() {
        UUID id = UUID.randomUUID();
        AddressEntity existing = buildEntity(null);
        Address pojo = buildPojo(UUID.randomUUID());

        doReturn(Optional.of(existing)).when(addressRepository).findById(id);
        doReturn(pojo).when(addressMapper).toTarget(existing);

        ResponseEntity<Address> response = addressService.delete(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pojo, response.getBody());
        verify(addressRepository).deleteById(id);
    }

    @Test
    @DisplayName("delete: returns 404 when address is absent")
    void delete_returnsNotFound_whenAbsent() {
        UUID id = UUID.randomUUID();

        doReturn(Optional.empty()).when(addressRepository).findById(id);

        ResponseEntity<Address> response = addressService.delete(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(addressRepository, never()).deleteById(id);
    }

    // ── listByPerson ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("listByPerson: returns 200 with list when person and addresses exist")
    void listByPerson_returnsOk_whenFound() {
        UUID personId = UUID.randomUUID();
        PersonEntity person = buildPersonEntity(personId);
        AddressEntity entity = buildEntity(person);
        Address pojo = buildPojo(personId);

        doReturn(true).when(personRepository).existsById(personId);
        doReturn(List.of(entity)).when(addressRepository).findByPersonId(personId);
        doReturn(pojo).when(addressMapper).toTarget(entity);

        ResponseEntity<List<Address>> response = addressService.listByPerson(personId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains(pojo));
    }

    @Test
    @DisplayName("listByPerson: returns 404 when person does not exist")
    void listByPerson_returnsNotFound_whenPersonAbsent() {
        UUID personId = UUID.randomUUID();

        doReturn(false).when(personRepository).existsById(personId);

        ResponseEntity<List<Address>> response = addressService.listByPerson(personId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verifyNoInteractions(addressRepository, addressMapper);
    }

    @Test
    @DisplayName("listByPerson: returns 404 when person exists but has no addresses")
    void listByPerson_returnsNotFound_whenNoAddresses() {
        UUID personId = UUID.randomUUID();

        doReturn(true).when(personRepository).existsById(personId);
        doReturn(List.of()).when(addressRepository).findByPersonId(personId);

        ResponseEntity<List<Address>> response = addressService.listByPerson(personId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Address buildPojo(UUID personId) {
        Address address = new Address();
        address.setId(UUID.randomUUID());
        address.setPersonId(personId);
        address.setContent(ADDRESS_TEXT);
        address.setZipcode(ZIPCODE);
        return address;
    }

    private AddressEntity buildEntity(PersonEntity person) {
        AddressEntity entity = new AddressEntity();
        entity.setId(UUID.randomUUID());
        entity.setAddress(ADDRESS_TEXT);
        entity.setZipcode(ZIPCODE);
        entity.setPerson(person);
        return entity;
    }

    private PersonEntity buildPersonEntity(UUID id) {
        PersonEntity person = new PersonEntity();
        person.setId(id);
        person.setName("Jane");
        person.setLastName("Doe");
        return person;
    }
}
