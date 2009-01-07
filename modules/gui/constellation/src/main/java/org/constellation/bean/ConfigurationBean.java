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
    private Logger logger = Logger.getLogger("org.constellation.bean");

    /**
     * A servlet context allowing to find the path to deployed file.
     */
    private ServletContext servletContext;
    
    private HttpServletRequest servletRequest;

    public ConfigurationBean() {
        // we get the sevlet context to read the capabilities files in the deployed war
        FacesContext context = FacesContext.getCurrentInstance();
        servletContext = (ServletContext) context.getExternalContext().getContext();

        servletRequest = (HttpServletRequest) context.getExternalContext().getRequest();
    }
    
    public void restartServices() {
        logger.info("GUI restart services");
        String URL = "";
        FacesContext context = FacesContext.getCurrentInstance();
        servletRequest = (HttpServletRequest) context.getExternalContext().getRequest();

        try {
            URL = servletRequest.getScheme() + "://" + servletRequest.getServerName() + ":" + servletRequest.getServerPort() + servletContext.getContextPath() + "/WS/configuration?request=restart";
            System.out.println(URL);
            URL source = new URL(URL);
            URLConnection conec = source.openConnection();

            // we get the response document
            InputStream in = conec.getInputStream();
            StringWriter out = new StringWriter();
            byte[] buffer = new byte[1024];
            int size;

            while ((size = in.read(buffer, 0, 1024)) > 0) {
                out.write(new String(buffer, 0, size));
            }

            String response = out.toString();

        } catch (MalformedURLException ex) {
            logger.severe("Malformed URL exception: " + URL);
        } catch (IOException ex) {
            logger.severe("IO exception");
        }
    }

    public void generateIndex() {
        logger.info("GUI generate index");
        String URL = "";
        FacesContext context = FacesContext.getCurrentInstance();
        servletRequest = (HttpServletRequest) context.getExternalContext().getRequest();

        try {
            URL = servletRequest.getScheme() + "://" + servletRequest.getServerName() + ":" + servletRequest.getServerPort() + servletContext.getContextPath() + "/WS/configuration?request=refreshIndex";
            URL source = new URL(URL);
            URLConnection conec = source.openConnection();

            // we get the response document
            InputStream in = conec.getInputStream();
            StringWriter out = new StringWriter();
            byte[] buffer = new byte[1024];
            int size;

            while ((size = in.read(buffer, 0, 1024)) > 0) {
                out.write(new String(buffer, 0, size));
            }

            String response = out.toString();

        } catch (MalformedURLException ex) {
            logger.severe("Malformed URL exception: " + URL);
        } catch (IOException ex) {
            logger.severe("IO exception");
        }
    }
}
