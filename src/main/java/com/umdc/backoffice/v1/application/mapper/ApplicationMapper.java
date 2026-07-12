/*
 *  @(#)ApplicationMapper.java
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

package com.umdc.backoffice.v1.application.mapper;

import com.umdc.commons.general.pojo.Application;
import com.umdc.commons.services.config.mapper.MapperAppConfig;
import com.umdc.persistence.general.domains.ApplicationEntity;
import org.mapstruct.*;

/// Mapper interface for converting between Service and ServiceEntity objects.
/// Utilizes MapStruct for automatic mapping.
///
/// @version 1.0.0, 20-10-2020
@Mapper(
        // Specifies the configuration class to use for this mapper.
        config = MapperAppConfig.class
)
@MapperConfig(
        // Specifies that the mapper should fail if there are any unmapped properties.
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        // Specifies that the mapper should fail if there are any unmapped properties.
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ApplicationMapper {

    /// Converts a ServiceEntity object to a Service object.
    ///
    /// @param applicationEntity the ServiceEntity object to convert
    /// @return the converted Service object
    @Mapping(target = "userList", ignore = true)
    @Mapping(target = "roleList", ignore = true)
    Application toTarget(ApplicationEntity applicationEntity);

    /// Converts a Service object to a ServiceEntity object.
    /// Inherits the inverse configuration from the toTarget method.
    ///
    /// @param application the Service object to convert
    /// @return the converted ServiceEntity object
    @InheritInverseConfiguration
    ApplicationEntity toSource(Application application);

}
