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
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.Worker;

// Geotoolkit dependencies
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
    WFSCapabilitiesType getCapabilities(final GetCapabilitiesType getCapab) throws CstlServiceException;

    /**
     * describe the structure of any feature type it can service.
     *
     * @param model A desribeFeatureType request contains typenames.
     *
     * @return A marshallable object representing a xsd.
     * @throws CstlServiceException
     */
    Schema describeFeatureType(final DescribeFeatureTypeType model) throws CstlServiceException;

    /**
     * Allows retrieval of features from a web feature service.
     * 
     * @param request e request containg typeNames, comparison filter, spatial filter, etc.
     * @return features instances.
     * @throws CstlServiceException
     */
    Object getFeature(final GetFeatureType request) throws CstlServiceException;

    /**
     * Allows retrieval of features and elements by ID from a web feature service.
     *
     * @param grbi a getGMLObject request containing IDs.
     * 
     * @return A GML representation of a fature instance or element.
     * @throws CstlServiceException
     */
    AbstractGMLEntry getGMLObject(GetGmlObjectType grbi) throws CstlServiceException;

    /**
     * lock request on one or more instancesof a feature type for the duration of a transaction
     *
     * @param gr a lockFeature request identifying which feature are to lock.
     *
     * @return An acknowledgement
     * @throws CstlServiceException
     */
    LockFeatureResponseType lockFeature(LockFeatureType gr) throws CstlServiceException;

    /**
     * Allow to insert, update, or remove feature instances.
     *
     * @param t A request containing feature to insert and filters identifying which feature have to be update/delete
     * @return
     * @throws CstlServiceException
     */
    TransactionResponseType transaction(TransactionType t) throws CstlServiceException;

    /**
     * Return the current outputFormat.
     *
     * @return the current outputFormat.
     */
    String getOutputFormat();

    /**
     * Return a map with namespace - xsd location.
     * this map is added to the XML output.
     *
     * @return a map with namespace - xsd location.
     */
    Map<String, String> getSchemaLocations();

    /**
     * Add mapping between prefix and namespace,
     * used for transaction request where the feature are extracted by JAXP.
     *
     * @param namespaceMapping a map of prefix - namespace.
     */
    void setprefixMapping(Map<String, String> namespaceMapping);

    /**
     * Return all the feature type that the service support.
     * @return all the feature type that the service support.
     */
    List<FeatureType> getFeatureTypes();

}
