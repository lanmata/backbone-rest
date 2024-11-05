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
package com.prx.backoffice.v1.users.service;

import com.prx.backoffice.enums.keys.RolMessageKey;
import com.prx.backoffice.enums.keys.UserMessageKey;
import com.prx.backoffice.v1.people.service.PersonService;
import com.prx.backoffice.v1.roles.mapper.RoleMapper;
import com.prx.backoffice.v1.roles.service.RoleService;
import com.prx.backoffice.v1.users.api.to.UserTO;
import com.prx.backoffice.v1.users.mapper.UserMapper;
import com.prx.commons.pojo.Person;
import com.prx.commons.pojo.User;
import com.prx.persistence.general.domains.UserRoleEntity;
import com.prx.persistence.general.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.prx.backoffice.util.MessageUtil.MESSAGE_HEADER_STR;

/**
 * Modelo para la gesti&oacute;n de usuarios
 *
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
 * @version 1.0.1.20200904-01, 2019-10-14
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PersonService personService;
    private final RoleService roleService;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    public UserServiceImpl(UserRepository userRepository, PersonService personService, RoleService roleService,
                           UserMapper userMapper, RoleMapper roleMapper) {
        this.userRepository = userRepository;
        this.personService = personService;
        this.roleService = roleService;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    public ResponseEntity<UserTO> update(String userId, UserTO user) {
        ResponseEntity<UserTO> responseEntity;
        if (Objects.isNull(userId) || userId.isEmpty()) {
            return ResponseEntity.badRequest().header(HttpHeaders.WARNING, "User ID empty or null").build();
        }
        final var userResponseEntity = findUserById(userId);
        try {
            if (HttpStatus.OK.equals(userResponseEntity.getStatusCode())) {
                final var responseEntityPerson = getPerson(user);
                if (HttpStatus.OK.equals(responseEntityPerson.getStatusCode())) {
                    user.setPerson(responseEntityPerson.getBody());
                    final var userEntity = userMapper.toSource(user);
                    userEntity.setId(UUID.fromString(userId));
                    if (null != userEntity.getUserRole()) {
                        userEntity.getUserRole().forEach(userRoleEntity -> {
                            userRoleEntity.setUser(userEntity);
                            userRoleEntity.setActive(true);
                        });
                    }
                    var result = userRepository.save(userEntity);
                    responseEntity = new ResponseEntity<>(userMapper.toTarget(result), HttpStatus.OK);
                } else {
                    responseEntity = ResponseEntity.badRequest()
                            .header(HttpHeaders.WARNING, "The user requested doesn't have a person associated.")
                            .build();
                }
            } else {
                responseEntity = ResponseEntity.badRequest().header(HttpHeaders.WARNING, "Invalid user").build();
            }
        } catch (Exception ex) {
            LOGGER.error(UserMessageKey.USER_ERROR_CREATED.getStatus() + "| {}", user, ex);
            responseEntity = ResponseEntity.unprocessableEntity().build();
        }
        LOGGER.info(responseEntity.getStatusCode().toString());
        return responseEntity;
    }

    @Override
    public ResponseEntity<UserTO> delete(String userId, UserTO user) {
        return null;
    }

    @Override
    public ResponseEntity<UserTO> find(String id) {
        return null;
    }

    @Override
    public ResponseEntity<List<UserTO>> list(String... id) {
        return null;
    }

    @Override
    public ResponseEntity<String> aliasValidate(String alias) {
        final var user = findByAlias(alias);
        if (Objects.isNull(user)) {
            return ResponseEntity.status(HttpStatus.OK).header(MESSAGE_HEADER_STR, "Alias available.").build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).header(MESSAGE_HEADER_STR, "Alias is not available.").build();
        }
    }

    @Override
    public ResponseEntity<UserTO> findUserById(String userId) {
        ResponseEntity<UserTO> responseEntity;
        final var optionalUser = userRepository.findById(UUID.fromString(userId));
        responseEntity = optionalUser.map(userEntity ->
                new ResponseEntity<>(userMapper.toTarget(userEntity), HttpStatus.OK)).orElseGet(() ->
                ResponseEntity.notFound().build());
        LOGGER.info(responseEntity.getStatusCode() + "| userId:{}", userId);
        return responseEntity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<UserTO> findUserByAlias(final String alias) {
        var userTO = findByAlias(alias);
        if (Objects.isNull(userTO)) {
            return ResponseEntity.notFound().build();
        } else {
            return new ResponseEntity<>(userTO, HttpStatus.OK);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<String> access(String alias, String password) {
        final ResponseEntity<UserTO> responseEntity = findUserByAlias(alias);
        ResponseEntity<String> responseResult;
        if (HttpStatus.OK.value() == responseEntity.getStatusCode().value()) {
            final var user = responseEntity.getBody();
            if (Objects.nonNull(user) && user.isActive()) {
                responseResult = user.getPassword().equals(password) ?
                        ResponseEntity.accepted().build() : ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } else {
                responseResult = new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } else {
            responseResult = ResponseEntity.notFound().build();
        }
        LOGGER.info(responseResult.getStatusCode().toString());
        return responseResult;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<UserTO>> findAll() {
        final var userEntityList = userRepository.findAll();
        if (userEntityList.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return new ResponseEntity<>(userEntityList.stream()
                    .map(userMapper::toTarget).collect(Collectors.toList()), HttpStatus.OK);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<UserTO> create(UserTO user) {
        if (null == user) {
            return ResponseEntity.badRequest().build();
        } else if (user.getAlias().isBlank()) {
            return ResponseEntity.badRequest().header(HttpHeaders.WARNING, "username is required").build();
        } else if (user.getPassword().isBlank()) {
            return ResponseEntity.badRequest().header(HttpHeaders.WARNING, "password is required").build();
        } else if (null == user.getRoles() || user.getRoles().isEmpty()) {
            return ResponseEntity.badRequest().header(HttpHeaders.WARNING, "Role is required").build();
        } else if (findUserByAlias(user.getAlias()).getStatusCode().equals(HttpStatus.OK)) {
            return ResponseEntity.badRequest().header(HttpHeaders.WARNING, "User previously exist.").build();
        }
        var personResponse = personService.create(user.getPerson());
        if (personResponse.getStatusCode().equals(HttpStatus.CREATED)) {
            user.setPerson(personResponse.getBody());
            return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toTarget(userRepository.save(userMapper.toSource(user))));
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<UserTO> unlink(String userId, String rolId) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<UserTO> link(String userId, String roleId) {
        ResponseEntity<UserTO> responseEntity = null;
        final var optionalUserEntity = userRepository.findById(UUID.fromString(userId));
        if (optionalUserEntity.isPresent()) {
            final var userEntity = optionalUserEntity.get();
            for (UserRoleEntity userRolEntity : userEntity.getUserRole()) {
                if (userRolEntity.getRole().getId().equals(roleId)) {
                    responseEntity = new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
                    return responseEntity;
                }
            }
            final var messageActivityRole = roleService.find(roleId);
            if (RolMessageKey.ROL_OK.getCode() == messageActivityRole.getStatusCode().value()) {
                if (null != userEntity.getUserRole()) {
                    final var userRoleEntity = new UserRoleEntity();
                    final var roleEntity = roleMapper.toSource(messageActivityRole.getBody());
                    userRoleEntity.setUser(userEntity);
                    userRoleEntity.setRole(roleEntity);
                    userRoleEntity.setActive(true);
                    userEntity.getUserRole().add(userRoleEntity);
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

    /**
     * Obtiene el objeto persona asociado al usuario o lo crea en caso de existir.
     *
     * @param user {@link User}
     * @return {@link Person}
     */
    private ResponseEntity<Person> getPerson(UserTO user) {
        final var responseEntity = personService.find(user.getPerson().getId());
        if (HttpStatus.FOUND.value() == responseEntity.getStatusCode().value()) {
            return personService.create(user.getPerson());
        }
        return responseEntity;
    }

    private UserTO findByAlias(String alias) {
        final var userEntity = userRepository.findByAlias(alias);
        return Objects.nonNull(userEntity) ? userMapper.toTarget(userEntity) : null;
    }
}
