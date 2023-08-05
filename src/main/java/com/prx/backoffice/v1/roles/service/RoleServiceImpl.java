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
package com.prx.backoffice.v1.roles.service;

import com.prx.backoffice.v1.features.mapper.FeatureMapper;
import com.prx.backoffice.v1.features.service.FeatureService;
import com.prx.backoffice.v1.roles.mapper.RoleMapper;
import com.prx.commons.pojo.Feature;
import com.prx.commons.pojo.Role;
import com.prx.persistence.general.domains.RoleEntity;
import com.prx.persistence.general.domains.RoleFeatureEntity;
import com.prx.persistence.general.domains.RoleFeaturePK;
import com.prx.persistence.general.repositories.RoleFeatureRepository;
import com.prx.persistence.general.repositories.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * RolServiceImpl.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 11-02-2021
 */
@Service
public class RoleServiceImpl implements RoleService {

	private static final Logger LOGGER = LoggerFactory.getLogger(RoleServiceImpl.class);

	private final RoleRepository roleRepository;
	private final RoleFeatureRepository roleFeatureRepository;
	private final FeatureService featureService;
	private final RoleMapper roleMapper;
	private final FeatureMapper featureMapper;

	public RoleServiceImpl(RoleRepository roleRepository, RoleFeatureRepository roleFeatureRepository,
						   FeatureService featureService, RoleMapper roleMapper, FeatureMapper featureMapper) {
		this.roleRepository = roleRepository;
		this.roleFeatureRepository = roleFeatureRepository;
		this.featureService = featureService;
		this.roleMapper = roleMapper;
		this.featureMapper = featureMapper;
	}

	/** {@inheritDoc} */
	@Override
	public ResponseEntity<Role> find(String rolId) {
		LOGGER.info("Inicia llamado al repositorio de Rol para busqueda por id");
		final var roleEntity = roleRepository.findById(UUID.fromString(rolId));
		return roleEntity.map(entity -> ResponseEntity.ok(roleMapper.toTarget(entity))).orElseGet(() -> ResponseEntity.notFound().build());
	}

