package com.prx.backoffice.v1.application.mapper;

import com.prx.backoffice.config.jackson.MapperAppConfig;
import com.prx.backoffice.v1.application.Service;
import com.prx.persistence.general.domains.ApplicationEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper interface for converting between Service and ServiceEntity objects.
 * Utilizes MapStruct for automatic mapping.
 *
 * @version 1.0.0, 20-10-2020
 */
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

    /**
     * Converts a ServiceEntity object to a Service object.
     *
     * @param applicationEntity the ServiceEntity object to convert
     * @return the converted Service object
     */
    Service toTarget(ApplicationEntity applicationEntity);

    /**
     * Converts a Service object to a ServiceEntity object.
     * Inherits the inverse configuration from the toTarget method.
     *
     * @param service the Service object to convert
     * @return the converted ServiceEntity object
     */
    @InheritInverseConfiguration
    ApplicationEntity toSource(Service service);

}
