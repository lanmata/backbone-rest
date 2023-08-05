package com.prx.backoffice.v1.contacts.to;

import com.prx.backoffice.v1.contacts.api.to.ContactRequest;
import com.prx.commons.pojo.Contact;
import com.prx.commons.pojo.ContactType;
import com.prx.commons.pojo.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ContactRequestTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void testGetterAndSetter() {
        final var contactRequest = new ContactRequest();
        final var contact = new Contact();
        final var person = new Person();
        final var contactType = new ContactType();
        person.setBirthdate(LocalDate.of(1979, 4, 14));
        person.setFirstName("Pepe");
        person.setGender("M");
        person.setId("1");
        person.setLastName("Perez");
        person.setMiddleName("Peter");
        contact.setPerson(person);
        contactType.setId("1L");
        contactType.setActive(true);
        contactType.setName("Contact TST 001");
        contactType.setDescription("Contact description TST 001");
        contact.setId("1");
        contact.setContent("Contact TST0 01");
        contact.setActive(true);
        contact.setContactType(contactType);
        contact.setContactType(contactType);
        contactRequest.setContact(contact);
        contactRequest.setAppToken("ABC001");
        contactRequest.setAppName("TST-001");
        contactRequest.setDateTime(LocalDateTime.now());

        assertAll(() -> assertNotNull(contactRequest),
                () -> assertNotNull(contactRequest.getContact()),
                () -> assertEquals(1, contactRequest.getContact().getId()),
                () -> assertNotNull(contactRequest.getContact().getPerson()),
                () -> assertNotNull(contactRequest.getContact().getContactType()),
                () -> assertNotNull(contactRequest.getContact().getActive()),
                () -> assertNotNull(contactRequest.getContact().getContent()),
                () -> assertNotNull(contactRequest.getContact().toString()),
                () -> assertNotNull(contactRequest.toString())
        );
    }
}
