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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.constellation.query.wms.DescribeLayer;
import org.constellation.query.wms.GetCapabilities;
import org.constellation.query.wms.GetFeatureInfo;
import org.constellation.query.wms.GetLegendGraphic;
import org.constellation.query.wms.GetMap;
import org.constellation.wms.AbstractWMSCapabilities;
import org.constellation.ws.ExceptionCode;
import org.constellation.ws.CstlServiceException;
import org.geotools.internal.jaxb.v110.sld.DescribeLayerResponseType;

import static org.constellation.query.wms.WMSQuery.*;


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
	public DescribeLayerResponseType describeLayer(final DescribeLayer descLayer) throws CstlServiceException {
		final URLConnection connec;
        try {
            final URL connectionURL = new URL(baseURL +"?"+ descLayer.toKvp());
            connec = connectionURL.openConnection();
        } catch (IOException ex) {
            throw new CstlServiceException(ex, ExceptionCode.NO_APPLICABLE_CODE);
        }
        connec.setDoOutput(true);
        connec.setRequestProperty("Content-Type", TEXT_XML);

        //Extract the describeLayer result.
        final InputStream in;
        try {
            in = connec.getInputStream();
        } catch (IOException ex) {
            throw new CstlServiceException(ex, ExceptionCode.NO_APPLICABLE_CODE);
        }

        //Unmarshall response
        Object response;
        try {
            response = unmarshaller.unmarshal(in);
        } catch (JAXBException ex) {
            throw new CstlServiceException(ex, ExceptionCode.NO_APPLICABLE_CODE);
        }

        try {
            in.close();
        } catch (IOException ex) {
            throw new CstlServiceException(ex, ExceptionCode.NO_APPLICABLE_CODE);
        }
        if (!(response instanceof DescribeLayerResponseType)) {
            throw new CstlServiceException("The respone is not unmarshallable, since it is not an" +
                    " instance of DescribeLayerResponseType", ExceptionCode.NO_APPLICABLE_CODE);
        }

        return (DescribeLayerResponseType) response;
	}

	@Override
	public AbstractWMSCapabilities getCapabilities(final GetCapabilities getCapabilities) throws CstlServiceException {//Connect to URL and get result
        
		final URLConnection connec;
        try {
            final URL connectionURL = new URL(baseURL +"?"+ getCapabilities.toKvp());
            connec = connectionURL.openConnection();
        } catch (IOException ex) {
            throw new CstlServiceException(ex, ExceptionCode.NO_APPLICABLE_CODE);
        }
        connec.setDoOutput(true);
        connec.setRequestProperty("Content-Type", TEXT_XML);

        //Extract the GetCapabilities XML result.
        final InputStream in;
        try {
            in = connec.getInputStream();
        } catch (IOException ex) {
            throw new CstlServiceException(ex, ExceptionCode.NO_APPLICABLE_CODE);
        }

        //Unmarshall response
        Object response;
        try {
            response = unmarshaller.unmarshal(in);
        } catch (JAXBException ex) {
            throw new CstlServiceException(ex, ExceptionCode.NO_APPLICABLE_CODE);
        }
        try {
            in.close();
        } catch (IOException ex) {
            throw new CstlServiceException(ex, ExceptionCode.NO_APPLICABLE_CODE);
        }
        if (!(response instanceof AbstractWMSCapabilities)) {
            throw new CstlServiceException("The respone is not unmarshallable, since it is not an" +
                    " instance of AbstractWMSCapabilities", ExceptionCode.NO_APPLICABLE_CODE);
        }

        return (AbstractWMSCapabilities) response;
	}

	@Override
	public String getFeatureInfo(final GetFeatureInfo getFeatureInfo) throws CstlServiceException {
		final URLConnection connec;
        try {
            final URL connectionURL = new URL(baseURL +"?"+ getFeatureInfo.toKvp());
            connec = connectionURL.openConnection();
        } catch (IOException ex) {
            throw new CstlServiceException(ex, ExceptionCode.NO_APPLICABLE_CODE);
        }
        connec.setDoOutput(true);
        String infoFormat = getFeatureInfo.getInfoFormat();
        if (infoFormat.equals(GML)) {
            infoFormat = APP_XML;
        }
        connec.setRequestProperty("Content-Type", infoFormat);

        //Extract the GetFeatureInfo XML result.
        final InputStream in;
        try {
            in = connec.getInputStream();
        } catch (IOException ex) {
            throw new CstlServiceException(ex, ExceptionCode.NO_APPLICABLE_CODE);
        }
        final BufferedReader buff = new BufferedReader(new InputStreamReader(in));
        final StringWriter writer = new StringWriter();
        final String result;
        String line = null;
        try {
            while ((line = buff.readLine()) != null) {
                writer.append(line).append("\n");
            }
            buff.close();
            result = writer.toString();
            writer.close();
        } catch (IOException ex) {
            throw new CstlServiceException(ex, ExceptionCode.NO_APPLICABLE_CODE);
        }
        return result;
	}

	@Override
	public BufferedImage getLegendGraphic(final GetLegendGraphic getLegend) throws CstlServiceException {
        final URL connectionURL;

        try {
            connectionURL = new URL(baseURL + "?" + getLegend.toKvp());
        } catch (MalformedURLException ex) {
            throw new CstlServiceException(ex, ExceptionCode.NO_APPLICABLE_CODE);
        }

        try {
            return ImageIO.read(connectionURL);
        } catch (IOException ex) {
            throw new CstlServiceException(ex, ExceptionCode.NO_APPLICABLE_CODE);
        }
	}

    @Override
    public BufferedImage getMap(final GetMap getMap) throws CstlServiceException {
        final URL connectionURL;

        try {
            connectionURL = new URL(baseURL + "?" + getMap.toKvp());
        } catch (MalformedURLException ex) {
            throw new CstlServiceException(ex, ExceptionCode.NO_APPLICABLE_CODE);
        }

        try {
            return ImageIO.read(connectionURL);
        } catch (IOException ex) {
            throw new CstlServiceException(ex, ExceptionCode.NO_APPLICABLE_CODE);
        }
    }
}
