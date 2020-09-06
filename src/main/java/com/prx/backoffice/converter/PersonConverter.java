package com.prx.backoffice.converter;

import com.prx.commons.converter.Converter;
import com.prx.commons.pojo.Contact;
import com.prx.commons.pojo.Person;
import com.prx.commons.pojo.User;
import com.prx.persistence.general.domain.ContactEntity;
import com.prx.persistence.general.domain.PersonEntity;
import com.prx.persistence.general.domain.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static com.prx.commons.util.ValidatorCommons.esNoNulo;
import static com.prx.commons.util.ValidatorCommons.esVacio;
import static java.time.LocalDateTime.now;

/**
 *
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
 */
@Service
public class PersonConverter extends Converter <Person, PersonEntity> {
    @Autowired
    private ContactConverter contactConverter;
    @Autowired
    private UserConverter userConverter;

    public PersonConverter() {
        initFunction();
    }

    @Override
    protected PersonEntity getB(Person person) {
        PersonEntity personEntity;
        Set<ContactEntity> contactEntityList = null;
        Set<UserEntity> userEntityList = null;
        LocalDateTime birthdate;

        if(esNoNulo(person.getBirthdate()) && !esVacio(person.getBirthdate())){
            birthdate = LocalDateTime.parse(person.getBirthdate());
        }else {
            //TODO - Quitar, este valor es solo para pruebas, la fecha de nacimiento no puede ser hoy
            birthdate = now();
        }

        if (esNoNulo(person.getUserList()) && !esVacio(person.getUserList())) {
            userEntityList = new HashSet<>(userConverter.createFromA(person.getUserList()));
        }

        if (esNoNulo(person.getContactList()) && !esVacio(person.getContactList())) {
            contactEntityList = new HashSet<>(contactConverter.createFromA(person.getContactList()));
        }

        personEntity = new PersonEntity(person.getId(), person.getFirstName(), person.getMiddleName(),
                person.getLastName(), person.getGender(), birthdate);

        return personEntity;
    }

    @Override
    protected Person getA(PersonEntity personEntity) {
        return new Person(personEntity.getId(), personEntity.getName(), personEntity.getMiddleName(),
                personEntity.getLastName(), personEntity.getGender(), personEntity.getBirthdate().toString(),
                new ArrayList<Contact>(), new ArrayList<User>());
    }
}
