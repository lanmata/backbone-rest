/*
 *  @(#)AddressControllerTest.java
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
package com.umdc.backoffice.v1.addresses.api.controller;

import com.umdc.backoffice.v1.addresses.api.to.Address;
import com.umdc.backoffice.v1.addresses.api.to.AddressRequest;
import com.umdc.backoffice.v1.addresses.service.AddressService;
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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

/// Unit tests for {@link AddressController} verifying correct delegation to the service layer.
@ExtendWith(MockitoExtension.class)
class AddressControllerTest {

    private static final String ADDRESS_TEXT = "123 Main Street";

    @Mock
    private AddressService addressService;

    private AddressController controller;

    @BeforeEach
    void setUp() {
        controller = new AddressController(addressService);
    }

    @Test
    @DisplayName("POST /addresses — delegates create and returns 201")
    void createAddress_delegates_returns201() {
        AddressRequest request = buildRequest(UUID.randomUUID());
        doReturn(ResponseEntity.status(HttpStatus.CREATED).body(request.getAddress()))
                .when(addressService).create(request.getAddress());

        ResponseEntity<Address> response = controller.createAddress(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(addressService).create(request.getAddress());
    }

    @Test
    @DisplayName("GET /addresses/{id} — delegates find and returns 200")
    void findAddressById_delegates_returns200() {
        UUID id = UUID.randomUUID();
        Address pojo = buildPojo(UUID.randomUUID());
        doReturn(ResponseEntity.ok(pojo)).when(addressService).find(id);

        ResponseEntity<Address> response = controller.findAddressById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(addressService).find(id);
    }

    @Test
    @DisplayName("GET /addresses/{id} — delegates find and returns 404")
    void findAddressById_delegates_returns404() {
        UUID id = UUID.randomUUID();
        doReturn(ResponseEntity.notFound().build()).when(addressService).find(id);

        ResponseEntity<Address> response = controller.findAddressById(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(addressService).find(id);
    }

    @Test
    @DisplayName("PUT /addresses/{id} — delegates update and returns 200")
    void updateAddress_delegates_returns200() {
        UUID id = UUID.randomUUID();
        AddressRequest request = buildRequest(UUID.randomUUID());
        doReturn(ResponseEntity.ok(request.getAddress()))
                .when(addressService).update(id, request.getAddress());

        ResponseEntity<Address> response = controller.updateAddress(id, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(addressService).update(id, request.getAddress());
    }

    @Test
    @DisplayName("PUT /addresses/{id} — delegates update and returns 404")
    void updateAddress_delegates_returns404() {
        UUID id = UUID.randomUUID();
        AddressRequest request = buildRequest(UUID.randomUUID());
        doReturn(ResponseEntity.notFound().build())
                .when(addressService).update(id, request.getAddress());

        ResponseEntity<Address> response = controller.updateAddress(id, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(addressService).update(id, request.getAddress());
    }

    @Test
    @DisplayName("DELETE /addresses/{id} — delegates delete and returns 200")
    void deleteAddress_delegates_returns200() {
        UUID id = UUID.randomUUID();
        Address pojo = buildPojo(UUID.randomUUID());
        doReturn(ResponseEntity.ok(pojo)).when(addressService).delete(id);

        ResponseEntity<Address> response = controller.deleteAddress(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(addressService).delete(id);
    }

    @Test
    @DisplayName("DELETE /addresses/{id} — delegates delete and returns 404")
    void deleteAddress_delegates_returns404() {
        UUID id = UUID.randomUUID();
        doReturn(ResponseEntity.notFound().build()).when(addressService).delete(id);

        ResponseEntity<Address> response = controller.deleteAddress(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(addressService).delete(id);
    }

    @Test
    @DisplayName("GET /addresses/person/{personId} — delegates listByPerson and returns 200")
    void listAddressesByPerson_delegates_returns200() {
        UUID personId = UUID.randomUUID();
        Address pojo = buildPojo(personId);
        doReturn(ResponseEntity.ok(List.of(pojo))).when(addressService).listByPerson(personId);

        ResponseEntity<List<Address>> response = controller.listAddressesByPerson(personId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(addressService).listByPerson(personId);
    }

    @Test
    @DisplayName("GET /addresses/person/{personId} — delegates listByPerson and returns 404")
    void listAddressesByPerson_delegates_returns404() {
        UUID personId = UUID.randomUUID();
        doReturn(ResponseEntity.notFound().build()).when(addressService).listByPerson(personId);

        ResponseEntity<List<Address>> response = controller.listAddressesByPerson(personId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(addressService).listByPerson(personId);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Address buildPojo(UUID personId) {
        Address address = new Address();
        address.setId(UUID.randomUUID());
        address.setPersonId(personId);
        address.setContent(ADDRESS_TEXT);
        address.setZipcode("90210");
        return address;
    }

    private AddressRequest buildRequest(UUID personId) {
        AddressRequest request = new AddressRequest();
        request.setAddress(buildPojo(personId));
        return request;
    }
}
