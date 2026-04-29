/*
 *  @(#)PersonService.java
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

package com.umdc.backoffice.v1.people.service;

import com.prx.commons.services.CrudService;
import com.prx.commons.general.pojo.Person;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

/**
 * PersonService.
 *
 * @author Luis Antonio Mata
 * @version 1.0.1.20200904-01, 02-11-2020
 */
public interface PersonService extends CrudService <UUID, Person> {

    @Override
    default ResponseEntity<Person> delete(UUID personId, Person person) {
        throw new NotImplementedException();
    }
}
