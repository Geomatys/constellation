/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2011, Geomatys
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.servlet.http.HttpServletRequest;

import org.constellation.admin.service.ConstellationServer;
import org.geotoolkit.util.logging.Logging;
import org.mapfaces.i18n.I18NBean;

/**
 *
 * @author Leo Pratlong (Geomatys)
 * @author Johann sorel (Geomatys)
 */
public final class NavigationBean extends I18NBean{

    /**
     * When user is log in, a ServiceAdministrator object is added in the session map.
     */
    public static final String SERVICE_ADMIN_KEY = "serviceAdmin";
    
    private static final Logger LOGGER = Logging.getLogger("org.constellation.bean");
    
    private String login = "";
    private String password = "";

    public NavigationBean() {
        addBundle("org.constellation.bundle.base");
    }

    public String authentify() {
        final ConstellationServer serviceAdmin = ConstellationServer.login(getServiceURL(), login, password);
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(SERVICE_ADMIN_KEY, serviceAdmin);
        return (serviceAdmin != null) ? "login" : "failed";
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

    public void setPassword(final String password) {
        this.password = password;
    }

    public void logout(){
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(SERVICE_ADMIN_KEY);
    }
    
    /**
     * Check if the session is logged in, if not redirect to authentication page.
     */
    public void checkLogged(final PhaseEvent event){

        final ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        if(context.getSessionMap().get(SERVICE_ADMIN_KEY) == null){
            final String webapp = context.getRequestContextPath();
            try {
                //the session is not logged, redirect him to the authentication page
                context.redirect(webapp+"/authentication.jsf");
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        }
    }
    
    /**
     * Return the base URL of the web-services.
     */
    private static String getServiceURL() {
        final HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String result = null;
        try {
            final String pathUrl = request.getRequestURL().toString();
            final URL url = new URL(pathUrl);
            result = url.getProtocol() + "://" + url.getAuthority() + request.getContextPath() + "/WS/";
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        return result;
    }
}
