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
package org.constellation.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.Unmarshaller;
import org.constellation.ws.Service;
import org.constellation.ws.ServiceVersion;


/**
 * Worker for a PEP (Policy Enforcement Point) which do the link between a client and
 * a webservice.
 *
 * @version $Id$
 * @author Cédric Briançon (Geomatys)
 */
public class PEPWorker {
    /**
     * The default logger.
     */
    public static final Logger LOGGER = Logger.getLogger("org.constellation.security");

    /**
     * The properties file allowing to store the id mapping between physical and database ID.
     */ 
    private final Properties props;

    /**
     * URL of the webservice to request.
     */
    private final String serviceURL;

    /**
     * The name of the webservice used.
     */
    private final ServiceVersion version;

    /**
     * The unmarshaller used to parse XML files, and to get the matching object.
     */
    public final Unmarshaller unmarshaller;

    /**
     * Creates a PEP that creates a request on the service, according to the request of the
     * client.
     *
     * @param unmarshaller The unmarshaller to use to parse the client response.
     */
    public PEPWorker(final Unmarshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
        props = new Properties();
        final File env = getSicadeDirectory();
        final File propFile = new File(env, "pep.properties");
        // TODO: use a JNDI resource if the .sicade file is not used.
        if (!propFile.exists()) {
            LOGGER.severe("Config file \"pep.properties\" not found in the .sicade directory.\n" +
                          "Now using the default configuration (localhost:8080/constellation/WS/wms");
            this.serviceURL = "http://localhost:8080/constellation/WS/wms";
            this.version = new ServiceVersion(Service.WMS, "1.1.1");
        } else {
            try {
                final FileInputStream in = new FileInputStream(propFile);
                props.load(in);
                in.close();
            } catch (IOException io) {
                LOGGER.log(Level.SEVERE, null, io);
            }
            this.serviceURL = props.getProperty("url");
            final String strService = props.getProperty("service");
            final String strVersion = props.getProperty("version");
            Service service;
            try {
                service = Service.valueOf(strService);
            } catch (IllegalArgumentException ex) {
                LOGGER.log(Level.INFO, "Service selected not found in the available choice", ex);
                service = Service.WMS;
            }
            this.version = new ServiceVersion(service, (strVersion == null) ? "1.1.1" : strVersion);
        }
    }

    /**
     * Generates a {GetCapabilities} request, send it to the webservice and returns the result.
     *
     * @return An {@link InputStream} that contains the result of a {@code GetCapabilities} request.
     * @throws IOException if an error occurs when doing the request on the webservice.
     */
    public InputStream getCapabilities() throws IOException {
        final StringBuilder builder = new StringBuilder();
        builder.append(serviceURL);
        if (!serviceURL.endsWith("?")) {
            builder.append("?");
        }
        builder.append("REQUEST=GetCapabilities&SERVICE=").append(version.getService().name());
        if (version != null) {
            builder.append("&VERSION=").append(version.toString());
        }
        final URL source = new URL(builder.toString());
        final URLConnection connection = source.openConnection();
        connection.setDoOutput(false);
        connection.setRequestProperty("Content-Type","text/xml");
        return connection.getInputStream();
    }

    /**
     * Returns the webservice url.
     */
    public String getServiceUrl() {
        return serviceURL;
    }

    /**
     * Returns a {@link ServiceVersion} of the requested webservice.
     */
    public ServiceVersion getServiceVersion() {
        return version;
    }

    /**
     * Return the ".sicade" directory.
     */
    private File getSicadeDirectory() {
        final String home = System.getProperty("user.home");
        return (System.getProperty("os.name", "").startsWith("Windows")) ?
             new File(home, "Application Data\\Sicade") :
             new File(home, ".sicade");
    }
}
