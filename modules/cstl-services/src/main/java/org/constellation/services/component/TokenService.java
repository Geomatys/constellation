package org.constellation.services.component;

import org.constellation.configuration.AppProperty;
import org.constellation.configuration.Application;
import org.constellation.token.TokenExtender;
import org.constellation.token.TokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

public class TokenService implements TokenExtender {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenService.class);

    private String secret = "cstl-sdi";

    @Inject
    private Environment env;

    @PostConstruct
    public void init() {
        secret = Application.getProperty(AppProperty.CSTL_TOKEN_SECRET, UUID.randomUUID().toString());
    }
    
    public String createToken(String username) {
        return TokenUtils.createToken(username, secret);
    }
    
    public boolean validate(String access_token) {
        return TokenUtils.validateToken(access_token, secret);
    }
    
    

    private String getUserName(String access_token) {
        return TokenUtils.getUserNameFromToken(access_token);
    }

    
    public String getUserName(HttpServletRequest request) {
        String token = TokenUtils.extractAccessToken(request);
        //FIXME We should use cache here.
        if (token == null)
            return null;
        if (validate(token))
            return getUserName(token);
        return null;
    }


    @Override
    public String extend(String token, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        return TokenUtils.createToken(token, secret);
    }

}
