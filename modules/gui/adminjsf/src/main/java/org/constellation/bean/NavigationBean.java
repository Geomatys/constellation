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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import org.geotoolkit.util.logging.Logging;

/**
 *
 * @author Leo Pratlong (Geomatys)
 * @author Johann sorel (Geomatys)
 */
public class NavigationBean {
    
    private static final Logger LOGGER = Logging.getLogger("org.constellation.bean");
    static final String AUTH_FILE_PATH = "WEB-INF/authentication.properties";
    
    private String login = "";
    private String password = "";

    public String authentify() {
        //TODO login authentification should be handle by services
        try{
            final Properties properties = getProperties();
            
            if(password.equals(properties.getProperty(login))){
                return "login";
            }
        }catch(IOException ex){
            LOGGER.log(Level.WARNING,"Failed to read authorization file.",ex);
        }

        return "failed";
    }
    
    private static Properties getProperties() throws IOException {
        final Properties properties = new Properties();
        InputStream inputStream = null;
        final FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            final ExternalContext externalContext = context.getExternalContext();
            if (externalContext != null) {
                final ServletContext sc = (ServletContext) externalContext.getContext();
                if (sc != null) {
                    try {
                        inputStream = new FileInputStream(sc.getRealPath(AUTH_FILE_PATH));
                    } catch (FileNotFoundException e) {
                        LOGGER.log(Level.SEVERE, "No configuration file found.");
                    }
                }
            }
        }

        if (inputStream == null) {
            inputStream = NavigationBean.class.getResourceAsStream(AUTH_FILE_PATH);
        }

        if (inputStream != null) {
            try {
                properties.load(inputStream);
            } finally {
                inputStream.close();
            }
        }
        return properties;
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

}
