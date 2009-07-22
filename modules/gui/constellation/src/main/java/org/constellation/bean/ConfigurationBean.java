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
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
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
    protected static final Logger logger = Logger.getLogger("org.constellation.bean");

    /**
     * A servlet context allowing to find the path to deployed file.
     */
    private ServletContext servletContext;
    
    private HttpServletRequest servletRequest;

    public ConfigurationBean() {
        // we get the sevlet context to read the capabilities files in the deployed war
        final FacesContext context = FacesContext.getCurrentInstance();
        servletContext = (ServletContext) context.getExternalContext().getContext();
        servletRequest = (HttpServletRequest) context.getExternalContext().getRequest();
    }

    protected String getConfigurationURL() {
        return servletRequest.getScheme() + "://" + servletRequest.getServerName() + ":" + servletRequest.getServerPort() + servletContext.getContextPath();
    }

    protected void refreshServletRequest() {
        final FacesContext context = FacesContext.getCurrentInstance();
        servletRequest = (HttpServletRequest) context.getExternalContext().getRequest();
    }
    
    public void restartServices() {
        logger.info("GUI restart services");
        refreshServletRequest();
        String url = getConfigurationURL() + "/WS/configuration?request=restart";
        logger.info(url);
        performRequest(url);
    }

    protected String performRequest(String url) {
        try {

            final URL source          = new URL(url);
            final URLConnection conec = source.openConnection();

            // we get the response document
            final InputStream in   = conec.getInputStream();
            final StringWriter out = new StringWriter();
            final byte[] buffer    = new byte[1024];
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
}
