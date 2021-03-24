/*
 *
 *  * @(#)PersonService.java.
 *  *
 *  * Copyright (c) Luis Antonio Mata Mata. All rights reserved.
 *  *
 *  * All rights to this product are owned by Luis Antonio Mata Mata and may only
 *  * be used under the terms of its associated license document. You may NOT
 *  * copy, modify, sublicense, or distribute this source file or portions of
 *  * it unless previously authorized in writing by Luis Antonio Mata Mata.
 *  * In any event, this notice and the above copyright must always be included
 *  * verbatim with this file.
 *
 */

package com.prx.backoffice.service;

import com.prx.commons.pojo.MessageActivity;
import com.prx.commons.pojo.Person;
import com.prx.persistence.general.domains.PersonEntity;

/**
 * PersonService.
 *
 * @author Luis Antonio Mata
 * @version 1.0.1.20200904-01, 02-11-2020
 */
public interface PersonService {

    /**
	 * Crea un registro de persona
	 *
	 * @param person Objeto de tipo {@link Person}
	 * @return Objeto de tipo {@link MessageActivity}
	 */
    MessageActivity<Person> create(Person person);

	/**
	 * Crea un registro de persona
	 *
	 * @param person Objeto de tipo {@link Person}
	 * @return Objeto de tipo {@link MessageActivity}
	 */
    MessageActivity<PersonEntity> save(Person person);

	/**
	 * Realiza la busqueda de una persona
	 *
	 * @param person Objeto de tipo {@link Person}
	 * @return Objeto de tipo {@link MessageActivity}
	 */
    MessageActivity<Person> find(Person person);

}
