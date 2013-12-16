package org.constellation.engine.register;


public interface DTOMapper {

    User dtoToEntity(UserDTO dto);
    Layer layer(LayerDTO dto);
    
}
