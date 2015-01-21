package org.constellation.services.security;

import javax.servlet.http.HttpServletRequest;

import org.constellation.engine.security.UserDetailsExtractor;
import org.constellation.token.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.util.StringUtils;

public class CstlUserDetailsExtractor implements UserDetailsExtractor{

    private static final Logger LOGGER = LoggerFactory.getLogger(CstlUserDetailsExtractor.class);

    
    private UserDetailsService userDetailsService;
    
    private TokenService tokenService;
    
    @Override
    public UserDetails userDetails(HttpServletRequest httpServletRequest) {

        UserDetails userDetails = fromToken(httpServletRequest);
        if (userDetails == null )
            userDetails = fromBasicAuth(httpServletRequest);
        return userDetails;
    }
    
    
    public UserDetailsService getUserDetailsService() {
        return userDetailsService;
    }


    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }


    public TokenService getTokenService() {
        return tokenService;
    }


    public void setTokenService(TokenService tokenService) {
        this.tokenService = tokenService;
    }


    private UserDetails fromBasicAuth(HttpServletRequest httpRequest) {
        String userName = basicAuth(httpRequest);
        if (userName == null)
            return null;
        return userDetailsService.loadUserByUsername(userName);

    }

    private UserDetails fromToken(HttpServletRequest httpRequest) {
        String userName = tokenService.getUserName(httpRequest);
        if (userName == null)
            return null;
        return userDetailsService.loadUserByUsername(userName);
    }

    private String basicAuth(HttpServletRequest httpRequest) {
        String header = httpRequest.getHeader("Authorization");
        if (StringUtils.hasLength(header) && header.length() > 6) {
            assert header.substring(0, 6).equals("Basic ");
            // will contain "Ym9iOnNlY3JldA=="
            String basicAuthEncoded = header.substring(6);
            // will contain "bob:secret"
            String basicAuthAsString = new String(Base64.decode(basicAuthEncoded.getBytes()));

            int indexOf = basicAuthAsString.indexOf(':');
            if (indexOf != -1) {
                String username = basicAuthAsString.substring(0, indexOf);
                LOGGER.debug("Basic auth: " + username);
                return username;
            }
        }
        return null;
    }


}
