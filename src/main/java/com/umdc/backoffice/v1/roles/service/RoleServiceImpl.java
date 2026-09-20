/*
 *  @(#)RoleServiceImpl.java
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
package com.umdc.backoffice.v1.roles.service;

import com.umdc.backoffice.constant.keys.FeatureMessageKey;
import com.umdc.backoffice.v1.features.service.FeatureService;
import com.umdc.backoffice.v1.roles.mapper.RoleMapper;
import com.umdc.backoffice.v1.rolefeatures.service.RoleFeatureLinkService;
import com.umdc.commons.exception.StandardException;
import com.umdc.persistence.general.repositories.ApplicationRepository;
import com.umdc.persistence.general.repositories.FeatureRepository;
import com.umdc.commons.general.pojo.Feature;
import com.umdc.commons.general.pojo.Role;
import com.umdc.persistence.general.domains.FeatureEntity;
import com.umdc.persistence.general.domains.RoleEntity;
import com.umdc.persistence.general.repositories.RoleRepository;
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
    private final RoleMapper roleMapper;
    private final FeatureRepository featureRepository;
    private final FeatureService featureService;
    private final ApplicationRepository applicationRepository;
    private final RoleFeatureLinkService roleFeatureLinkService;

    public RoleServiceImpl(RoleRepository roleRepository, RoleMapper roleMapper, FeatureRepository featureRepository,
                           FeatureService featureService, ApplicationRepository applicationRepository,
                           RoleFeatureLinkService roleFeatureLinkService) {
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
        this.featureRepository = featureRepository;
        this.featureService = featureService;
        this.applicationRepository = applicationRepository;
        this.roleFeatureLinkService = roleFeatureLinkService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Role> find(UUID rolId) {
        LOGGER.info("Inicia llamado al repositorio de Rol para busqueda por id");
        final var roleEntity = roleRepository.findById(rolId);
        return roleEntity.map(entity -> ResponseEntity.ok(roleMapper.toTarget(entity))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<Role>> list() {
        return getRoleList(roleRepository.findAll()).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<Role>> list(UUID... id) {
        return Objects.isNull(id) ?
                ResponseEntity.badRequest().build()
                : getRoleList(roleRepository.findById(Arrays.stream(id).toList()))
                .map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<Role>> list(Boolean inactiveIncluded, List<UUID> roleIds) {
        if (Objects.isNull(roleIds) || roleIds.isEmpty()) {
            return getRoleList(roleRepository.findByStatus(inactiveIncluded)).map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } else {
            return getRoleList(roleRepository.findByStatusAndRoleId(inactiveIncluded, roleIds.stream().toList()))
                    .map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Role> create(Role role) {
        LOGGER.info("Create new role initiated");
        if (null == role) {
            LOGGER.warn("Role null.");
            return ResponseEntity.badRequest().build();
        }
        var roleEntity = roleMapper.toSource(role);
        if (null == roleEntity) {
            LOGGER.warn("Role with content bad.");
            return ResponseEntity.unprocessableContent().build();
        }
        if (null == role.getApplicationId()) {
            LOGGER.warn("Role without applicationId.");
            return ResponseEntity.badRequest().build();
        }
        var optionApplicationEntity = applicationRepository.findById(role.getApplicationId());
        if (optionApplicationEntity.isEmpty()) {
            LOGGER.warn("Application {} not found for role creation.", role.getApplicationId());
            return ResponseEntity.notFound().build();
        }
        final List<FeatureEntity> resolvedFeatures;
        try {
            resolvedFeatures = resolveFeatures(role.getFeatures());
        } catch (StandardException ex) {
            LOGGER.warn("Error resolving features for role creation: {}", ex.getStatus().getStatus());
            return ResponseEntity.status(ex.getCode()).build();
        }
        roleEntity.setApplication(optionApplicationEntity.get());
        roleEntity.setRoleFeatures(null);
        var roleEntityResult = roleRepository.save(roleEntity);
        roleFeatureLinkService.replaceRoleFeatures(roleEntityResult, resolvedFeatures);
        LOGGER.info("Role created.");
        return new ResponseEntity<>(roleMapper.toTarget(roleEntityResult), HttpStatus.CREATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Role> update(UUID roleId, Role role) {
        LOGGER.info("Inicia la actualización del role.");
        final ResponseEntity<Role> roleResponseEntity;
        LOGGER.info("Se busca el role solicitado para actualizar los datos.");
        final var optionRoleEntity = roleRepository.findById(roleId);
        if (optionRoleEntity.isPresent()) {
            final var roleEntity = optionRoleEntity.get();
            roleEntity.setName(role.getName());
            roleEntity.setDescription(role.getDescription());
            roleEntity.setActive(role.getActive());
            final List<FeatureEntity> resolvedFeatures;
            try {
                resolvedFeatures = resolveFeatures(role.getFeatures());
            } catch (StandardException ex) {
                LOGGER.warn("Error resolving features for role update {}: {}", roleId, ex.getStatus().getStatus());
                return ResponseEntity.status(ex.getCode()).build();
            }
            roleFeatureLinkService.replaceRoleFeatures(roleEntity, resolvedFeatures);
            roleResponseEntity = ResponseEntity.accepted().body(roleMapper.toTarget(roleRepository.save(roleEntity)));
        } else {
            roleResponseEntity = ResponseEntity.notFound().build();
        }
        LOGGER.info("{}| Rol {}", roleResponseEntity.getStatusCode() , role);
        return roleResponseEntity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<Role>> listByUser(UUID userId) {
        LOGGER.info("STARTED - Find role by user id {}", userId);
        return Objects.isNull(userId) ?
                ResponseEntity.badRequest().build()
                : getRoleList(roleRepository.findByUserId(userId))
                .map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<Role>> listByApplication(UUID applicationId) {
        LOGGER.info("STARTED - Find role by application id {}", applicationId);
        return Objects.isNull(applicationId) ?
                ResponseEntity.badRequest().build()
                : getRoleList(roleRepository.findByApplicationId(applicationId))
                .map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Resolves the list of features to link to a role: features with an id are
     * looked up (must already exist), and features without an id are created
     * through {@link FeatureService#create} so its business rules (e.g. duplicate
     * name checks) are applied consistently.
     *
     * @param features the features coming from the request payload
     * @return the resolved, persisted {@link FeatureEntity} list
     * @throws StandardException if an existing feature id is not found, or a new feature fails to be created
     */
    private List<FeatureEntity> resolveFeatures(List<Feature> features) {
        if (Objects.isNull(features) || features.isEmpty()) {
            return List.of();
        }
        final List<FeatureEntity> resolved = new ArrayList<>();
        for (Feature feature : features) {
            if (Objects.nonNull(feature.getId())) {
                resolved.add(featureRepository.findById(feature.getId())
                        .orElseThrow(() -> new StandardException(FeatureMessageKey.FEATURE_NOT_FOUND)));
            } else {
                var createdFeature = featureService.create(feature);
                if (!createdFeature.getStatusCode().is2xxSuccessful() || Objects.isNull(createdFeature.getBody())) {
                    throw new StandardException(FeatureMessageKey.FEATURE_PREVIOUS_EXIST);
                }
                resolved.add(featureRepository.findById(createdFeature.getBody().getId())
                        .orElseThrow(() -> new StandardException(FeatureMessageKey.FEATURE_NOT_FOUND)));
            }
        }
        return resolved;
    }

    private Optional<List<Role>> getRoleList(Optional<List<RoleEntity>> optionalRoleEntityList) {
        Optional<List<Role>> optional = Optional.empty();
        if (optionalRoleEntityList.isPresent()) {
            var roleEntities = optionalRoleEntityList.get();
            optional = Optional.of(roleEntities.stream().map(roleMapper::toTarget).toList());
        }
        return optional;
    }

    private Optional<List<Role>> getRoleList(Iterable<RoleEntity> roleEntityIterable) {
        if (Objects.isNull(roleEntityIterable)) {
            return Optional.empty();
        } else {
            var roles = new ArrayList<Role>();
            roleEntityIterable.forEach(roleEntity -> roles.add(roleMapper.toTarget(roleEntity)));
            return Optional.of(roles);
        }
    }

}
