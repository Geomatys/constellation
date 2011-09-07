/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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

package org.constellation.menu.system;

import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import org.constellation.admin.service.ConstellationServer;
import org.geotoolkit.util.logging.Logging;
import org.mapfaces.i18n.I18NBean;

/**
 * Bean for general constellation configuration.
 * 
 * @author Johann Sorel (Geomatys)
 * @author Guilhem Legal (Geomatys)
 */
public class CstlBean extends I18NBean {

    private static final Logger LOGGER = Logging.getLogger(CstlBean.class);
    /**
     * When user is log in, a ServiceAdministrator object is added in the session map.
     */
    public static final String SERVICE_ADMIN_KEY = "serviceAdmin";
    
    private String configurationDirectory;
    
    private String userName;
    
    private String password;
    
    protected ConstellationServer getServer(){
        return (ConstellationServer) FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap().get(SERVICE_ADMIN_KEY);
    }
    
    public String getConfigurationDirectory(){
        if (configurationDirectory == null) {
            final ConstellationServer server = getServer();
            if (server != null) {
                configurationDirectory = server.getConfigurationPath();
            }
        }
        return configurationDirectory;
    }

    public void setConfigurationDirectory(final String path){
        this.configurationDirectory = path;
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        final ConstellationServer server = getServer();
        if (server != null) {
            return server.getUserName();
        }
        return null;
    }

    /**
     * @param userName the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    public void save() {
        // Set the new user directory
        if (configurationDirectory != null && !configurationDirectory.isEmpty()) {
            //reload services
            final ConstellationServer server = getServer();
            if (server != null) {
                final String oldConfigDirectory = server.getConfigurationPath();
                if (!oldConfigDirectory.equals(configurationDirectory)) {
                    LOGGER.info("updating configuration Path");
                    server.setConfigurationPath(configurationDirectory);
                    server.services.restartAll();
                    server.providers.restartAllLayerProviders();
                    server.providers.restartAllStyleProviders();
                }
            }
        }
        // save the new login / password
        if (userName != null && !userName.isEmpty() && password != null && !password.isEmpty()) {
            final ConstellationServer server = getServer();
            if (server != null) {
                LOGGER.info("updating User");
                server.updateUser(userName, password, server.currentUser);
                /*
                 * we must disconnect the user
                 */
                FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(SERVICE_ADMIN_KEY);
                //redirect
                //the session is not logged, redirect him to the authentication page
                FacesContext.getCurrentInstance().getViewRoot().setViewId("/authentication.xhtml");
            }
        }
    }
}
