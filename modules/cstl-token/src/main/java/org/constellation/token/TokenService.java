package org.constellation.token;

import javax.servlet.http.HttpServletRequest;


public interface TokenService {

    String createToken(String username);

    String getUserName(String authToken);
    
    String getUserName(HttpServletRequest request);

    boolean validate(String authToken);

    String extractToken(HttpServletRequest httpRequest);

}
