package com.prx.backoffice.v1.application.service;

import com.prx.backoffice.services.CrudService;
import com.prx.backoffice.v1.application.Service;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Interface for service operations.
 * Extends the CrudService interface to provide CRUD operations for Service entities.
 *
 * @version 1.0.0, 20-10-2020
 */
public interface ApplicationService extends CrudService<Service> {

    /**
     * Creates a new service.
     *
     * @param service the service to create
     * @return the created service wrapped in a ResponseEntity
     * @throws NotImplementedException if the method is not implemented
     */
    @Override
    default ResponseEntity<Service> create(Service service)  {
        throw new NotImplementedException();
    }

    /**
     * Finds a service by its ID.
     *
     * @param id the ID of the service to find
     * @return the found service wrapped in a ResponseEntity
     * @throws NotImplementedException if the method is not implemented
     */
    @Override
    default ResponseEntity<Service> find(String id)  {
        throw new NotImplementedException();
    }

    /**
     * Updates an existing service.
     *
     * @param id the ID of the service to update
     * @param service the service with updated information
     * @return the updated service wrapped in a ResponseEntity
     * @throws NotImplementedException if the method is not implemented
     */
    @Override
    default ResponseEntity<Service> update(String id, Service service)  {
        throw new NotImplementedException();
    }

    /**
     * Deletes a service.
     *
     * @param id the ID of the service to delete
     * @param service the service to delete
     * @return the deleted service wrapped in a ResponseEntity
     * @throws NotImplementedException if the method is not implemented
     */
    @Override
    default ResponseEntity<Service> delete(String id, Service service)  {
        throw new NotImplementedException();
    }

    /**
     * Lists services by their IDs.
     *
     * @param id the IDs of the services to list
     * @return a list of services wrapped in a ResponseEntity
     * @throws NotImplementedException if the method is not implemented
     */
    @Override
    default ResponseEntity<List<Service>> list(String... id) {
        throw new NotImplementedException();
    }
}
