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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.constellation.query.Query;
import org.constellation.query.wms.WMSQuery;
import org.constellation.wms.AbstractWMSCapabilities;
import org.constellation.wms.v111.WMT_MS_Capabilities;
import org.constellation.wms.v130.WMSCapabilities;
import org.constellation.ws.ExceptionCode;
import org.constellation.ws.WebServiceException;


/**
 * {@code REST} client for a {@code WMS} request.
 *
 * @version $Id$
 * @author Cédric Briançon (Geomatys)
 */
public class WmsRestClient {
    /**
     * The URL of the webservice to request.
     */
    private final String url;

    /**
     * The marshaller of the result given by the service.
     */
    private final Marshaller marshaller;

    /**
     * The unmarshaller of the result given by the service.
     */
    private final Unmarshaller unmarshaller;

    public WmsRestClient(final String url, final Marshaller marshaller, final Unmarshaller unmarshaller) {
        this.url = url;
        this.marshaller = marshaller;
        this.unmarshaller = unmarshaller;
    }

    public AbstractWMSCapabilities sendGetCapabilities(final String service, final String request,
                                                       final String version) throws WebServiceException
    {
        assert (service.equalsIgnoreCase("wms"));
        assert (request.equalsIgnoreCase(WMSQuery.GETCAPABILITIES));

        //Connect to URL and get result
        final URLConnection connec;
        try {
            final URL connectionURL = new URL(url +"?"+ Query.KEY_REQUEST +"="+ request +"&"+
                Query.KEY_SERVICE +"="+ service +"&"+ Query.KEY_VERSION +"="+ version);
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

        //Parse response
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

        response = removeCapabilitiesInfo(response);
        response = addAccessConstraints(response);
        return (AbstractWMSCapabilities) response;
    }

    /**
     * Remove the {@code Capability} tag from an XML Capabilities file.
     *
     * @param xmlCapab An unmarshalled GetCapabilities.
     * @return The getCapabilities without the {@code Capability} tag filled.
     * @throws WebServiceException if the given GetCapabilities does not match with the supposed WMS version.
     */
    private AbstractWMSCapabilities removeCapabilitiesInfo(final Object xmlCapab) throws WebServiceException {
        if (xmlCapab instanceof WMT_MS_Capabilities) {
            final WMT_MS_Capabilities cap = (WMT_MS_Capabilities) xmlCapab;
            org.constellation.wms.v111.Capability capability =
                    new org.constellation.wms.v111.Capability(null, null, null, null);
            cap.setCapability(capability);
            return cap;
        }
        if (xmlCapab instanceof WMSCapabilities) {
            final WMSCapabilities cap = (WMSCapabilities) xmlCapab;
            org.constellation.wms.v130.Capability capability =
                    new org.constellation.wms.v130.Capability(null, null, null, (JAXBElement<?>) null);
            cap.setCapability(capability);
            return cap;
        }
        throw new WebServiceException("Capabilities response is not valid, because it does not match" +
                " with JAXB classes.", ExceptionCode.NO_APPLICABLE_CODE);
    }

    /**
     * Add an {@code AccessConstraints} tag in a GetCapabilities XML file, to specify that an authentication
     * is required.
     *
     * @param xmlCapab An unmarshalled GetCapabilities.
     * @return The getCapabilities with the {@code AccessConstraints} tag filled.
     * @throws WebServiceException if the given GetCapabilities does not match with the supposed WMS version.
     */
    private AbstractWMSCapabilities addAccessConstraints(final Object xmlCapab) throws WebServiceException {
        if (xmlCapab instanceof WMT_MS_Capabilities) {
            final WMT_MS_Capabilities cap = (WMT_MS_Capabilities) xmlCapab;
            cap.getService().setAccessConstraints("Require an authentication !");
            return cap;
        }
        if (xmlCapab instanceof WMSCapabilities) {
            final WMSCapabilities cap = (WMSCapabilities) xmlCapab;
            cap.getService().setAccessConstraints("Require an authentication !");
            return cap;
        }
        throw new WebServiceException("Capabilities response is not valid, because it does not match" +
                " with JAXB classes.", ExceptionCode.NO_APPLICABLE_CODE);
    }
}
