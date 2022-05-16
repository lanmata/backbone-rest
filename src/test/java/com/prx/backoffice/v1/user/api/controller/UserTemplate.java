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

package com.prx.backoffice.v1.user.api.controller;

import com.prx.backoffice.v1.user.api.to.UserAccessRequest;
import com.prx.backoffice.v1.user.api.to.UserCreateRequest;
import com.prx.backoffice.v1.user.api.to.UserTO;
import com.prx.backoffice.v1.util.TemplateUtil;
import com.prx.persistence.general.domains.UserEntity;

/**
 * UserTemplate.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 15-05-2022
 * @since 11
 */
public interface UserTemplate extends TemplateUtil<UserTO, UserEntity> {
    UserCreateRequest getUserCreateRequest();

    UserAccessRequest getUserAccessRequest();
}
