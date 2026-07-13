/*
 *  @(#)UserService.java
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

import com.umdc.backoffice.v1.session.to.UserAliasTO;
import com.umdc.backoffice.v1.users.api.to.UserCreateRequest;
import com.umdc.backoffice.v1.users.api.to.UserCreateResponse;
import com.umdc.backoffice.v1.users.api.to.UserTO;
import com.umdc.commons.services.CrudService;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

/**
 * UserService. Interface that defines the operations that can be performed on a user.
 *
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
 * @version 0.0.2
 */
public interface UserService extends CrudService<UUID, UserTO> {

    /**
     * Find a user by ID.
     *
     * @param userId the user ID to find the user
     * @return the user wrapped in a ResponseEntity
     */
    default ResponseEntity<UserTO> findUserById(UUID userId) {
        throw new NotImplementedException();
    }

    /**
     * Find a user by alias.
     *
     * @param alias         the user alias to find the user
     * @param applicationId the application ID to find the user
     * @return the user wrapped in a ResponseEntity
     */
    default ResponseEntity<UserTO> findUserByAlias(String alias, UUID applicationId) {
        throw new NotImplementedException();
    }

    /**
     * Find a user alias by alias.
     *
     * @param alias the user alias to find the user alias
     * @return the user alias wrapped in a ResponseEntity
     */
    default ResponseEntity<UserAliasTO> findUserAliasByAlias(String alias, UUID applicationId) {
        throw new NotImplementedException();
    }

    /**
     * Find a user by email.
     *
     * @param alias    the user email to find the user
     * @param password the user password to find the user
     * @return the user wrapped in a ResponseEntity
     */
    default ResponseEntity<UserTO> access(String alias, String password) {
        throw new NotImplementedException();
    }

    /**
     * Find all users.
     *
     * @param applicationId the application ID to find the users
     * @return the list of users wrapped in a ResponseEntity
     */
    default ResponseEntity<List<UserTO>> findAll(UUID applicationId) {
        throw new NotImplementedException();
    }

    /**
     * Create a new user.
     *
     * @param userCreateRequest the user create request
     * @return the user create response wrapped in a ResponseEntity
     */
    default ResponseEntity<UserCreateResponse> create(UserCreateRequest userCreateRequest) {
        throw new NotImplementedException();
    }

    /**
     * Realiza la desvinculaci&oacute;n de un rol a un usuario especifico.
     *
     * @param userId [UUID] the user ID to unlink the role from
     * @param roleId [UUID] the role ID to unlink from the user
     * @return Object type [ResponseEntity] with the user
     */
    default ResponseEntity<UserTO> unlink(UUID userId, UUID roleId) {
        throw new NotImplementedException();
    }

    /**
     * Realiza la vinculaci&oacute;n de un rol a un usuario especifico.
     *
     * @param userId [UUID] the user ID to link the role to
     * @param roleId [UUID] the role ID to link to the user
     * @return Object type [ResponseEntity] with the user
     */
    default ResponseEntity<UserTO> roleLink(UUID userId, UUID roleId) {
        throw new NotImplementedException();
    }

    /**
     * Validate the user alias is available.
     *
     * @param alias         [String] Object type.
     * @param applicationId [UUID] Object type.
     * @return [String] Object type.
     */
    default ResponseEntity<Void> validateAlias(String alias, UUID applicationId) {
        throw new NotImplementedException();
    }

    /**
     * Validate the user email is available.
     *
     * @param email         [String] Object type.
     * @param applicationId [UUID] Object type.
     * @return [String] Object type.
     */
    default ResponseEntity<Void> validateEmail(String email, UUID applicationId) {
        throw new NotImplementedException();
    }

    /**
     * Deletes a user by applicationId and userId.
     *
     * @param applicationId the application ID
     * @param userId        the user ID
     * @return ResponseEntity<Void> with appropriate status
     */
    default ResponseEntity<Void> deleteUserByApplicationAndUserId(
            UUID applicationId,
            UUID userId) {
        throw new NotImplementedException();
    }
}
