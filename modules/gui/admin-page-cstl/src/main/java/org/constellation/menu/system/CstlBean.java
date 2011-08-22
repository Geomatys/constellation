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

import javax.faces.context.FacesContext;
import org.constellation.admin.service.ConstellationServer;
import org.mapfaces.i18n.I18NBean;

/**
 * Bean for general constellation configuration.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class CstlBean extends I18NBean{

    /**
     * When user is log in, a ServiceAdministrator object is added in the session map.
     */
    public static final String SERVICE_ADMIN_KEY = "serviceAdmin";
    
    
    protected ConstellationServer getServer(){
        return (ConstellationServer) FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap().get(SERVICE_ADMIN_KEY);
    }
    
    public String getConfigurationDirectory(){
        final ConstellationServer server = getServer();
        if (server != null) {
            return server.getConfigurationPath();
        }
        return null;
    }

    public void setConfigurationDirectory(final String path){
        // Set the new user directory
        if (path != null && !path.isEmpty()) {
            //reload services
            final ConstellationServer server = getServer();
            if (server != null) {
                server.setConfigurationPath(path);
                server.services.restartAll();
                server.providers.restartAllLayerProviders();
                server.providers.restartAllStyleProviders();
            }
        }

    }

}
