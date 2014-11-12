package org.constellation.services.component;

import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.constellation.security.spring.TokenService;
import org.constellation.token.TokenUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class TokenServiceImpl implements TokenService {

    private String secret = "cstl-sdi";
    
    @Inject
    private Environment env;
    
    @PostConstruct
    public void init() {
        secret = env.getProperty("cstl.secret", UUID.randomUUID().toString());
    }
    
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
