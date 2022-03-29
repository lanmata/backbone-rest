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
package com.prx.backoffice.v1.role.service;

import com.prx.backoffice.v1.feature.mapper.FeatureMapper;
import com.prx.backoffice.v1.feature.service.FeatureService;
import com.prx.backoffice.v1.role.mapper.RoleMapper;
import com.prx.commons.pojo.Feature;
import com.prx.commons.pojo.Role;
import com.prx.persistence.general.domains.RoleEntity;
import com.prx.persistence.general.domains.RoleFeatureEntity;
import com.prx.persistence.general.repositories.RoleFeatureRepository;
import com.prx.persistence.general.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.inject.Named;
import javax.transaction.Transactional;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * RolServiceImpl.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 11-02-2021
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Named(value = "RoleService")
public class RoleServiceImpl implements RoleService {

	private final RoleRepository roleRepository;
	private final RoleFeatureRepository roleFeatureRepository;
	private final FeatureService featureService;
	private final FeatureMapper featureMapper;
	private final RoleMapper roleMapper;

	/** {@inheritDoc} */
	@Override
	public ResponseEntity<Role> find(Long rolId) {
		log.info("Inicia llamado al repositorio de Rol para busqueda por id");
		final var roleEntity = roleRepository.findById(rolId);
		return roleEntity.map(entity -> ResponseEntity.ok(roleMapper.toTarget(entity))).orElseGet(() -> ResponseEntity.notFound().build());
	}

	/** {@inheritDoc} */
	@Override
	public ResponseEntity<List<Role>> list(Long... id) {
		List<Role> roleList = new ArrayList<>();
		final var roleEntity = roleRepository.findAllById(Arrays.asList(id));
		return roleEntity.map(roleEntities -> {
			roleEntities.forEach(roleEntity1 -> {
				Role roleResult = roleMapper.toTarget(roleEntity1);
				roleList.add(roleResult);
			});
			return ResponseEntity.ok(sort(roleList));
		}).orElseGet(() -> ResponseEntity.notFound().build());
	}

	/** {@inheritDoc} */
	@Override
	@Transactional
	public ResponseEntity<Role> create(Role role) {
		log.info("Init create new role");
		if(null == role ){
			log.warn("Role null.");
			return ResponseEntity.badRequest().build();
		}
		var roleEntity = roleMapper.toSource(role);
		if(null == roleEntity ){
			log.warn("Role with bad content.");
			return ResponseEntity.unprocessableEntity().build();
		}
		var roleFeatureEntities = roleEntity.getRoleFeatures();
		roleEntity.setRoleFeatures(null);
		var roleEntityResult = roleRepository.save(roleEntity);
		roleFeatureEntities.forEach(roleFeatureEntity -> {
			roleFeatureEntity.setRole(roleEntityResult.getId());
			roleFeatureEntity.setActive(true);
			roleFeatureRepository.save(roleFeatureEntity);
		});
		roleEntityResult.setRoleFeatures(roleFeatureEntities);
		roleRepository.save(roleEntityResult);
		log.info("Role created.");
		return new ResponseEntity<>(roleMapper.toTarget(roleEntityResult), HttpStatus.CREATED);
	}

	/** {@inheritDoc} */
	@Override
	public ResponseEntity<Role> link(Long roleId, List<Long> featureIdList) {
		log.info("Inicia vinculación de Rol con uno o mas features.");
		final var roleEntity = roleRepository.findById(roleId).orElseThrow();
		//Filtra los feature eliminando los item que se encuentren vinculados previamente con el rol.
		final var featureLinkNew = featureIdList.stream().filter(featureId ->
				validFeatureInRole(roleEntity.getRoleFeatures(), featureId)).collect(Collectors.toList());
		//Obtiene los feature que seran luego vinculados al rol
		final var listResponseEntity = featureService.list(featureLinkNew, false);
		if (null == roleEntity.getRoleFeatures()) {
			log.info("El Rol no tiene features previamente vinculados.");
			roleEntity.setRoleFeatures(new HashSet<>());
		}
		if(null == listResponseEntity){
			log.info("Se ha vinculado el Rol con los features indicados.");
			return ResponseEntity.noContent().build();
		}
		if(null == listResponseEntity.getBody() || listResponseEntity.getBody().isEmpty()) {
			return ResponseEntity.noContent().build();
		} else {
			listResponseEntity.getBody().forEach(feature -> {
				RoleFeatureEntity rolFeatureEntity = new RoleFeatureEntity();
				rolFeatureEntity.setRole(roleEntity.getId());
				rolFeatureEntity.setFeature(feature.getId());
				rolFeatureEntity.setActive(true);
				roleEntity.getRoleFeatures().add(rolFeatureEntity);
			});
			return new ResponseEntity<>(roleMapper.toTarget(roleRepository.save(roleEntity)), HttpStatus.CREATED);
		}
	}

