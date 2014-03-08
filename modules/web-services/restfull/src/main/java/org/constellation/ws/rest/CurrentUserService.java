package org.constellation.ws.rest;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.constellation.configuration.AcknowlegementType;
import org.constellation.engine.register.DTOMapper;
import org.constellation.engine.register.User;
import org.constellation.engine.register.UserDTO;
import org.constellation.engine.register.repository.UserRepository;
import org.constellation.security.SecurityManagerHolder;
import org.springframework.util.StringUtils;

/**
 * RestFull user configuration service
 * 
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
@Named
@Path("/1/account")
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public class CurrentUserService {

    @Inject
    private DTOMapper dtoMapper;

    @Inject
    private UserRepository userRepository;

    /**
     * @return a {@link Response} which contains requester user name
     */
    @GET
    @Path("/")
    public Response current() {
        String login = SecurityManagerHolder.getInstance().getCurrentUserLogin();
        if (StringUtils.hasText(login)) {
            User user = userRepository.findOneWithRole(login);
            UserDTO userDTO = dtoMapper.entityToDTO(user);
            return Response.ok(userDTO).build();
        }
        return Response.status(401).build();
    }

    @GET
    @Path("/access")
    public Response access() {
        final AcknowlegementType response = new AcknowlegementType("Success",
                "You have access to the configuration service");
        return Response.ok(response).build();
    }

}
