package org.constellation.services.component;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.constellation.token.TokenExtender;
import org.constellation.token.TokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

public class TokenService implements TokenExtender {

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
    
    public boolean validate(String access_token) {
        return TokenUtils.validateToken(access_token, secret);
    }
    
    

    private String getUserName(String access_token) {
        return TokenUtils.getUserNameFromToken(access_token);
    }

    
    public String getUserName(HttpServletRequest request) {
        String token = extractToken(request);
        //FIXME We should use cache here.
        if (token == null)
            return null;
        if (validate(token))
            return getUserName(token);
        return null;
    }

   

    private String extractToken(HttpServletRequest httpRequest) {

        String access_token = headers(httpRequest);
        if (access_token != null) {
            return access_token;
        }

        access_token = cookie(httpRequest);
        if (access_token != null) {
            return access_token;
        }

        access_token = queryString(httpRequest);
        if (access_token != null) {
            return access_token;
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
                String access_token;
                if (tokenEndIndex == -1)
                    access_token = queryString.substring(tokenIndex);
                else
                    access_token = queryString.substring(tokenIndex, tokenEndIndex);
                LOGGER.debug("QueryString: " + access_token + " (" + httpRequest.getRequestURI() + ")");
                return access_token;
            }
        }
        return null;
    }

    private String cookie(HttpServletRequest httpRequest) {
        /* Extract from cookie */
        Cookie[] cookies = httpRequest.getCookies();
        if (cookies != null)
            for (Cookie cookie : cookies) {
                if ("access_token".equals(cookie.getName())) {
                    try {
                        String access_token = URLDecoder.decode(cookie.getValue(), "UTF-8");
                        LOGGER.debug("Cookie: " + access_token + " (" + httpRequest.getRequestURI() + ")");
                        return access_token;
                    } catch (UnsupportedEncodingException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }
        return null;
    }

    private String headers(HttpServletRequest httpRequest) {
        String access_token = httpRequest.getHeader("access_token");
        if (access_token != null) {
            LOGGER.debug("Header: " + access_token + " (" + httpRequest.getRequestURI() + ")");
            return access_token;
        }
        return access_token;
    }

    @Override
    public String extend(String token, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        return TokenUtils.createToken(token, secret);
    }

}
