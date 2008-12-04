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
package org.constellation.security.ws.soap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.ws.WebServiceContext;
import org.constellation.security.PEPWorker;
import org.constellation.wms.AbstractWMSCapabilities;
import org.constellation.wms.v111.WMT_MS_Capabilities;
import org.constellation.wms.v130.WMSCapabilities;


/**
 *
 * @version $Id$
 * @author Cédric Briançon (Geomatys)
 */
@WebService(name = "PEP")
@SOAPBinding(parameterStyle = ParameterStyle.BARE)
public class PEPService {
    /**
     * The default logger.
     */
    private static final Logger LOGGER = Logger.getLogger("org.constellation.security.ws.soap");

    /**
     * The PEP worker.
     */
    private final PEPWorker worker;

    @Resource
    private WebServiceContext context;

    /**
     * Constructs a PEP.
     *
     * @throws JAXBException if an error occurs at unmarshalling-time.
     */
    public PEPService() throws JAXBException {
        final JAXBContext context = JAXBContext.newInstance("org.constellation.ws:" +
                "org.constellation.wms.v111:org.constellation.wms.v130:" +
                "org.geotools.internal.jaxb.v110.sld");
        final Unmarshaller unmarshaller = context.createUnmarshaller();
        worker = new PEPWorker(unmarshaller);
    }

    /**
     * Returns a {@linkplain AbstractWMSCapabilities GetCapabilities} done on a webservice
     * that support this request, and add an assertion in the body, specifying that it lacks
     * the identification part.
     */
    @WebMethod(action="getCapabilities")
    public @WebResult(name="abstractWMSCapabilities") AbstractWMSCapabilities getCapabilities() {
        final Object capab;
        InputStream streamCapab = null;
        try {
            streamCapab = worker.getCapabilities();
            try {
                capab = worker.unmarshaller.unmarshal(streamCapab);
            } catch (JAXBException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                streamCapab.close();
                return null;
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            return null;
        } finally {
            try {
                streamCapab.close();
            } catch (IOException ex) {
                LOGGER.info("The stream is already closed.");
            }
        }
        if (capab instanceof WMT_MS_Capabilities) {
            // WMS version 1.1.1
            LOGGER.info("WMS Capabilities in version 1.1.1");
            final WMT_MS_Capabilities cap = (WMT_MS_Capabilities) capab;
            // TODO: insert an assertion in the getcapabilities, to specify that the
            //       identification part is missing.
        } else {
            // WMS version 1.3.0
            LOGGER.info("WMS Capabilities in version 1.3.0");
            final WMSCapabilities cap = (WMSCapabilities) capab;
            // TODO: insert an assertion in the getcapabilities, to specify that the
            //       identification part is missing.
        }
        return (AbstractWMSCapabilities) capab;
    }

    /**
     * Log the body of an {@link InputStream}.
     *
     * @param input An {@link InputStream} containing
     * @throws java.io.IOException
     */
    private void logInputStream(final InputStream input) throws IOException {
        final InputStreamReader streamReader = new InputStreamReader(input);
        final BufferedReader reader = new BufferedReader(streamReader);
        final StringBuilder sw = new StringBuilder();
        String line;
        while ((line=reader.readLine()) != null) {
            sw.append(line).append("\n");
        }
        reader.close();
        LOGGER.info(sw.toString());
    }
}
