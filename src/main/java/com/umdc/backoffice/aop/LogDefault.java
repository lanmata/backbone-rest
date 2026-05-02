/*
 *  @(#)LogDefault.java
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

package com.umdc.backoffice.aop;

import com.umdc.backoffice.constant.keys.LogActionKey;
import com.umdc.backoffice.constant.keys.UserMessageKey;
import com.umdc.commons.constants.httpstatus.type.MessageType;

import java.lang.annotation.*;

/**
 * Annotation for logging default actions in the application.
 * Can be applied to methods, fields, and parameters.
 * Specifies the log action key and the message type detail.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 19-03-2021
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
public @interface LogDefault {

    /**
     * Specifies the log action key.
     * Defaults to LogActionKey.EMPTY.
     *
     * @return the log action key
     */
    LogActionKey action() default LogActionKey.EMPTY;

    /**
     * Specifies the message type detail.
     * Defaults to UserMessageKey.class.
     *
     * @return the message type detail class
     */
    Class<? extends MessageType> detail() default UserMessageKey.class;

}
