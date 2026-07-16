/*
 *  @(#)ServiceTypeMapper.java
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
package com.umdc.backoffice.v1.servicetype.mapper;

import com.umdc.commons.general.pojo.ServiceType;
import com.umdc.commons.services.config.mapper.MapperAppConfig;
import com.umdc.persistence.general.domains.ServiceTypeEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper interface for converting between {@link ServiceType} and {@link ServiceTypeEntity}.
 * Utilizes MapStruct for automatic mapping.
 */
@Mapper(
        config = MapperAppConfig.class
)
@MapperConfig(
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ServiceTypeMapper {

    /**
     * Converts a {@link ServiceTypeEntity} to a {@link ServiceType}.
     *
     * @param entity the entity to convert
     * @return the converted POJO
     */
    ServiceType toTarget(ServiceTypeEntity entity);

    /**
     * Converts a {@link ServiceType} to a {@link ServiceTypeEntity}.
     *
     * @param serviceType the POJO to convert
     * @return the converted entity
     */
    @InheritInverseConfiguration
    ServiceTypeEntity toSource(ServiceType serviceType);
}
