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

package com.prx.backoffice.v1.role.api.to;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.prx.commons.pojo.Role;
import com.prx.commons.to.Response;
import com.prx.commons.util.JsonUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * RolCollectionResponse.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 16-02-2021
 */
@Getter
@Setter
@JsonNaming
@NoArgsConstructor
public class RoleCollectionResponse extends Response {
	private List<Role> roles;

	@Override
	public String toString() {
		return JsonUtil.toJson(this);
	}
}
