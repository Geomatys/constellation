/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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


import java.util.List;
import java.util.Map;

// Constellation dependencies
import javax.ws.rs.core.MediaType;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.Worker;

// Geotoolkit dependencies
import org.geotoolkit.gml.xml.v311.AbstractGMLType;
import org.geotoolkit.wfs.xml.DescribeFeatureType;
import org.geotoolkit.wfs.xml.GetCapabilities;
import org.geotoolkit.wfs.xml.WFSCapabilities;
import org.geotoolkit.wfs.xml.GetGmlObject;
import org.geotoolkit.wfs.xml.LockFeatureResponse;
import org.geotoolkit.wfs.xml.LockFeature;
import org.geotoolkit.wfs.xml.v110.GetCapabilitiesType;
import org.geotoolkit.wfs.xml.GetFeature;
import org.geotoolkit.wfs.xml.v110.TransactionResponseType;
import org.geotoolkit.wfs.xml.v110.TransactionType;
import org.geotoolkit.xsd.xml.v2001.Schema;

// GeoAPI dependencies
import org.opengis.feature.type.FeatureType;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public interface WFSWorker extends Worker {

    /**
     * Describe the capabilities and the layers available of this service.
     *
     * @param getCapab       The {@linkplain GetCapabilitiesType get capabilities} request.
     * @return a WFSCapabilities XML document describing the capabilities of the service.
     *
     * @throws CstlServiceException
     */
    WFSCapabilities getCapabilities(final GetCapabilities getCapab) throws CstlServiceException;

    /**
     * describe the structure of any feature type it can service.
     *
     * @param model A desribeFeatureType request contains typeNames.
     *
     * @return A marshallable object representing a xsd.
     * @throws CstlServiceException
     */
    Schema describeFeatureType(final DescribeFeatureType model) throws CstlServiceException;

    /**
     * Allows retrieval of features from a web feature service.
     * 
     * @param request a request containing typeNames, comparison filter, spatial filter, etc.
     * @return features instances.
     * @throws CstlServiceException
     */
    Object getFeature(final GetFeature request) throws CstlServiceException;

    /**
     * Allows retrieval of features and elements by ID from a web feature service.
     *
     * @param grbi a getGMLObject request containing IDs.
     * 
     * @return A GML representation of a feature instance or element.
     * @throws CstlServiceException
     */
    AbstractGMLType getGMLObject(GetGmlObject grbi) throws CstlServiceException;

    /**
     * lock request on one or more instances of a feature type for the duration of a transaction
     *
     * @param gr a lockFeature request identifying which feature are to lock.
     *
     * @return An acknowledgment
     * @throws CstlServiceException
     */
    LockFeatureResponse lockFeature(LockFeature gr) throws CstlServiceException;

    /**
     * Allow to insert, update, or remove feature instances.
     *
     * @param t A request containing feature to insert and filters identifying which feature have to be update/delete
     * @return
     * @throws CstlServiceException
     */
    TransactionResponseType transaction(TransactionType t) throws CstlServiceException;

    /**
     * Return a map with namespace - xsd location.
     * this map is added to the XML output.
     *
     * @return a map with namespace - xsd location.
     *
     * @deprecated Thread unsafe must be replaced.
     */
    @Deprecated
    Map<String, String> getSchemaLocations();

    /**
     * Return all the feature type that the service support.
     * @return all the feature type that the service support.
     */
    List<FeatureType> getFeatureTypes();

}
