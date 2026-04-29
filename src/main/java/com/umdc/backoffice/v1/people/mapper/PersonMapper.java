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
package com.umdc.backoffice.v1.people.mapper;

import com.prx.commons.general.pojo.Contact;
import com.prx.commons.general.pojo.ContactType;
import com.prx.commons.general.pojo.Person;
import com.prx.commons.services.config.mapper.MapperAppConfig;
import com.prx.persistence.general.domains.ContactEntity;
import com.prx.persistence.general.domains.ContactTypeEntity;
import com.prx.persistence.general.domains.PersonEntity;
import org.mapstruct.*;

import java.util.ArrayList;
import java.util.List;

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



    default List<Contact> getContactList(PersonEntity personEntity) {
        List<Contact> contactList = new ArrayList<>();
        personEntity.getContacts().forEach(contactEntity -> {
            ContactType contactType = new ContactType();
            contactType.setId(contactEntity.getContactType().getId());
            contactType.setActive(contactEntity.getActive());
            contactType.setName(contactEntity.getContactType().getName());
            contactType.setDescription(contactEntity.getContactType().getDescription());
            Contact contact = new Contact();
            contact.setId(contactEntity.getId());
            contact.setContent(contactEntity.getContent());
            contact.setActive(contactEntity.getActive());
            contact.setContactType(contactType);
            contactList.add(contact);
        });
        return contactList;
    }

    default List<ContactEntity> getContactEntityList(Person person) {
        List<ContactEntity> contactEntityList = new ArrayList<>();
        if (person != null && person.getContacts() != null) {
            for (Contact contact : person.getContacts()) {
                ContactEntity contactEntity = new ContactEntity();
                if (contact.getContactType() != null) {
                    contactEntity.setContactType(new ContactTypeEntity());
                    contactEntity.getContactType().setId(contact.getContactType().getId());
                    contactEntity.getContactType().setName(contact.getContactType().getName());
                    contactEntity.getContactType().setDescription(contact.getContactType().getDescription());
                    contactEntity.getContactType().setActive(contact.getContactType().getActive());
                }
                contactEntity.setId(contact.getId());
                contactEntity.setContent(contact.getContent());
                contactEntity.setActive(contact.getActive());
                contactEntityList.add(contactEntity);
            }
        }
        return contactEntityList;
    }

    @AfterMapping
    default void linkContacts(@MappingTarget PersonEntity personEntity) {
        if (personEntity.getContacts() != null) {
            for (ContactEntity contact : personEntity.getContacts()) {
                contact.setPerson(personEntity);
            }
        }
    }
}
