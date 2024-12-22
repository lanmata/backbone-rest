/*
 *  @(#)RoleRequest.java
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

package com.prx.backoffice.v1.roles.api.to;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.prx.commons.pojo.Role;
import com.prx.commons.to.Request;

/**
 * RolFindRequest.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 12-02-2021
 */
@JsonNaming
public class RoleRequest extends Request {
	private Role role;

	/**
	 * Default Constructor
	 */
	public RoleRequest() {
		super();
		// Default Constructor
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	@Override
	public String toString() {
		return "RoleRequest{" +
				"role=" + role +
				'}';
	}
}
