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
package com.prx.backoffice.v1.feature.service;

import com.prx.backoffice.util.MessageUtil;
import com.prx.backoffice.v1.feature.mapper.FeatureMapper;
import com.prx.commons.pojo.Feature;
import com.prx.persistence.general.domains.FeatureEntity;
import com.prx.persistence.general.repositories.FeatureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.prx.commons.util.ValidatorCommonsUtil.esNulo;

/**
 * FeatureServiceImpl.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 14-02-2021
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureServiceImpl implements FeatureService {

	private final FeatureRepository featureRepository;
	private final FeatureMapper featureMapper;

	/** {@inheritDoc} */
	@Override
	public ResponseEntity<Feature> create(Feature feature) {
		log.info("Starting feature creation");
		ResponseEntity<Feature> responseEntity;
		log.info("Feature name duplication validation.");
		final var optFeature = featureRepository.findByName(feature.getName());
		//TODO Falta manejo de casos bordes en el metodo
		if (optFeature.isPresent()) {
			responseEntity = new ResponseEntity<>(HttpStatus.ALREADY_REPORTED);
		} else {
			responseEntity = new ResponseEntity<>(featureMapper.toTarget(featureRepository
					.save(featureMapper.toSource(feature))), HttpStatus.CREATED);
		}
		log.info(responseEntity.getStatusCode() + MessageUtil.LOG_PATH_SEPARATOR + feature);
		return responseEntity;
	}

	/** {@inheritDoc} */
	@Override
	public ResponseEntity<Feature> update(Long featureId, Feature feature) {
		ResponseEntity<Feature> responseEntity;
		//TODO Falta manejo de casos bordes en el metodo
		responseEntity = ResponseEntity.accepted().body(featureMapper.toTarget(
				featureRepository.save(featureMapper.toSource(feature))));
		log.info(responseEntity.getStatusCode() + MessageUtil.LOG_PATH_SEPARATOR + feature.toString());
		return responseEntity;
	}

	@Override
	public ResponseEntity<Feature> delete(Long featureId, Feature feature) {
		throw new NotImplementedException();
	}

	@Override
	public ResponseEntity<List<Feature>> list(Long... id) {
		throw new NotImplementedException();
	}

	/** {@inheritDoc} */
	@Override
	public ResponseEntity<List<Feature>> list(List<Long> featureIds, boolean includeInactive) {
		ResponseEntity<List<Feature>> listResponseEntity;
		final var featureListResult = new ArrayList<Feature>();
		Iterable<FeatureEntity> featureEntityListResult;
		if(null == featureIds) {
			featureEntityListResult = featureRepository.findAll();
		} else {
			featureEntityListResult = featureRepository.findAllById(featureIds);
		}

		featureEntityListResult.forEach(featureEntity -> {
			if (includeInactive || Boolean.TRUE.equals(featureEntity.getActive())) {
				featureListResult.add(featureMapper.toTarget(featureEntity));
			}
		});

		if(featureListResult.isEmpty()) {
			return ResponseEntity.notFound().build();
		} else {
			listResponseEntity = new ResponseEntity<>(featureListResult, HttpStatus.OK);
			log.info(listResponseEntity.getStatusCode().toString());
			return listResponseEntity;
		}
	}

	/** {@inheritDoc} */
	@Override
	public ResponseEntity<Feature> find(Long featureId) {
		ResponseEntity<Feature> responseEntity;
		final var featureEntity = featureRepository.findById(featureId).orElse(new FeatureEntity());
		if (esNulo(featureEntity.getId())) {
			responseEntity = ResponseEntity.notFound().build();
		} else {
			responseEntity = new ResponseEntity<>(featureMapper.toTarget(featureEntity), HttpStatus.FOUND);
		}
		log.info(responseEntity.getStatusCode() + MessageUtil.LOG_PATH_SEPARATOR + featureId);
		return responseEntity;
	}
}
