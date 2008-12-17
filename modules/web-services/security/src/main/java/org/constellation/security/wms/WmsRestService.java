/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2008, Geomatys
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
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.xml.bind.JAXBException;
import org.constellation.map.ws.rs.WMSService;
import org.constellation.security.WmsSecuredWorker;


/**
 * The REST facade to this WMS Policy Enforcement Point (PEP).
 * <p>
 * This facade covers both clients which call the service using an HTTP GET
 * message and include the request and all other parameters in the URL itself as
 * well as clients which call the service using an HTTP POST message and include
 * the request in the body of the message either as Key-Value pairs or as an XML
 * document. The latter has not yet been formalized by the OGC for WMS and so is
 * an extension of the existing standards.
 * </p>
 * <p>
 * The facade calls the {@code org.constellation.security.Worker} for all the
 * complex logic of the PEP. All the initial processing of the incoming requests 
 * is performed in the parent classes. This class only instantiates the security 
 * worker it needs and initializes its {@code SecurityContext} field prior to 
 * calling the worker's methods.
 * </p>
 * <p>
 * Access control necessitates that the user be authenticated to the container.
 * If the user has not proceeded with the authentication part, the service will
 * return a response indicating the policy which requires access constraint.
 * </p>
 * 
 * @version $Id$
 * 
 * @author Cédric Briançon (Geomatys)
 * @author Adrian Custer (Geomatys)
 * @since 0.3
 */
@Path("wms-sec")
@Singleton
public class WmsRestService extends WMSService {
	/**
	 * Information on the identity and credentials of the principal making a 
	 * request of this service. The instance is injected by the JEE container.
     */
    @Context
    private SecurityContext secCntxt;

    /**
     * Constructor building the security worker to perform the logic of this
     * gateway.
     *
     * TODO: review the usage of the exceptions and then fill out the text below.
     * @throws JAXBException
     */
    public WmsRestService() throws JAXBException {
        worker = new WmsSecuredWorker(marshaller, unmarshaller);
        LOGGER.info("WMS secured service running");
    }

    /**
     * Calls the same method in the parent class after initializing the security 
     * context in the worker.
     * 
     * @see WMSService#treatIncomingRequest(Object)
     */
    @Override
    public Response treatIncomingRequest(Object objectRequest) throws JAXBException{
    	worker.initSecurityContext(secCntxt);
    	return super.treatIncomingRequest(objectRequest);
    }

}