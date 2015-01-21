package org.constellation.engine.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface UnauthorizedHandler {
    /**
     * Handles unauthoriszed request.
     * @param httpServletRequest
     * @param httpServletResponse
     * @return true if response is redirected (ended).
     */
    boolean onUnauthorized(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse);
}
