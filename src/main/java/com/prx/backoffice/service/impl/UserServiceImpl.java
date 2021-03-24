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

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import com.prx.backoffice.enums.keys.UserMessageKey;
import com.prx.backoffice.mapper.UserMapper;
import com.prx.backoffice.service.PersonService;
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
	private final PersonService personService;
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
			messageActivity.setCode(UserMessageKey.USER_NOT_FOUND.getCode());
			messageActivity.setMessage(messageUtil.getSinDatos());
			LOGGER.warn(UserMessageKey.USER_NOT_FOUND.getStatus());
		} else {
			messageActivity.setObjectResponse(userMapper.toTarget(userEntity));
			messageActivity.setCode(UserMessageKey.USER_OK.getCode());
			messageActivity.setMessage(UserMessageKey.USER_OK.getStatus());
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

        if (ValidatorCommonsUtil.esNulo(userEntity)) {
			messageActivity.setObjectResponse(null);
			LOGGER.warn(UserMessageKey.USER_NOT_FOUND.getStatus());
		} else {
			messageActivity.setObjectResponse(userMapper.toTarget(userEntity));
			messageActivity.setCode(UserMessageKey.USER_CREATED.getCode());
			messageActivity.setMessage(UserMessageKey.USER_CREATED.getStatus());
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
			messageActivityResult.setCode(FailCode.UNAUTHORIZED.getCode());
			messageActivityResult.setMessage(FailCode.UNAUTHORIZED.getStatus());
			LOGGER.warn(FailCode.UNAUTHORIZED.getStatus());
		} else {
            final var user = messageActivity.getObjectResponse();
            if (user.isActive()) {
                if (user.getPassword().equals(password)) {
					// Usuario valido y activo
					messageActivityResult.setCode(UserMessageKey.USER_OK.getCode());
					messageActivityResult.setMessage(messageActivity.getMessage());
					LOGGER.info(UserMessageKey.USER_OK.getStatus());
				} else {
					// Clave errada de usuario
					messageActivityResult.setCode(UserMessageKey.USER_PASSWORD_WRONG.getCode());
					messageActivityResult.setMessage(UserMessageKey.USER_PASSWORD_WRONG.getStatus());
					LOGGER.warn(UserMessageKey.USER_PASSWORD_WRONG.getStatus());
				}
            } else {
				// Usuario inactivo
				messageActivityResult.setCode(UserMessageKey.USER_BLOCKED.getCode());
				messageActivityResult.setMessage(UserMessageKey.USER_BLOCKED.getStatus());
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
			listMessageActivity.setCode(UserMessageKey.USER_NOT_FOUND.getCode());
			listMessageActivity.setMessage(UserMessageKey.USER_NOT_FOUND.getStatus());
			LOGGER.warn(UserMessageKey.USER_NOT_FOUND.getStatus());
		} else {
			listMessageActivity
					.setObjectResponse(userEntityList.stream().map(userMapper::toTarget).collect(Collectors.toList()));
			listMessageActivity.setCode(UserMessageKey.USER_OK.getCode());
			listMessageActivity.setMessage(UserMessageKey.USER_OK.getStatus());
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

		try {
			if (ValidatorCommonsUtil.esNulo(messageActivity.getObjectResponse())) {
				user.setPerson(getPerson(user));
				final var userEntity = userMapper.toSource(user);
				userEntity.getUserRol().forEach(userRolEntity -> {
					userRolEntity.setUser(userEntity);
					userRolEntity.setActive(true);
				});
				messageActivity.setObjectResponse(userMapper.toTarget(userRepository.save(userEntity)));
				messageActivity.setCode(UserMessageKey.USER_CREATED.getCode());
				messageActivity.setMessage(UserMessageKey.USER_CREATED.getStatus());
				LOGGER.info(UserMessageKey.USER_CREATED.getStatus());
			}
			else {
				messageActivity.setCode(UserMessageKey.USER_PREVIOUS_EXIST.getCode());
				messageActivity.setMessage(UserMessageKey.USER_PREVIOUS_EXIST.getStatus());
				LOGGER.warn(UserMessageKey.USER_PREVIOUS_EXIST.getStatus());
			}
		}
		catch (Exception ex) {
			LOGGER.error(UserMessageKey.USER_ERROR_CREATED.getStatus(), ex);
			messageActivity.setCode(UserMessageKey.USER_ERROR_CREATED.getCode());
			messageActivity.setMessage(UserMessageKey.USER_ERROR_CREATED.getStatus());
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
			final var messageActivityResult = personService.create(user.getPerson());

			if (Response.Status.CREATED.getStatusCode() == messageActivityResult.getCode()) {
				return messageActivityResult.getObjectResponse();
			}
			else {
				messageActivityPerson.setCode(UserMessageKey.USER_ERROR_CREATED.getCode());
				messageActivityPerson.setMessage(UserMessageKey.USER_ERROR_CREATED.getStatus());
			}
		}

		return messageActivityPerson.getObjectResponse();
	}

//    /**
//     *
//     * @param rolId {@link Long}
//     *
//     * @return {@link Rol}
//     */
//    private Rol getRol(Long rolId){
//        final var rol = new Rol();
//        rol.setId(rolId);
//        MessageActivity<Rol> messageActivity = rolService.find(rol);
//
//
//        return  messageActivity.getObjectResponse();
//    }

}
