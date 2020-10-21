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

import com.prx.backoffice.mapper.PersonMapper;
import com.prx.commons.pojo.Person;
import com.prx.commons.to.Response;
import static com.prx.commons.util.ValidatorCommonsUtil.esNoNulo;
import com.prx.persistence.general.domain.PersonEntity;
import com.prx.persistence.general.repository.PersonRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Modelo para la gesti&oacute;n de persona
 *
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
 * @since 2019-11-14
 */
@Service
@RequiredArgsConstructor
public class PersonService {
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

    /**
     * Crea un registro
     *
     * @param person Objeto de tipo {@link Person}
     * @return Objeto de tipo {@link Response}
     */
    public Response create(Person person){
        PersonEntity personEntity;
        Response response;
        personEntity = save(person);
        response = new Response();
        if (esNoNulo(personEntity) && esNoNulo(personEntity.getId())){
            response.setDateTime(LocalDateTime.now(ZoneId.systemDefault()));
        }

        return response;
    }

    /**
     * Crea un registro
     *
     * @param person Objeto de tipo {@link Person}
     * @return Objeto de tipo {@link PersonEntity}
     */
    public PersonEntity save(Person person){
        PersonEntity personEntity = null;

        if(esNoNulo(person)){
            personEntity = personRepository.save(personMapper.toSource(person));
        }

        return personEntity;
    }

    public Person find(Person person){
        PersonEntity personResult;
        Person personRs = null;

        personResult = personRepository.findByFirstNameMiddleNameLastName(person.getFirstName(), person.getMiddleName(),
            person.getLastName());
        if(esNoNulo(personResult)){
            personRs = personMapper.toTarget(personResult);
        }

        return personRs;
    }

}
