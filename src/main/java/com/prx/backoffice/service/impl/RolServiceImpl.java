/*
 * @(#)RolServiceImpl.java.
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
package com.prx.backoffice.service.impl;

import com.prx.backoffice.enums.keys.RolFeatureMessageKey;
import com.prx.backoffice.enums.keys.RolMessageKey;
import com.prx.backoffice.mapper.FeatureMapper;
import com.prx.backoffice.mapper.RolMapper;
import com.prx.backoffice.service.FeatureService;
import com.prx.backoffice.service.RolService;
import com.prx.commons.pojo.Feature;
import com.prx.commons.pojo.MessageActivity;
import com.prx.commons.pojo.Rol;
import com.prx.persistence.general.domains.RolEntity;
import com.prx.persistence.general.domains.RolFeatureEntity;
import com.prx.persistence.general.repositories.RolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

import static com.prx.commons.util.ValidatorCommonsUtil.esNulo;

/**
 * RolServiceImpl.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 11-02-2021
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RolServiceImpl implements RolService {

	private final RolRepository rolRepository;
	private final FeatureService featureService;
	private final FeatureMapper featureMapper;
	private final RolMapper rolMapper;

	/** {@inheritDoc} */
	@Override
	public MessageActivity<Rol> find(Integer rolId) {
		final var messageActivity = new MessageActivity<Rol>();
		log.info("Inicia llamado al repositorio de Rol para busqueda por id");
		final var rolEntity = rolRepository.findById(rolId).orElse(new RolEntity());
		if (esNulo(rolEntity.getId())) {
			messageActivity.setCode(Response.Status.NOT_FOUND.getStatusCode());
			messageActivity.setMessage(Response.Status.NOT_FOUND.toString().concat(". Rol id: " + rolId));
			log.info(Response.Status.NOT_FOUND.getStatusCode() + "| rolId {}", rolId);
		} else {
			messageActivity.setObjectResponse(rolMapper.toTarget(rolEntity));
			messageActivity.setCode(Response.Status.OK.getStatusCode());
			messageActivity.setMessage(Response.Status.OK.getReasonPhrase());
			log.info(Response.Status.OK.getReasonPhrase());
		}
		return messageActivity;
	}

	/** {@inheritDoc} */
	@Override
	public MessageActivity<Rol> create(Rol rol) {
		log.info("Inicia creación de Rol.");
		final var messageActivity = new MessageActivity<Rol>();
		messageActivity.setObjectResponse(rolMapper.toTarget(rolRepository.save(rolMapper.toSource(rol))));
		messageActivity.setCode(RolMessageKey.ROL_CREATED.getCode());
		messageActivity.setMessage(RolMessageKey.ROL_CREATED.getStatus());
		log.info(RolMessageKey.ROL_CREATED.getStatus() + "| Rol {}.", rol.toString());
		return messageActivity;
	}


	/** {@inheritDoc} */
	@Override
	public MessageActivity<Rol> link(Integer rolId, List<Long> featureIdList) {
		log.info("Inicia vinculación de Rol con uno o mas features.");
		final var messageActivityResult = new MessageActivity<Rol>();
		final var rolEntity = rolRepository.findById(rolId).orElseThrow();
		//Filtra los feature eliminando los item que se encuentren vinculados previamente con el rol.
		final var featureLinkNew = featureIdList.stream().filter(featureId ->
				validFeatureInRol(rolEntity.getRolFeatures(), featureId)).collect(Collectors.toList());
		//Obtiene los feature que seran luego vinculados al rol
		final var messageActivityFeatures = featureService.list(featureLinkNew, false);

		if (null == rolEntity.getRolFeatures()) {
			log.info("El Rol no tiene features previamente vinculados.");
			rolEntity.setRolFeatures(new HashSet<>());
		}
		messageActivityFeatures.getObjectResponse().forEach(feature -> {
			RolFeatureEntity rolFeatureEntity = new RolFeatureEntity();
			rolFeatureEntity.setRol(rolEntity);
			rolFeatureEntity.setFeature(featureMapper.toSource(feature));
			rolFeatureEntity.setActive(true);
			rolEntity.getRolFeatures().add(rolFeatureEntity);
		});
		//TODO - Falta caso alterno concerniente al fallo de registro al vincular un rol con uno o mas feature
		messageActivityResult.setObjectResponse(rolMapper.toTarget(rolRepository.save(rolEntity)));
		messageActivityResult.setCode(RolFeatureMessageKey.ROL_FEATURE_CREATED.getCode());
		messageActivityResult.setMessage(RolFeatureMessageKey.ROL_FEATURE_CREATED.getStatus());
		log.info("Se ha vinculado el Rol con los features indicados.");
		return messageActivityResult;
	}

	/** {@inheritDoc} */
	@Override
	public MessageActivity<Rol> update(Rol rol) {
		log.info("Inicia la actualización del rol.");
		final var messageActivityResult = new MessageActivity<Rol>();
		log.info("Se busca el rol solicitado para actualizar los datos.");
		final var optionRolEntity = rolRepository.findById(rol.getId());
		if (optionRolEntity.isPresent()) {
			final var rolEntity = optionRolEntity.get();
			rolEntity.setName(rol.getName());
			rolEntity.setDescription(rol.getDescription());
			rolEntity.setActive(rol.getActive());
			messageActivityResult.setObjectResponse(rolMapper.toTarget(rolRepository.save(rolEntity)));
			messageActivityResult.setCode(RolMessageKey.ROL_UPDATE.getCode());
			messageActivityResult.setMessage(RolMessageKey.ROL_UPDATE.getStatus());
			log.info(RolMessageKey.ROL_UPDATE.getStatus() + "| Rol {}", rol.toString());
		} else {
			messageActivityResult.setCode(RolMessageKey.ROL_NOT_FOUND.getCode());
			messageActivityResult.setMessage(RolMessageKey.ROL_NOT_FOUND.getStatus());
			log.info(RolMessageKey.ROL_NOT_FOUND.getStatus() + "| Rol {}", rol.toString());
		}
		return messageActivityResult;
	}

	/** {@inheritDoc} */
	@Override
	public MessageActivity<List<Rol>> list(boolean includeInactive, List<Integer> roles) {
		log.info("Inicia la búsqueda del rol en base a los id {}.", roles.toArray());
		final var messageActivity = new MessageActivity<List<Rol>>();
		final var rolList = new ArrayList<Rol>();
		Optional<List<RolEntity>> rolListResult;
		if (null == roles || roles.isEmpty()) {
			rolListResult = findAll(includeInactive);
		}
		else {
			rolListResult = findAll(includeInactive, roles);
		}
		rolListResult
				.ifPresent(rolEntities -> rolEntities.forEach(rolEntity -> rolList.add(rolMapper.toTarget(rolEntity))));
		messageActivity.setObjectResponse(rolList);
		if (null == messageActivity.getObjectResponse() || messageActivity.getObjectResponse().isEmpty()) {
			messageActivity.setCode(Response.Status.NOT_FOUND.getStatusCode());
			messageActivity.setMessage(Response.Status.NOT_FOUND.getReasonPhrase());
			log.info(Response.Status.NOT_FOUND.getReasonPhrase() + "| roles {}", (null == roles) ? "": roles.toString());
		}
		else {
			messageActivity.setCode(Response.Status.OK.getStatusCode());
			messageActivity.setMessage(Response.Status.OK.getReasonPhrase());
			log.info(Response.Status.OK.toString());
		}
		return messageActivity;
	}

	/** {@inheritDoc} */
	@Override
	public MessageActivity<Rol> unlink(Integer rolId, List<Long> featureIdList) {
		log.info("Inicia la desvinculación del rol y los features {}", featureIdList.toArray());
		final var messageActivityResult = new MessageActivity<Rol>();
		final RolEntity rolEntity = rolRepository.findById(rolId).orElseThrow();
		rolEntity.getRolFeatures().forEach(rolFeatureEntity -> rolFeatureEntity.setActive(false));
		rolRepository.save(rolEntity);
		messageActivityResult.setMessage(Response.Status.OK.getReasonPhrase());
		messageActivityResult.setCode(Response.Status.OK.getStatusCode());
		messageActivityResult.setObjectResponse(rolMapper.toTarget(rolEntity));
		//TODO - Falta cubrir casos bordes para el metodo unlink
		log.info(Response.Status.OK.getReasonPhrase());
		return messageActivityResult;
	}

	/** {@inheritDoc} */
	@Override
	public MessageActivity<List<Rol>> list(long userId) {
		final var messageActivityResult = new MessageActivity<List<Rol>>();
		log.info("Inicia la búsqueda de rol paa el usuario con id {}", userId);
		final var result = rolRepository.findAllByUserId(userId);
		final var rolList = new ArrayList<Rol>();
		if (result.isPresent() && !result.get().isEmpty()){
			result.get().forEach(rolEntity -> rolList.add(rolMapper.toTarget(rolEntity)));
			messageActivityResult.setObjectResponse(rolList);
			messageActivityResult.setCode(RolMessageKey.ROL_OK.getCode());
			messageActivityResult.setMessage(RolMessageKey.ROL_OK.getStatus());
			log.info(RolMessageKey.ROL_OK.getStatus());
		} else {
			messageActivityResult.setCode(RolMessageKey.ROL_NOT_FOUND.getCode());
			messageActivityResult.setMessage(RolMessageKey.ROL_NOT_FOUND.getStatus());
			log.info(RolMessageKey.ROL_NOT_FOUND.getStatus() + "| userId {}", userId);
		}
		return messageActivityResult;
	}

	/**
	 * Realiza la busqueda de un conjunto de roles en base al estado de actividad. Si el parametro {@code includeActivive}
	 * es verdadero, el resultado obtenido incluye los roles que se encuentren inactivos, en caso de se falso, solo
	 * serán obtenidos los roles que esten activos
	 *
	 * @param includeInactive {@link boolean}
	 * @return Objeto de tipo {@link Optional} de tipo {@link List} con elementos de tipo {@link RolEntity}
	 */
	private Optional<List<RolEntity>> findAll(boolean includeInactive) {
		final var result = rolRepository.findAll();
		return filterRol(includeInactive, result);
	}

	private Optional<List<RolEntity>> filterRol(boolean includeInactive, Iterable<RolEntity> result){
		Optional<List<RolEntity>> optionalRolEntities = Optional.of(new ArrayList<>());
		if(includeInactive) {
			result.forEach(rolEntity -> optionalRolEntities.get().add(rolEntity));
		}else {
			result.forEach(rolEntity -> {
				if(rolEntity.isActive()){
					optionalRolEntities.get().add(rolEntity);
				}
			});
		}
		return optionalRolEntities;
	}

	/**
	 *
	 * @param idRoles {@link List} con elementos de tipo {@link Integer}
	 * @return Objeto de tipo {@link Optional} de tipo {@link List} con elementos de tipo {@link RolEntity}
	 */
	private Optional<List<RolEntity>> findAll(boolean includeInactive, List<Integer> idRoles) throws ClassCastException {
		final var result = rolRepository.findAllById(idRoles);
		if(result.isPresent() && !includeInactive) {
			final var finalList = result.get().stream().filter(RolEntity::isActive).collect(Collectors.toList());
			result.get().clear();
			result.get().addAll(finalList);
		}
		return result;
	}

	/**
	 * Valida la viculación prevía de {@link Feature} a un {@link Rol} determinado.
	 *
	 * @param rolFeatureEntitySet {@link Set}<{@link RolFeatureEntity}>.
	 * @param featureId {@link Long}
	 * @return Objeto de tipo {@link boolean}
	 */
	private boolean validFeatureInRol(Set<RolFeatureEntity> rolFeatureEntitySet, Long featureId) {
		for (var rolFeatureEntity : rolFeatureEntitySet) {
			if (rolFeatureEntity.getFeature().getId().equals(featureId)) {
				return false;
			}
		}
		return true;
	}

}
