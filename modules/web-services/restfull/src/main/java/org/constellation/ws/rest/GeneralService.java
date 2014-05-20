package org.constellation.ws.rest;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.constellation.engine.register.repository.UserRepository;



@Path("/1/general")
public class GeneralService {

    @Inject
    private UserRepository userRepository;
    
    @GET
    @Path("/counts")
    public Map<String, Integer> usercount() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("nbuser", userRepository.countUser());
        return map;
    }
    
}
