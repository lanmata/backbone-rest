/*
 *  @(#)RoleLinkRequest.java
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

package com.umdc.backoffice.v1.roles.api.to;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.prx.commons.general.to.Request;

import java.util.List;

/**
 * RolLinkRequest.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 13-02-2021
 */
@JsonNaming
public class RoleLinkRequest extends Request {
	private List<String> featureIdList;

	public RoleLinkRequest() {
		super();
	}

	public List<String> getFeatureIdList() {
		return featureIdList;
	}

	public void setFeatureIdList(List<String> featureIdList) {
		this.featureIdList = featureIdList;
	}

	@Override
	public String toString() {
		return "RoleLinkRequest{" +
				"featureIdList=" + featureIdList +
				'}';
	}
}
