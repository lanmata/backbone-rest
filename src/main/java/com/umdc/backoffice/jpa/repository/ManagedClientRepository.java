/*
 *  @(#)ManagedClientRepository.java
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

import com.umdc.backoffice.jpa.domain.ManagedClientEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link ManagedClientEntity}.
 * <p>
 * Provides standard CRUD operations plus MCAM-specific derived query methods
 * used by {@code ManagedClientServiceImpl} and {@code ManagedClientTokenServiceImpl}.
 * </p>
 */
public interface ManagedClientRepository extends JpaRepository<ManagedClientEntity, UUID> {

    /**
     * Finds an active managed client by its primary key.
     *
     * @param id the client UUID
     * @return an {@link Optional} containing the entity if found and active
     */
    Optional<ManagedClientEntity> findByIdAndActiveTrue(UUID id);

    /**
     * Checks whether a client with the given name already exists for an application.
     * Used to enforce the UNIQUE (name, application_id) constraint at the service layer
     * before persisting.
     *
     * @param name          the candidate client name
     * @param applicationId the owning application UUID
     * @return {@code true} if a record with matching name and application_id already exists
     */
    boolean existsByNameAndApplicationId(String name, UUID applicationId);

    /**
     * Returns a paginated list of clients for a given application, regardless of active status.
     *
     * @param applicationId the owning application UUID
     * @param pageable      pagination and sort parameters
     * @return a page of matching entities
     */
    Page<ManagedClientEntity> findByApplicationId(UUID applicationId, Pageable pageable);

    /**
     * Returns a paginated list of clients filtered by active status.
     *
     * @param active   {@code true} for active clients, {@code false} for inactive
     * @param pageable pagination and sort parameters
     * @return a page of matching entities
     */
    Page<ManagedClientEntity> findByActive(boolean active, Pageable pageable);

    /**
     * Returns a paginated list of clients filtered by both application and active status.
     *
     * @param applicationId the owning application UUID
     * @param active        {@code true} for active clients, {@code false} for inactive
     * @param pageable      pagination and sort parameters
     * @return a page of matching entities
     */
    Page<ManagedClientEntity> findByApplicationIdAndActive(UUID applicationId, boolean active, Pageable pageable);

    /**
     * Returns clients with a non-null {@code prevSecretHash} whose
     * {@code secretLastRotatedAt} is before the given cutoff timestamp.
     * Used by the maintenance task to clear stale grace-period hashes.
     *
     * @param cutoff the timestamp threshold: clients rotated before this time are stale
     * @return list of entities with an expired grace period hash
     */
    @Query("SELECT e FROM ManagedClientEntity e WHERE e.prevSecretHash IS NOT NULL " +
           "AND e.secretLastRotatedAt < :cutoff")
    List<ManagedClientEntity> findWithExpiredPrevSecretHash(@Param("cutoff") LocalDateTime cutoff);
}

