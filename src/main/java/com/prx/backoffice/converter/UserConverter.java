package com.prx.backoffice.converter;

import com.prx.commons.converter.Converter;
import com.prx.commons.pojo.User;
import com.prx.persistence.general.domain.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
 */
@Service
public class UserConverter extends Converter<User, UserEntity> {
    @Autowired
    private PersonConverter personConverter;

    public UserConverter() {
        initFunction();}

    @Override
    protected User getA(UserEntity userEntity) {
        return new User(userEntity.getId(), userEntity.getAlias(), userEntity.getPassword(),
                userEntity.getActive(), personConverter.convertFromB(userEntity.getPerson()));
    }

    @Override
    protected UserEntity getB(User user) {
        return new UserEntity(user.getId(),user.getAlias(),user.getPassword(),user.getActive(),
                personConverter.convertFromA(user.getPerson()));
    }
}
