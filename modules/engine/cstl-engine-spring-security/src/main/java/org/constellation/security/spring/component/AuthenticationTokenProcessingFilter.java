package org.constellation.security.spring.component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.constellation.security.spring.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

@Component
public class AuthenticationTokenProcessingFilter extends GenericFilterBean {

    private static final Logger LOGGER = LoggerFactory.getLogger("token");

    @Autowired
    private UserDetailsService userService;

    @Autowired
    private TokenService tokenService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = this.getAsHttpRequest(request);

        UserDetails userDetails = fromToken(httpRequest);
        if (userDetails == null)
            userDetails = fromBasicAuth(httpRequest);

        if (userDetails != null) {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                    userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        chain.doFilter(request, response);
    }

    private UserDetails fromBasicAuth(HttpServletRequest httpRequest) {
        String userName = basicAuth(httpRequest);
        if (userName == null)
            return null;
        return this.userService.loadUserByUsername(userName);

    }

    private UserDetails fromToken(HttpServletRequest httpRequest) {
        String authToken = this.extractAuthTokenFromRequest(httpRequest);
        if (authToken != null) {
            String userName = tokenService.getUserNameFromToken(authToken);
            if (tokenService.validateToken(authToken, userName)) {
                return this.userService.loadUserByUsername(userName);
            }
        }
        return null;
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

    private HttpServletRequest getAsHttpRequest(ServletRequest request) {
        if (!(request instanceof HttpServletRequest)) {
            throw new RuntimeException("Expecting an HTTP request");
        }

        return (HttpServletRequest) request;
    }

    private String extractAuthTokenFromRequest(HttpServletRequest httpRequest) {

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