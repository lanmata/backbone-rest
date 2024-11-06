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
package com.prx.backoffice.v1.features.service;

import com.prx.backoffice.util.MessageUtil;
import com.prx.backoffice.v1.features.mapper.FeatureMapper;
import com.prx.commons.pojo.Feature;
import com.prx.persistence.general.domains.FeatureEntity;
import com.prx.persistence.general.repositories.FeatureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.prx.commons.util.ValidatorCommonsUtil.esNulo;

/**
 * FeatureServiceImpl.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 14-02-2021
 */
@Service
public class FeatureServiceImpl implements FeatureService {

	private static final Logger LOGGER = LoggerFactory.getLogger(FeatureServiceImpl.class);

	private final FeatureRepository featureRepository;

	private final FeatureMapper featureMapper;

	public FeatureServiceImpl(FeatureRepository featureRepository, FeatureMapper featureMapper) {
		this.featureRepository = featureRepository;
		this.featureMapper = featureMapper;
	}

	/** {@inheritDoc} */
	@Override
	public ResponseEntity<Feature> create(Feature feature) {
		LOGGER.info("Starting feature creation");
		ResponseEntity<Feature> responseEntity;
		LOGGER.info("Feature name duplication validation.");
		final var optFeature = featureRepository.findByName(feature.getName());
		//TODO Falta manejo de casos bordes en el metodo
		if (optFeature.isPresent()) {
			responseEntity = new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
		} else {
			responseEntity = new ResponseEntity<>(featureMapper.toTarget(featureRepository
					.save(featureMapper.toSource(feature))), HttpStatus.CREATED);
		}
		LOGGER.info(responseEntity.getStatusCode() + MessageUtil.LOG_PATH_SEPARATOR + feature);
		return responseEntity;
	}

	/** {@inheritDoc} */
	@Override
	public ResponseEntity<Feature> update(String featureId, Feature feature) {
		ResponseEntity<Feature> responseEntity;
		final var optFeature = featureRepository.findById(UUID.fromString(featureId));
		if (optFeature.isPresent()) {
			feature.setId(featureId);
			responseEntity = ResponseEntity.accepted().body(featureMapper.toTarget(
					featureRepository.save(featureMapper.toSource(feature))));
		} else {
			responseEntity = ResponseEntity.of(ProblemDetail.forStatusAndDetail(HttpStatus.NOT_ACCEPTABLE,"Feature not registered")).build();
		}
		LOGGER.info(responseEntity.getStatusCode() + MessageUtil.LOG_PATH_SEPARATOR + feature.toString());
		return responseEntity;
	}

	/** {@inheritDoc} */
	@Override
	public ResponseEntity<List<Feature>> list(List<String> featureIds, boolean includeInactive) {
		final var featureListResult = new ArrayList<Feature>();
		List<UUID> uuidList = new ArrayList<>();
		Optional<Iterable<FeatureEntity>> featureEntityListResult;
		if(Objects.isNull(featureIds) || featureIds.isEmpty()) {
			featureEntityListResult = Optional.of(featureRepository.findAll());
		} else {
			featureIds.forEach(s -> uuidList.add(UUID.fromString(s)));
			featureEntityListResult = featureRepository.findByIdAndStatus(uuidList.stream().toList(), includeInactive);
		}

        featureEntityListResult.ifPresent(featureEntities -> featureEntities.forEach(featureEntity -> {
            if (includeInactive || Boolean.TRUE.equals(featureEntity.getActive())) {
                featureListResult.add(featureMapper.toTarget(featureEntity));
            }
        }));

		if(featureListResult.isEmpty()) {
			return ResponseEntity.notFound().build();
		} else {
			return ResponseEntity.ok(sort(featureListResult));
		}
	}

	/** {@inheritDoc} */
	@Override
	public ResponseEntity<Feature> find(String featureId) {
		ResponseEntity<Feature> responseEntity;
		final var featureEntity = featureRepository.findById(UUID.fromString(featureId)).orElse(new FeatureEntity());
		if (esNulo(featureEntity.getId())) {
			responseEntity = ResponseEntity.notFound().build();
		} else {
			responseEntity = new ResponseEntity<>(featureMapper.toTarget(featureEntity), HttpStatus.OK);
		}
		LOGGER.info(responseEntity.getStatusCode() + MessageUtil.LOG_PATH_SEPARATOR + featureId);
		return responseEntity;
	}

	private List<Feature> sort(List<Feature> features) {
		return features.stream().sorted(Comparator.comparing(Feature::getId)).toList();
	}
}
