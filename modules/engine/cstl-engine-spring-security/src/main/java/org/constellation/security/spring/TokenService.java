package org.constellation.security.spring;


public interface TokenService {

    String createToken(String username);

    String getUserNameFromToken(String authToken);

    boolean validateToken(String authToken, String username);

}
