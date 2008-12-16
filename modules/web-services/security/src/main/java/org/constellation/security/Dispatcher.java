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

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.constellation.query.wms.DescribeLayer;
import org.constellation.query.wms.GetCapabilities;
import org.constellation.query.wms.GetFeatureInfo;
import org.constellation.query.wms.GetLegendGraphic;
import org.constellation.query.wms.GetMap;
import org.constellation.security.wms.WmsRestClient;
import org.constellation.security.wms.WmsSoapClient;
import org.constellation.wms.AbstractWMSCapabilities;
import org.constellation.ws.WebServiceException;
import org.geotools.internal.jaxb.v110.sld.DescribeLayerResponseType;


/**
 * Services requests from the worker which are dispatched to client OGC services 
 * based on the configuration of this class either through REST or SOAP based 
 * clients.
 * <p>
 * This class will be quite static for now since we don't yet have it working 
 * but eventually should be configurable so we can aggregate layers from other 
 * services.
 * </p>
 * 
 * @version $Id$
 * 
 * @author Cédric Briançon (Geomatys)
 * @since 0.3
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
     * @param isRest Defines whether the client to call will be a REST or SOAP client.
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

    AbstractWMSCapabilities requestGetCapabilities(GetCapabilities getCaps) throws WebServiceException
    {
    	final String service = null;
    	final String request = null;
    	final String version = null;
        if (isRest) {
            wmsRestClient = new WmsRestClient(url, marshaller, unmarshaller);
            return wmsRestClient.sendGetCapabilities( service, request, version);
        } else {
            // implement a SOAP client.
            throw new UnsupportedOperationException();
        }
    }

	
	public DescribeLayerResponseType describeLayer(DescribeLayer descLayer)
			throws WebServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public AbstractWMSCapabilities getCapabilities(
			GetCapabilities getCapabilities) throws WebServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public String getFeatureInfo(GetFeatureInfo getFeatureInfo)
			throws WebServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public BufferedImage getLegendGraphic(GetLegendGraphic getLegend)
			throws WebServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public BufferedImage getMap(GetMap getMap) throws WebServiceException {
        if (isRest) {
            wmsRestClient = new WmsRestClient(url, marshaller, unmarshaller);
            return wmsRestClient.sendGetMap(getMap);
        } else {
            // implement a SOAP client.
            throw new UnsupportedOperationException();
        }
	}
}
