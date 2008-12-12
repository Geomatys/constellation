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

import java.io.IOException;
import java.util.logging.Logger;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.constellation.wms.AbstractWMSCapabilities;
import org.constellation.ws.WebServiceException;


/**
 * Builds a {@code REST Web Map Service} request {@code GetCapabilities} on a server,
 * and send it to the {@link Dispatcher}.
 *
 * @version $Id$
 * @author Cédric Briançon (Geomatys)
 */
public class Worker {
    /**
     * The url of the WMS web service, where the request will be sent.
     */
    private final String WMSURL = "http://solardev:8080/constellation/WS/wms";

    /**
     * Defines whether it is a REST or SOAP request.
     */
    private final boolean isRest = true;

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
     * @throws IOException if an error occurs at the URL creation.
     */
    public Worker(final Marshaller marshaller, final Unmarshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
        this.  marshaller =   marshaller;

        dispatcher = new Dispatcher(WMSURL, isRest, marshaller, unmarshaller);
    }

    public AbstractWMSCapabilities launchGetCapabilities(final String service, final String request,
                                                         final String version) throws WebServiceException
    {
        return dispatcher.requestGetCapabilities(service, request, version);
    }
}
