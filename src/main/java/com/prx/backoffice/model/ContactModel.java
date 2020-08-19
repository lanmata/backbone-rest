package com.prx.backoffice.model;

import com.prx.backoffice.converter.ContactConverter;
import com.prx.commons.pojo.Contact;
import com.prx.persistence.general.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
 * @since
 */
@Component
public class  ContactModel {
    @Autowired
    private ContactRepository contactRepository;
    @Autowired
    private ContactConverter contactConverter;


    public void saveAll(List<Contact> contacts) {
        contacts.forEach(contact -> {
            contactRepository.save(contactConverter.convertFromPojo(contact));
        });
    }
}
