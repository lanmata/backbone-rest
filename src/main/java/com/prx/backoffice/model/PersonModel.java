package com.prx.backoffice.model;

import com.prx.backoffice.converter.PersonConverter;
import com.prx.commons.pojo.Person;
import com.prx.commons.to.Response;
import com.prx.persistence.general.domain.PersonEntity;
import com.prx.persistence.general.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.prx.commons.util.ValidatorCommons.esNoNulo;
import static java.time.LocalDateTime.now;

/**
 * Modelo para la gesti&oacute;n de persona
 *
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
 * @since 2019-11-14
 */
@Component
public class PersonModel {
    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private PersonConverter personConverter;

    /**
     * Crea un registro
     *
     * @param person
     * @return
     */
    public Response create(Person person){
        PersonEntity personEntity;
        Response response;
        personEntity = save(person);
        response = new Response();
        if (esNoNulo(personEntity) && esNoNulo(personEntity.getId())){
            response.setDateTime(now().toString());
        }

        return response;
    }

    /**
     * Crea un registro
     *
     * @param person
     * @return
     */
    public PersonEntity save(Person person){
        PersonEntity personEntity = null;

        if(esNoNulo(person)){
            personEntity = personRepository.save(personConverter.convertFromA(person));
        }

        return personEntity;
    }

    public Person find(Person person){
        PersonEntity personResult;

        personResult = personRepository.findByFirstNameMiddleNameLastName(person.getFirstName(), person.getMiddleName(), person.getLastName());
        if(esNoNulo(personResult)){
            person = personConverter.convertFromB(personResult);
        }

        return person;
    }

}
