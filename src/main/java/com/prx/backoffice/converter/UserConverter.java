package com.prx.backoffice.converter;

import com.prx.commons.converter.Converter;
import com.prx.commons.pojo.User;
import com.prx.persistence.general.domain.UserEntity;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:luis.antonio.mata@gmail.com">Luis Antonio Mata</a>
 */
@Component
@NoArgsConstructor
public class UserConverter extends Converter<User, UserEntity> {
    @Autowired
    private PersonConverter personConverter;

//    @PostConstruct
//    @Override
//    protected void initFunction() {
//        setFunction(this::getB, this::getA);
//    }

    @Override
    protected User getA(UserEntity userEntity) {
        return new User(userEntity.getId(), userEntity.getAlias(), userEntity.getPassword(),
                userEntity.getActive(), personConverter.convertFromB(userEntity.getPerson()));
    }

    @Override
    protected UserEntity getB(User user) {
        return new UserEntity(user.getId(),user.getAlias(),user.getPassword(),user.getActive(),
                personConverter.convertFromPojo(user.getPerson()));
    }
}
