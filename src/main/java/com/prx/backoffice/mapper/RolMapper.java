
package com.prx.backoffice.mapper;

import com.prx.backoffice.mapper.decorator.FeatureMapperUtil;
import com.prx.commons.pojo.Rol;
import com.prx.persistence.general.domains.RolEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

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
}
