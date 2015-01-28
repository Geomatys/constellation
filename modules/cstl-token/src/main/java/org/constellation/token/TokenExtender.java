package org.constellation.token;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface TokenExtender {
    String extend(String token, HttpServletRequest httpServletRequest,  HttpServletResponse httpServletResponse);
}
