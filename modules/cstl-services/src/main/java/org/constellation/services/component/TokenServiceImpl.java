package org.constellation.services.component;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.constellation.token.TokenService;
import org.constellation.token.TokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class TokenServiceImpl implements TokenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenService.class);
    
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
    
    public String extractAuthTokenFromRequest(HttpServletRequest httpRequest) {

        String authToken = headers(httpRequest);
        if (authToken != null) {
            return authToken;
        }

        authToken = cookie(httpRequest);
        if (authToken != null) {
            return authToken;
        }

        authToken = queryString(httpRequest);
        if (authToken != null) {
            return authToken;
        }

        return null;

    }

    private String queryString(HttpServletRequest httpRequest) {

        /*
         * If token not found get it from request query string 'token' parameter
         */
        String queryString = httpRequest.getQueryString();
        if (StringUtils.hasText(queryString)) {
            int tokenIndex = queryString.indexOf("token=");
            if (tokenIndex != -1) {
                tokenIndex += "token=".length();
                int tokenEndIndex = queryString.indexOf('&', tokenIndex);
                String authToken;
                if (tokenEndIndex == -1)
                    authToken = queryString.substring(tokenIndex);
                else
                    authToken = queryString.substring(tokenIndex, tokenEndIndex);
                LOGGER.debug("QueryString: " + authToken + " (" + httpRequest.getRequestURI() + ")");
                return authToken;
            }
        }
        return null;
    }

    private String cookie(HttpServletRequest httpRequest) {
        /* Extract from cookie */
        Cookie[] cookies = httpRequest.getCookies();
        if (cookies != null)
            for (Cookie cookie : cookies) {
                if ("authToken".equals(cookie.getName())) {
                    try {
                        String authToken = URLDecoder.decode(cookie.getValue(), "UTF-8");
                        LOGGER.debug("Cookie: " + authToken + " (" + httpRequest.getRequestURI() + ")");
                        return authToken;
                    } catch (UnsupportedEncodingException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }
        return null;
    }

    private String headers(HttpServletRequest httpRequest) {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        if (authToken != null) {
            LOGGER.debug("Header: " + authToken + " (" + httpRequest.getRequestURI() + ")");
            return authToken;
        }
        return authToken;
    }
    
}
