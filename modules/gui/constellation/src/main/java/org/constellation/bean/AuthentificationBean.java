/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.bean;

import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Guilhem Legal
 */
public class AuthentificationBean {
    
    /**
     * Debugging purpose
     */
    private static final Logger LOGGER = Logger.getLogger("org.constellation.bean");
    
    private String login;
    
    private String password;
    
    private HttpServletResponse response;
    
    private String contextPath;
    
    public AuthentificationBean() {
        
        // we get the sevlet context to read the capabilities files in the deployed war
        final FacesContext context = FacesContext.getCurrentInstance();
        response    = (HttpServletResponse) context.getExternalContext().getResponse();
        contextPath = ((ServletContext)context.getExternalContext().getContext()).getContextPath();
        
    }
    
    public String authentify() {
        
        //TODO remove this ugly thing
        if (login != null && login.equals("admin") && password != null && password.equals("admin")) {
            final Cookie cookie = new Cookie("authent", login + ':' + password);
            cookie.setPath(contextPath);
            response.addCookie(cookie);
            LOGGER.info("cookie added with value:" + login + ':' + password);
            return "authentified";
        } else {
            return "notAllowed";
        }
        
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
