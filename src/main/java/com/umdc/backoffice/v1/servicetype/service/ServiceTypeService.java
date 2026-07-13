/*
 *  @(#)ServiceTypeService.java
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

import com.umdc.commons.general.pojo.ServiceType;
import com.umdc.commons.services.CrudService;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

/// Interface for service type operations.
/// Extends the CrudService interface to provide CRUD operations for service types.
///
/// @version 1.0.0, 11-07-2026
public interface ServiceTypeService extends CrudService<UUID, ServiceType> {

    /// Creates a new service type.
    ///
    /// @param serviceType the service type to create
    /// @return the created service type wrapped in a ResponseEntity
    /// @throws NotImplementedException if the method is not implemented
    @Override
    default ResponseEntity<ServiceType> create(ServiceType serviceType) {
        throw new NotImplementedException();
    }

    /// Finds a service type by its ID.
    ///
    /// @param id the ID of the service type to find
    /// @return the found service type wrapped in a ResponseEntity
    /// @throws NotImplementedException if the method is not implemented
    @Override
    default ResponseEntity<ServiceType> find(UUID id) {
        throw new NotImplementedException();
    }

    /// Updates an existing service type.
    ///
    /// @param id          the ID of the service type to update
    /// @param serviceType the service type with updated information
    /// @return the updated service type wrapped in a ResponseEntity
    /// @throws NotImplementedException if the method is not implemented
    @Override
    default ResponseEntity<ServiceType> update(UUID id, ServiceType serviceType) {
        throw new NotImplementedException();
    }

    /// Deletes a service type.
    ///
    /// @param id          the ID of the service type to delete
    /// @param serviceType the service type to delete
    /// @return the deleted service type wrapped in a ResponseEntity
    /// @throws NotImplementedException if the method is not implemented
    @Override
    default ResponseEntity<ServiceType> delete(UUID id, ServiceType serviceType) {
        throw new NotImplementedException();
    }

    /// Lists service types by their IDs.
    ///
    /// @param id the IDs of the service types to list
    /// @return a list of service types wrapped in a ResponseEntity
    /// @throws NotImplementedException if the method is not implemented
    @Override
    default ResponseEntity<List<ServiceType>> list(UUID... id) {
        throw new NotImplementedException();
    }

    /// Returns all registered service types.
    ///
    /// @return all service types wrapped in a ResponseEntity, or 404 if none exist
    default ResponseEntity<List<ServiceType>> listAll() {
        throw new NotImplementedException();
    }

    /// Returns service types filtered by active status.
    ///
    /// @param active {@code true} for active, {@code false} for inactive
    /// @return matching service types wrapped in a ResponseEntity, or 404 if none exist
    default ResponseEntity<List<ServiceType>> listByStatus(boolean active) {
        throw new NotImplementedException();
    }
}
