package org.constellation.rest.api;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.constellation.auth.transfer.TokenTransfer;
import org.constellation.auth.transfer.UserTransfer;
import org.constellation.engine.register.CstlUser;
import org.constellation.engine.register.Domain;
import org.constellation.engine.register.repository.DomainRepository;
import org.constellation.engine.register.repository.UserRepository;
import org.constellation.services.web.controller.AuthController;
import org.constellation.token.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import com.google.common.base.Optional;


@Component
@Path("/user")
public class UserResource
{
    
    
    
  
	
	@Autowired
	private UserRepository userRepository;
	
	 
    @Autowired
    private DomainRepository domainRepository;


	/**
	 * Retrieves the currently logged in user.
	 * 
	 * @return A transfer containing the username and the roles.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public UserTransfer getUser()
	{
		UserDetails userDetails = AuthController.extractUserDetail();
		
		return new UserTransfer(userDetails.getUsername(), this.createRoleMap(userDetails));
	}


    


	
	
	
	private Map<String, Boolean> createRoleMap(UserDetails userDetails)
	{
		Map<String, Boolean> roles = new HashMap<String, Boolean>();
		for (GrantedAuthority authority : userDetails.getAuthorities()) {
			roles.put(authority.getAuthority(), Boolean.TRUE);
		}

		return roles;
	}

}