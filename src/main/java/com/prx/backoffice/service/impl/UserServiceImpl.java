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
import com.prx.backoffice.mapper.RolMapper;
import com.prx.backoffice.mapper.UserMapper;
import com.prx.backoffice.service.PersonService;
import com.prx.backoffice.service.UserService;
import com.prx.commons.enums.keys.FailCode;
import com.prx.commons.enums.types.MessageType;
import com.prx.commons.pojo.MessageActivity;
import com.prx.commons.pojo.Person;
import com.prx.commons.pojo.User;
import com.prx.commons.util.ValidatorCommonsUtil;
import com.prx.persistence.general.domains.UserEntity;
import com.prx.persistence.general.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Modelo para la gesti&oacute;n de usuarios
 *
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
 * @version 1.0.1.20200904-01, 2019-10-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final PersonService personService;
	private final UserMapper userMapper;
	private final RolMapper rolMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MessageActivity<User> findUserById(Long userId) {
		final var userEntity = userRepository.findById(userId).orElse(new UserEntity());
		if (ValidatorCommonsUtil.esNulo(userEntity.getId())) {
			log.info(UserMessageKey.USER_NOT_FOUND.getStatus() + "| userId:{}", userId);
			return getMessageActivity(null, UserMessageKey.USER_NOT_FOUND);
		} else {
			log.info(UserMessageKey.USER_OK.getStatus());
			return getMessageActivity(userEntity, UserMessageKey.USER_OK);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MessageActivity<User> findUserByAlias(final String alias) {
		final var userEntity = userRepository.findByAlias(alias);
		if (ValidatorCommonsUtil.esNulo(userEntity)) {
			log.warn(UserMessageKey.USER_NOT_FOUND.getStatus() + "| alias:{}", alias);
			return getMessageActivity(null, UserMessageKey.USER_NOT_FOUND);
		} else {
			log.info(UserMessageKey.USER_FOUND.getStatus());
			return getMessageActivity(userEntity, UserMessageKey.USER_FOUND);
		}
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
			log.warn(FailCode.UNAUTHORIZED.getStatus() + "| alias: {}, password {}", alias, password);
		} else {
			final var user = messageActivity.getObjectResponse();
			if (user.isActive()) {
				if (user.getPassword().equals(password)) {
					messageActivityResult.setCode(UserMessageKey.USER_OK.getCode());
					messageActivityResult.setMessage(messageActivity.getMessage());
					log.info(UserMessageKey.USER_OK.getStatus());
				} else {
					messageActivityResult.setCode(UserMessageKey.USER_PASSWORD_WRONG.getCode());
					messageActivityResult.setMessage(UserMessageKey.USER_PASSWORD_WRONG.getStatus());
					log.warn(UserMessageKey.USER_PASSWORD_WRONG.getStatus());
				}
			} else {
				messageActivityResult.setCode(UserMessageKey.USER_BLOCKED.getCode());
				messageActivityResult.setMessage(UserMessageKey.USER_BLOCKED.getStatus());
				log.warn(UserMessageKey.USER_BLOCKED.getStatus());
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
			log.warn(UserMessageKey.USER_NOT_FOUND.getStatus());
		} else {
			listMessageActivity
					.setObjectResponse(userEntityList.stream().map(userMapper::toTarget).collect(Collectors.toList()));
			listMessageActivity.setCode(UserMessageKey.USER_OK.getCode());
			listMessageActivity.setMessage(UserMessageKey.USER_OK.getStatus());
			log.info(UserMessageKey.USER_OK.getStatus());
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
				log.info(UserMessageKey.USER_CREATED.getStatus());
				return getMessageActivity(userRepository.save(userEntity), UserMessageKey.USER_CREATED);
			} else {
				log.warn(UserMessageKey.USER_PREVIOUS_EXIST.getStatus() + "| {}", user.toString());
				return getMessageActivity(null, UserMessageKey.USER_PREVIOUS_EXIST);
			}
		} catch (Exception ex) {
			log.error(UserMessageKey.USER_ERROR_CREATED.getStatus()+ "| {}", user.toString(), ex);
			return getMessageActivity(null, UserMessageKey.USER_ERROR_CREATED);
		}
	}

	/**
	 * Obtiene el objeto persona asociado al usuario o lo crea en caso de existir.
	 *
	 * @param user {@link User}
	 *
	 * @return {@link Person}
	 */
	private Person getPerson(User user) {
		final var messageActivityPerson = personService.find(user.getPerson());
		if (ValidatorCommonsUtil.esNulo(messageActivityPerson.getObjectResponse())) {
			final var messageActivityResult = personService.create(user.getPerson());
			if (Response.Status.CREATED.getStatusCode() == messageActivityResult.getCode()) {
				return messageActivityResult.getObjectResponse();
			} else {
				messageActivityPerson.setCode(UserMessageKey.USER_ERROR_CREATED.getCode());
				messageActivityPerson.setMessage(UserMessageKey.USER_ERROR_CREATED.getStatus());
			}
		}
		return messageActivityPerson.getObjectResponse();
	}

	/**
	 * Obtiene la respuesta de tipo {@link MessageActivity}<{@link User}>
	 *
	 * @param userEntity {@link UserEntity}
	 * @param messageType {@link MessageType}
	 * @return Objeto de tipo {@link MessageActivity}<{@link User}>
	 */
	private MessageActivity<User> getMessageActivity(UserEntity userEntity, MessageType messageType) {
		final var messageActivity = new MessageActivity<User>();
		if (null != userEntity){
			final var user = userMapper.toTarget(userEntity);
			user.setRoles(new ArrayList<>());
			if(null != userEntity.getUserRol()) {
				userEntity.getUserRol().forEach(userRolEntity -> user.getRoles().add(rolMapper.userRoltoRol(userRolEntity)));
			}
			messageActivity.setObjectResponse(user);
		}
		messageActivity.setCode(messageType.getCode());
		messageActivity.setMessage(messageType.getStatus());
		return messageActivity;
	}
}
