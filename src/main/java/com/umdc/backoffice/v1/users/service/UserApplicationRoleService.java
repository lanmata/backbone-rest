/*
 *  @(#)UserApplicationRoleService.java
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

import com.umdc.backoffice.v1.users.api.to.UserTO;
import com.umdc.persistence.general.domains.ApplicationEntity;
import com.umdc.persistence.general.domains.RoleEntity;
import com.umdc.persistence.general.domains.UserEntity;

import java.util.UUID;

/// Service interface for managing user-application-role relationships.
///
/// @version 1.0.0, 2026-04-08
public interface UserApplicationRoleService {

    /// Refreshes the role assignment for a user within an application.
    /// Updates the ApplicationRoleUser entity based on current user data.
    ///
    /// @param userEntity the user entity to update
    /// @param userTO the user DTO containing the desired role and application
    void refreshRoleByApplication(UserEntity userEntity, UserTO userTO);

    /// Builds an ApplicationRoleUser entity linking user, application, and role.
    ///
    /// @param userId the user ID
    /// @param userEntity the user entity
    /// @param applicationEntity the application entity
    /// @param roleEntity the role entity
    /// @return the configured ApplicationRoleUserEntity
    com.umdc.persistence.general.domains.ApplicationRoleUserEntity buildApplicationRoleUser(
            UUID userId,
            UserEntity userEntity,
            ApplicationEntity applicationEntity,
            RoleEntity roleEntity);
}

