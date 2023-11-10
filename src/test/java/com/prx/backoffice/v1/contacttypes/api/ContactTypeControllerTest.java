package com.prx.backoffice.v1.contacttypes.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prx.backoffice.MockLoaderBase;
import com.prx.backoffice.v1.contacttypes.service.ContactTypeServiceImpl;
import com.prx.backoffice.v1.contacttypes.to.ContactTypeRequest;
import com.prx.commons.pojo.ContactType;
import com.prx.persistence.general.repositories.ContactRepository;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;

class ContactTypeControllerTest extends MockLoaderBase {
    @Autowired
    private ContactTypeController contactTypeController;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ContactTypeServiceImpl contactTypeService;

    @Mock
    ContactRepository contactRepository;

    private static final String PATH;

    static {
        PATH = "/v1/contact-types/";
    }

    private MockMvcRequestSpecification mockMvcRequestSpecification;

    /**
     * Method under test: {@link ContactTypeController#create(ContactTypeRequest)}
     */
    @Test
    void testCreate() throws JsonProcessingException {
        var contactTypeRequest = new ContactTypeRequest();
        contactTypeRequest.setContactType(getContactType());
        final var contactResponse = ResponseEntity.status(HttpStatus.CREATED).body(getContactType());

        //when:
        Mockito.when(contactTypeService.create(Mockito.any(ContactTypeRequest.class))).thenReturn(contactResponse);
        //then:
        given().contentType(MediaType.APPLICATION_JSON_VALUE).body(objectMapper.writeValueAsString(contactTypeRequest))
                .accept(MediaType.APPLICATION_JSON_VALUE).when().post(PATH)
                .then().assertThat().statusCode(HttpStatus.CREATED.value()).expect(MvcResult::getResponse);
    }

    @Test
    void testUpdate() throws JsonProcessingException {
        var contactTypeRequest = new ContactTypeRequest();
        var uuid = UUID.randomUUID();
        contactTypeRequest.setContactType(getContactType());
        final var contactResponse = ResponseEntity.status(HttpStatus.ACCEPTED).body(getContactType());

        //when:
        Mockito.when(contactTypeService.update(Mockito.anyString(), Mockito.any(ContactType.class))).thenReturn(contactResponse);
        //then:
        given().contentType(MediaType.APPLICATION_JSON_VALUE).body(objectMapper.writeValueAsString(getContactType()))
                .accept(MediaType.APPLICATION_JSON_VALUE).when().put(PATH + "/" + uuid.toString())
                .then().assertThat().statusCode(HttpStatus.ACCEPTED.value()).expect(MvcResult::getResponse);
    }

    private static ContactType getContactType() {
        final var contactTypeUUID = UUID.randomUUID();
        final var contactType2UUID = UUID.randomUUID();
        ContactType contactType = new ContactType();
        contactType.setActive(true);
        contactType.setDescription("The characteristics of someone or something");
        contactType.setId(contactTypeUUID.toString());
        contactType.setName("Name");
        return contactType;
    }
}

