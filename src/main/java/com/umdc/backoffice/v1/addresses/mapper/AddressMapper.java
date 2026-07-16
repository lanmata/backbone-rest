/*
 *  @(#)AddressMapper.java
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
package com.umdc.backoffice.v1.addresses.mapper;

import com.umdc.backoffice.v1.addresses.api.to.Address;
import com.umdc.commons.services.config.mapper.MapperAppConfig;
import com.umdc.persistence.general.domains.AddressEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper interface for converting between {@link Address} and {@link AddressEntity}.
 * Utilizes MapStruct for automatic mapping.
 * <p>
 * {@link AddressEntity#getPerson()} is a relation, while {@link Address#getPersonId()}
 * is a flat identifier, so the {@code personId} property is derived explicitly on the
 * read direction. The write direction (entity's {@code person}) is intentionally left
 * unmapped here; {@code AddressServiceImpl} is responsible for resolving and setting
 * the {@link com.umdc.persistence.general.domains.PersonEntity} relation before persisting.
 * </p>
 */
@Mapper(
        config = MapperAppConfig.class
)
@MapperConfig(
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface AddressMapper {

    /**
     * Converts an {@link AddressEntity} to an {@link Address}.
     *
     * @param entity the entity to convert
     * @return the converted DTO
     */
    @Mapping(target = "personId", expression = "java(entity.getPerson() != null ? entity.getPerson().getId() : null)")
    Address toTarget(AddressEntity entity);

    /**
     * Converts an {@link Address} to an {@link AddressEntity}.
     * The {@code person} relation is left unset; the caller must resolve and assign it.
     *
     * @param address the DTO to convert
     * @return the converted entity
     */
    @Mapping(target = "person", ignore = true)
    AddressEntity toSource(Address address);
}
