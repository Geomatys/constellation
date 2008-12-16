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

import java.awt.image.BufferedImage;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.constellation.query.wms.DescribeLayer;
import org.constellation.query.wms.GetCapabilities;
import org.constellation.query.wms.GetFeatureInfo;
import org.constellation.query.wms.GetLegendGraphic;
import org.constellation.query.wms.GetMap;
import org.constellation.wms.AbstractWMSCapabilities;
import org.constellation.ws.WebServiceException;
import org.geotools.internal.jaxb.v110.sld.DescribeLayerResponseType;

/**
 *
 * @version $Id$
 * @author Cédric Briançon (Geomatys)
 */
public class WmsSoapClient implements WmsClient {
    /**
     * The URL of the webservice to request.
     */
    private final String baseURL;

    /**
     * The marshaller of the result given by the service.
     */
    private final Marshaller marshaller;

    /**
     * The unmarshaller of the result given by the service.
     */
    private final Unmarshaller unmarshaller;

    public WmsSoapClient(final String baseurl, final Marshaller marshaller, final Unmarshaller unmarshaller) {
        this.baseURL = baseurl;
        this.marshaller = marshaller;
        this.unmarshaller = unmarshaller;
    }

	@Override
	public DescribeLayerResponseType describeLayer(DescribeLayer descLayer)
			throws WebServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractWMSCapabilities getCapabilities(
			GetCapabilities getCapabilities) throws WebServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFeatureInfo(GetFeatureInfo getFeatureInfo)
			throws WebServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BufferedImage getLegendGraphic(GetLegendGraphic getLegend)
			throws WebServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BufferedImage getMap(GetMap getMap) throws WebServiceException {
		// TODO Auto-generated method stub
		return null;
	}

}
