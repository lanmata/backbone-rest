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
package com.umdc.backoffice.v1.users.service;

import com.umdc.backoffice.constant.keys.UserMessageKey;
import com.umdc.backoffice.v1.application.service.ApplicationService;
import com.umdc.backoffice.v1.contacts.mapper.ContactMapper;
import com.umdc.backoffice.v1.contacttypes.mapper.ContactTypeMapper;
import com.umdc.backoffice.v1.users.api.to.UserCreateRequest;
import com.umdc.backoffice.v1.users.api.to.UserCreateResponse;
import com.umdc.backoffice.v1.users.api.to.UserTO;
import com.umdc.backoffice.v1.users.mapper.UserMapper;
import com.prx.commons.exception.StandardException;
import com.prx.commons.general.pojo.Application;
import com.prx.commons.general.pojo.Contact;
import com.prx.commons.general.pojo.Role;
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
@SuppressWarnings("PMD.GodClass") // Class has many responsibilities; decomposed some logic but further refactor is recommended
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final ApplicationRoleUserRepository applicationRoleUserRepository;
    private final ApplicationService applicationService;
    private final UserMapper userMapper;
    private final ContactMapper contactMapper;
    private final ContactTypeMapper contactTypeMapper;
    private final UserApplicationRoleService userApplicationRoleService;

    /// Constructs a new UserServiceImpl with the provided dependencies.
    ///
    /// @param userRepository the repository to manage user data
    /// @param applicationRoleUserRepository the repository to manage application role-user mappings
    /// @param applicationService the service to manage application operations
    /// @param userMapper the mapper to convert between user entities and DTOs
    /// @param userApplicationRoleService the service to manage user-application-role relationships
    public UserServiceImpl(UserRepository userRepository,
                           ApplicationRoleUserRepository applicationRoleUserRepository,
                           ApplicationService applicationService,
                           UserMapper userMapper, ContactMapper contactMapper, ContactTypeMapper contactTypeMapper,
                           UserApplicationRoleService userApplicationRoleService) {
        this.userRepository = userRepository;
        this.applicationRoleUserRepository = applicationRoleUserRepository;
        this.applicationService = applicationService;
        this.userMapper = userMapper;
        this.contactMapper = contactMapper;
        this.contactTypeMapper = contactTypeMapper;
        this.userApplicationRoleService = userApplicationRoleService;
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
    /// Supports partial updates - only non-null fields will be updated.
    ///
    /// @param userId the user ID
    /// @param user   the user data
    /// @return the response entity containing the updated user data
    @Transactional
    @Override
    public ResponseEntity<UserTO> update(UUID userId, UserTO user) {
        LOGGER.info("Starting user update {}", user);
        if (Objects.isNull(userId)) {
            return ResponseEntity.badRequest().header(HttpHeaders.WARNING, "User ID empty or null").build();
        }
        try {
            // findById will throw StandardException if not found; keep logic simple
            final var userEntity = findById(userId);
            // Update fields
            updateUserFields(userEntity, user);
            // Delegate person update - helper checks for null/emptiness
            updatePersonFields(userEntity, user);
            // If roles are provided but application is not, use the existing application
            if (Objects.nonNull(user.getRoles()) && !user.getRoles().isEmpty() &&
                (Objects.isNull(user.getApplications()) || user.getApplications().isEmpty())) {
                var existingApp = userEntity.getApplicationRoleUser().stream()
                        .findFirst()
                        .map(ApplicationRoleUserEntity::getApplication)
                        .map(appEntity -> {
                            var app = new Application();
                            app.setId(appEntity.getId());
                            return app;
                        });
                existingApp.ifPresent(application -> user.setApplications(Set.of(application)));
            }
            // Delegate role refresh to dedicated service
            userApplicationRoleService.refreshRoleByApplication(userEntity, user);

            LOGGER.info("Before to save {}", userEntity);
            var result = userRepository.save(userEntity);
            LOGGER.info("User updated.");
            return new ResponseEntity<>(userMapper.toTarget(result), HttpStatus.OK);
        } catch (Exception ex) {
            LOGGER.error("{}| {}", UserMessageKey.USER_ERROR_UPDATED.getStatus(), user, ex);
            return ResponseEntity.unprocessableEntity().build();
        }
    }

    // New helper: update simple user fields
    private void updateUserFields(UserEntity target, UserTO source) {
        if (isNonEmpty(source.getDisplayName())) {
            target.setDisplayName(source.getDisplayName());
        }
        if (isNonEmpty(source.getPassword()) && !Objects.equals(target.getPassword(), source.getPassword())) {
            target.setPassword(source.getPassword());
        }
        if (Objects.nonNull(source.getNotificationEmail())) {
            target.setNotificationEmail(source.getNotificationEmail());
        }
        if (Objects.nonNull(source.getNotificationSms())) {
            target.setNotificationSms(source.getNotificationSms());
        }
        if (Objects.nonNull(source.getPrivacyDataOutActive())) {
            target.setPrivacyDataOutActive(source.getPrivacyDataOutActive());
        }
        target.setLastUpdate(LocalDateTime.now());
    }

    // New helper: update person and contacts
    private void updatePersonFields(UserEntity target, UserTO source) {
        if (Objects.isNull(source) || Objects.isNull(source.getPerson())) {
            return; // nothing to update
        }
        var personSource = source.getPerson();
        var personTarget = target.getPerson();
        if (personTarget == null) {
            // If no person entity exists, create one to keep behavior predictable
            personTarget = new PersonEntity();
            target.setPerson(personTarget);
        }

        if (isNonEmpty(personSource.getFirstName())) {
            personTarget.setName(personSource.getFirstName());
        }
        if (isNonEmpty(personSource.getMiddleName())) {
            personTarget.setMiddleName(personSource.getMiddleName());
        }
        if (isNonEmpty(personSource.getLastName())) {
            personTarget.setLastName(personSource.getLastName());
        }
        if (isNonEmpty(personSource.getGender())) {
            personTarget.setGender(personSource.getGender());
        }
        if (Objects.nonNull(personSource.getBirthdate())) {
            personTarget.setBirthdate(personSource.getBirthdate());
        }

        // Update contacts if provided - convert POJOs to entities
        if (Objects.nonNull(personSource.getContacts()) && !personSource.getContacts().isEmpty()) {
            var contactEntities = convertContacts(personSource.getContacts(), personTarget);
            personTarget.setContacts(contactEntities);
        }
    }

    private boolean isNonEmpty(String s) {
        return Objects.nonNull(s) && !s.isEmpty();
    }

    /// Converts Contact POJOs to ContactEntity objects.
    /// Handles cases where only ContactType ID is provided (common in updates).
    ///
    /// @param contacts the list of Contact POJOs
    /// @param personEntity the person entity to link contacts to
    /// @return the list of ContactEntity objects
    private List<ContactEntity> convertContacts(List<Contact> contacts, PersonEntity personEntity) {
        if (contacts == null || contacts.isEmpty()) {
            return Collections.emptyList();
        }
        List<ContactEntity> contactEntities = new ArrayList<>(contacts.size());
        for (var contact : contacts) {
            ContactEntity contactEntity = contactMapper.toSource(contact);
            contactEntity.setPerson(personEntity);
            extracted(contact, contactEntity);

            contactEntities.add(contactEntity);
        }
        return contactEntities;
    }

    private void extracted(Contact contact, ContactEntity contactEntity) {
        // Handle contact type - only ID is required for JPA relationship
        if (contact.getContactType() != null && contact.getContactType().getId() != null) {
            ContactTypeEntity contactTypeEntity = contactTypeMapper.toSource(contact.getContactType());

            // Only set other fields if they are provided (not null)
            if (isNonEmpty(contact.getContactType().getName())) {
                contactTypeEntity.setName(contact.getContactType().getName());
            }
            if (isNonEmpty(contact.getContactType().getDescription())) {
                contactTypeEntity.setDescription(contact.getContactType().getDescription());
            }
            if (Objects.nonNull(contact.getContactType().getActive())) {
                contactTypeEntity.setActive(contact.getContactType().getActive());
            }
            contactEntity.setContactType(contactTypeEntity);
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

        // Delegate to UserApplicationRoleService to create and link application-role-user
        try {
            var userTO = new UserTO();
            userTO.setId(userEntity.getId());

            // Set application
            var application = new Application();
            application.setId(userCreateRequest.applicationId());
            userTO.setApplications(Set.of(application));

            // Set role
            var role = new Role();
            role.setId(userCreateRequest.roleId());
            userTO.setRoles(Set.of(role));

            // Refresh role/application linkage
            userApplicationRoleService.refreshRoleByApplication(userEntity, userTO);
        } catch (StandardException ex) {
            LOGGER.error("Error linking application and role to user", ex);
            return ResponseEntity.badRequest().header(HttpHeaders.WARNING, "Application or Role not found").build();
        }

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

        // Use applicationService instead of repository
        var applicationResponse = applicationService.find(applicationId);
        if (applicationResponse.getStatusCode().isError() || applicationResponse.getBody() == null) {
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
