package org.constellation.engine.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.GenericFilterBean;

public class AuthenticationTokenProcessingFilter extends GenericFilterBean {


    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationTokenProcessingFilter.class);

    private UnauthorizedHandler unauthorizedHandler = new UnauthorizedHandler() {

        @Override
        public boolean onUnauthorized(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
            return allowUnauthorized;
        }
    };

    private boolean allowUnauthorized;

    private UserDetailsExtractor userDetailsExtractor;

    public void setAllowUnauthorized(boolean allowUnauthorized) {
        this.allowUnauthorized = allowUnauthorized;
    }

    public void setUnauthorizedHandler(UnauthorizedHandler unauthorizedHandler) {
        this.unauthorizedHandler = unauthorizedHandler;
    }

    public UnauthorizedHandler getUnauthorizedHandler() {
        return unauthorizedHandler;
    }

    public void setUserDetailsExtractor(UserDetailsExtractor userDetailsExtractor) {
        this.userDetailsExtractor = userDetailsExtractor;
    }

    public UserDetailsExtractor getUserDetailsExtractor() {
        return userDetailsExtractor;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = getAsHttpRequest(request);

        UserDetails userDetails = userDetailsExtractor.userDetails(httpRequest);

        if (userDetails == null) {
            if(!unauthorizedHandler.onUnauthorized(httpRequest, getAsHttpResponse(response))) {
                LOGGER.warn("ATPF: unauthorized for URI:" + httpRequest.getRequestURI());
                getAsHttpResponse(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            chain.doFilter(request, response);
            return;
        }

        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug(userDetails.getUsername() + ": " + userDetails.getAuthorities());
        }

        try {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null,userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            chain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }

    }

    private HttpServletRequest getAsHttpRequest(ServletRequest request) {
        if (!(request instanceof HttpServletRequest)) {
            throw new RuntimeException("Expecting an HTTP request");
        }

        return (HttpServletRequest) request;
    }

    private HttpServletResponse getAsHttpResponse(ServletResponse response) {
        if (!(response instanceof HttpServletResponse)) {
            throw new RuntimeException("Expecting an HTTP response");
        }

        return (HttpServletResponse) response;
    }

}