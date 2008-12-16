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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.constellation.query.Query;
import org.constellation.query.wms.DescribeLayer;
import org.constellation.query.wms.GetCapabilities;
import org.constellation.query.wms.GetFeatureInfo;
import org.constellation.query.wms.GetLegendGraphic;
import org.constellation.query.wms.GetMap;
import org.constellation.wms.AbstractWMSCapabilities;
import org.constellation.ws.ExceptionCode;
import org.constellation.ws.WebServiceException;
import org.geotools.internal.jaxb.v110.sld.DescribeLayerResponseType;


/**
 * Fulfills WMS requests on a separate WMS server using a {@code REST}.
 *
 * @version $Id$
 * 
 * @author Cédric Briançon (Geomatys)
 * @author Adrian Custer (Geomatys)
 * @since 0.3
 */
public class WmsRestClient implements WmsClient {
    /**
     * The logger for the constellation
     */
	private static final Logger LOGGER = Logger.getLogger("org.constellation.security.wms");

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
    
    
    
    
    public WmsRestClient(final String baseurl, final Marshaller marshaller, final Unmarshaller unmarshaller) {
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
	public AbstractWMSCapabilities getCapabilities(GetCapabilities getCapabilities) throws WebServiceException {//Connect to URL and get result
        
		final URLConnection connec;
        try {
            final URL connectionURL = new URL(baseURL +"?"+ getCapabilities.toKvp());
            connec = connectionURL.openConnection();
        } catch (IOException ex) {
            throw new WebServiceException(ex, ExceptionCode.NO_APPLICABLE_CODE);
        }
        connec.setDoOutput(true);
        connec.setRequestProperty("Content-Type", Query.TEXT_XML);

        //Extract the GetCapabilities XML result.
        final InputStream in;
        try {
            in = connec.getInputStream();
        } catch (IOException ex) {
            throw new WebServiceException(ex, ExceptionCode.NO_APPLICABLE_CODE);
        }

        //Unmarshall response
        Object response;
        try {
            response = unmarshaller.unmarshal(in);
        } catch (JAXBException ex) {
            throw new WebServiceException(ex, ExceptionCode.NO_APPLICABLE_CODE);
        }
        if (!(response instanceof AbstractWMSCapabilities)) {
            throw new WebServiceException("The respone is not unmarshallable, since it is not an" +
                    " instance of AbstractWMSCapabilities", ExceptionCode.NO_APPLICABLE_CODE);
        }

        return (AbstractWMSCapabilities) response;
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

        //Connect to URL and get result
        final URLConnection connec;
        try {
            LOGGER.info(baseURL + "?" + getMap.toKvp());
            final URL connectionURL = new URL(baseURL + "?" + getMap.toKvp());
            connec = connectionURL.openConnection();
        } catch (IOException ex) {
            throw new WebServiceException(ex, ExceptionCode.NO_APPLICABLE_CODE);
        }
        connec.setDoOutput(true);
        connec.setRequestProperty("Content-Type", getMap.getFormat());

        final InputStream in;
        try {
            in = connec.getInputStream();
        } catch (IOException ex) {
            throw new WebServiceException(ex, ExceptionCode.NO_APPLICABLE_CODE);
        }
        final ImageReader reader = ImageIO.getImageReadersByFormatName(getMap.getFormat()).next();
        reader.setInput(in);
        try {
            return reader.read(0);
        } catch (IOException ex) {
            throw new WebServiceException(ex, ExceptionCode.NO_APPLICABLE_CODE);
        }
    }
}
