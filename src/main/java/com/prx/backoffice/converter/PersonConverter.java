package com.prx.backoffice.converter;

import com.prx.commons.converter.Converter;
import com.prx.commons.pojo.Person;
import com.prx.commons.util.DateUtil;
import com.prx.persistence.general.domain.ContactEntity;
import com.prx.persistence.general.domain.PersonEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static com.prx.commons.util.ValidatorCommons.esNoNulo;
import static com.prx.commons.util.ValidatorCommons.esVacio;
import static java.time.LocalDateTime.now;

/**
 *
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
 */
@Component
public class PersonConverter extends Converter <Person, PersonEntity> {
    @Autowired
    private ContactConverter contactConverter;

    @PostConstruct
    @Override
    protected void initFunction() {
        setFunction(this::getPersonEntity,
                this::getPerson);
    }

    private PersonEntity getPersonEntity(Person person) {
        PersonEntity personEntity;
        Set<ContactEntity> contactEntityList = null;
        LocalDateTime birthdate = null;

        if(esNoNulo(person.getBirthdate()) && !esVacio(person.getBirthdate())){
            birthdate = LocalDateTime.parse(person.getBirthdate());
        }else {
            //TODO - Quitar, este valor es solo para pruebas, la fecha de nacimiento no puede ser hoy
            birthdate = now();
        }

        if (esNoNulo(person.getContactList()) && !esVacio(person.getContactList())) {
            contactEntityList = new HashSet<>(contactConverter.createFromPojo(person.getContactList()));
        }

        personEntity = new PersonEntity(person.getId(), person.getFirstName(), person.getMiddleName(),
                person.getLastName(), person.getGender(), birthdate,
                contactEntityList);

        return personEntity;
    }

    private Person getPerson(PersonEntity personEntity) {
        return new Person(personEntity.getId(), personEntity.getName(), personEntity.getMiddleName(),
                personEntity.getLastName(), personEntity.getGender(), personEntity.getBirthdate().toString(),
                contactConverter.createFromDataObject(personEntity.getContactEntities()));
    }
}
