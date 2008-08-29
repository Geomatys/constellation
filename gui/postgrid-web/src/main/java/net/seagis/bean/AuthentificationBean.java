/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package net.seagis.bean;

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
    private Logger logger = Logger.getLogger("net.seagis.bean");
    
    private String login;
    
    private String password;
    
    private HttpServletResponse response;
    
    public AuthentificationBean() {
        
        // we get the sevlet context to read the capabilities files in the deployed war
        FacesContext context = FacesContext.getCurrentInstance();
        response = (HttpServletResponse) context.getExternalContext().getResponse();
        
    }
    
    public String authentify() {
        Cookie cookie = new Cookie("authent", login + ':' + password);
        response.addCookie(cookie);
        return "authentified";
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
