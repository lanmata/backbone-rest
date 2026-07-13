/*
 *  @(#)ServiceTypeRepository.java
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
package com.umdc.backoffice.jpa.repository;

import com.umdc.backoffice.jpa.domain.ServiceTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link ServiceTypeEntity}.
 * <p>
 * Provides standard CRUD operations plus service-type-specific derived query methods.
 * </p>
 */
public interface ServiceTypeRepository extends JpaRepository<ServiceTypeEntity, UUID> {

    /**
     * Returns all service types matching the given active flag.
     *
     * @param active {@code true} for active service types, {@code false} for inactive
     * @return list of matching entities
     */
    List<ServiceTypeEntity> findByActive(boolean active);

    /**
     * Checks whether a service type with the given name already exists.
     *
     * @param name the candidate service type name
     * @return {@code true} if a record with matching name already exists
     */
    boolean existsByName(String name);
}
