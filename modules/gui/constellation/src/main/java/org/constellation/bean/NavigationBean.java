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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

/**
 *
 * @author Leo Pratlong
 */
public class NavigationBean {
    
    /**
     * Debugging purpose
     */
    private static final Logger LOGGER = Logger.getLogger("org.constellation.bean");
    private static final String AUTH_FILE_PATH = "WEB-INF/authentication.properties";
    private static final String HOME_HREF = "pages/home.xhtml";
    private static final String SERVICES_HREF = "pages/services.xhtml";
    private static final String USERCONFIG_HREF = "pages/configuration.xhtml";
    
    private String login;
    private String passwd;
    private boolean authenticated;
    private AUTHENTICATIONERROR authenticationError;
    
    private String newLogin;
    private String newPasswd1;
    private String newPasswd2;
    
    
    private String currentHref = HOME_HREF;
    
    public NavigationBean() {
    	
    }
    
    private static enum AUTHENTICATIONERROR {
    	MISMATCH,
    	CONFIGFILENOTFOUND,
    	EMPTYFIELD,
    	SUCCESS
    }
    
    public void authentify() throws IOException {
    	final Properties properties = getProperties();
		
		if (login.isEmpty() || passwd.isEmpty()) {
			authenticationError = AUTHENTICATIONERROR.EMPTYFIELD;
		} else {
			if (checkEqualProperty(properties, "user", login) && checkEqualProperty(properties, "passwd", passwd)) {
				authenticated = true;
				authenticationError = AUTHENTICATIONERROR.SUCCESS;
			} else {
				authenticated = false;
				authenticationError = AUTHENTICATIONERROR.MISMATCH;
			}
		}
    }
    
    public void changePasswd() throws IOException {
    	if (checkEqualProperty(getProperties(), "passwd", passwd)) {
    		if (newPasswd1.isEmpty() || newLogin.isEmpty()) {
    			// error
    		} else {
    			if (newPasswd1.equals(newPasswd2)) {
    				final Properties properties = new Properties();
    				properties.setProperty("user", newLogin);
    				properties.setProperty("passwd", newPasswd1);
    				
    				final ServletContext sc = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
    				
    				final File file = new File(sc.getRealPath(AUTH_FILE_PATH));
    				final FileOutputStream fos = new FileOutputStream(file);
    				properties.store(fos, "Config");
    			}
    		}
    	} else {
    		// error
    	}
    }
    
    public void logout() {
    	authenticated = false;
    	currentHref = HOME_HREF;
    }
    
    public void goToServices() {
    	currentHref = SERVICES_HREF;
    }
    
    public void goToUserConfiguration() {
    	currentHref = USERCONFIG_HREF;
    }

    private boolean checkEqualProperty(final Properties properties, final String property, final String value) {
    	if ((properties != null) && (property != null) && (value != null)) {
	    	final String cfgProperty = properties.getProperty(property);
			if (value.equals(cfgProperty)) {
				return true;
			} else {
				return false;
			}
	    }
    	return false;
    }
    
    private Properties getProperties() throws IOException {
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
						authenticationError = AUTHENTICATIONERROR.CONFIGFILENOTFOUND;
					}
    			}
    		}
    	}
    	
    	if (inputStream == null) {
    		inputStream = getClass().getResourceAsStream(AUTH_FILE_PATH);
    	}
    	
    	if (inputStream == null) {
    		authenticationError = AUTHENTICATIONERROR.CONFIGFILENOTFOUND;
    	} else {
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

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String password) {
        this.passwd = password;
    }

	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}

	public boolean isAuthenticated() {
		return authenticated;
	}

	public void setAuthenticationError(AUTHENTICATIONERROR authenticationError) {
		this.authenticationError = authenticationError;
	}

	public AUTHENTICATIONERROR getAuthenticationError() {
		return authenticationError;
	}

	public void setCurrentHref(String currentHref) {
		this.currentHref = currentHref;
	}

	public String getCurrentHref() {
		return currentHref;
	}

	public void setNewLogin(String newLogin) {
		this.newLogin = newLogin;
	}

	public String getNewLogin() {
		return newLogin;
	}

	public void setNewPasswd1(String newPasswd1) {
		this.newPasswd1 = newPasswd1;
	}

	public String getNewPasswd1() {
		return newPasswd1;
	}

	public void setNewPasswd2(String newPasswd2) {
		this.newPasswd2 = newPasswd2;
	}

	public String getNewPasswd2() {
		return newPasswd2;
	}

}
