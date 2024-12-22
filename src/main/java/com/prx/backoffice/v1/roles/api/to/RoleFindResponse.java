/*
 *  @(#)RoleFindResponse.java
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
import com.prx.commons.to.Response;

/**
 * RolFindResponse.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 12-02-2021
 */
@JsonNaming
public class RoleFindResponse extends Response {
    private Role rol;

    /**
     * Default Constructor
     */
    public RoleFindResponse() {
        super();
        // Default Constructor
    }

    public Role getRol() {
        return rol;
    }

    public void setRol(Role rol) {
        this.rol = rol;
    }

    @Override
    public String toString() {
        return "RoleFindResponse{" +
                "rol=" + rol +
                '}';
    }
}