	/** {@inheritDoc} */
	@Override
	@Transactional
	public ResponseEntity<Role> update(Long roleId, Role role) {
		log.info("Inicia la actualización del role.");
		final ResponseEntity<Role> roleResponseEntity;
		log.info("Se busca el role solicitado para actualizar los datos.");
		final var optionRoleEntity = roleRepository.findById(role.getId());
		if (optionRoleEntity.isPresent()) {
			final var roleEntity = optionRoleEntity.get();
			roleEntity.setId(roleId);
			roleEntity.setName(role.getName());
			roleEntity.setDescription(role.getDescription());
			roleEntity.setActive(role.getActive());
			removeRoleFeature(role.getFeatures(), roleEntity);
			role.getFeatures().forEach(feature -> {
				AtomicBoolean mustInclude = new AtomicBoolean(true);
				roleEntity.getRoleFeatures().forEach(roleFeatureEntity -> {
					if(Objects.equals(roleFeatureEntity.getFeature(), feature.getId())) {
						mustInclude.set(false);
					}
				});
				if(mustInclude.get()){
					var roleFeatureEntity = new RoleFeatureEntity();
					roleFeatureEntity.setActive(true);
					roleFeatureEntity.setRole(roleId);
					roleFeatureEntity.setFeature(feature.getId());
					roleFeatureRepository.save(roleFeatureEntity);
				}
			});
			roleResponseEntity = ResponseEntity.accepted().body(roleMapper.toTarget(roleRepository.save(roleEntity)));
		} else {
			roleResponseEntity = ResponseEntity.notFound().build();
		}
		log.info(roleResponseEntity.getStatusCode().getReasonPhrase() + "| Rol {}", role);
		return roleResponseEntity;
	}

	private void removeRoleFeature(List<Feature> featuresToLink, RoleEntity roleEntity) {
		Set<Long> listResult;
		Set<Long> newFeatures = featuresToLink.stream().map(Feature::getId).collect(Collectors.toSet());
		Set<Long> oldFeatures = roleEntity.getRoleFeatures().stream().map(RoleFeatureEntity::getFeature).collect(Collectors.toSet());
		listResult = oldFeatures.stream().filter(aLong -> !newFeatures.contains(aLong)).collect(Collectors.toSet());
		listResult.forEach(roleFeatureEntity -> roleFeatureRepository
				.deleteRoleFeatureByRoleIdAndFeatureId(roleEntity.getId(), roleFeatureEntity));
	}

	@Override
	public ResponseEntity<Role> delete(Long rolId, Role role) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public ResponseEntity<List<Role>> list(boolean includeInactive, List<Long> roles) {
		ResponseEntity<List<Role>> responseEntity;
		final var roleList = new ArrayList<Role>();
		Optional<List<RoleEntity>> optionalRoleEntityList;

		if (null == roles || roles.isEmpty()) {
			log.info("Inicia la búsqueda de los roles.");
			optionalRoleEntityList = findAll(includeInactive);
		} else {
			log.info("Ids pendientes por buscar en DDBB {}.", roles);
			optionalRoleEntityList = findAll(includeInactive, roles);
		}

		if(optionalRoleEntityList.isPresent()){
			optionalRoleEntityList
					.ifPresent(roleEntities -> roleEntities
							.forEach(roleEntity -> {
								var role = roleMapper.toTarget(roleEntity);
								roleList.add(role);
							}));
		}
		responseEntity = roleList.isEmpty()?  ResponseEntity.notFound().build() : ResponseEntity.ok(sort(roleList));
		log.info(responseEntity.getStatusCode().getReasonPhrase() + "| roles {}", (null == roles) ? "": roles.toString());
		return responseEntity;
	}

