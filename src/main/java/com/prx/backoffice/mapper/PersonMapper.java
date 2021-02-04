/*
 *
 *  * @(#)PersonMapper.java.
 *  *
 *  * Copyright (c) Luis Antonio Mata Mata. All rights reserved.
 *  *
 *  * All rights to this product are owned by Luis Antonio Mata Mata and may only
 *  * be used under the terms of its associated license document. You may NOT
 *  * copy, modify, sublicense, or distribute this source file or portions of
 *  * it unless previously authorized in writing by Luis Antonio Mata Mata.
 *  * In any event, this notice and the above copyright must always be included
 *  * verbatim with this file.
 *
 */
package com.prx.backoffice.mapper;

import com.prx.commons.pojo.Person;
import com.prx.persistence.general.domains.PersonEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * PersonMapper.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 20-10-2020
 */
@Mapper(componentModel = "spring")
public interface PersonMapper {

    @Mapping( source = "name", target = "firstName")
    Person toTarget(PersonEntity personEntity);

    @InheritInverseConfiguration
    PersonEntity toSource(Person person);
}
