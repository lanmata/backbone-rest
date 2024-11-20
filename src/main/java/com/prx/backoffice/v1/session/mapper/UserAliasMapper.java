package com.prx.backoffice.v1.session.mapper;

import com.prx.backoffice.config.jackson.MapperAppConfig;
import com.prx.backoffice.v1.people.mapper.PersonMapper;
import com.prx.backoffice.v1.roles.mapper.RoleMapper;
import com.prx.backoffice.v1.session.to.UserAliasTO;
import com.prx.backoffice.v1.users.api.to.UserTO;
import com.prx.backoffice.v1.users.mapper.UserMapper;
import com.prx.commons.pojo.Role;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(config = MapperAppConfig.class,
        uses = {UserMapper.class, PersonMapper.class, RoleMapper.class}
)
public interface UserAliasMapper {

    @Mapping(target = "userId", source = "id")
    @Mapping(target = "alias", source = "alias")
    @Mapping(target = "firstname", source = "person.firstName")
    @Mapping(target = "lastname", source = "person.lastName")
    @Mapping(target = "roles", source = "roles")
    UserAliasTO toTarget(UserTO userTO);

    default Set<UUID> map(Set<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .filter(Objects::nonNull)
                .map(role -> UUID.fromString(role.getId()))
                .collect(Collectors.toSet());
    }

    @AfterMapping
    default void map(Role role, @MappingTarget Set<UUID> uuid) {
        if(Objects.nonNull(role)) {
            uuid.add(UUID.fromString(role.getId()));
        }
    }
}
