package com.prx.backoffice.model;

import com.prx.backoffice.converter.UserConverter;
import com.prx.backoffice.to.user.UserAccessResponse;
import com.prx.backoffice.util.MessageUtil;
import com.prx.commons.pojo.Contact;
import com.prx.commons.pojo.MessageActivity;
import com.prx.commons.pojo.Person;
import com.prx.commons.pojo.User;
import com.prx.commons.to.Response;
import com.prx.persistence.general.domain.PersonEntity;
import com.prx.persistence.general.domain.UserEntity;
import com.prx.persistence.general.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.prx.commons.util.ValidatorCommons.esNoNulo;
import static com.prx.commons.util.ValidatorCommons.esVacio;
import static java.time.LocalDateTime.now;

/**
 * Modelo para la gesti&oacute;n de usuarios
 *
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
 * @since 2019-10-14
 */
@Component
public class UserModel {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserConverter userConverter;
    @Autowired
    private PersonModel personModel;
    @Autowired
    private ContactModel contactModel;
    @Autowired
    private MessageUtil messageUtil;

    public MessageActivity findUserById(Long userId){
        UserEntity userEntity;
        User user = null;
        MessageActivity messageActivity;
        messageActivity = new MessageActivity();

        userEntity = userRepository.findById(userId).orElse(new UserEntity());

        if(esNoNulo(userEntity.getId())){
            user = userConverter.convertFromDataObject(userEntity);
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

        if(esNoNulo(userEntity)){
            user = userConverter.convertFromDataObject(userEntity);

            if(user.getActive()){
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

    public List<User> findAll(){
        List<UserEntity> userEntity;
        List<User> user = null;

        userEntity = userRepository.findAll();

        if(!esVacio(userEntity)){
            user = userConverter.createFromDataObject(userEntity);
        }

        return user;
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

        if(esNoNulo(userEntity)){
            messageActivity.getMessages().put(402, "Nombre de usuario ya se encuentra ocupado, ingrese un nombre de usuario diferente");
        }else{
            person = personModel.find(user.getPerson());
            if(esNoNulo(person.getId())){
                user.getPerson().setId(person.getId());
                //TODO Primero se debe validar si la persona tiene un usuario ya vinculado, no se puede crear un nuevo usuario
            }else{
                contacts = user.getPerson().getContactList();
                user.getPerson().setContactList(null);
                personEntity = personModel.save(user.getPerson());
                contacts.forEach(contact -> contact.getPerson().setId(personEntity.getId()));
                //TODO Crear contactos
                contactModel.saveAll(contacts);
                user.getPerson().setId(personEntity.getId());

                userEntity = userConverter.convertFromPojo(user);
                userRepository.save(userEntity);
                messageActivity.getMessages().put(200, "Usuario creado");
            }
        }


        return messageActivity;
    }
}
