package com.prx.backoffice.v1.people.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.prx.backoffice.v1.people.mapper.PersonMapper;
import com.prx.commons.exception.StandardException;
import com.prx.commons.pojo.Person;
import com.prx.persistence.general.domains.PersonEntity;
import com.prx.persistence.general.repositories.PersonRepository;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {PersonServiceImpl.class})
@ExtendWith(SpringExtension.class)
class PersonServiceImplTest {
    @MockBean
    private PersonMapper personMapper;

    @MockBean
    private PersonRepository personRepository;

    @Autowired
    private PersonServiceImpl personServiceImpl;

    /**
     * Method under test: {@link PersonServiceImpl#save(Person)}
     */
    @Test
    void testSave() {
        PersonEntity personEntity = new PersonEntity();
        personEntity.setBirthdate(LocalDate.of(1970, 1, 1));
        personEntity.setGender("Gender");
        personEntity.setId(UUID.randomUUID());
        personEntity.setLastName("Doe");
        personEntity.setMiddleName("Middle Name");
        personEntity.setName("Name");
        when(personRepository.save(Mockito.<PersonEntity>any())).thenReturn(personEntity);

        PersonEntity personEntity2 = new PersonEntity();
        personEntity2.setBirthdate(LocalDate.of(1970, 1, 1));
        personEntity2.setGender("Gender");
        personEntity2.setId(UUID.randomUUID());
        personEntity2.setLastName("Doe");
        personEntity2.setMiddleName("Middle Name");
        personEntity2.setName("Name");
        when(personMapper.toSource(Mockito.<Person>any())).thenReturn(personEntity2);

        Person person = new Person();
        person.setBirthdate(LocalDate.of(1970, 1, 1));
        person.setFirstName("Jane");
        person.setGender("Gender");
        person.setLastName("Doe");
        person.setMiddleName("Middle Name");
        ResponseEntity<PersonEntity> actualSaveResult = personServiceImpl.save(person);
        assertTrue(actualSaveResult.hasBody());
        assertTrue(actualSaveResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.CREATED, actualSaveResult.getStatusCode());
        verify(personRepository).save(Mockito.<PersonEntity>any());
        verify(personMapper).toSource(Mockito.<Person>any());
    }

    /**
     * Method under test: {@link PersonServiceImpl#save(Person)}
     */
    @Test
    void testSave2() {
        when(personMapper.toSource(Mockito.<Person>any())).thenThrow(new StandardException(null));

        Person person = new Person();
        person.setBirthdate(LocalDate.of(1970, 1, 1));
        person.setFirstName("Jane");
        person.setGender("Gender");
        person.setLastName("Doe");
        person.setMiddleName("Middle Name");
        assertThrows(StandardException.class, () -> personServiceImpl.save(person));
        verify(personMapper).toSource(Mockito.<Person>any());
    }

    @Test
    void testSave3() {
        ResponseEntity<PersonEntity> actualSaveResult = personServiceImpl.save(null);
        assertFalse(actualSaveResult.hasBody());
        assertTrue(actualSaveResult.getHeaders().isEmpty());
        assertEquals(HttpStatus.NOT_FOUND, actualSaveResult.getStatusCode());
    }
}

