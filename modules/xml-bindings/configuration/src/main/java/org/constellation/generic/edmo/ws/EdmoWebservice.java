/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le DÃƒÂ©veloppement
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

package org.constellation.generic.edmo.ws;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;


/**
 * 
 */
@WebServiceClient(name = "edmo_webservice", targetNamespace = "ns_ws_edmo", wsdlLocation = "http://seadatanet.maris2.nl/ws/ws_edmo.asmx?wsdl")
public class EdmoWebservice extends Service {
    

    private final static URL EDMOWEBSERVICE_WSDL_LOCATION;
    private final static Logger logger = Logger.getLogger("org.constellation.generic.edmo.ws");

    static {
        URL url = null;
        try {
            URL baseUrl;
            baseUrl = org.constellation.generic.edmo.ws.EdmoWebservice.class.getResource(".");
            url = new URL(baseUrl, "http://seadatanet.maris2.nl/ws/ws_edmo.asmx?wsdl");
        } catch (MalformedURLException e) {
            logger.warning("Failed to create URL for the wsdl Location: 'http://seadatanet.maris2.nl/ws/ws_edmo.asmx?wsdl', retrying as a local file");
            logger.warning(e.getMessage());
        }
        EDMOWEBSERVICE_WSDL_LOCATION = url;
    }

    public EdmoWebservice(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public EdmoWebservice() {
        super(EDMOWEBSERVICE_WSDL_LOCATION, new QName("ns_ws_edmo", "edmo_webservice"));
    }

    /**
     * 
     * @return
     *     returns EdmoWebserviceSoap
     */
    @WebEndpoint(name = "edmo_webserviceSoap")
    public EdmoWebserviceSoap getEdmoWebserviceSoap() {
        return super.getPort(new QName("ns_ws_edmo", "edmo_webserviceSoap"), EdmoWebserviceSoap.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns EdmoWebserviceSoap
     */
    @WebEndpoint(name = "edmo_webserviceSoap")
    public EdmoWebserviceSoap getEdmoWebserviceSoap(WebServiceFeature... features) {
        return super.getPort(new QName("ns_ws_edmo", "edmo_webserviceSoap"), EdmoWebserviceSoap.class, features);
    }

    /**
     * 
     * @return
     *     returns EdmoWebserviceSoap
     */
    @WebEndpoint(name = "edmo_webserviceSoap12")
    public EdmoWebserviceSoap getEdmoWebserviceSoap12() {
        return super.getPort(new QName("ns_ws_edmo", "edmo_webserviceSoap12"), EdmoWebserviceSoap.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns EdmoWebserviceSoap
     */
    @WebEndpoint(name = "edmo_webserviceSoap12")
    public EdmoWebserviceSoap getEdmoWebserviceSoap12(WebServiceFeature... features) {
        return super.getPort(new QName("ns_ws_edmo", "edmo_webserviceSoap12"), EdmoWebserviceSoap.class, features);
    }

}
