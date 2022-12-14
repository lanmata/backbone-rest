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
package com.prx.backoffice.v1.user.service;

import com.prx.backoffice.enums.keys.RolMessageKey;
import com.prx.backoffice.enums.keys.UserMessageKey;
import com.prx.backoffice.v1.person.service.PersonService;
import com.prx.backoffice.v1.role.mapper.RoleMapper;
import com.prx.backoffice.v1.role.service.RoleService;
import com.prx.backoffice.v1.user.api.to.UserTO;
import com.prx.backoffice.v1.user.mapper.UserMapper;
import com.prx.commons.pojo.Person;
import com.prx.commons.pojo.User;
import com.prx.commons.util.ValidatorCommonsUtil;
import com.prx.persistence.general.domains.UserRoleEntity;
import com.prx.persistence.general.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
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
	private final RoleService roleService;
	private final UserMapper userMapper;
	private final RoleMapper roleMapper;

	@Override
	public ResponseEntity<UserTO> update(Long userId, UserTO user) {
		ResponseEntity<UserTO> responseEntity;
		final var userResponseEntity = findUserByAlias(user.getAlias());
		try {
			if (HttpStatus.OK.value() == userResponseEntity.getStatusCodeValue()) {
				final var responseEntityPerson = getPerson(user);
				if(HttpStatus.OK.value() == responseEntityPerson.getStatusCodeValue()) {
					user.setPerson(responseEntityPerson.getBody());
					final var userEntity = userMapper.toSource(user);
					if(null != userEntity.getUserRole()) {
						userEntity.getUserRole().forEach(userRoleEntity -> {
							userRoleEntity.setUser(userEntity);
							userRoleEntity.setActive(true);
						});
					}
					var result = userRepository.save(userEntity);
					responseEntity = new ResponseEntity<>(userMapper.toTarget(result), HttpStatus.OK);
				} else {
					responseEntity = ResponseEntity.unprocessableEntity().body(user);
				}
			} else {
					responseEntity = ResponseEntity.unprocessableEntity().body(user);
			}
		} catch (Exception ex) {
			log.error(UserMessageKey.USER_ERROR_CREATED.getStatus()+ "| {}", user, ex);
			responseEntity = ResponseEntity.unprocessableEntity().build();
		}
		log.info(responseEntity.getStatusCode().toString());
		return responseEntity;
	}

	@Override
	public ResponseEntity<UserTO> delete(Long userId, UserTO user) {
		return null;
	}

	@Override
	public ResponseEntity<UserTO> find(Long id) {
		return null;
	}

	@Override
	public ResponseEntity<List<UserTO>> list(Long... id) {
		return null;
	}



	@Override
	public ResponseEntity<UserTO> findUserById(Long userId) {
		ResponseEntity<UserTO> responseEntity;
		final var optionalUser = userRepository.findById(userId);
		responseEntity = optionalUser.map(userEntity ->
				new ResponseEntity<>(userMapper.toTarget(userEntity), HttpStatus.OK)).orElseGet(() ->
				ResponseEntity.notFound().build());
		log.info(responseEntity.getStatusCode() + "| userId:{}", userId);
		return responseEntity;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseEntity<UserTO> findUserByAlias(final String alias) {
		final var userEntity = userRepository.findByAlias(alias);
		ResponseEntity<UserTO> responseEntity;
		responseEntity = ValidatorCommonsUtil.esNulo(userEntity) ?
				 ResponseEntity.notFound().build() : new ResponseEntity<>(userMapper.toTarget(userEntity), HttpStatus.OK);
		log.info("{}| alias:{}", responseEntity.getStatusCode(), alias);
		return responseEntity;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseEntity<String> access(String alias, String password) {
		final ResponseEntity<UserTO> responseEntity = findUserByAlias(alias);
		ResponseEntity<String> responseResult;
		if (HttpStatus.OK.value() == responseEntity.getStatusCodeValue()) {
			final var user = responseEntity.getBody();
			if (null != user && user.isActive()) {
				responseResult = user.getPassword().equals(password) ?
						ResponseEntity.accepted().build() : ResponseEntity.status(HttpStatus.FORBIDDEN).build();
			} else {
				responseResult = new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
		} else {
			responseResult = ResponseEntity.notFound().build();
		}
		log.info(responseResult.getStatusCode().toString());
		return responseResult;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseEntity<List<UserTO>> findAll() {
		ResponseEntity<List<UserTO>> listResponseEntity;
		final var userEntityList = userRepository.findAll();
		if (userEntityList.isEmpty()) {
			listResponseEntity = ResponseEntity.notFound().build();
		} else {
			listResponseEntity = new ResponseEntity<>(userEntityList.stream()
					.map(userMapper::toTarget).collect(Collectors.toList()), HttpStatus.OK);
		}
		log.info(listResponseEntity.getStatusCode().toString());
		return listResponseEntity;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	public ResponseEntity<UserTO> create(UserTO user) {
		if(null == user) {
			return ResponseEntity.badRequest().build();
		} else if(user.getAlias().isBlank()) {
			return ResponseEntity.badRequest().header(HttpHeaders.WARNING, "username is required").build();
		} else if(user.getPassword().isBlank()) {
			return ResponseEntity.badRequest().header(HttpHeaders.WARNING, "password is required").build();
		} else if(null == user.getRoles() || user.getRoles().isEmpty()) {
			return ResponseEntity.badRequest().header(HttpHeaders.WARNING, "Role is required").build();
		} else if(findUserByAlias(user.getAlias()).getStatusCode().equals(HttpStatus.OK)) {
			return ResponseEntity.badRequest().header(HttpHeaders.WARNING, "User previously exist.").build();
		}
		var personResponse = personService.create(user.getPerson());
		if(personResponse.getStatusCode().equals(HttpStatus.CREATED)) {
			user.setPerson(personResponse.getBody());
			return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toTarget(userRepository.save(userMapper.toSource(user))));
		}
		return ResponseEntity.badRequest().build();
	}

	/**{@inheritDoc}*/
	@Override
	public ResponseEntity<UserTO> unlink(Long userId, Long rolId) {
		throw new UnsupportedOperationException();
	}

	/**{@inheritDoc}*/
	@Override
	public ResponseEntity<UserTO> link(Long userId, Long roleId) {
		ResponseEntity<UserTO> responseEntity = null;
		final var optionalUserEntity = userRepository.findById(userId);
		if (optionalUserEntity.isPresent()) {
			final var userEntity = optionalUserEntity.get();
			for (UserRoleEntity userRolEntity: userEntity.getUserRole()){
				if(userRolEntity.getRole().getId().equals(roleId)) {
					responseEntity = new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
					return responseEntity;
				}
			}
			final var messageActivityRole = roleService.find(roleId);
			if(RolMessageKey.ROL_OK.getCode() == messageActivityRole.getStatusCodeValue()){
				if(null != userEntity.getUserRole()){
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
	 *
	 * @return {@link Person}
	 */
	private ResponseEntity<Person> getPerson(UserTO user) {
		final var responseEntity = personService.find(user.getPerson().getId());
		if (HttpStatus.FOUND.value() == responseEntity.getStatusCodeValue()) {
			return personService.create(user.getPerson());
		}
		return responseEntity;
	}
}
