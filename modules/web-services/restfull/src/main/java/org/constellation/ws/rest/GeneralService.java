package org.constellation.ws.rest;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.constellation.engine.register.repository.UserRepository;



@Path("/1/general")
public class GeneralService {

    @Inject
    private UserRepository userRepository;
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/counts")
    public Map<String, Integer> usercount() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("nbuser", userRepository.countUser());
        return map;
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/logincheck")
    public Map<String, String> loginAvailable(String login) {
        Map<String, String> ret = new HashMap<String, String>();
        if(userRepository.loginAvailable(login)) {
            ret.put("available", "true");
        }else {
            ret.put("available", "false");
        }
        return ret;
    }
    
}
