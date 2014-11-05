package org.constellation.services.component;

import org.constellation.security.spring.TokenService;
import org.constellation.token.TokenUtils;
import org.springframework.stereotype.Component;

@Component
public class TokenServiceImpl implements TokenService {

    private String secret = "cstl-sdi";
    
    public String createToken(String username) {
        return TokenUtils.createToken(username, secret);
    }

    @Override
    public String getUserNameFromToken(String authToken) {
        return TokenUtils.getUserNameFromToken(authToken);
    }

    @Override
    public boolean validateToken(String authToken, String username) {
        return TokenUtils.validateToken(authToken, username, secret);
    }
    
    
}
