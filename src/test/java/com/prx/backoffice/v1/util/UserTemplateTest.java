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

package com.prx.backoffice.v1.util;

import com.prx.backoffice.v1.user.api.controller.UserTemplate;
import com.prx.backoffice.v1.user.api.to.UserAccessRequest;
import com.prx.backoffice.v1.user.api.to.UserCreateRequest;
import com.prx.backoffice.v1.user.api.to.UserTO;
import com.prx.persistence.general.domains.UserEntity;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;

import static com.prx.backoffice.util.ConstantUtilTest.APP_NAME_VALUE;
import static com.prx.backoffice.util.ConstantUtilTest.APP_TOKEN_VALUE;

/**
 * UserTemplateTest.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 14-05-2022
 * @since 11
 */
public enum UserTemplateTest implements UserTemplate {
    USER {
        @Override
        public UserTO getModel() {
            final var user = new UserTO();
            user.setActive(true);
            user.setAlias("apepe");
            user.setId(1L);
            user.setPassword("234567890");
            user.setPerson( PersonTemplateTest.PERSON.getModel());
            user.setRoles(new HashSet<>());
            user.getRoles().add(1L);
            return user;
        }

        @Override
        public UserEntity getEntity() {
            final var userEntity = new UserEntity();
            userEntity.setPerson(PersonTemplateTest.PERSON.getEntity());
            userEntity.setPassword("5as46fdas7fs");
            userEntity.setActive(true);
            userEntity.setAlias("ccastro");
            userEntity.setId(1L);
            return userEntity;
        }

        @Override
        public UserCreateRequest getUserCreateRequest() {
            var userCreateRequest = new UserCreateRequest();
            userCreateRequest.setAppName(APP_NAME_VALUE);
            userCreateRequest.setAppToken(APP_TOKEN_VALUE);
            userCreateRequest.setDateTime(LocalDateTime.now(ZoneId.systemDefault()));
            userCreateRequest.setUser(UserTemplateTest.USER.getModel());
            return userCreateRequest;
        }

        @Override
        public UserAccessRequest getUserAccessRequest(){
            final var userAccessRequest = new UserAccessRequest();
            userAccessRequest.setAlias("pepe");
            userAccessRequest.setPassword("234567890");
            userAccessRequest.setDateTime(LocalDateTime.now(ZoneId.systemDefault()));
            userAccessRequest.setAppName("TEST-APP");
            userAccessRequest.setAppToken("TEST-APP/00252336");
            return userAccessRequest;
        }
    }
}