	/** {@inheritDoc} */
	@Override
	public ResponseEntity<Role> unlink(Long roleId, List<Long> featureIdList) {
		log.info("Inicia la desvinculación del rol y los features {}", featureIdList.toArray());
		final ResponseEntity<Role> roleResponseEntity;
		final RoleEntity roleEntity = roleRepository.findById(roleId).orElseThrow();
		roleEntity.getRoleFeatures().forEach(roleFeatureEntity -> roleFeatureEntity.setActive(false));
		roleRepository.save(roleEntity);
		roleResponseEntity = ResponseEntity.accepted().body(roleMapper.toTarget(roleEntity));
		//TODO - Falta cubrir casos bordes para el metodo unlink
		log.info(roleResponseEntity.getStatusCode().getReasonPhrase());
		return roleResponseEntity;
	}

	/** {@inheritDoc} */
	@Override
	public ResponseEntity<List<Role>> list(long userId) {
		log.info("Inicia la búsqueda de rol paa el usuario con id {}", userId);
		final ResponseEntity<List<Role>> roleResponseEntity;
		final var result = roleRepository.findAllByUserId(userId);
		final var roleList = new ArrayList<Role>();
		if (result.isPresent() && !result.get().isEmpty()){
			result.get().forEach(roleEntity -> roleList.add(roleMapper.toTarget(roleEntity)));
			roleResponseEntity = ResponseEntity.ok().body(roleList);
		} else {
			roleResponseEntity = ResponseEntity.notFound().build();
		}
		log.info(roleResponseEntity.getStatusCode().getReasonPhrase());
		return roleResponseEntity;
	}

	/**
	 * Realiza la busqueda de un conjunto de roles en base al estado de actividad. Si el parametro {@code includeActivive}
	 * es verdadero, el resultado obtenido incluye los roles que se encuentren inactivos, en caso de se falso, solo
	 * serán obtenidos los roles que esten activos
	 *
	 * @param includeInactive {@link boolean}
	 * @return Objeto de tipo {@link Optional} de tipo {@link List} con elementos de tipo {@link RoleEntity}
	 */
	private Optional<List<RoleEntity>> findAll(boolean includeInactive) {
		final var result = roleRepository.findAll();
		log.info("Despues de la llamada al repository");
		return filterRol(includeInactive, result);
	}

	private Optional<List<RoleEntity>> filterRol(boolean includeInactive, Iterable<RoleEntity> result){
		Optional<List<RoleEntity>> optionalRolEntities = Optional.of(new ArrayList<>());
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
	 * @return Objeto de tipo {@link Optional} de tipo {@link List} con elementos de tipo {@link RoleEntity}
	 */
	private  Optional<List<RoleEntity>> findAll(boolean includeInactive, List<Long> idRoles) throws ClassCastException {
		final var result = roleRepository.findAllById(idRoles);
		log.info("Linea siguiente del llamado a repository");
		if(result.isPresent() && !includeInactive) {
			final var finalList = result.get().stream().filter(RoleEntity::isActive).collect(Collectors.toList());
			result.get().clear();
			result.get().addAll(finalList);
		}
		return result;
	}

	/**
	 * Valida la viculación prevía de {@link Feature} a un {@link Role} determinado.
	 *
	 * @param roleFeatureEntitySet {@link Set}<{@link RoleFeatureEntity}>.
	 * @param featureId {@link Long}
	 * @return Objeto de tipo {@link boolean}
	 */
	private boolean validFeatureInRole(Set<RoleFeatureEntity> roleFeatureEntitySet, Long featureId) {
		for (var roleFeatureEntity : roleFeatureEntitySet) {
			if (roleFeatureEntity.getFeature().equals(featureId)) {
				return false;
			}
		}
		return true;
	}

	private List<Role> sort(List<Role> roleEntities) {
		roleEntities.sort((o1, o2) -> {
			return o1.getId() < o2.getId() ? -1 : 1;
		});
		return roleEntities;
	}

}
