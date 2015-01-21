package org.constellation.engine.security;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.userdetails.UserDetails;

public interface UserDetailsExtractor {

    
    UserDetails userDetails(HttpServletRequest httpServletRequest);
    
}
