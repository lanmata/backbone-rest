/*
 *
 *  * @(#)UserServiceImpl.java.
 *  *
 *  * Copyright (c) Luis Antonio Mata Mata. All rights reserved.
 *  *
 *  * All rights to this product are owned by Luis Antonio Mata Mata and may only
 *  * be used under the terms of its associated license document. You may NOT
 *  * copy, modify, sublicense, or distribute this source file or portions of
 *  * it unless previously authorized in writing by Luis Antonio Mata Mata.
 *  * In any event, this notice and the above copyright must always be included
 *  * verbatim with this file.
 *
 */
package com.prx.backoffice.service.impl;

import com.prx.backoffice.enums.keys.UserMessageKey;
import com.prx.backoffice.mapper.UserMapper;
import com.prx.backoffice.service.UserService;
import com.prx.backoffice.util.MessageUtil;
import com.prx.commons.enums.keys.FailCode;
import com.prx.commons.pojo.MessageActivity;
import com.prx.commons.pojo.Person;
import com.prx.commons.pojo.User;
import com.prx.commons.util.ValidatorCommonsUtil;
import com.prx.persistence.general.domains.UserEntity;
import com.prx.persistence.general.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Modelo para la gesti&oacute;n de usuarios
 *
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
 * @version 1.0.1.20200904-01, 2019-10-14
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final PersonServiceImpl personService;
    private final MessageUtil messageUtil;
    private final UserMapper userMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageActivity<User> findUserById(Long userId) {
        final var messageActivity = new MessageActivity<User>();
        final var userEntity = userRepository.findById(userId).orElse(new UserEntity());

        if (ValidatorCommonsUtil.esNulo(userEntity.getId())) {
            messageActivity.getMessages().put(UserMessageKey.USER_NOT_FOUND.getCode(), messageUtil.getSinDatos());
            LOGGER.warn(UserMessageKey.USER_NOT_FOUND.getStatus());
        } else {
            messageActivity.setObjectResponse(userMapper.toTarget(userEntity));
            messageActivity.getMessages().put(UserMessageKey.USER_OK.getCode(), UserMessageKey.USER_OK.getStatus());
            LOGGER.info(UserMessageKey.USER_OK.getStatus());
        }

        return messageActivity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageActivity<User> findUserByAlias(final String alias) {
        final var userEntity = userRepository.findByAlias(alias);
        final var messageActivity = new MessageActivity<User>();
        User user;

        if (ValidatorCommonsUtil.esNulo(userEntity)) {
            messageActivity.getMessages().put(UserMessageKey.USER_CREATED.getCode(),
                UserMessageKey.USER_CREATED.getStatus());
            LOGGER.warn(UserMessageKey.USER_CREATED.getStatus());
        } else {
            user = userMapper.toTarget(userEntity);
            messageActivity.setObjectResponse(user);
            messageActivity.getMessages().put(UserMessageKey.USER_CREATED.getCode(),
                UserMessageKey.USER_CREATED.getStatus());
            LOGGER.info(UserMessageKey.USER_CREATED.getStatus());
        }
        return messageActivity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageActivity<String> access(String alias, String password) {
        final var messageActivity = findUserByAlias(alias);
        final var messageActivityResult = new MessageActivity<String>();

        if (ValidatorCommonsUtil.esNulo(messageActivity)) {
            messageActivityResult.getMessages().put(FailCode.UNAUTHORIZED.getCode(), FailCode.FORBIDDEN.getStatus());
            LOGGER.warn(FailCode.UNAUTHORIZED.getStatus());
        } else {
            final var user = messageActivity.getObjectResponse();
            if (user.isActive()) {
                if (user.getPassword().equals(password)) {
                    // Usuario valido y activo
                    messageActivityResult.setObjectResponse(UserMessageKey.USER_OK.getStatus());
                    messageActivityResult.setMessages(messageActivity.getMessages());
                    LOGGER.info(UserMessageKey.USER_OK.getStatus());
                } else {
                    // Clave errada de usuario
                    messageActivityResult.getMessages().put(UserMessageKey.USER_PASSWORD_WRONG.getCode(),
                        UserMessageKey.USER_PASSWORD_WRONG.getStatus());
                    LOGGER.warn(UserMessageKey.USER_PASSWORD_WRONG.getStatus());
                }
            } else {
                // Usuario inactivo
                messageActivityResult.getMessages().put(UserMessageKey.USER_BLOCKED.getCode(),
                    UserMessageKey.USER_BLOCKED.getStatus());
                LOGGER.warn(UserMessageKey.USER_BLOCKED.getStatus());
            }
        }

        return messageActivityResult;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageActivity<List<User>> findAll() {
        final var userEntityList = userRepository.findAll();
        final var listMessageActivity = new MessageActivity<List<User>>();

        if (userEntityList.isEmpty()) {
            listMessageActivity.getMessages().put(UserMessageKey.USER_NOT_FOUND.getCode(),
                UserMessageKey.USER_NOT_FOUND.getStatus());
            LOGGER.warn(UserMessageKey.USER_NOT_FOUND.getStatus());
        } else {
            listMessageActivity.setObjectResponse(userEntityList.stream().map(userMapper::toTarget).collect(Collectors.toList()));
            listMessageActivity.getMessages().put(UserMessageKey.USER_OK.getCode(), UserMessageKey.USER_OK.getStatus());
            LOGGER.info(UserMessageKey.USER_OK.getStatus());
        }

        return listMessageActivity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageActivity<User> create(User user) {
        final var messageActivity = findUserByAlias(user.getAlias());

        if (ValidatorCommonsUtil.esNulo(messageActivity.getObjectResponse())) {
            user.setPerson(getPerson(user));
            messageActivity.setObjectResponse(userMapper.toTarget(userRepository.save(userMapper.toSource(user))));
            messageActivity.getMessages().put(UserMessageKey.USER_CREATED.getCode(),
                UserMessageKey.USER_CREATED.getStatus());
            LOGGER.info(UserMessageKey.USER_CREATED.getStatus());
        } else {
            messageActivity.getMessages().put(UserMessageKey.USER_PREVIOUS_EXIST.getCode(),
                UserMessageKey.USER_PREVIOUS_EXIST.getStatus());
            LOGGER.warn(UserMessageKey.USER_PREVIOUS_EXIST.getStatus());
        }

        return messageActivity;
    }

    /**
     * Obtiene el objeto persona asociado al usuario o lo crea en caso de existir.
     *
     * @param user {@link User}
     *
     * @return {@link Person}
     */
    private Person getPerson(User user) {
        MessageActivity<Person> messageActivityPerson = personService.find(user.getPerson());
        if (ValidatorCommonsUtil.esNulo(messageActivityPerson.getObjectResponse())) {
            return personService.create(user.getPerson()).getObjectResponse();
        }
        return messageActivityPerson.getObjectResponse();
    }

}
