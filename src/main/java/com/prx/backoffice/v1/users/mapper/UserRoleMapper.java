package com.prx.backoffice.v1.users.mapper;

import com.prx.backoffice.config.jackson.MapperAppConfig;
import com.prx.backoffice.v1.features.mapper.FeatureMapper;
import com.prx.commons.pojo.Role;
import com.prx.commons.pojo.User;
import com.prx.persistence.general.domains.UserRoleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        // Specifies the configuration class to use for this mapper.
        config = MapperAppConfig.class,
        uses = {UserMapper.class, FeatureMapper.class}
)
public interface UserRoleMapper {

    @Mapping(target="id", source = "role.id")
    @Mapping(target="name", source = "role.name")
    @Mapping(target="description", source = "role.description")
    @Mapping(target="active", source = "role.active")
    Role toRoleTarget(UserRoleEntity userRoleEntity);

    @Mapping(target="id", source = "user.id")
    @Mapping(target="alias", source = "user.alias")
    @Mapping(target="password", source = "user.password")
    @Mapping(target="active", source = "user.active")
    User toUserTarget(UserRoleEntity userRoleEntity);
}
