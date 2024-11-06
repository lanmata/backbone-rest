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
	private final RoleMapper roleMapper;
	private final FeatureMapper featureMapper;

	public RoleServiceImpl(RoleRepository roleRepository, RoleFeatureRepository roleFeatureRepository,
						   RoleMapper roleMapper, FeatureMapper featureMapper) {
		this.roleRepository = roleRepository;
		this.roleFeatureRepository = roleFeatureRepository;
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
	public ResponseEntity<List<Role>> list() {
		return getRoleList(roleRepository.findAll()).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	/** {@inheritDoc} */
	@Override
	public ResponseEntity<List<Role>> list(String... id) {
		return Objects.isNull(id)?
				ResponseEntity.badRequest().build()
				:getRoleList(roleRepository.findById(Arrays.stream(id).map(UUID::fromString).toList()))
				.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	/** {@inheritDoc} */
	@Override
	public ResponseEntity<List<Role>> list(Boolean inactiveIncluded, List<String> roleIds) {
		if(Objects.isNull(roleIds) || roleIds.isEmpty()) {
			return getRoleList(roleRepository.findByStatus(inactiveIncluded)).map(ResponseEntity::ok)
					.orElseGet(() -> ResponseEntity.notFound().build());
		} else {
			return getRoleList(roleRepository.findByStatusAndRoleId(inactiveIncluded, roleIds.stream().map(UUID::fromString)
					.toList())).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
		}
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
	public ResponseEntity<Role> update(String roleId, Role role) {
		LOGGER.info("Inicia la actualización del role.");
		final ResponseEntity<Role> roleResponseEntity;
		LOGGER.info("Se busca el role solicitado para actualizar los datos.");
		final var optionRoleEntity = roleRepository.findById(UUID.fromString(roleId));
		if (optionRoleEntity.isPresent()) {
			final var roleEntity = optionRoleEntity.get();
			roleEntity.setName(role.getName());
			roleEntity.setDescription(role.getDescription());
			roleEntity.setActive(role.getActive());
			updateRoleFeature(role.getFeatures(), roleEntity);
			roleResponseEntity = ResponseEntity.accepted().body(roleMapper.toTarget(roleRepository.save(roleEntity)));
		} else {
			roleResponseEntity = ResponseEntity.notFound().build();
		}
		LOGGER.info(roleResponseEntity.getStatusCode() + "| Rol {}", role);
		return roleResponseEntity;
	}

	/** {@inheritDoc} */
	@Override
	public ResponseEntity<List<Role>> listByUser(String userId) {
		LOGGER.info("STARTED - Find role by user id {}", userId);
		return Objects.isNull(userId)?
				ResponseEntity.badRequest().build()
				:getRoleList(roleRepository.findByUserId(UUID.fromString(userId)))
				.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	private void updateRoleFeature(List<Feature> featuresToLink, RoleEntity roleEntity) {
		roleFeatureRepository.deleteAll(roleEntity.getRoleFeatures());
		roleEntity.setRoleFeatures(null);
		if(Objects.nonNull(featuresToLink) && !featuresToLink.isEmpty()) {
			var roleFeatureEntities = new HashSet<RoleFeatureEntity>();
			featuresToLink.forEach(feature -> {
				var roleFeatureEntity = new RoleFeatureEntity();
				var roleFeaturePk = new RoleFeaturePK();
				roleFeaturePk.setFeatureId(UUID.fromString(feature.getId()));
				roleFeaturePk.setRoleId(roleEntity.getId());
				roleFeatureEntity.setRoleFeaturePK(roleFeaturePk);
				roleFeatureEntity.setFeature(featureMapper.toSource(feature));
				roleFeatureEntity.setRole(roleEntity);
				roleFeatureEntity.setActive(true);
				roleFeatureEntities.add(roleFeatureEntity);
			});
			roleFeatureRepository.saveAll(roleFeatureEntities);
			roleEntity.setRoleFeatures(roleFeatureEntities);
		}
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

	private Optional<List<Role>> getRoleList(Optional<List<RoleEntity>> optionalRoleEntityList) {
		Optional<List<Role>> optional = Optional.empty();
		if(optionalRoleEntityList.isPresent()) {
			var roleEntities = optionalRoleEntityList.get();
			optional = Optional.of(roleEntities.stream().map(roleMapper::toTarget).toList());
		}
		return optional;
	}

	private Optional<List<Role>> getRoleList(Iterable<RoleEntity> roleEntityIterable) {
		if(Objects.isNull(roleEntityIterable)) {
			return Optional.empty();
		} else {
			var roles = new ArrayList<Role>();
			roleEntityIterable.forEach(roleEntity -> roles.add(roleMapper.toTarget(roleEntity)));
			return Optional.of(roles);
		}
	}

}
