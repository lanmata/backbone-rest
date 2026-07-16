/*
 *  @(#)AddressApi.java
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
import com.umdc.commons.util.HttpStatusUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

/**
 * Interface for the Address API.
 */
@Tag(name = "addresses", description = "Address management")
public interface AddressApi {

    /**
     * Gets the address service.
     *
     * @return the address service
     */
    default AddressService getService() {
        return new AddressService() {
        };
    }

    /**
     * Creates a new address.
     *
     * @param addressRequest the address creation request
     * @return the created address wrapped in a ResponseEntity
     */
    @Operation(summary = "Create an address", description = "Creates a new address.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.CREATED_STR, description = "Address created"),
            @ApiResponse(responseCode = HttpStatusUtil.BAD_REQUEST_STR, description = "Invalid request body")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<Address> createAddress(@RequestBody AddressRequest addressRequest) {
        return this.getService().create(addressRequest.getAddress());
    }

    /**
     * Finds an address by its ID.
     *
     * @param addressId the address UUID
     * @return the address wrapped in a ResponseEntity
     */
    @Operation(summary = "Find address by ID", description = "Returns the address matching the given UUID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "Address found"),
            @ApiResponse(responseCode = HttpStatusUtil.NOT_FOUND_STR, description = "Address not found")
    })
    @GetMapping(value = "/{addressId}", produces = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<Address> findAddressById(@PathVariable UUID addressId) {
        return this.getService().find(addressId);
    }

    /**
     * Updates an existing address.
     *
     * @param addressId      the address UUID
     * @param addressRequest the update request body
     * @return the updated address wrapped in a ResponseEntity
     */
    @Operation(summary = "Update an address", description = "Updates the address identified by the given UUID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "Address updated"),
            @ApiResponse(responseCode = HttpStatusUtil.NOT_FOUND_STR, description = "Address not found")
    })
    @PutMapping(value = "/{addressId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<Address> updateAddress(@PathVariable UUID addressId, @RequestBody AddressRequest addressRequest) {
        return this.getService().update(addressId, addressRequest.getAddress());
    }

    /**
     * Deletes an address by its ID.
     *
     * @param addressId the address UUID
     * @return the deleted address wrapped in a ResponseEntity
     */
    @Operation(summary = "Delete an address", description = "Deletes the address identified by the given UUID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "Address deleted"),
            @ApiResponse(responseCode = HttpStatusUtil.NOT_FOUND_STR, description = "Address not found")
    })
    @DeleteMapping(value = "/{addressId}", produces = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<Address> deleteAddress(@PathVariable UUID addressId) {
        return this.getService().delete(addressId);
    }

    /**
     * Lists addresses by person.
     *
     * @param personId the person UUID
     * @return the addresses wrapped in a ResponseEntity
     */
    @Operation(summary = "List addresses by person",
            description = "Business rule: a person has one or more addresses.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = HttpStatusUtil.OK_STR, description = "Address list returned"),
            @ApiResponse(responseCode = HttpStatusUtil.NOT_FOUND_STR, description = "Person not found")
    })
    @GetMapping(value = "/person/{personId}", produces = MediaType.APPLICATION_JSON_VALUE)
    default ResponseEntity<List<Address>> listAddressesByPerson(
            @Parameter(description = "Person UUID", required = true)
            @PathVariable UUID personId) {
        return this.getService().listByPerson(personId);
    }
}
