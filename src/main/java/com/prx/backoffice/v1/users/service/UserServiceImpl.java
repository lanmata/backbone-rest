/*
 *  @(#)UserServiceImpl.java
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
package com.prx.backoffice.v1.users.service;

import com.prx.backoffice.constant.keys.RoleMessageKey;
import com.prx.backoffice.constant.keys.UserMessageKey;
import com.prx.backoffice.v1.people.mapper.PersonMapper;
import com.prx.backoffice.v1.people.service.PersonService;
import com.prx.backoffice.v1.roles.mapper.RoleMapper;
import com.prx.backoffice.v1.roles.service.RoleService;
import com.prx.backoffice.v1.users.api.to.UserCreateRequest;
import com.prx.backoffice.v1.users.api.to.UserCreateResponse;
import com.prx.backoffice.v1.users.api.to.UserTO;
import com.prx.backoffice.v1.users.mapper.UserMapper;
import com.prx.persistence.general.domains.*;
import com.prx.persistence.general.repositories.ApplicationRoleUserRepository;
import com.prx.persistence.general.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/// Implementation of the UserService interface for managing users.
/// Provides methods for creating, updating, deleting, and finding users.
/// Use various mappers and services to handle user-related operations.
///
/// @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
/// @version 1.0.1.20200904-01, 2019-10-14
@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final ApplicationRoleUserRepository applicationRoleUserRepository;
    private final PersonService personService;
    private final RoleService roleService;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PersonMapper personMapper;

    /// Constructor for UserServiceImpl.
    ///
    /// @param userRepository                the user repository
    /// @param applicationRoleUserRepository the application role user repository
    /// @param personService                 the person service
    /// @param roleService                   the role service
    /// @param userMapper                    the user mapper
    /// @param roleMapper                    the role mapper
    /// @param personMapper                  the person mapper
    public UserServiceImpl(UserRepository userRepository, ApplicationRoleUserRepository applicationRoleUserRepository,
                           PersonService personService, RoleService roleService, UserMapper userMapper, RoleMapper roleMapper, PersonMapper personMapper) {
        this.userRepository = userRepository;
        this.applicationRoleUserRepository = applicationRoleUserRepository;
        this.personService = personService;
        this.roleService = roleService;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.personMapper = personMapper;
    }


    @Override
    public ResponseEntity<Void> validateEmail(String email, UUID applicationId) {
        var result = userRepository.findByEmailAndApplication(email, applicationId);
        AtomicReference<ResponseEntity<Void>> responseEntity = new AtomicReference<>();
        result.ifPresentOrElse(
                userEntity -> responseEntity.set(new ResponseEntity<>(HttpStatus.CONFLICT)),
                () -> responseEntity.set(ResponseEntity.status(HttpStatus.OK).build()));
        return responseEntity.get();
    }

    @Override
    public ResponseEntity<Void> validateAlias(String alias, UUID applicationId) {
        var result = userRepository.findByAliasAndApplication(alias, applicationId);
        AtomicReference<ResponseEntity<Void>> responseEntity = new AtomicReference<>();
        result.ifPresentOrElse(
                userEntity -> responseEntity.set(new ResponseEntity<>(HttpStatus.CONFLICT)),
                () -> responseEntity.set(ResponseEntity.status(HttpStatus.OK).build()));
        return responseEntity.get();
    }

    /// Updates a user with the given user ID and user data.
    ///
    /// @param userId the user ID
    /// @param user   the user data
    /// @return the response entity containing the updated user data
    @Override
    public ResponseEntity<UserTO> update(UUID userId, UserTO user) {
        ResponseEntity<UserTO> responseEntity;
        if (Objects.isNull(userId)) {
            return ResponseEntity.badRequest().header(HttpHeaders.WARNING, "User ID empty or null").build();
        }
        final var userResponseEntity = findUserById(userId);
        try {
            if (HttpStatus.OK.equals(userResponseEntity.getStatusCode()) && Objects.nonNull(userResponseEntity.getBody())) {
                var previousUser = userResponseEntity.getBody();
                var previousPerson = previousUser.getPerson();
                if (Objects.nonNull(user.getPerson().getGender()) && !user.getPerson().getGender().isEmpty()) {
                    previousPerson.setGender(user.getPerson().getGender());
                }
                if (Objects.nonNull(user.getPerson().getBirthdate())) {
                    previousPerson.setBirthdate(user.getPerson().getBirthdate());
                }
                if (Objects.nonNull(user.getPerson().getFirstName()) && !user.getPerson().getFirstName().isEmpty()) {
                    previousPerson.setFirstName(user.getPerson().getFirstName());
                }
                if (Objects.nonNull(user.getPerson().getLastName()) && !user.getPerson().getLastName().isEmpty()) {
                    previousPerson.setLastName(user.getPerson().getLastName());
                }
                if (Objects.nonNull(user.getPerson().getContacts())) {
                    previousPerson.setContacts(user.getPerson().getContacts());
                }
                if (Objects.nonNull(user.getDisplayName()) && !user.getDisplayName().isEmpty()) {
                    previousUser.setDisplayName(user.getDisplayName());
                }
                if (Objects.nonNull(user.getPassword()) && !user.getPassword().isEmpty() && !previousUser.getPassword().equals(user.getPassword())) {
                    previousUser.setPassword(user.getPassword());
                }

                previousUser.setNotificationEmail(user.getNotificationEmail());
                previousUser.setNotificationSms(user.getNotificationSms());
                previousUser.setPrivacyDataOutActive(user.getPrivacyDataOutActive());
                previousUser.setActive(user.isActive());
                previousUser.setLastUpdate(LocalDateTime.now());

                final var userEntity = userMapper.toSource(previousUser);
                userEntity.setId(userId);
                if (Objects.isNull(userEntity.getApplicationRoleUser()) || userEntity.getApplicationRoleUser().isEmpty()) {
                    responseEntity = ResponseEntity.badRequest()
                            .header(HttpHeaders.WARNING, "The user requested doesn't have a person associated.")
                            .build();
                } else {
                    userEntity.getApplicationRoleUser().forEach(applicationRoleUserEntity -> {
                        applicationRoleUserEntity.setUser(userEntity);
                        applicationRoleUserEntity.setActive(Boolean.TRUE);
                    });
                    var result = userRepository.save(userEntity);
                    responseEntity = new ResponseEntity<>(userMapper.toTarget(result), HttpStatus.OK);
                }
            } else {
                responseEntity = ResponseEntity.badRequest().header(HttpHeaders.WARNING, "Invalid user").build();
            }
        } catch (Exception ex) {
            LOGGER.error("{}| {}", UserMessageKey.USER_ERROR_CREATED.getStatus(), user, ex);
            responseEntity = ResponseEntity.unprocessableEntity().build();
        }
        return responseEntity;
    }

    /// Finds a user with the given ID.
    ///
    /// @param id the user ID
    /// @return the response entity containing the user data
    @Override
    public ResponseEntity<UserTO> find(UUID id) {
        return null;
    }

    /// Finds a user by the given user ID.
    ///
    /// @param userId the user ID
    /// @return the response entity containing the user data
    @Override
    public ResponseEntity<UserTO> findUserById(UUID userId) {
        ResponseEntity<UserTO> responseEntity;
        final var optionalUser = userRepository.findById(userId);
        responseEntity = optionalUser.map(userEntity ->
                new ResponseEntity<>(userMapper.toTarget(userEntity), HttpStatus.OK)).orElseGet(() ->
                ResponseEntity.notFound().build());
        LOGGER.info("{}| userId:{}", responseEntity.getStatusCode().value(), userId);
        return responseEntity;
    }

    /// Finds a user by the given alias.
    ///
    /// @param alias the user alias
    /// @return the response entity containing the user data
    @Override
    public ResponseEntity<UserTO> findUserByAlias(final String alias, final UUID applicationId) {
        var userTO = findByAlias(alias, applicationId);
        if (Objects.isNull(userTO)) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } else {
            return new ResponseEntity<>(userTO, HttpStatus.OK);
        }
    }

    /// Finds all users.
    ///
    /// @return the response entity containing the list of users
    @Override
    public ResponseEntity<List<UserTO>> findAll(UUID applicationId) {
        final List<UserTO> userEntityList = new ArrayList<>();
        if (Objects.nonNull(applicationId)) {
            userRepository.findByApplication(applicationId).forEach(userEntity -> userEntityList.add(userMapper.toTarget(userEntity)));
        } else {
            userRepository.findAll().forEach(userEntity -> userEntityList.add(userMapper.toTarget(userEntity)));
        }
        if (userEntityList.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return new ResponseEntity<>(userEntityList, HttpStatus.OK);
        }
    }

    /// Creates a new user with the given user creation request.
    ///
    /// @param userCreateRequest the user creation request
    /// @return the response entity containing the created user data
    @Override
    @Transactional
    public ResponseEntity<UserCreateResponse> create(UserCreateRequest userCreateRequest) {
        if (null == userCreateRequest) {
            return ResponseEntity.badRequest().build();
        } else if (userCreateRequest.alias().isBlank()) {
            return ResponseEntity.badRequest().header(HttpHeaders.WARNING, "username is required").build();
        } else if (userCreateRequest.password().isBlank()) {
            return ResponseEntity.badRequest().header(HttpHeaders.WARNING, "password is required").build();
        } else if (Objects.isNull(userCreateRequest.roleId())) {
            return ResponseEntity.badRequest().header(HttpHeaders.WARNING, "Role is required").build();
        } else if (findUserByAlias(userCreateRequest.alias(), userCreateRequest.applicationId()).getStatusCode().equals(HttpStatus.OK)) {
            return ResponseEntity.badRequest().header(HttpHeaders.WARNING, "User previously exist.").build();
        }
        var personResponse = personService.create(userCreateRequest.person());
        if (personResponse.getStatusCode().equals(HttpStatus.CREATED)) {
            var userEntity = userMapper.toSource(userCreateRequest);
            userEntity.setCreatedDate(LocalDateTime.now());
            userEntity.setLastUpdate(LocalDateTime.now());
            userEntity.setPerson(personMapper.toSource(personResponse.getBody()));
            userEntity.setApplicationRoleUser(new HashSet<>());
            userEntity.setActive(Boolean.TRUE);

            var userEntityResult = userRepository.save(userEntity); // Save
            // Adding ApplicationRoleUser
            userEntityResult.getApplicationRoleUser().add(applicationLink(userEntityResult, userCreateRequest.roleId(), userCreateRequest.applicationId()));
            var userResult = userMapper.toUserCreateResponse(userEntityResult);

            return ResponseEntity.status(HttpStatus.CREATED).body(userResult);
        }
        return ResponseEntity.badRequest().build();
    }

    /// Unlinks a role from a user with the given user ID and role ID.
    ///
    /// @param userId the user ID
    /// @param roleId the role ID
    /// @return the response entity
    @Override
    public ResponseEntity<UserTO> unlink(UUID userId, UUID roleId) {
        throw new UnsupportedOperationException();
    }

    /// Links a role to a user with the given user ID and role ID.
    ///
    /// @param userId the user ID
    /// @param roleId the role ID
    /// @return the response entity containing the updated user data
    @Override
    public ResponseEntity<UserTO> roleLink(UUID userId, UUID roleId) {
        ResponseEntity<UserTO> responseEntity = null;
        final var optionalUserEntity = userRepository.findById(userId);
        if (optionalUserEntity.isPresent()) {
            final var userEntity = optionalUserEntity.get();
            for (ApplicationRoleUserEntity userRolEntity : userEntity.getApplicationRoleUser()) {
                if (userRolEntity.getRole().getId().equals(roleId)) {
                    responseEntity = new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
                    return responseEntity;
                }
            }
            final var messageActivityRole = roleService.find(roleId);
            if (RoleMessageKey.ROL_OK.getCode() == messageActivityRole.getStatusCode().value()) {
                if (null != userEntity.getApplicationRoleUser()) {
                    final var applicationRoleUserEntity = new ApplicationRoleUserEntity();
                    final var roleEntity = roleMapper.toSource(messageActivityRole.getBody());
                    applicationRoleUserEntity.setUser(userEntity);
                    applicationRoleUserEntity.setRole(roleEntity);
                    applicationRoleUserEntity.setActive(Boolean.TRUE);
                    userEntity.getApplicationRoleUser().add(applicationRoleUserEntity);
                    userRepository.save(userEntity);
                    responseEntity = new ResponseEntity<>(userMapper.toTarget(userEntity), HttpStatus.ACCEPTED);
                }
            } else {
                // Role not found
                responseEntity = ResponseEntity.notFound().build();
            }
        } else {
            // User not found
            responseEntity = ResponseEntity.notFound().build();
        }
        return responseEntity;
    }

    /// Creates an ApplicationRoleUserEntity link between a user and a role in an application.
    ///
    /// @param userEntity    the user entity
    /// @param roleId        the role ID
    /// @param applicationId the application ID
    /// @return the created ApplicationRoleUserEntity
    private ApplicationRoleUserEntity applicationLink(UserEntity userEntity, UUID roleId, UUID applicationId) {
        final var applicationRoleUserEntities = new HashSet<ApplicationRoleUserEntity>();
        final var applicationRoleUserEntity = new ApplicationRoleUserEntity();
        final var applicationRoleUserEntityId = new ApplicationRoleUserEntityId();
        final var roleEntity = new RoleEntity();
        roleEntity.setId(roleId);

        ApplicationEntity applicationEntity = new ApplicationEntity();
        applicationEntity.setId(applicationId);
        applicationRoleUserEntityId.setApplicationId(applicationEntity.getId());
        applicationRoleUserEntityId.setUserId(userEntity.getId());
        applicationRoleUserEntityId.setRoleId(roleEntity.getId());
        applicationRoleUserEntity.setActive(true);
        applicationRoleUserEntity.setId(applicationRoleUserEntityId);

        applicationRoleUserEntity.setApplication(applicationEntity);
        applicationRoleUserEntity.setRole(roleEntity);
        applicationRoleUserEntity.setUser(userEntity);
        applicationRoleUserEntities.add(applicationRoleUserEntity);
        userEntity.setApplicationRoleUser(applicationRoleUserEntities);

        return applicationRoleUserRepository.save(applicationRoleUserEntity);
    }

    /// Finds a user by the given alias.
    ///
    /// @param alias the user alias
    /// @return the user data
    private UserTO findByAlias(String alias, UUID applicationId) {
        Optional<UserEntity> userEntityOptional;
        UserEntity userEntity;
        if (Objects.isNull(applicationId)) {
            userEntityOptional = Optional.of(userRepository.findByAlias(alias));
        } else {
            userEntityOptional = userRepository.findByAliasAndApplication(alias, applicationId);
        }

        userEntity = userEntityOptional.orElse(null);

        return Objects.nonNull(userEntity) ? userMapper.toTarget(userEntity) : null;
    }
}
