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
import com.prx.backoffice.v1.roles.api.to.RoleLinkRequest;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static com.prx.backoffice.util.ConstantUtilTest.APP_NAME_VALUE;
import static com.prx.backoffice.util.ConstantUtilTest.APP_TOKEN_VALUE;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;

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

    private static final String PATH_LIST_BY_USER;
    private static final String PATH_UNLINK;
    private static final String PATH;
    private static final String PATH_CREATE;
    private static final String PATH_LIST;
    private static final String PATH_LINK;
    private static final String PATH_FIND;

    private MockMvcRequestSpecification mockMvcRequestSpecification;

    static {
        PATH_LIST_BY_USER = "/v1/role/listByUser/";
        PATH = "/v1/roles/";
        PATH_UNLINK = "/v1/role/unlink/";
        PATH_CREATE = "/v1/role/";
        PATH_LINK = "/v1/role/link/";
        PATH_FIND = "/v1/role/find/";
        PATH_LIST = "/v1/role/list/";
    }

    @BeforeEach
    void setUp() {
        mockMvcRequestSpecification = given().header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    @DisplayName("Found role")
    void findOK() {
        //when:
        var response = mockMvcRequestSpecification.get(PATH_FIND.concat("5"));
        // then:
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Not found role")
    void findNotFound() {
        //when:
        Mockito.when(roleService.find(Mockito.anyString())).thenReturn(ResponseEntity.notFound().build());
        var response = mockMvcRequestSpecification.get(PATH_FIND.concat("0"));
        // then:
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Create a new role")
    void create_ok() throws Exception {
        final var role = getRole();
        final var response = ResponseEntity.status(HttpStatus.CREATED).body(role);
        //when:
        Mockito.when(roleService.create(Mockito.any(Role.class))).thenReturn(response);
        //then:
        given().contentType(MediaType.APPLICATION_JSON_VALUE).body(objectMapper.writeValueAsString(getRoleRequest(role)))
                .accept(MediaType.APPLICATION_JSON_VALUE).when().post(PATH_CREATE).then().assertThat()
                .statusCode(HttpStatus.CREATED.value()).expect(MvcResult::getResponse);
    }

    @Test
    @DisplayName("Update role - Successfully")
    void testUpdate() throws JsonProcessingException {
        final var roleRequest = new RoleRequest();
        roleRequest.setRole(getRole());
        final var response = ResponseEntity.status(HttpStatus.ACCEPTED).body(roleRequest.getRole());
        //when:
        Mockito.when(roleService.update(Mockito.anyString(), Mockito.<Role>any())).thenReturn(response);
        //then:
        given().contentType(MediaType.APPLICATION_JSON_VALUE).body(objectMapper.writeValueAsString(roleRequest))
                .accept(MediaType.APPLICATION_JSON_VALUE).when().put(PATH.concat(roleRequest.getRole().getId()))
                .then().assertThat().statusCode(HttpStatus.ACCEPTED.value()).expect(MvcResult::getResponse);
    }

    @Test
    @DisplayName("Role not updated - NOT_FOUND")
    void testUpdate_not_accepted() throws JsonProcessingException {
        final var roleRequest = new RoleRequest();
        roleRequest.setRole(getRole());
        //when:
        Mockito.when(roleService.update(Mockito.anyString(), Mockito.<Role>any())).thenReturn(ResponseEntity.notFound().build());
        //then:
        given().contentType(MediaType.APPLICATION_JSON_VALUE).body(objectMapper.writeValueAsString(roleRequest))
                .accept(MediaType.APPLICATION_JSON_VALUE).when().put(PATH.concat(roleRequest.getRole().getId()))
                .then().assertThat().statusCode(HttpStatus.NOT_FOUND.value()).expect(MvcResult::getResponse);
    }

    @Test
    @DisplayName("Unlink role with features")
    void unlink() throws JsonProcessingException {
        var roleLinkRequest = getRoleLinkRequest();
        var role = getRole();
        var features = new ArrayList<String>();
        features.add("1L");
        features.add("5L");
        roleLinkRequest.setFeatureIdList(features);
        //when:
        Mockito.when(roleService.unlink(Mockito.anyString(), Mockito.anyList())).thenReturn(ResponseEntity.status(HttpStatus.ACCEPTED).body(role));
        //then:
        given().contentType(MediaType.APPLICATION_JSON_VALUE).body(objectMapper.writeValueAsString(roleLinkRequest))
                .accept(MediaType.APPLICATION_JSON_VALUE).when().put(PATH_UNLINK.concat("1")).then().assertThat()
                .statusCode(HttpStatus.ACCEPTED.value()).expect(MvcResult::getResponse);
    }

    @Test
    @DisplayName("Link role with features")
    void link() throws JsonProcessingException {
        var roleLinkRequest = getRoleLinkRequest();
        var role = getRole();
        var features = new ArrayList<String>();
        roleLinkRequest.setFeatureIdList(features);
        features.add("1L");
        features.add("3L");
        features.add("5L");
        //when:
        Mockito.when(roleService.link(Mockito.anyString(), Mockito.anyList())).thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(role));
        //then:
        given().contentType(MediaType.APPLICATION_JSON_VALUE).body(objectMapper.writeValueAsString(roleLinkRequest))
                .accept(MediaType.APPLICATION_JSON_VALUE).when().put(PATH_LINK.concat("1")).then().assertThat()
                .statusCode(HttpStatus.CREATED.value()).expect(MvcResult::getResponse);
    }

    @Test
    @DisplayName("Find a list with few inactive roles include and id role list")
    void list_ok_001() {
        //when:
        var response = mockMvcRequestSpecification.get(PATH_LIST.concat("true").concat("/1,2,3"));
        // then:
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Find a list with few inactive roles and id role list and a nonexistent role")
    void list_ok_002() {
        //when:
        var response = mockMvcRequestSpecification.get(PATH_LIST.concat("true").concat("/1,2,3,0"));
        // then:
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Find a list with all active roles")
    void list_ok_003() {
        //when:
        var response = mockMvcRequestSpecification.get(PATH_LIST.concat("true"));
        // then:
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Find roles list by user")
    void list_ok_004() {
        //when:
        var response = mockMvcRequestSpecification.get(PATH_LIST_BY_USER.concat("5"));
        // then:
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Find a list with few inactive roles include - NOT_FOUND expected response")
    void list_not_found() {
        //when:
        Mockito.when(roleService.list(Mockito.anyBoolean(),Mockito.anyList())).thenReturn(ResponseEntity.notFound().build());
        var response = mockMvcRequestSpecification.get(PATH_LIST.concat("true").concat("/0,-1,-5"));
        // then:
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
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

    private @NotNull RoleLinkRequest getRoleLinkRequest() {
        var roleLinkRequest = new RoleLinkRequest();
        roleLinkRequest.setAppName(APP_NAME_VALUE);
        roleLinkRequest.setAppToken(APP_TOKEN_VALUE);
        roleLinkRequest.setDateTime(LocalDateTime.now());
        return roleLinkRequest;
    }
}
