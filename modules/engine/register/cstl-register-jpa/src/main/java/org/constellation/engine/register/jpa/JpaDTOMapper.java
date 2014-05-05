package org.constellation.engine.register.jpa;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.BeanUtils;
import org.constellation.engine.register.ConstellationPersistenceException;
import org.constellation.engine.register.ConstellationRegistryRuntimeException;
import org.constellation.engine.register.DTOMapper;
import org.constellation.engine.register.Domain;
import org.constellation.engine.register.DomainAccess;
import org.constellation.engine.register.DomainAccessDTO;
import org.constellation.engine.register.DomainDTO;
import org.constellation.engine.register.DomainRole;
import org.constellation.engine.register.DomainRoleDTO;
import org.constellation.engine.register.Layer;
import org.constellation.engine.register.LayerDTO;
import org.constellation.engine.register.Property;
import org.constellation.engine.register.Role;
import org.constellation.engine.register.RoleDTO;
import org.constellation.engine.register.User;
import org.constellation.engine.register.UserDTO;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxyHelper;
import org.springframework.orm.hibernate3.HibernateAccessor;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class JpaDTOMapper implements DTOMapper {

    @Override
    public User dtoToEntity(UserDTO dto) {
        return copy(dto, new UserEntity());
    }


    @Override
    public Layer layer(LayerDTO dto) {
        return copy(dto, new LayerEntity());
    }

    public <T> T copy(Object orig, T dest) {
        try {
            BeanUtils.copyProperties(dest, orig);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ConstellationPersistenceException(e);
        }
        return dest;
    }


    @Override
    public List<RoleDTO> dtoToEntity(List<? extends Role> roles) {
        List<RoleDTO> dtos = new ArrayList<RoleDTO>();
        for (Role role : roles) {
            RoleDTO roleDTO = new RoleDTO();
            roleDTO.setName(role.getName());
            dtos.add(roleDTO);
        }
        return dtos;
    }


    @Override
    public UserDTO entityToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        
        userDTO.setLogin(user.getLogin());
        userDTO.setLastname(user.getLastname());
        userDTO.setFirstname(user.getFirstname());
        userDTO.setEmail(user.getEmail());
        userDTO.setPassword(user.getPassword());
        userDTO.setRoles(user.getRoles());
           
        return userDTO;
    }


	@Override
	public Property propertyEntity(String key, String value) {
	   PropertyEntity propertyEntity = new PropertyEntity();
	   propertyEntity.setKey(key);
	   propertyEntity.setValue(value);
	return propertyEntity;
	}
}
