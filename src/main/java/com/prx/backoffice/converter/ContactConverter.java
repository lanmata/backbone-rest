package com.prx.backoffice.converter;

import com.prx.commons.converter.Converter;
import com.prx.commons.enums.types.ContactType;
import com.prx.commons.pojo.Contact;
import com.prx.persistence.general.domain.ContactEntity;
import com.prx.persistence.general.domain.PersonEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static com.prx.commons.util.ValidatorCommons.esNoNulo;

/**
 *
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
 */
@Component
public class ContactConverter extends Converter<Contact, ContactEntity> {
    @Autowired
    private PersonConverter personConverter;
    @PostConstruct
    @Override
    protected void initFunction() {
        setFunction(this::getContactEntity,
                contactEntity -> new Contact());
    }

    private ContactEntity getContactEntity(Contact contact) {
        PersonEntity personEntity = null;
        ContactEntity contactEntity;

        if (esNoNulo(contact.getPerson())) {
            personEntity = personConverter.convertFromPojo(contact.getPerson());
        }

        personConverter.convertFromPojo(contact.getPerson());
        contactEntity = new ContactEntity(contact.getId(), contact.getContent(),
                ContactType.getConctactType(contact.getContactTypeId()),
                contact.getActive(), personEntity);

        return contactEntity;
    }
}
