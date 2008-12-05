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
package org.constellation.security.wms;

import com.sun.jersey.spi.resource.Singleton;
import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Level;
import javax.annotation.PreDestroy;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.xml.bind.JAXBException;
import org.constellation.query.Query;
import org.constellation.query.wms.WMSQuery;
import org.constellation.security.Worker;
import org.constellation.wms.AbstractWMSCapabilities;
import org.constellation.ws.ExceptionCode;
import org.constellation.ws.Service;
import org.constellation.ws.ServiceExceptionReport;
import org.constellation.ws.ServiceExceptionType;
import org.constellation.ws.ServiceVersion;
import org.constellation.ws.WebServiceException;
import org.constellation.ws.rs.OGCWebService;


/**
 * Web service designed to get a WMS request from a client and to give it to a
 * {@link org.constellation.security.ws.Dispatcher}. The dispatcher will do its job and return the result of the
 * request from the web service.
 * If the user has not proceeded the authentication part, it will return a response
 * containing an access constraint part, which will indicate that an authentication
 * is required.
 *
 * @version $Id$
 * @author Cédric Briançon (Geomatys)
 */
@Path("wms-sec")
@Singleton
public class WmsRestService extends OGCWebService {
    /**
     * A worker for requesting a WMS service.
     */
    private final Worker worker;

    /**
     * Defines the WMS version available to a request.
     *
     * @throws JAXBException if an error in the context occurs.
     */
    public WmsRestService() throws JAXBException, IOException {
        super("WMS", new ServiceVersion(Service.WMS, "1.1.1"), new ServiceVersion(Service.WMS, "1.3.0"));
        setXMLContext("org.constellation.ws:org.constellation.wms.v111:" +
                "org.constellation.wms.v130:org.geotools.internal.jaxb.v110.sld",
                "http://www.opengis.net/wms");
        worker = new Worker(marshaller, unmarshaller);
    }

    /**
     * When a request is done by a client on this webservice, it falls down on this method
     * that is in charge to launch the right process, according to the parameters given in
     * the request.
     *
     * @param objectRequest An eventual request object. In this implementation, it will be
     *                      always {@code null}.
     * @return The response of the request done by the user.
     * @throws javax.xml.bind.JAXBException
     */
    public Response treatIncomingRequest(Object objectRequest) throws JAXBException {
        
        try {
            //Extract parameters
            final String service = getParameter(Query.KEY_SERVICE, false); //WMS
            final String request = getParameter(Query.KEY_REQUEST, true);  //e.g. getCaps
            String version = getParameter(Query.KEY_VERSION, false);       //e.g. 1.3.0
            if (version == null) {
                version = "1.1.1";//We choose to handle the lowest version for older clients that may not know about 1.3.0
            }

            //Used for internal version tracking
            final ServiceVersion serviceVersion = new ServiceVersion(Service.WMS, version);
            setCurrentVersion(version);

            //Handle the different kinds of Requests
            if (request.equalsIgnoreCase(WMSQuery.GETCAPABILITIES)) {
                final AbstractWMSCapabilities awc = worker.launchGetCapabilities(service, request, version);
                final ResponseBuilder respb = Response.ok(awc, Query.TEXT_XML);
                return respb.build();
            } else if (request.equalsIgnoreCase(WMSQuery.GETMAP)) {
                throw new UnsupportedOperationException("Can't handle that yet!");
            } else if (request.equalsIgnoreCase(WMSQuery.GETFEATUREINFO)) {
                throw new UnsupportedOperationException("Can't handle that yet!");
            } else {
                //User has asked for a non-existant request
                throw new WebServiceException("The operation " + request + " is not supported by the service",
                        ExceptionCode.OPERATION_NOT_SUPPORTED,
                        serviceVersion,
                        "request");
            }

        } catch (WebServiceException ex) {
            final ServiceExceptionReport report = new ServiceExceptionReport(getCurrentVersion(),
                    new ServiceExceptionType(ex.getMessage(), (ExceptionCode) ex.getExceptionCode()));
            LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
            StringWriter sw = new StringWriter();
            marshaller.marshal(report, sw);
            return Response.ok(cleanSpecialCharacter(sw.toString()), Query.APP_XML).build();
        }
    }

    @PreDestroy
    public void destroy() {
        LOGGER.info("destroying secured wms.");
    }
}
