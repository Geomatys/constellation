package org.constellation.engine.register;

import java.util.List;


public interface DTOMapper {

    User dtoToEntity(UserDTO dto);
    Layer layer(LayerDTO dto);
    List<RoleDTO> dtoToEntity(List<? extends Role> roles);
    UserDTO entityToDTO(User user);
	Property propertyEntity(String key, String value);
    
}
