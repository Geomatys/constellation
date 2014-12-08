package org.constellation.rest.api;

import org.constellation.engine.register.repository.UserRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

@Component
@Path("/1/general")
public class GeneralRest {

    @Inject
    private UserRepository userRepository;
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/counts")
    public Map<String, Integer> usercount() {
        Map<String, Integer> map = new HashMap<>();
        map.put("nbuser", userRepository.countUser());
        return map;
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/logincheck")
    public Map<String, String> loginAvailable(String login) {
        Map<String, String> ret = new HashMap<>();
        if(userRepository.loginAvailable(login)) {
            ret.put("available", "true");
        }else {
            ret.put("available", "false");
        }
        return ret;
    }
    
}
