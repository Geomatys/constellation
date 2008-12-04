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
package org.constellation.security.ws;

import org.constellation.security.client.Dispatcher;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.constellation.wms.AbstractWMSCapabilities;
import org.constellation.ws.ServiceVersion;
import org.constellation.ws.WebServiceException;


/**
 * Builds a {@code REST Web Map Service} request {@code GetCapabilities} on a server,
 * and send it to the {@link Dispatcher}.
 *
 * @version $Id$
 * @author Cédric Briançon (Geomatys)
 */
public class WmsWorker {
    /**
     * The dispatcher which will receive the request generated.
     */
    private final Dispatcher dispatcher;

    /**
     * The marshaller of the result given by the service.
     */
    private final Marshaller marshaller;

    /**
     * The unmarshaller of the result given by the service.
     */
    private final Unmarshaller unmarshaller;

    /**
     * The default logger.
     */
    private static final Logger LOGGER = Logger.getLogger("org.constellation.security.ws");

    /**
     * Builds a {@code GetCapabilities} request.
     *
     * @param marshaller
     * @param unmarshaller
     */
    public WmsWorker(final Marshaller marshaller, final Unmarshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
        this.  marshaller =   marshaller;
        URL url = null;
        try {
            url = new URL("http", "solardev", 8080, "/constellation/WS/wms?request=GetCapabilities&service=wms&version=1.1.1");
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.SEVERE, "Not a valid URL specified for the dispatcher.", ex);
        }
        dispatcher = (url == null) ? null : new Dispatcher(url, true, marshaller, unmarshaller);
    }

    public AbstractWMSCapabilities launchGetCapabilities(final ServiceVersion serviceVersion) throws IOException, WebServiceException {
        if (dispatcher == null) {
            throw new IOException("Dispatcher not well instanciated !");
        }
        return dispatcher.requestGetCapabilities(serviceVersion);
    }
}
