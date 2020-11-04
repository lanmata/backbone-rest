/*
 *
 *  * @(#)PersonServiceImpl.java.
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

package com.prx.backoffice.service.impl;

import com.prx.backoffice.mapper.PersonMapper;
import com.prx.backoffice.service.PersonService;
import com.prx.commons.pojo.MessageActivity;
import com.prx.commons.pojo.Person;
import static com.prx.commons.util.ValidatorCommonsUtil.esNoNulo;
import com.prx.persistence.general.domain.PersonEntity;
import com.prx.persistence.general.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Modelo para la gesti&oacute;n de persona
 *
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
 * @version 1.0.1.20200904-01, 2019-11-14
 */
@Service
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

    /** {@inheritDoc}
     * @return*/
    public MessageActivity<Person> create(Person person){
        var messageActivity = save(person);
        var messageActivityRs = new MessageActivity<Person>();

        messageActivityRs.setMessages(messageActivity.getMessages());
        messageActivityRs.setObjectResponse(personMapper.toTarget(messageActivity.getObjectResponse()));

        return messageActivityRs;
    }

    /** {@inheritDoc} */
    public MessageActivity<PersonEntity> save(Person person){
        final var messageActivity = new MessageActivity<PersonEntity>();

        if(esNoNulo(person)){
            messageActivity.setObjectResponse(personRepository.save(personMapper.toSource(person)));
            messageActivity.getMessages().put(200, "Persona registrada correctamente");
        }

        return messageActivity;
    }

    /** {@inheritDoc} */
    public MessageActivity<Person> find(Person person){
        final var messageActivity = new MessageActivity<Person>();
        final var personResult = personRepository.findByFirstNameMiddleNameLastName(
            person.getFirstName(),
            person.getMiddleName(),
            person.getLastName());
        if(esNoNulo(personResult)){
            messageActivity.setObjectResponse(personMapper.toTarget(personResult));
            //PENDING Colocar un mensaje parametrizado
            messageActivity.getMessages().put(200, "Persona encontrada");
        }

        return messageActivity;
    }

}
