/*
 *
 *  * @(#)UserService.java.
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

import com.prx.backoffice.mapper.UserMapper;
import com.prx.backoffice.to.user.UserListResponse;
import com.prx.backoffice.util.MessageUtil;
import com.prx.commons.pojo.Contact;
import com.prx.commons.pojo.MessageActivity;
import com.prx.commons.pojo.Person;
import com.prx.commons.pojo.User;
import com.prx.commons.to.Response;
import com.prx.commons.util.ValidatorCommonsUtil;
import com.prx.persistence.general.domain.PersonEntity;
import com.prx.persistence.general.domain.UserEntity;
import com.prx.persistence.general.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Modelo para la gesti&oacute;n de usuarios
 *
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
 * @since 2019-10-14
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final ContactService contactService;
    private final UserRepository userRepository;
    private final PersonService personService;
    private final MessageUtil messageUtil;
    private final UserMapper userMapper;

    public MessageActivity findUserById(Long userId){
        UserEntity userEntity;
        User user;
        MessageActivity messageActivity;
        messageActivity = new MessageActivity();

        userEntity = userRepository.findById(userId).orElse(new UserEntity());

        if(ValidatorCommonsUtil.esNoNulo(userEntity.getId())){
            user = userMapper.toTarget(userEntity);
            messageActivity.setObjectResponse(user);
            messageActivity.getMessages().put(200, messageUtil.getSolicitudExitosa());
        }else {
            messageActivity.getMessages().put(404, messageUtil.getSinDatos());
        }

        return messageActivity;
    }

    /**
     * Realiza la busqueda del usuario requerido, valida los datos, si los datos son corrector, retorna un token, en caso
     * contrario, informa el motivo de rechazo.
     *
     * @param alias Objeto de tipo String
     * @param password Objeto de tipo String
     * @return Objeto de tipo {@link MessageActivity}
     */
    public MessageActivity access(String alias, String password){
        UserEntity userEntity;
        User user;
        MessageActivity messageActivity = new MessageActivity();

        userEntity = userRepository.findByAlias(alias);

        if(ValidatorCommonsUtil.esNoNulo(userEntity)){
            user = userMapper.toTarget(userEntity);

            if(user.isActive()){
                if(user.getPassword().equals(password)){
                    //TODO - Agregar logica para generacion de token que da acceso al aplicativo
                    // Usuario valido y activo
                    messageActivity.setObjectResponse("ABC123");
                    messageActivity.getMessages().put(200, "Acceso autorizado!");
                }else {
                    // Clave errada de usuario
                    messageActivity.getMessages().put(401, "Clave invalida!");
                }
            }else {
                // Usuario inactivo
                messageActivity.getMessages().put(401, "Usuario inactivo, comuniquese con el administrador.");
            }
        }else {
            // Usuario no existe
            messageActivity.getMessages().put(401, "Usuario no registrado!");
        }

        return messageActivity;
    }

    public UserListResponse findAll(){
        final var userListResponse = new UserListResponse();
        List<UserEntity> userEntityList;
        List<User> userList = null;

        userEntityList = userRepository.findAll();
        userListResponse.setDateTime(LocalDateTime.now(ZoneId.systemDefault()));
        if(userEntityList.isEmpty()){
            userListResponse.setCode(204);
            userListResponse.setMessage("No existes usuarios registrados");
        }else {
            userList = userEntityList.stream().map(userMapper::toTarget).collect(Collectors.toList());
            userListResponse.setCode(200);
            userListResponse.setList(userList);
            userListResponse.setMessage("Busqueda completada con exito");
        }

        return userListResponse;
    }

    /**
     * Realiza la creacion de un usuario
     *
     * @param user Objeto de tipo {@link User}
     * @return Objeto de tipo {@link Response}
     */
    public MessageActivity create(User user) {
        UserEntity userEntity;
        Person person;
        PersonEntity personEntity;
        List<Contact> contacts;
        MessageActivity messageActivity = new MessageActivity();

        userEntity = userRepository.findByAlias(user.getAlias());

        if(ValidatorCommonsUtil.esNoNulo(userEntity)){
            messageActivity.getMessages().put(402, "Nombre de usuario ya se encuentra ocupado, ingrese un nombre de usuario diferente");
        }else{
            person = personService.find(user.getPerson());
            if(ValidatorCommonsUtil.esNoNulo(person.getId())){
                user.getPerson().setId(person.getId());
                //TODO Primero se debe validar si la persona tiene un usuario ya vinculado, no se puede crear un nuevo usuario
            }else{
                contacts = user.getPerson().getContactList();
                user.getPerson().setContactList(null);
                personEntity = personService.save(user.getPerson());
                contacts.forEach(contact -> contact.getPerson().setId(personEntity.getId()));
                //TODO Crear contactos
                contactService.saveAll(contacts);
                user.getPerson().setId(personEntity.getId());

                userEntity = userMapper.toSource(user);
                userRepository.save(userEntity);
                messageActivity.getMessages().put(200, "Usuario creado");
            }
        }

        return messageActivity;
    }
}
