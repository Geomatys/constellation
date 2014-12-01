package org.constellation.token;

import javax.servlet.http.HttpServletRequest;


public interface TokenService {

    String createToken(String username);

    String getUserNameFromToken(String authToken);

    boolean validateToken(String authToken, String username);

    String extractAuthTokenFromRequest(HttpServletRequest httpRequest);

}
