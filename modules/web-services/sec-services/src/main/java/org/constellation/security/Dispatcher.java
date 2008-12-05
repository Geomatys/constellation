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
import java.net.URL;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.constellation.security.wms.WmsRestClient;
import org.constellation.security.wms.WmsSoapClient;
import org.constellation.wms.AbstractWMSCapabilities;
import org.constellation.ws.WebServiceException;


/**
 * Dispatch the request to the REST or SOAP client, according to what have been specified.
 *
 * @version $Id$
 * @author Cédric Briançon (Geomatys)
 */
public class Dispatcher {
    /**
     * The URL of the webservice to request.
     */
    private final String url;

    /**
     * Defines whether the dispatcher has received a REST or SOAP request.
     */
    private final boolean isRest;

    /**
     * The marshaller of the result given by the service.
     */
    private final Marshaller marshaller;

    /**
     * The unmarshaller of the result given by the service.
     */
    private final Unmarshaller unmarshaller;

    /**
     * Instance of a {@code REST WMS} client.
     */
    private WmsRestClient wmsRestClient;

    /**
     * Instance of a {@code SOAP WMS} client.
     */
    private WmsSoapClient wmsSoapClient;

    /**
     * Instanciates a dispatcher for the specified url. The constructor defines whether it
     * is a {@code REST} or {@code SOAP} request.
     *
     * @param url The URL of the webservice to request.
     * @param marshaller The marshaller to use for parsing the request.
     * @param unmarshaller The unmarshaller to use for the response.
     */
    public Dispatcher(final String url, final boolean isRest, final Marshaller marshaller,
                                                          final Unmarshaller unmarshaller)
    {
        this.url    = url;
        this.isRest = isRest;
        this.unmarshaller = unmarshaller;
        this.  marshaller =   marshaller;
    }

    AbstractWMSCapabilities requestGetCapabilities(final String service, final String request,
                                                                         final String version) throws WebServiceException
    {
        if (isRest) {
            wmsRestClient = new WmsRestClient(url, marshaller, unmarshaller);
            return wmsRestClient.sendGetCapabilities( service, request, version);
        } else {
            // implement a SOAP client.
            throw new UnsupportedOperationException();
        }
    }
}
