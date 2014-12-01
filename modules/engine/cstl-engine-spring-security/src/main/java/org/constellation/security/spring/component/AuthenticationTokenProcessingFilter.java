package org.constellation.security.spring.component;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.constellation.token.TokenService;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenService.class);

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
        String authToken = tokenService.extractAuthTokenFromRequest(httpRequest);
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

   
}