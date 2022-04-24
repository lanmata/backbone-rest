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

package com.prx.backoffice.v1.contacttype.mapper;

import com.prx.backoffice.v1.contact.mapper.ContactMapper;
import com.prx.commons.pojo.ContactType;
import com.prx.persistence.general.domains.ContactTypeEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

/**
 * ContactTypeMapper.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata.</a>.
 * @version 1.0.1.20200904-01, 01-02-2021
 */
@Mapper(componentModel = "spring", uses = {ContactMapper.class})
public interface ContactTypeMapper {

    ContactType toTarget(ContactTypeEntity contactTypeEntity);

    @InheritInverseConfiguration
    ContactTypeEntity toSource(ContactType contactType);

}
