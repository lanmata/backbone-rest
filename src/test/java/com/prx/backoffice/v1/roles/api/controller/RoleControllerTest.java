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
package com.prx.backoffice.v1.roles.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prx.backoffice.MockLoaderBase;
import com.prx.backoffice.v1.roles.api.to.RoleRequest;
import com.prx.backoffice.v1.roles.service.RoleServiceImpl;
import com.prx.commons.pojo.Feature;
import com.prx.commons.pojo.Role;
import com.prx.persistence.general.repositories.RoleRepository;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.prx.backoffice.util.ConstantUtilTest.APP_NAME_VALUE;
import static com.prx.backoffice.util.ConstantUtilTest.APP_TOKEN_VALUE;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

/**
 * RolControllerTest.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 15-06-2021
 */
class RoleControllerTest extends MockLoaderBase {

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    RoleServiceImpl roleService;

    @Mock
    RoleRepository roleRepository;

    private static final String LIST_BY_USER;
    private static final String PATH;
    private static final String FIND;

    private MockMvcRequestSpecification mockMvcRequestSpecification;

    static {
        LIST_BY_USER = "user/";
        PATH = "/v1/roles";
        FIND = "find/";
    }

    @BeforeEach
    void setUp() {
        mockMvcRequestSpecification = given().header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    @DisplayName("Found role")
    void findOK() {
        //when:
        var response = mockMvcRequestSpecification.get(PATH.concat("/").concat(FIND.concat("0f9c32bf-33ea-401c-9da2-a2fb47231540")));
        // then:
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Not found role")
    void findNotFound() {
        //when:
        when(roleService.find(Mockito.anyString())).thenReturn(ResponseEntity.notFound().build());
        var response = mockMvcRequestSpecification.get(FIND.concat("0f9c32bf-33ea-401c-9da2-a2fb47231540"));
        // then:
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Update role - Successfully")
    void testUpdate() throws JsonProcessingException {
        final var roleRequest = new RoleRequest();
        roleRequest.setRole(getRole());
        final var response = ResponseEntity.status(HttpStatus.ACCEPTED).body(roleRequest.getRole());
        //when:
        when(roleService.update(Mockito.anyString(), Mockito.<Role>any())).thenReturn(response);
        //then:
        given().contentType(MediaType.APPLICATION_JSON_VALUE).body(objectMapper.writeValueAsString(roleRequest))
                .accept(MediaType.APPLICATION_JSON_VALUE).when().put(PATH.concat("/").concat(roleRequest.getRole().getId()))
                .then().assertThat().statusCode(HttpStatus.ACCEPTED.value()).expect(MvcResult::getResponse);
    }

    @Test
    @DisplayName("Role not updated - NOT_FOUND")
    void testUpdate_not_accepted() throws JsonProcessingException {
        final var roleRequest = new RoleRequest();
        roleRequest.setRole(getRole());
        //when:
        when(roleService.update(Mockito.anyString(), Mockito.<Role>any())).thenReturn(ResponseEntity.notFound().build());
        //then:
        given().contentType(MediaType.APPLICATION_JSON_VALUE).body(objectMapper.writeValueAsString(roleRequest))
                .accept(MediaType.APPLICATION_JSON_VALUE).when().put(PATH.concat("/").concat(roleRequest.getRole().getId()))
                .then().assertThat().statusCode(HttpStatus.NOT_FOUND.value()).expect(MvcResult::getResponse);
    }

    @Test
    @DisplayName("Find a list with few inactive roles include and id role list")
    void list_ok_001() {
        //when:
        var response = mockMvcRequestSpecification.get(PATH.concat("/").concat("true")
                .concat("/0393857c-cdce-4b01-b73c-d1b561ecc57d,803c743c-b217-4895-9816-8bb3981fb782,adae2cd4-5adf-4d94-adb5-9295dee2a70a"));
        // then:
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Find a list with few inactive roles and id role list and a nonexistent role")
    void list_ok_002() {
        //when:
        var response = mockMvcRequestSpecification.get(PATH.concat("/").concat("true")
                .concat("/0393857c-cdce-4b01-b73c-d1b561ecc57d,803c743c-b217-4895-9816-8bb3981fb782,adae2cd4-5adf-4d94-adb5-9295dee2a70a"));
        // then:
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Find roles list by user")
    void list_by_user_id_ok_004() {
        //when:
        var response = mockMvcRequestSpecification.get(PATH.concat("/") + LIST_BY_USER.concat("0f9c32bf-33ea-401c-9da2-a2fb47231540"));
        // then:
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Find roles list by user - NOT FOUND")
    void list_not_found() {
        final var roleResponse = ResponseEntity.status(HttpStatus.OK).body(List.of(getRole()));
        //when:
        when(roleService.listByUser(Mockito.anyString())).thenReturn(roleResponse);
        //when:
        var response = mockMvcRequestSpecification.get(PATH.concat("/") + LIST_BY_USER.concat("0f9c32bf-33ea-401c-9da2-a2fb47231540"));
        // then:
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Search roles by status and role Id's - OK")
    void testListById_ok() {
        //when:
        when(roleService.list(Mockito.anyBoolean(), anyList())).thenReturn(ResponseEntity.ok(List.of(getRole())));
        //when:
        var response = mockMvcRequestSpecification.get(PATH.concat("/").concat(true+"/").concat("0f9c32bf-33ea-401c-9da2-a2fb47231540,0f9c32bf-33ea-401c-9da2-a2fb48521540"));
        // then:
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Search roles by status and role Id's - OK")
    void testList_ok() {
        //when:
        when(roleService.list()).thenReturn(ResponseEntity.ok(List.of(getRole())));
        //when:
        var response = mockMvcRequestSpecification.get(PATH);
        // then:
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    private @NotNull RoleRequest getRoleRequest(Role role) {
        final var roleRequest = new RoleRequest();
        roleRequest.setRole(role);
        roleRequest.setAppName(APP_NAME_VALUE);
        roleRequest.setAppToken(APP_TOKEN_VALUE);

        return roleRequest;
    }

    private @NotNull Role getRole() {
        final var roleId = UUID.randomUUID();
        final var featureId = UUID.randomUUID();
        final var role = new Role();
        final var feature = new Feature();
        feature.setId(featureId.toString());
        feature.setActive(true);
        feature.setName("Feature name");
        feature.setDescription("Feature description");
        role.setId(roleId.toString());
        role.setActive(true);
        role.setName("Role name");
        role.setFeatures(new ArrayList<>());
        role.getFeatures().add(feature);
        role.setDescription("Role description");
        return role;
    }
}
