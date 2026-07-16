/*
 *  @(#)AddressController.java
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing addresses.
 */
@RestController
@RequestMapping("/api/v1/addresses")
public class AddressController implements AddressApi {

    private final AddressService addressService;

    /**
     * Constructor for AddressController.
     *
     * @param addressService the address service
     */
    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @Override
    public ResponseEntity<Address> createAddress(AddressRequest addressRequest) {
        return addressService.create(addressRequest.getAddress());
    }

    @Override
    public ResponseEntity<Address> findAddressById(UUID addressId) {
        return addressService.find(addressId);
    }

    @Override
    public ResponseEntity<Address> updateAddress(UUID addressId, AddressRequest addressRequest) {
        return addressService.update(addressId, addressRequest.getAddress());
    }

    @Override
    public ResponseEntity<Address> deleteAddress(UUID addressId) {
        return addressService.delete(addressId);
    }

    @Override
    public ResponseEntity<List<Address>> listAddressesByPerson(UUID personId) {
        return addressService.listByPerson(personId);
    }
}
