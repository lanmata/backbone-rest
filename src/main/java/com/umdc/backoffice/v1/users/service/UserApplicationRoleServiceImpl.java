/*
 *  @(#)UserApplicationRoleServiceImpl.java
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
package com.umdc.backoffice.v1.users.service;

import com.umdc.backoffice.constant.keys.ApplicationMessageKey;
import com.umdc.backoffice.constant.keys.RoleMessageKey;
import com.umdc.backoffice.v1.application.mapper.ApplicationMapper;
import com.umdc.backoffice.v1.application.service.ApplicationService;
import com.umdc.backoffice.v1.roles.mapper.RoleMapper;
import com.umdc.backoffice.v1.roles.service.RoleService;
import com.umdc.backoffice.v1.users.api.to.UserTO;
import com.prx.commons.exception.StandardException;
import com.prx.commons.general.pojo.Application;
import com.prx.persistence.general.domains.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

/// Service implementation for managing user-application-role relationships.
/// Delegates to ApplicationService and RoleService for data retrieval.
/// Uses ApplicationMapper and RoleMapper for entity conversions.
///
/// @version 1.0.0, 2026-04-08
@Service
public class UserApplicationRoleServiceImpl implements UserApplicationRoleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserApplicationRoleServiceImpl.class);

    private final ApplicationService applicationService;
    private final RoleService roleService;
    private final ApplicationMapper applicationMapper;
    private final RoleMapper roleMapper;

    public UserApplicationRoleServiceImpl(ApplicationService applicationService,
                                          RoleService roleService,
                                          ApplicationMapper applicationMapper,
                                          RoleMapper roleMapper) {
        this.applicationService = applicationService;
        this.roleService = roleService;
        this.applicationMapper = applicationMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    public void refreshRoleByApplication(UserEntity userEntity, UserTO userTO) {
        if (userTO == null) {
            return;
        }
        var optApplication = Optional.ofNullable(userTO.getApplications())
                .flatMap(list -> list.stream().findFirst());
        var optRole = Optional.ofNullable(userTO.getRoles())
                .flatMap(list -> list.stream().findFirst());

        if (optApplication.isEmpty() || optRole.isEmpty()) {
            return;
        }

        // Use applicationService and mapper to get application entity
        var applicationResponse = applicationService.find(optApplication.get().getId());
        if (applicationResponse.getStatusCode().isError() || applicationResponse.getBody() == null) {
            throw new StandardException(ApplicationMessageKey.APPLICATION_NOT_FOUND);
        }
        var applicationEntity = applicationMapper.toSource(applicationResponse.getBody());

        // Use roleService and mapper to get role entity
        var roleResponse = roleService.find(optRole.get().getId());
        if (roleResponse.getStatusCode().isError() || roleResponse.getBody() == null) {
            throw new StandardException(RoleMessageKey.ROL_NOT_FOUND);
        }
        var roleEntity = roleMapper.toSource(roleResponse.getBody());

        // Get the existing application-role-user to preserve profile image reference
        var applicationRoleUserPrevious = userEntity.getApplicationRoleUser().stream()
                .filter(aru -> aru.getId().getApplicationId().equals(applicationEntity.getId())
                        && aru.getId().getUserId().equals(userTO.getId()))
                .findFirst();

        // Check if role actually changed
        boolean roleChanged = !applicationRoleUserPrevious.isPresent() ||
                !applicationRoleUserPrevious.get().getId().getRoleId().equals(roleEntity.getId());

        if (roleChanged && applicationRoleUserPrevious.isPresent()) {
            var application = new Application();
            application.setId(applicationEntity.getId());
            applicationService.delete(userTO.getId(), application);
        }

        LOGGER.info("Updating roles from {} to {}", roleEntity.getName(), userTO.getLastUpdate());
        var applicationRoleUserEntity = buildApplicationRoleUser(userTO.getId(), userEntity, applicationEntity, roleEntity);

        // Preserve profile image reference from the previous application role user
        if (applicationRoleUserPrevious.isPresent() && applicationRoleUserPrevious.get().getProfileImageRef() != null) {
            applicationRoleUserEntity.setProfileImageRef(applicationRoleUserPrevious.get().getProfileImageRef());
        }

        var roleSet = new HashSet<ApplicationRoleUserEntity>();
        roleSet.add(applicationRoleUserEntity);
        userEntity.setApplicationRoleUser(roleSet);
    }

    @Override
    public ApplicationRoleUserEntity buildApplicationRoleUser(UUID userId,
                                                               UserEntity userEntity,
                                                               ApplicationEntity applicationEntity,
                                                               RoleEntity roleEntity) {
        var applicationRoleUserEntity = new ApplicationRoleUserEntity();
        var applicationRoleUserId = new ApplicationRoleUserEntityId();
        applicationRoleUserId.setUserId(userId);
        applicationRoleUserId.setRoleId(roleEntity.getId());
        applicationRoleUserId.setApplicationId(applicationEntity.getId());
        applicationRoleUserEntity.setId(applicationRoleUserId);
        applicationRoleUserEntity.setApplication(applicationEntity);
        applicationRoleUserEntity.setUser(userEntity);
        applicationRoleUserEntity.setRole(roleEntity);
        applicationRoleUserEntity.setActive(true);
        return applicationRoleUserEntity;
    }
}

