/*
 * @(#)$file.className.java.
 *
 * Copyright (c) Luis Antonio Mata Mata. All rights reserved.
 *
 * All rights to this product are owned by Luis Antonio Mata Mata and may only
 * be used under the terms of its associated license document. You may NOT
 * copy, modify, sublicense, or distribute this source file or portions of
 * it unless previously authorized in writing by Luis Antonio Mata Mata.
 * In any event, this notice and the above copyright must always be included
 * verbatim with this file.
 */
package com.prx.backoffice.v1.user.mapper;

import com.prx.backoffice.v1.person.mapper.PersonMapper;
import com.prx.backoffice.v1.role.mapper.RoleMapper;
import com.prx.commons.pojo.User;
import com.prx.persistence.general.domains.UserEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

/**
 * UserMapper.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 20-10-2020
 */
@Mapper(componentModel = "spring", uses = {RoleMapper.class, PersonMapper.class})
public interface UserMapper {

    User toTarget(UserEntity userEntity);

    @InheritInverseConfiguration
    UserEntity toSource(User user);
}
