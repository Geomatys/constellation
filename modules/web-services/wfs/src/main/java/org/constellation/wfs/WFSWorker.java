/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2009, Geomatys
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

package org.constellation.wfs;

import java.util.logging.Logger;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.ExceptionCode;
import org.geotoolkit.gml.xml.v311.AbstractFeatureCollectionType;
import org.geotoolkit.gml.xml.v311.AbstractGMLEntry;
import org.geotoolkit.ows.xml.v100.ExceptionReport;
import org.geotoolkit.ows.xml.v100.ExceptionType;
import org.geotoolkit.wfs.xml.v110.DescribeFeatureTypeType;
import org.geotoolkit.wfs.xml.v110.GetCapabilitiesType;
import org.geotoolkit.wfs.xml.v110.GetFeatureType;
import org.geotoolkit.wfs.xml.v110.GetGmlObjectType;
import org.geotoolkit.wfs.xml.v110.LockFeatureResponseType;
import org.geotoolkit.wfs.xml.v110.LockFeatureType;
import org.geotoolkit.wfs.xml.v110.TransactionResponseType;
import org.geotoolkit.wfs.xml.v110.TransactionType;
import org.geotoolkit.wfs.xml.v110.WFSCapabilitiesType;
import org.geotoolkit.xsd.xml.v2001.Schema;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class WFSWorker {

    /**
     * The default logger.
     */
    private static final Logger LOGGER = Logger.getLogger("org.constellation.wfs");

    

    /**
     *
     */
    public WFSWorker() throws CstlServiceException {
        
    }

    public Schema describeFeatureType(DescribeFeatureTypeType dr) throws CstlServiceException {
        return null;
    }

    public WFSCapabilitiesType getCapabilities(GetCapabilitiesType gc) throws CstlServiceException {
        return null;
    }

    public AbstractFeatureCollectionType getFeature(GetFeatureType gd) throws CstlServiceException {
        return null;
    }

    public AbstractGMLEntry getGMLObject(GetGmlObjectType grbi) throws CstlServiceException {
        return null;
    }

    public LockFeatureResponseType lockFeature(LockFeatureType gr) throws CstlServiceException {
        return null;
    }

    public TransactionResponseType transaction(TransactionType t) throws CstlServiceException {
        return null;
    }

    public String getOutputFormat() {
        return "text/xml";
    }

    public void relayException(ExceptionReport report) throws CstlServiceException {
        LOGGER.severe("\nPROXY: The PEP threw an Exception:\n" + report);
        String message     = "no message";
        String locator     = null;
        ExceptionCode code = null;
        if (report.getException() != null && report.getException().size() > 0) {
            ExceptionType ex = report.getException().get(0);
            if (ex.getExceptionCode() != null) {
                code = ExceptionCode.valueOf(ex.getExceptionCode());
            }
            locator = ex.getLocator();
            if (ex.getExceptionText() != null && ex.getExceptionText().size() > 0) {
                message = ex.getExceptionText().get(0);
            }
        }
        throw new CstlServiceException(message, code, locator);
    }
}
