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

package org.constellation.wfs.ws;


// Constellation dependencies
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.Worker;

// Geotoolkit dependencies
import org.geotoolkit.data.collection.FeatureCollection;
import org.geotoolkit.gml.xml.v311.AbstractGMLEntry;
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

// GeoAPI dependencies
import org.opengis.webservice.capability.GetCapabilities;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public interface WFSWorker extends Worker {

    /**
     * Describe the capabilities and the layers available of this service.
     *
     * @param getCapab       The {@linkplain GetCapabilities get capabilities} request.
     * @return a WFSCapabilities XML document describing the capabilities of the service.
     *
     * @throws CstlServiceException
     */
    WFSCapabilitiesType getCapabilities(final GetCapabilitiesType getCapab) throws CstlServiceException;

    Schema describeFeatureType(final DescribeFeatureTypeType model) throws CstlServiceException;

    FeatureCollection getFeature(final GetFeatureType request) throws CstlServiceException;

    AbstractGMLEntry getGMLObject(GetGmlObjectType grbi) throws CstlServiceException;

    LockFeatureResponseType lockFeature(LockFeatureType gr) throws CstlServiceException;

    TransactionResponseType transaction(TransactionType t) throws CstlServiceException;

    String getOutputFormat();

}
