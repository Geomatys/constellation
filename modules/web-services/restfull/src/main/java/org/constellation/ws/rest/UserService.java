package org.constellation.ws.rest;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.constellation.configuration.AcknowlegementType;
import org.constellation.engine.register.DTOMapper;
import org.constellation.engine.register.User;
import org.constellation.engine.register.UserDTO;
import org.constellation.engine.register.repository.UserRepository;
import org.constellation.security.SecurityManagerHolder;
import org.geotoolkit.util.StringUtilities;

/**
 * RestFull user configuration service
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
@Named
@Path("/1/user")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class UserService {

    @Inject
    private DTOMapper dtoMapper;
    
    @Inject
    private UserRepository userRepository;
    
    /**
     * @return a {@link Response} which contains requester user name
     */
    @GET
    @Path("/")
    public Response findAll() {
        List<? extends User> list = userRepository.findAll();
        List<UserDTO> dtos = new ArrayList<UserDTO>();
        for (User user : list) {
            UserDTO userDTO = new UserDTO();
            userDTO.setLogin(user.getLogin());
            userDTO.setLastname(user.getLastname());
            userDTO.setFirstname(user.getFirstname());
            userDTO.setEmail(user.getEmail());
            userDTO.setPassword(user.getPassword());
            dtos.add(userDTO);
        }
        return Response.ok(dtos).build();
    }
    
    
    /**
     * @return a {@link Response} which contains requester user name
     */
    @GET
    @Path("/{login}")
    public Response findOne(@PathParam("login") String login) {
        User findOne = userRepository.findOne(login);
        UserDTO userDTO = new UserDTO();
        userDTO.setLogin(login);
        userDTO.setLogin(login);
        userDTO.setLastname(findOne.getLastname());
        userDTO.setFirstname(findOne.getFirstname());
        userDTO.setEmail(findOne.getEmail());
        userDTO.setPassword(findOne.getPassword());
        return Response.ok(userDTO).build();
    }
    
  
    
    @DELETE
    @Path("/{login}")
    public Response delete(@PathParam("login") String login) {
        userRepository.delete(login);
        return Response.noContent().build();
    }
    
    @POST
    @Path("/")
    public Response post(UserDTO userDTO) {
        User user = dtoMapper.dtoToEntity(userDTO);
        userRepository.save(user);
        return Response.ok(user).build();
    }

    /**
     * Called on login. To know if login is granted to access to server
     *
     * @return an {@link AcknowlegementType} on {@link Response} to know operation state
     */
    @GET
    @Path("/access")
    public Response access() {
        final AcknowlegementType response = new AcknowlegementType("Success", "You have access to the configuration service");
        return Response.ok(response).build();
    }

}
