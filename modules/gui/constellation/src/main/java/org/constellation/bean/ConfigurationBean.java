/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2009, Geomatys
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
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Guilhem Legal
 */
public class ConfigurationBean {

    /**
     * Debugging purpose
     */
    private Logger logger = Logger.getLogger("org.constellation.bean");

    /**
     * A servlet context allowing to find the path to deployed file.
     */
    private ServletContext servletContext;
    
    private HttpServletRequest servletRequest;

    private List<SelectItem> synchroneMode;

    private String currentSynchroneMode;

    private String serviceIdentifier;

    public ConfigurationBean() {
        // we get the sevlet context to read the capabilities files in the deployed war
        FacesContext context = FacesContext.getCurrentInstance();
        servletContext = (ServletContext) context.getExternalContext().getContext();
        servletRequest = (HttpServletRequest) context.getExternalContext().getRequest();

        synchroneMode = new ArrayList<SelectItem>();
        synchroneMode.add(new SelectItem("synchrone", "synchrone"));
        synchroneMode.add(new SelectItem("asynchrone", "asynchrone"));
        currentSynchroneMode = "synchrone";
    }

    private String getConfigurationURL() {
        return servletRequest.getScheme() + "://" + servletRequest.getServerName() + ":" + servletRequest.getServerPort() + servletContext.getContextPath();
    }

    private void refreshServletRequest() {
        FacesContext context = FacesContext.getCurrentInstance();
        servletRequest = (HttpServletRequest) context.getExternalContext().getRequest();
    }
    
    public void restartServices() {
        logger.info("GUI restart services");
        refreshServletRequest();
        String URL = getConfigurationURL() + "/WS/configuration?request=restart";
        logger.info(URL);
        String response = performRequest(URL);
    }

    public void generateIndex() {
        logger.info("GUI refresh index");
        refreshServletRequest();
        String URL = getConfigurationURL() + "/WS/configuration?request=refreshIndex&asynchrone=";
        if (currentSynchroneMode.equals("synchrone"))
            URL = URL + "false";
        else
            URL = URL + "true";
        
        logger.info(URL);
        String response = performRequest(URL);
    }

    public void addToIndex() {
        logger.info("GUI add to index");
        refreshServletRequest();
        String URL = getConfigurationURL() + "/WS/configuration?request=addToIndex";
        logger.info(URL);
        String response = performRequest(URL);
    }

    public void resfreshContact() {
        logger.info("GUI refresh contact index");
        refreshServletRequest();
        String URL = getConfigurationURL() + "/WS/configuration?request=resfreshContact";
        logger.info(URL);
        String response = performRequest(URL);
    }

    public void resfreshVocabulary() {
        logger.info("GUI refresh vocabulary index");
        refreshServletRequest();
        String URL = getConfigurationURL() + "/WS/configuration?request=resfreshVocabulary";
        logger.info(URL);
        String response = performRequest(URL);
    }


    private String performRequest(String url) {
        try {

            URL source = new URL(url);
            URLConnection conec = source.openConnection();

            // we get the response document
            InputStream in = conec.getInputStream();
            StringWriter out = new StringWriter();
            byte[] buffer = new byte[1024];
            int size;

            while ((size = in.read(buffer, 0, 1024)) > 0) {
                out.write(new String(buffer, 0, size));
            }

            return out.toString();

        } catch (MalformedURLException ex) {
            logger.severe("Malformed URL exception: " + url);
        } catch (IOException ex) {
            logger.severe("IO exception: " + ex.getMessage());
        }
        return null;
    }

    /**
     * @return the synchroneMode
     */
    public List<SelectItem> getSynchroneMode() {
        return synchroneMode;
    }

    /**
     * @param synchroneMode the synchroneMode to set
     */
    public void setSynchroneMode(List<SelectItem> synchroneMode) {
        this.synchroneMode = synchroneMode;
    }

    /**
     * @return the currentSynchroneMode
     */
    public String getCurrentSynchroneMode() {
        return currentSynchroneMode;
    }

    /**
     * @param currentSynchroneMode the currentSynchroneMode to set
     */
    public void setCurrentSynchroneMode(String currentSynchroneMode) {
        this.currentSynchroneMode = currentSynchroneMode;
    }

    /**
     * @return the serviceIdentifier
     */
    public String getServiceIdentifier() {
        return serviceIdentifier;
    }

    /**
     * @param serviceIdentifier the serviceIdentifier to set
     */
    public void setServiceIdentifier(String serviceIdentifier) {
        this.serviceIdentifier = serviceIdentifier;
    }
}
