package com.prx.backoffice.v1.users.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prx.backoffice.MockLoaderBase;
import com.prx.backoffice.v1.users.api.to.UserTO;
import com.prx.backoffice.v1.users.service.UserServiceImpl;
import com.prx.commons.pojo.Person;
import com.prx.commons.pojo.Role;
import com.prx.commons.pojo.User;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class UserControllerTest extends MockLoaderBase {

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    UserServiceImpl userService;

    private static final String PATH;

    static {
        PATH = "/v1/users/";
    }

    private MockMvcRequestSpecification mockMvcRequestSpecification;

    @BeforeEach
    void setUp() {
        mockMvcRequestSpecification = given().header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }

    /**
     * Method under test: {@link UserController#findAll()}
     */
    @Test
    @DisplayName("Find a list with all user")
    void list_ok_001() {
        UserTO user1 = new UserTO();
        UserTO user2 = new UserTO();
        UserTO user3 = new UserTO();

        user1.setId(UUID.randomUUID().toString());
        user1.setAlias("bent");
        user1.setPassword("bfg1534");
        user1.setRoles(Set.of(1L));
        user1.setPerson(new Person());
        user1.setActive(true);
        user2.setId(UUID.randomUUID().toString());
        user2.setAlias("glasses");
        user2.setPassword("mfn7483");
        user2.setRoles(Set.of(2L));
        user2.setPerson(new Person());
        user2.setActive(true);
        user3.setId(UUID.randomUUID().toString());
        user3.setAlias("rent");
        user3.setPassword("mgj84950");
        user3.setRoles(Set.of(3L));
        user3.setPerson(new Person());
        user3.setActive(true);

        //when:
        when(userService.findAll()).thenReturn(ResponseEntity.ok(List.of(user1, user2, user3)));
        var response = mockMvcRequestSpecification.get(PATH);
        // then:
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    /**
     * Method under test: {@link UserController#findAll()}
     */
    @Test
    @DisplayName("Find a list with few inactive roles include and id role list")
    void list_ok_002() {
        //when:
        when(userService.findAll()).thenReturn(ResponseEntity.ok().build());
        var response = mockMvcRequestSpecification.get(PATH);
        // then:
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }
}

