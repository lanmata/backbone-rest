/*
 *  @(#)ManagedClientMapper.java
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
package com.umdc.backoffice.v1.managedclient.mapper;

import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientCreateRequest;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientTO;
import com.umdc.backoffice.v1.managedclient.api.to.ManagedClientUpdateRequest;
import com.umdc.backoffice.jpa.domain.ManagedClientEntity;
import com.umdc.commons.services.config.mapper.MapperAppConfig;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/// MapStruct mapper for {@link ManagedClientEntity} and DTO conversions.
/// <p>
/// Secret fields ({@code secretHash}, {@code prevSecretHash}) are NEVER
/// mapped to {@link ManagedClientTO} to prevent accidental exposure in API
/// responses (AC-SEC-02).
/// </p>
///
/// @author Luis Antonio Mata
@Mapper(config = MapperAppConfig.class)
public interface ManagedClientMapper {

    /// Maps a {@link ManagedClientEntity} to a read-only {@link ManagedClientTO}.
    /// <p>
    /// Secret fields are intentionally ignored. The {@code id} field maps to
    /// {@code clientId} on the DTO.
    /// </p>
    ///
    /// @param entity the source entity
    /// @return the mapped transfer object
    @Mapping(target = "clientId", source = "id")
    ManagedClientTO toTO(ManagedClientEntity entity);

    /// Maps a {@link ManagedClientCreateRequest} to a {@link ManagedClientEntity}.
    /// <p>
    /// The {@code id}, {@code secretHash}, {@code prevSecretHash}, and all
    /// timestamps are deliberately ignored — the service layer sets these after
    /// this mapping is called.
    /// </p>
    ///
    /// @param request the source request DTO
    /// @return the mapped entity (without id, secret, or timestamps)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "secretHash", ignore = true)
    @Mapping(target = "prevSecretHash", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastUpdatedAt", ignore = true)
    @Mapping(target = "secretLastRotatedAt", ignore = true)
    ManagedClientEntity toEntity(ManagedClientCreateRequest request);

    /// Partially updates an existing {@link ManagedClientEntity} from a
    /// {@link ManagedClientUpdateRequest}.
    /// <p>
    /// Only non-null fields in the request are applied; null fields leave
    /// the existing entity values untouched (partial update semantics).
    /// Secret and immutable fields are always ignored.
    /// </p>
    ///
    /// @param request the source update request
    /// @param entity  the target entity to update in place
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "secretHash", ignore = true)
    @Mapping(target = "prevSecretHash", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastUpdatedAt", ignore = true)
    @Mapping(target = "secretLastRotatedAt", ignore = true)
    @Mapping(target = "applicationId", ignore = true)
    void updateEntityFromRequest(ManagedClientUpdateRequest request, @MappingTarget ManagedClientEntity entity);

    /// Maps a list of {@link ManagedClientEntity} instances to a list of
    /// {@link ManagedClientTO} instances.
    ///
    /// @param entities the source entity list
    /// @return the mapped transfer object list
    List<ManagedClientTO> toTOList(List<ManagedClientEntity> entities);
}
