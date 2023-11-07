package com.prx.backoffice.v1.contacts.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prx.backoffice.MockLoaderBase;
import com.prx.backoffice.v1.contacts.api.to.ContactRequest;
import com.prx.backoffice.v1.contacts.service.ContactServiceImpl;
import com.prx.commons.pojo.Contact;
import com.prx.commons.pojo.ContactType;
import com.prx.commons.pojo.Person;
import com.prx.persistence.general.repositories.ContactRepository;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;

class ContactControllerTest extends MockLoaderBase {

    @Autowired
    private ContactController contactController;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ContactServiceImpl contactService;

    @Mock
    ContactRepository contactRepository;

    private static final String PATH;

    static {
        PATH = "/v1/contacts/";
    }

    private MockMvcRequestSpecification mockMvcRequestSpecification;

    @BeforeEach
    void setUp() {
        mockMvcRequestSpecification = given().header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }

    /**
     * Method under test: {@link ContactController#create(Contact)}
     */
    @Test
    void testCreate() throws JsonProcessingException {
        final var contactId = UUID.randomUUID();
        final var contactRequest = getContactRequest();
        final var response = ResponseEntity.status(HttpStatus.CREATED).body(contactRequest.getContact());
        //when:
        Mockito.when(contactService.create(Mockito.any(Contact.class))).thenReturn(response);
        //then:
        given().contentType(MediaType.APPLICATION_JSON_VALUE).body(objectMapper.writeValueAsString(contactRequest))
                .accept(MediaType.APPLICATION_JSON_VALUE).when().post(PATH)
                .then().assertThat().statusCode(HttpStatus.CREATED.value()).expect(MvcResult::getResponse);
    }

    /**
     * Method under test: {@link ContactController#update(String, Contact)}
     */
    @Test
    void testUpdate() throws JsonProcessingException {
        final var contactId = UUID.randomUUID();
        final var contactRequest = getContactRequest();
        final var response = ResponseEntity.status(HttpStatus.OK).body(contactRequest.getContact());
        //when:
        Mockito.when(contactService.update(Mockito.any(Contact.class), Mockito.anyString())).thenReturn(response);
        //then:
        given().contentType(MediaType.APPLICATION_JSON_VALUE).body(objectMapper.writeValueAsString(contactRequest))
                .accept(MediaType.APPLICATION_JSON_VALUE).when().put(PATH.concat("/".concat(contactId.toString())))
                .then().assertThat().statusCode(HttpStatus.OK.value()).expect(MvcResult::getResponse);

    }

    /**
     * Method under test: {@link ContactController#find(String)}
     */
    @Test
    void testFind() {
        final var contactId = UUID.randomUUID();
        final var contactRequest = getContactRequest();
        final var response = ResponseEntity.status(HttpStatus.OK).body(contactRequest.getContact());
        //when:
        Mockito.when(contactService.find(Mockito.anyString())).thenReturn(response);
        //then:
        given().contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON_VALUE).when()
                .get(PATH.concat("/".concat(contactId.toString()))).then().assertThat()
                .statusCode(HttpStatus.OK.value()).expect(MvcResult::getResponse);
    }

    /**
     * Method under test: {@link ContactController#list()}
     */
    @Test
    void testList() {

    }

    /**
     * Method under test: {@link ContactController#list(List)}
     */
    @Test
    void testList3() {

    }

    /**
     * Method under test: {@link ContactController#list(List)}
     */
    @Test
    void testList4() {

    }

    /**
     * Method under test: {@link ContactController#list(List)}
     */
    @Test
    void testList5() {

    }

    /**
     * Method under test: {@link ContactController#delete(String)}
     */
    @Test
    void testDelete() {
        final var contactId = UUID.randomUUID();
        final var response = ResponseEntity.status(HttpStatus.OK).body("");
        //when:
        Mockito.when(contactService.deleteById(Mockito.anyString())).thenReturn(response);
        //then:
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE).when().delete(PATH.concat("/".concat(contactId.toString())))
                .then().assertThat().statusCode(HttpStatus.OK.value()).expect(MvcResult::getResponse);
    }

    /**
     * Method under test: {@link ContactController#list(String)}
     */
    @Test
    void testList2() throws Exception {

    }


    private ContactRequest getContactRequest() {
        var contactRequest = new ContactRequest();
        ContactType contactType1 = new ContactType();
        contactType1.setActive(true);
        contactType1.setDescription("The characteristics of someone or something");
        contactType1.setId(UUID.randomUUID().toString());
        contactType1.setName("Name");

        Person person1 = new Person();
        person1.setBirthdate(LocalDate.of(1970, 1, 1));
        person1.setFirstName("Jane");
        person1.setGender("Gender");
        person1.setId(UUID.randomUUID().toString());
        person1.setLastName("Doe");
        person1.setMiddleName("Middle Name");

        Contact contact = new Contact();
        contact.setActive(true);
        contact.setContactType(contactType1);
        contact.setContent("Not all who wander are lost");
        contact.setId(UUID.randomUUID().toString());
        contact.setPerson(person1);
        contactRequest.setContact(contact);

        return contactRequest;
    }
}

