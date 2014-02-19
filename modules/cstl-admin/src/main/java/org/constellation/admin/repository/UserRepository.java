package org.constellation.admin.repository;

import javax.inject.Inject;
import javax.inject.Named;

import org.constellation.engine.register.UserDTO;
import org.constellation.gui.admin.conf.CstlConfig;
import org.springframework.web.client.RestTemplate;


/**
 * Spring Data JPA repository for the User entity.
 */
@Named
public class UserRepository {
    
    @Inject
    private CstlConfig cstlConfig;
    
    public UserDTO findOne(String login) {
    
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(cstlConfig.getUrl() +  "spring/user/" + login, UserDTO.class);
        
        
    }

    public void save(UserDTO currentUser) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.put(cstlConfig.getUrl() +  "spring/user/" + currentUser.getLogin(), currentUser);
    }

}
