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

package com.prx.backoffice.v1.user.api.to;

import com.prx.commons.pojo.Person;
import com.prx.commons.util.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

/**
 * UserTO.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 17-05-2022
 * @since 11
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserTO {
    private Long id;
    private String alias;
    private String password;
    private boolean active;
    private Person person;
    private Set<Long> roles;

    @Override
    public String toString() {
        return JsonUtil.toJson(this);
    }
}
