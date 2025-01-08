/*
 *  @(#)ApplicationService.java
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

package com.prx.backoffice.v1.application.service;

import com.prx.commons.general.pojo.Application;
import com.prx.commons.services.CrudService;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

/// Interface for application operations.
/// Extends the CrudService interface to provide CRUD operations for [com.prx.persistence.general.domains.ApplicationEntity].
///
/// @version 1.0.0, 20-10-2020
public interface ApplicationService extends CrudService<UUID, Application> {

    /// Creates a new application.
    ///
    /// @param application the application to create
    /// @return the created application wrapped in a ResponseEntity
    /// @throws NotImplementedException if the method is not implemented
    @Override
    default ResponseEntity<Application> create(Application application)  {
        throw new NotImplementedException();
    }

    /// Finds a application by its ID.
    ///
    /// @param id the ID of the application to find
    /// @return the found application wrapped in a ResponseEntity
    /// @throws NotImplementedException if the method is not implemented
    @Override
    default ResponseEntity<Application> find(UUID id)  {
        throw new NotImplementedException();
    }

    /// Updates an existing application.
    ///
    /// @param id the ID of the application to update
    /// @param application the application with updated information
    /// @return the updated application wrapped in a ResponseEntity
    /// @throws NotImplementedException if the method is not implemented
    @Override
    default ResponseEntity<Application> update(UUID id, Application application)  {
        throw new NotImplementedException();
    }

    /// Deletes a application.
    ///
    /// @param id the ID of the application to delete
    /// @param application the application to delete
    /// @return the deleted application wrapped in a ResponseEntity
    /// @throws NotImplementedException if the method is not implemented
    @Override
    default ResponseEntity<Application> delete(UUID id, Application application)  {
        throw new NotImplementedException();
    }

    /// Lists services by their IDs.
    ///
    /// @param id the IDs of the application to list
    /// @return a list of application wrapped in a ResponseEntity
    /// @throws NotImplementedException if the method is not implemented
    @Override
    default ResponseEntity<List<Application>> list(UUID... id) {
        throw new NotImplementedException();
    }
}
