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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Logger;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.constellation.map.ws.AbstractWMSWorker;
import org.constellation.query.wms.DescribeLayer;
import org.constellation.query.wms.GetCapabilities;
import org.constellation.query.wms.GetFeatureInfo;
import org.constellation.query.wms.GetLegendGraphic;
import org.constellation.query.wms.GetMap;
import org.constellation.wms.AbstractWMSCapabilities;
import org.constellation.ws.WebServiceException;
import org.geotools.internal.jaxb.v110.sld.DescribeLayerResponseType;


/**
 * A WMS worker for a security Policy Enforcement Point (PEP) gateway following 
 * the OASIS XACML model.
 * <p>
 * This worker takes the request issued by the REST and SOAP server facades, 
 * ensures an Access Control decision is made based on both the security 
 * credentials of the requestor and the parameters of the request, and then 
 * either performs the request or denies it depending on the Access Control 
 * decision.
 * </p>
 * <p>
 * <b>WARNING:</b> This class is still experimental and not behaving correctly. 
 * Using it in production is sure to void your warranty, shorten your life, and 
 * increase the likelyhood that meteorites fall on your home.
 *</p>
 *<p>
 * This class will send all its requests to a Dispatcher configured to 
 * re-expresses that as an Access Control request, 
 * performs an Access Control decision 
 * expresses the request and authentication
 * validates the right for t
 * Builds a {@code REST Web Map Service} request {@code GetCapabilities} on a server,
 * and send it to the {@link Dispatcher}.
 *
 * @version $Id$
 * 
 * @author Cédric Briançon (Geomatys)
 * @since 0.3
 */
public class WMSSecuredWorker extends AbstractWMSWorker {
	
    /**
     * The default logger.
     */
    private static final Logger LOGGER = Logger.getLogger("org.constellation.security.ws");
    
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
     * <p>
     * NB this is the marshaller for the service presented as a facade, not 
     * necessarily for any of the clients.
     * </p>
     */
    private final Marshaller marshaller;

    /**
     * The unmarshaller of the result given by the service.
     * <p>
     * NB this is the marshaller for the service presented as a facade, not 
     * necessarily for any of the clients.
     * </p>
     */
    private final Unmarshaller unmarshaller;


    /**
     * Builds a {@code GetCapabilities} request.
     *
     * @param marshaller
     * @param unmarshaller
     * @throws IOException if an error occurs at the URL creation.
     */
    public WMSSecuredWorker(final Marshaller marshaller, final Unmarshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
        this.  marshaller =   marshaller;

        dispatcher = new Dispatcher(WMSURL, isRest, marshaller, unmarshaller);
    }

    @Override
    public DescribeLayerResponseType describeLayer(DescribeLayer descLayer) throws WebServiceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AbstractWMSCapabilities getCapabilities(GetCapabilities getCapabilities) throws WebServiceException {
        return dispatcher.getCapabilities(getCapabilities);
    }

    @Override
    public String getFeatureInfo(GetFeatureInfo getFeatureInfo) throws WebServiceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BufferedImage getLegendGraphic(GetLegendGraphic getLegend) throws WebServiceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BufferedImage getMap(GetMap getMap) throws WebServiceException {
        return dispatcher.getMap(getMap);
    }
}
