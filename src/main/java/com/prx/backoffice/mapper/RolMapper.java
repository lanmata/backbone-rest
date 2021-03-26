
package com.prx.backoffice.mapper;

import com.prx.backoffice.mapper.decorator.FeatureMapperUtil;
import com.prx.commons.pojo.Rol;
import com.prx.persistence.general.domains.RolEntity;
import com.prx.persistence.general.domains.UserRolEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * RolMapper.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 12-02-2021
 */
@Mapper(componentModel = "spring", uses = {FeatureMapperUtil.class})
public interface RolMapper {

    @Mapping(target = "features", source = "rolFeatures")
    Rol toTarget(RolEntity rolEntity);

    @InheritInverseConfiguration
    RolEntity toSource(Rol rol);

    @Mappings({
            @Mapping(target = "id", source = "rol.id"),
            @Mapping(target = "name", source = "rol.name"),
            @Mapping(target = "description", source = "rol.description"),
            @Mapping(target = "active", source = "rol.active"),
            @Mapping(target = "features", source = "rol.rolFeatures")
    })
    Rol userRoltoRol(UserRolEntity userRolEntity);
}
