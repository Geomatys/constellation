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
import org.constellation.security.spring.TokenService;
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
    
    static class Login {
        private String username;
        public String getUsername() {
            return username;
        }
        public void setUsername(String username) {
            this.username = username;
        }
        public String getPassword() {
            return password;
        }
        public void setPassword(String password) {
            this.password = password;
        }
        private String password;
    }
    
    @Autowired
    private TokenService tokenService;
    

	@Autowired
	private UserDetailsService userService;
	
	@Autowired
	private UserRepository userRepository;
	
	 
    @Autowired
    private DomainRepository domainRepository;

	@Autowired
	@Qualifier("authenticationManager")
	private AuthenticationManager authManager;


	/**
	 * Retrieves the currently logged in user.
	 * 
	 * @return A transfer containing the username and the roles.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public UserTransfer getUser()
	{
		UserDetails userDetails = extractUserDetail();
		
		return new UserTransfer(userDetails.getUsername(), this.createRoleMap(userDetails));
	}


    private UserDetails extractUserDetail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Object principal = authentication.getPrincipal();
		if (principal instanceof String && ((String) principal).equals("anonymousUser")) {
			throw new WebApplicationException(401);
		}
		UserDetails userDetails = (UserDetails) principal;
        return userDetails;
    }


	/**
	 * Authenticates a user and creates an authentication token.
	 * 
	 * @param username
	 *            The name of the user.
	 * @param password
	 *            The password of the user.
	 * @return A transfer containing the authentication token.
	 */
	@Path("authenticate")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response authenticate(@Context HttpServletRequest request,  Login login)
	{
		
		UsernamePasswordAuthenticationToken authenticationToken =
				new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword());
		
		try {
		    Authentication authentication = this.authManager.authenticate(authenticationToken);
		    SecurityContextHolder.getContext().setAuthentication(authentication);
		} catch (BadCredentialsException e){
		    
		    return Response.status(Status.FORBIDDEN).build();
		}

		/*
		 * Reload user as password of authentication principal will be null after authorization and
		 * password is needed for token generation
		 */
		UserDetails userDetails = this.userService.loadUserByUsername(login.getUsername());

		String createToken = tokenService.createToken(userDetails.getUsername());
		
		
        Optional<CstlUser> findOne = userRepository.findOne(userDetails.getUsername());
        
        int id = findOne.get().getId();
        Domain defaultDomain = domainRepository.findDefaultByUserId(id);
        
        if(defaultDomain==null) {
            //No domain associated.
            return Response.status(Status.FORBIDDEN).build();
        }
		
		return Response.ok(new TokenTransfer(createToken, id, defaultDomain.getId())).build();
	}
	

	@Path("extendToken")
	@GET
	public String extendToken() {
	    UserDetails userDetails = extractUserDetail();

	    return tokenService.createToken(userDetails.getUsername());
	    
	}
	
	@Path("logout")
	@DELETE
	public void logout(@Context HttpServletRequest request, @Context HttpServletResponse response){
		request.getSession().invalidate();
		response.setStatus(HttpServletResponse.SC_NO_CONTENT);
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