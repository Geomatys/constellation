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
import org.constellation.security.wms.WmsClient;
import org.constellation.security.wms.WmsRestClient;
import org.constellation.security.wms.WmsSoapClient;
import org.constellation.wms.AbstractWMSCapabilities;
import org.constellation.ws.WebServiceException;
import org.geotools.internal.jaxb.v110.sld.DescribeLayerResponseType;


/**
 * Builds a client to an OGC WMS service, using either a REST or SOAP interface 
 * depending on the service instance, and then forwards requests from the 
 * worker to the instantiated client.
 * <p>
 * This class will be quite static for now since we don't yet have it working 
 * but eventually should be configurable so we can aggregate layers from other 
 * services.
 * </p>
 * 
 * @version $Id$
 * 
 * @author Cédric Briançon (Geomatys)
 * @author Adrian Custer (Geomatys)
 * @since 0.3
 */
public class WmsDispatcher {
	
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
    private WmsClient wmsClient;
    
    
    
    
    /**
     * Instantiates a dispatcher for the specified url. The constructor defines whether it
     * is a {@code REST} or {@code SOAP} request.
     *
     * @param baseURL The URL of the webservice to request.
     * @param isRest Defines whether the client to call will be a REST or SOAP client.
     * @param marshaller The marshaller to use for parsing the request.
     * @param unmarshaller The unmarshaller to use for the response.
     */
    public WmsDispatcher(final String baseURL, final boolean isRest, final Marshaller marshaller,
                                                          final Unmarshaller unmarshaller)
    {
        this.unmarshaller = unmarshaller;
        this.marshaller   = marshaller;
        
        if (isRest){
        	this.wmsClient = new WmsRestClient(baseURL,this.marshaller,this.unmarshaller);
        } else {
        	this.wmsClient = new WmsSoapClient(baseURL,this.marshaller,this.unmarshaller);
        }
        
    }
    
    
    
	
    
	public DescribeLayerResponseType describeLayer(DescribeLayer descLayer)
			throws WebServiceException {
        return wmsClient.describeLayer(descLayer);
	}
	
	
	public AbstractWMSCapabilities getCapabilities(GetCapabilities getCapabilities) 
	                                                    throws WebServiceException {
        return wmsClient.getCapabilities(getCapabilities);
	}
	
	
	public String getFeatureInfo(GetFeatureInfo getFeatureInfo)
			throws WebServiceException {
		return wmsClient.getFeatureInfo(getFeatureInfo);
	}
	
	
	public BufferedImage getLegendGraphic(GetLegendGraphic getLegend)
			throws WebServiceException {
		return wmsClient.getLegendGraphic(getLegend);
	}
	
	
	public BufferedImage getMap(GetMap getMap) throws WebServiceException {
        return wmsClient.getMap(getMap);
	}
}
