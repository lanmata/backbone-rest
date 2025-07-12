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

import com.prx.backoffice.constant.keys.ApplicationMessageKey;
import com.prx.backoffice.constant.keys.UserMessageKey;
import com.prx.backoffice.v1.users.api.to.UserCreateRequest;
import com.prx.backoffice.v1.users.api.to.UserCreateResponse;
import com.prx.backoffice.v1.users.api.to.UserTO;
import com.prx.backoffice.v1.users.mapper.UserMapper;
import com.prx.commons.exception.StandardException;
import com.prx.persistence.general.domains.*;
import com.prx.persistence.general.repositories.ApplicationRepository;
import com.prx.persistence.general.repositories.ApplicationRoleUserRepository;
import com.prx.persistence.general.repositories.RoleRepository;
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

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationRoleUserRepository applicationRoleUserRepository;
    private final UserMapper userMapper;

    /// Constructs a new UserServiceImpl with the provided dependencies.
    ///
    /// @param userRepository the repository to manage user data
    /// @param applicationRoleUserRepository the repository to manage application role-user mappings
    /// @param roleRepository the repository to manage role data
    /// @param applicationRepository the repository to manage application data
    /// @param userMapper the mapper to convert between user entities and DTOs
    public UserServiceImpl(UserRepository userRepository, ApplicationRoleUserRepository applicationRoleUserRepository,
                           RoleRepository roleRepository, ApplicationRepository applicationRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.applicationRepository = applicationRepository;
        this.applicationRoleUserRepository = applicationRoleUserRepository;
        this.userMapper = userMapper;
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
    @Transactional
    @Override
    public ResponseEntity<UserTO> update(UUID userId, UserTO user) {
        LOGGER.info("Starting user update {}", user);
        ResponseEntity<UserTO> responseEntity;
        if (Objects.isNull(userId)) {
            return ResponseEntity.badRequest().header(HttpHeaders.WARNING, "User ID empty or null").build();
        }
        final var userResponseEntity = findById(userId);
        try {
            if (Objects.nonNull(userResponseEntity)) {
                if (Objects.nonNull(user.getDisplayName()) && !user.getDisplayName().isEmpty()) {
                    userResponseEntity.setDisplayName(user.getDisplayName());
                }
                if (Objects.nonNull(user.getPassword()) && !user.getPassword().isEmpty() && !userResponseEntity.getPassword().equals(user.getPassword())) {
                    userResponseEntity.setPassword(user.getPassword());
                }

                userResponseEntity.setNotificationEmail(user.getNotificationEmail());
                userResponseEntity.setNotificationSms(user.getNotificationSms());
                userResponseEntity.setPrivacyDataOutActive(user.getPrivacyDataOutActive());
                userResponseEntity.setActive(user.isActive());
                userResponseEntity.setLastUpdate(LocalDateTime.now());

                refreshRoleByApplication(userResponseEntity, user);
                LOGGER.info("Before to save {}", userResponseEntity);
                var result = userRepository.save(userResponseEntity);
                responseEntity = new ResponseEntity<>(userMapper.toTarget(result), HttpStatus.OK);
                LOGGER.info("User updated.");
            } else {
                responseEntity = ResponseEntity.badRequest().header(HttpHeaders.WARNING, "Invalid user").build();
            }
        } catch (Exception ex) {
            LOGGER.error("{}| {}", UserMessageKey.USER_ERROR_CREATED.getStatus(), user, ex);
            responseEntity = ResponseEntity.unprocessableEntity().build();
        }
        return responseEntity;
    }

    private void refreshRoleByApplication(UserEntity userPrevious, UserTO userCurrent) {
        if (Objects.nonNull(userCurrent.getApplications()) && !userCurrent.getApplications().isEmpty() && Objects.nonNull(userCurrent.getRoles()) && !userCurrent.getRoles().isEmpty()) {
            Optional<ApplicationEntity> applicationEntityOptional;
            Optional<RoleEntity> roleEntityOptional;
            var optApplication = userCurrent.getApplications().stream().findFirst();
            var optRole = userCurrent.getRoles().stream().findFirst();
            if (optApplication.isPresent() && optRole.isPresent()) {
                applicationEntityOptional = applicationRepository.findById(optApplication.get().getId());
                roleEntityOptional = roleRepository.findById(optRole.get().getId());
                var applicationEntity = applicationEntityOptional.orElseThrow(() -> new StandardException(ApplicationMessageKey.APPLICATION_NOT_FOUND));
                var roleEntity = roleEntityOptional.orElseThrow(() -> new StandardException(ApplicationMessageKey.APPLICATION_NOT_FOUND));

                var applicationRoleUserPrevious = userPrevious.getApplicationRoleUser().stream().filter(aru ->
                        aru.getId().getApplicationId().equals(applicationEntity.getId())
                                && aru.getId().getUserId().equals(userCurrent.getId())
                                && !aru.getId().getRoleId().equals(roleEntity.getId())
                ).findFirst();

                if (applicationRoleUserPrevious.isPresent() && Objects.nonNull(applicationRoleUserPrevious.get().getId())) {
                    applicationRoleUserRepository.deleteByUserIdAndApplicationId(userCurrent.getId(), applicationEntity.getId());
                }

                LOGGER.info("Updating roles from {} to {}", roleEntity.getName(), userCurrent.getLastUpdate());
                Set<ApplicationRoleUserEntity> applicationRoleUser = new HashSet<>();
                var applicationRoleUserEntity = new ApplicationRoleUserEntity();
                var applicationRoleUserId = new ApplicationRoleUserEntityId();
                applicationRoleUserId.setUserId(userCurrent.getId());
                applicationRoleUserId.setRoleId(roleEntity.getId());
                applicationRoleUserId.setApplicationId(applicationEntity.getId());
                applicationRoleUserEntity.setId(applicationRoleUserId);
                applicationRoleUserEntity.setApplication(applicationEntity);
                applicationRoleUserEntity.setUser(userPrevious);
                applicationRoleUserEntity.setRole(roleEntity);
                applicationRoleUserEntity.setActive(true);
                applicationRoleUser.add(applicationRoleUserEntity);

                userPrevious.setApplicationRoleUser(applicationRoleUser);
            }
        }
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

    private UserEntity findById(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new StandardException(UserMessageKey.USER_NOT_FOUND));
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

        var userEntity = userMapper.toSource(userCreateRequest);
        userEntity.setCreatedDate(LocalDateTime.now());
        userEntity.setLastUpdate(LocalDateTime.now());

        userEntity.setApplicationRoleUser(new HashSet<>());
        userEntity.setActive(Boolean.TRUE);

        var applicationEntity = applicationRepository.findById(userCreateRequest.applicationId());
        var roleEntity = roleRepository.findById(userCreateRequest.roleId());
        var applicationRoleUserEntity = new ApplicationRoleUserEntity();
        var applicationRoleUserEntityId = new ApplicationRoleUserEntityId();
        applicationRoleUserEntity.setRole(roleEntity.get());
        applicationRoleUserEntity.setApplication(applicationEntity.get());
        applicationRoleUserEntity.setId(applicationRoleUserEntityId);
        applicationRoleUserEntity.setUser(userEntity);

        userEntity.getApplicationRoleUser().add(applicationRoleUserEntity);

        var userEntityResult = userRepository.save(userEntity); // Save
        // Adding ApplicationRoleUser
        var userResult = userMapper.toUserCreateResponse(userEntityResult);

        return ResponseEntity.status(HttpStatus.CREATED).body(userResult);
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

    @Override
    public ResponseEntity<Void> deleteUserByApplicationAndUserId(UUID applicationId, UUID userId) {
        if (applicationId == null || userId == null) {
            return ResponseEntity.badRequest().build();
        }
        var applicationOpt = applicationRepository.findById(applicationId);
        if (applicationOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        var userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        // Permission check placeholder (implement as needed)
        boolean hasPermission = true; // Replace with actual permission logic
        if (!hasPermission) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        // Check if user belongs to application
        boolean userInApp = userOpt.get().getApplicationRoleUser().stream()
                .anyMatch(aru -> aru.getId().getApplicationId().equals(applicationId));
        if (!userInApp) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        // Delete user from application (remove ApplicationRoleUserEntity)
        applicationRoleUserRepository.deleteByUserIdAndApplicationId(userId, applicationId);

        return ResponseEntity.noContent().build();
    }
}
