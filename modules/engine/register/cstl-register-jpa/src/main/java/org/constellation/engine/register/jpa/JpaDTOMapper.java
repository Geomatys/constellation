package org.constellation.engine.register.jpa;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;
import org.constellation.engine.register.ConstellationPersistenceException;
import org.constellation.engine.register.DTOMapper;
import org.constellation.engine.register.Layer;
import org.constellation.engine.register.LayerDTO;
import org.constellation.engine.register.User;
import org.constellation.engine.register.UserDTO;

public class JpaDTOMapper implements DTOMapper {

    @Override
    public User dtoToEntity(UserDTO dto) {
        return copy(dto, new UserEntity());
    }


    @Override
    public Layer layer(LayerDTO dto) {
        return copy(dto, new LayerEntity());
    }

    private <T> T copy(Object dto, T entity) {
        try {
            BeanUtils.copyProperties(entity, dto);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ConstellationPersistenceException(e);
        }
        return entity;
    }
}
