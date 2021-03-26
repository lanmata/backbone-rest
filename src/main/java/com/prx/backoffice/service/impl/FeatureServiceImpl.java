/*
 * @(#)FeatureServiceImpl.java.
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

import com.prx.backoffice.enums.keys.FeatureMessageKey;
import com.prx.backoffice.mapper.FeatureMapper;
import com.prx.backoffice.service.FeatureService;
import com.prx.commons.pojo.Feature;
import com.prx.commons.pojo.MessageActivity;
import com.prx.persistence.general.domains.FeatureEntity;
import com.prx.persistence.general.repositories.FeatureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
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
	public MessageActivity<Feature> create(Feature feature) {
		log.info("Inicia la creación de feature");
		final var messageActivity = new MessageActivity<Feature>();
		log.info("Se valida que no exista duplicidad en el nombre del feature");
		final var optFeature = featureRepository.findByName(feature.getName());
		//TODO Falta manejo de casos bordes en el metodo
		if (optFeature.isPresent()) {
			messageActivity.setObjectResponse(featureMapper.toTarget(optFeature.get()));
			messageActivity.setCode(FeatureMessageKey.FEATURE_PREVIOUS_EXIST.getCode());
			messageActivity.setMessage(FeatureMessageKey.FEATURE_PREVIOUS_EXIST.getStatus());
			log.info("No se crea feature debido que existe previamente");
		} else {
			messageActivity.setCode(FeatureMessageKey.FEATURE_OK.getCode());
			messageActivity.setMessage(FeatureMessageKey.FEATURE_OK.getStatus());
			messageActivity.setObjectResponse(featureMapper.toTarget(featureRepository.save(
					featureMapper.toSource(feature))));
			log.info("Feature creado satisfactoriamente");
		}

		return messageActivity;
	}

	/** {@inheritDoc} */
	@Override
	public MessageActivity<Feature> update(Feature feature) {
		final var messageActivity = new MessageActivity<Feature>();
		//TODO Falta manejo de casos bordes en el metodo
		messageActivity.setObjectResponse(featureMapper.toTarget(
				featureRepository.save(featureMapper.toSource(feature))));
		messageActivity.setCode(FeatureMessageKey.FEATURE_OK.getCode());
		messageActivity.setMessage(FeatureMessageKey.FEATURE_OK.getStatus());
		return messageActivity;
	}

	/** {@inheritDoc} */
	@Override
	public MessageActivity<List<Feature>> list(List<Long> featureIds, boolean includeInactive) {
		final var messageActivity = new MessageActivity<List<Feature>>();
		final var featureListResult = new ArrayList<Feature>();
		final var featureEntityListResult = featureRepository.findAllById(featureIds);
		//TODO Falta manejo de casos bordes en el metodo
		featureEntityListResult.forEach(featureEntity -> {
			if (includeInactive || featureEntity.getActive()) {
				featureListResult.add(featureMapper.toTarget(featureEntity));
			}
		});
		messageActivity.setCode(FeatureMessageKey.FEATURE_OK.getCode());
		messageActivity.setMessage(FeatureMessageKey.FEATURE_OK.getStatus());
		messageActivity.setObjectResponse(featureListResult);
		return messageActivity;
	}

	/** {@inheritDoc} */
	@Override
	public MessageActivity<Feature> find(Long featureId) {
		final var messageActivity = new MessageActivity<Feature>();
		final var featureEntity = featureRepository.findById(featureId).orElse(new FeatureEntity());
		if (esNulo(featureEntity.getId())) {
			messageActivity.setCode(Response.Status.NOT_FOUND.getStatusCode());
			messageActivity.setMessage(Response.Status.NOT_FOUND.toString().concat(". Feature id: " + featureId));
		} else {
			messageActivity.setObjectResponse(featureMapper.toTarget(featureEntity));
			messageActivity.setCode(Response.Status.OK.getStatusCode());
			messageActivity.setMessage(Response.Status.OK.toString());
		}
		return messageActivity;
	}
}
