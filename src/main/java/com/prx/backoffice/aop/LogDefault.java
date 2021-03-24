/*
 * @(#)LogDefault.java.
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

package com.prx.backoffice.aop;

import com.prx.backoffice.enums.keys.LogActionKey;
import com.prx.backoffice.enums.keys.UserMessageKey;
import com.prx.commons.enums.types.MessageType;

import java.lang.annotation.*;

/**
 * LogDefault.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 19-03-2021
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
public @interface LogDefault {

    LogActionKey action() default LogActionKey.EMPTY;

    Class<? extends MessageType> detail() default UserMessageKey.class;

}
