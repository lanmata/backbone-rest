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

package com.prx.backoffice.v1.feature.api.to;

import com.prx.backoffice.v1.role.api.to.RoleLinkRequest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RolLinkRequestTest.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 18-02-2021
 */

class RoleLinkRequestTest {

	@Test
	void testGettersAndSetters() {
		final var roleLinkRequest = new RoleLinkRequest();
		roleLinkRequest.setFeatureIdList(new ArrayList<>());
		roleLinkRequest.getFeatureIdList().add(1L);
		roleLinkRequest.getFeatureIdList().add(2L);
		roleLinkRequest.getFeatureIdList().add(3L);

		assertAll(
				() -> assertNotNull(roleLinkRequest),
				() -> assertNotNull(roleLinkRequest.toString()),
				() -> assertNotNull(roleLinkRequest.getFeatureIdList())
		);

		Long[] b = new Long[roleLinkRequest.getFeatureIdList().size()];
		b = roleLinkRequest.getFeatureIdList().toArray(b);
		assertArrayEquals(new Long[] {1L, 2L, 3L}, b);

	}

}
