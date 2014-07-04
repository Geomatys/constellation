/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.engine.register.jpa;

import org.apache.commons.beanutils.BeanUtils;
import org.constellation.engine.register.ConstellationPersistenceException;
import org.constellation.engine.register.DTOMapper;
import org.constellation.engine.register.Layer;
import org.constellation.engine.register.LayerDTO;
import org.constellation.engine.register.Property;
import org.constellation.engine.register.Role;
import org.constellation.engine.register.RoleDTO;
import org.constellation.engine.register.User;
import org.constellation.engine.register.UserDTO;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

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
