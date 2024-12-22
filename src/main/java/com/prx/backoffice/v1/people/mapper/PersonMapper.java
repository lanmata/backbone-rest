/*
 *  @(#)PersonMapper.java
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
package com.prx.backoffice.v1.people.mapper;

import com.prx.backoffice.config.jackson.MapperAppConfig;
import com.prx.commons.pojo.Person;
import com.prx.persistence.general.domains.PersonEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper interface for converting between Person and PersonEntity objects.
 * Utilizes MapStruct for automatic mapping.
 *
 * @author Luis
 * @version 1.0.0, 20-10-2020
 */
@Mapper(
        // Specifies the configuration class to use for this mapper.
        config = MapperAppConfig.class
)
public interface PersonMapper {

    /**
     * Maps a PersonEntity object to a Person object.
     *
     * @param personEntity the PersonEntity object to map from
     * @return the mapped Person object
     */
    @Mapping(target = "firstName", source = "name")
    Person toTarget(PersonEntity personEntity);

    /**
     * Maps a Person object to a PersonEntity object.
     * Inherits the inverse configuration from the toTarget method.
     *
     * @param person the Person object to map from
     * @return the mapped PersonEntity object
     */
    @InheritInverseConfiguration
    PersonEntity toSource(Person person);
}
