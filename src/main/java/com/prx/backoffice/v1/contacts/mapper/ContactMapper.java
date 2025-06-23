/*
 *  @(#)ContactMapper.java
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
package com.prx.backoffice.v1.contacts.mapper;

import com.prx.backoffice.v1.contacttypes.mapper.ContactTypeMapper;
import com.prx.commons.general.pojo.Contact;
import com.prx.persistence.general.domains.ContactEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * ContactMapper.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 20-10-2020
 */
@Mapper(componentModel = "spring", uses = {ContactTypeMapper.class})
public interface ContactMapper {

    Contact toTarget(ContactEntity contactEntity);

    ContactEntity toSource(Contact contact);
}