	/** {@inheritDoc} */
	@Override
	public ResponseEntity<List<Role>> list(String... id) {
		List<Role> roleList = new ArrayList<>();
		List<UUID> uuidList = new ArrayList<>();
		if(Objects.nonNull(id)) {
			Arrays.stream(id).toList().forEach(s -> uuidList.add(UUID.fromString(s)));
		}
		final var roleEntity = roleRepository.findAllById(uuidList);
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
	public ResponseEntity<Role> create(Role role) {
		LOGGER.info("Create new role initiated");
		if(null == role ){
			LOGGER.warn("Role null.");
			return ResponseEntity.badRequest().build();
		}
		var roleEntity = roleMapper.toSource(role);
		if(null == roleEntity ){
			LOGGER.warn("Role with content bad.");
			return ResponseEntity.unprocessableEntity().build();
		}
		roleEntity.setRoleFeatures(null);
		var roleEntityResult = roleRepository.save(roleEntity);
		LOGGER.info("Role created.");
		if(Objects.nonNull(role.getFeatures()) && !role.getFeatures().isEmpty()) {
			roleEntityResult.setRoleFeatures(updateRoleFeatureLink(role.getFeatures(), roleEntityResult));
		    LOGGER.info("Role & features references created.");
		}
		return new ResponseEntity<>(roleMapper.toTarget(roleEntityResult), HttpStatus.CREATED);
	}

	/** {@inheritDoc} */
	@Override
	public ResponseEntity<Role> link(String roleId, List<String> featureIdList) {
		LOGGER.info("Inicia vinculación de Rol con uno o mas features.");
		final var roleEntity = roleRepository.findById(UUID.fromString(roleId)).orElseThrow();
		//Filtra los feature eliminando los item que se encuentren vinculados previamente con el rol.
		final var featureLinkNew = featureIdList.stream().filter(featureId ->
				validFeatureInRole(roleEntity.getRoleFeatures(), featureId)).collect(Collectors.toList());
		//Getting the features to being link with the role
		final var listResponseEntity = featureService.list(featureLinkNew, false);
		if (Objects.isNull(roleEntity.getRoleFeatures())) {
			LOGGER.info("El Rol no tiene features previamente vinculados.");
			roleEntity.setRoleFeatures(new HashSet<>());
		}
		if(Objects.isNull(listResponseEntity)){
			LOGGER.info("Se ha vinculado el Rol con los features indicados.");
			return ResponseEntity.noContent().build();
		}
		if(Objects.isNull(listResponseEntity.getBody()) || listResponseEntity.getBody().isEmpty()) {
			return ResponseEntity.noContent().build();
		} else {
            roleEntity.setRoleFeatures(updateRoleFeatureLink(listResponseEntity.getBody(), roleEntity));
			return new ResponseEntity<>(roleMapper.toTarget(roleRepository.save(roleEntity)), HttpStatus.CREATED);
		}
	}

	/** {@inheritDoc} */
	@Override
	public ResponseEntity<Role> update(String roleId, Role role) {
		LOGGER.info("Inicia la actualización del role.");
		final ResponseEntity<Role> roleResponseEntity;
		LOGGER.info("Se busca el role solicitado para actualizar los datos.");
		final var optionRoleEntity = roleRepository.findById(UUID.fromString(role.getId()));
		if (optionRoleEntity.isPresent()) {
			final var roleEntity = optionRoleEntity.get();
			roleEntity.setId(UUID.fromString(roleId));
			roleEntity.setName(role.getName());
			roleEntity.setDescription(role.getDescription());
			roleEntity.setActive(role.getActive());
			// FIXME Pending to fix
//			removeRoleFeature(role.getFeatures(), roleEntity);
			role.getFeatures().forEach(feature -> {
				AtomicBoolean mustInclude = new AtomicBoolean(true);
				roleEntity.getRoleFeatures().forEach(roleFeatureEntity -> {
					if(Objects.equals(roleFeatureEntity.getFeature().getId().toString(), feature.getId())) {
						mustInclude.set(false);
					}
				});
				if(mustInclude.get()){
					var roleFeatureEntity = new RoleFeatureEntity();
					roleFeatureEntity.setActive(true);
					roleFeatureEntity.setRole(roleEntity);
					roleFeatureEntity.setFeature(featureMapper.toSource(feature));
					roleFeatureRepository.save(roleFeatureEntity);
				}
			});
			roleResponseEntity = ResponseEntity.accepted().body(roleMapper.toTarget(roleRepository.save(roleEntity)));
		} else {
			roleResponseEntity = ResponseEntity.notFound().build();
		}
		LOGGER.info(roleResponseEntity.getStatusCode() + "| Rol {}", role);
		return roleResponseEntity;
	}

	// FIXME Pending to fix it, required to delete and create new RoleFeature records.
//	private void removeRoleFeature(List<Feature> featuresToLink, RoleEntity roleEntity) {
//		Set<UUID> listResult;
//		roleEntity.getRoleFeatures().stream().filter(roleFeatureEntity -> featuresToLink.stream().filter(feature -> roleFeatureEntity.getFeature().getId().equals(feature.getId())))
//		Set<String> newFeatures = featuresToLink.stream().map(Feature::getId).collect(Collectors.toSet());
//		Set<UUID> oldFeatures = roleEntity.getRoleFeatures().stream().map(RoleFeatureEntity::getFeature).map(FeatureEntity::getId).collect(Collectors.toSet());
//		listResult = oldFeatures.stream().filter(aLong -> !newFeatures.contains(aLong)).collect(Collectors.toSet());
//		listResult.forEach(roleFeatureEntity -> roleFeatureRepository
//				.delete(roleFeatureEntity));
//	}

	@Override
	public ResponseEntity<Role> delete(String rolId, Role role) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public ResponseEntity<List<Role>> list(boolean includeInactive, List<String> roles) {
		ResponseEntity<List<Role>> responseEntity;
		final var roleList = new ArrayList<Role>();
		Optional<List<RoleEntity>> optionalRoleEntityList;

		if (null == roles || roles.isEmpty()) {
			LOGGER.info("Inicia la búsqueda de los roles.");
			optionalRoleEntityList = findAll(includeInactive);
		} else {
			LOGGER.info("Ids pendientes por buscar en DDBB {}.", roles);
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
		LOGGER.info(responseEntity.getStatusCode() + "| roles {}", (null == roles) ? "": roles.toString());
		return responseEntity;
	}

	/** {@inheritDoc} */
	@Override
	public ResponseEntity<Role> unlink(String roleId, List<String> featureIdList) {
		LOGGER.info("Inicia la desvinculación del rol y los features {}", featureIdList.toArray());
		final ResponseEntity<Role> roleResponseEntity;
		final RoleEntity roleEntity = roleRepository.findById(UUID.fromString(roleId)).orElseThrow();
		roleEntity.getRoleFeatures().forEach(roleFeatureEntity -> roleFeatureEntity.setActive(false));
		roleRepository.save(roleEntity);
		roleResponseEntity = ResponseEntity.accepted().body(roleMapper.toTarget(roleEntity));
		//TODO - Falta cubrir casos bordes para el metodo unlink
		LOGGER.info(roleResponseEntity.getStatusCode().toString());
		return roleResponseEntity;
	}

	/** {@inheritDoc} */
	@Override
	public ResponseEntity<List<Role>> list(String userId) {
		LOGGER.info("Inicia la búsqueda de rol paa el usuario con id {}", userId);
		final ResponseEntity<List<Role>> roleResponseEntity;
		final var result = roleRepository.findAllByUserId(UUID.fromString(userId));
		final var roleList = new ArrayList<Role>();
		if (result.isPresent() && !result.get().isEmpty()){
			result.get().forEach(roleEntity -> roleList.add(roleMapper.toTarget(roleEntity)));
			roleResponseEntity = ResponseEntity.ok().body(roleList);
		} else {
			roleResponseEntity = ResponseEntity.notFound().build();
		}
		LOGGER.info(roleResponseEntity.getStatusCode().toString());
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
		LOGGER.info("Despues de la llamada al repository");
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
	private  Optional<List<RoleEntity>> findAll(boolean includeInactive, List<String> idRoles) throws ClassCastException {
		List<UUID> uuidList = new ArrayList<>();
		if(Objects.nonNull(idRoles)) {
			idRoles.forEach(s -> uuidList.add(UUID.fromString(s)));
		}
		final var result = roleRepository.findAllById(uuidList);
		LOGGER.info("Linea siguiente del llamado a repository");
		if(result.isPresent() && !includeInactive) {
			final var finalList = result.get().stream().filter(RoleEntity::isActive).toList();
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
	private boolean validFeatureInRole(Set<RoleFeatureEntity> roleFeatureEntitySet, String featureId) {
		for (var roleFeatureEntity : roleFeatureEntitySet) {
			if (roleFeatureEntity.getFeature().getId().toString().equals(featureId)) {
				return false;
			}
		}
		return true;
	}

    private Set<RoleFeatureEntity> updateRoleFeatureLink(List<Feature> features, RoleEntity roleEntity) {
        var roleFeatureEntities = new HashSet<RoleFeatureEntity>();
        features.forEach(feature -> {
            var featureEntity = featureMapper.toSource(feature);
            var roleFeaturePk = new RoleFeaturePK();
            var roleFeatureEntity = new RoleFeatureEntity();
            roleFeatureEntity.setActive(true);
            roleFeatureEntity.setRole(roleEntity);
            roleFeatureEntity.setFeature(featureEntity);
            roleFeaturePk.setRoleId(roleEntity.getId());
            roleFeaturePk.setFeatureId(featureEntity.getId());
            roleFeatureEntity.setRoleFeaturePK(roleFeaturePk);
            roleFeatureEntities.add(roleFeatureEntity);
        });
        roleFeatureRepository.saveAll(roleFeatureEntities);
        return roleFeatureEntities;
    }

	private List<Role> sort(List<Role> roles) {
		return roles.stream().sorted(Comparator.comparing(Role::getId)).toList();
	}

}
