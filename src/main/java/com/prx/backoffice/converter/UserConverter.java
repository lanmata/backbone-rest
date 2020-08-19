package com.prx.backoffice.converter;

import com.prx.commons.converter.Converter;
import com.prx.commons.pojo.User;
import com.prx.persistence.general.domain.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 *
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
 */
@Component
public class UserConverter extends Converter<User, UserEntity> {
    @Autowired
    private PersonConverter personConverter;

    @PostConstruct
    @Override
    protected void initFunction() {
        setFunction(user -> new UserEntity(user.getId(),user.getAlias(),user.getPassword(),user.getActive(),
                        personConverter.convertFromPojo(user.getPerson())),
                userEntity -> new User(userEntity.getId(), userEntity.getAlias(), userEntity.getPassword(),
                        userEntity.getActive(), personConverter.convertFromDataObject(userEntity.getPerson())));
    }
}
