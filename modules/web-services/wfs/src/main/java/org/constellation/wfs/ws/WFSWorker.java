/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009-2012, Geomatys
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

// Constellation dependencies
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.Worker;

// Geotoolkit dependencies
import org.geotoolkit.gml.xml.AbstractGML;
import org.geotoolkit.wfs.xml.*;
import org.geotoolkit.xsd.xml.v2001.Schema;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public interface WFSWorker extends Worker {

    /**
     * Describe the capabilities and the layers available of this service.
     *
     * @param request The {@linkplain GetCapabilities get capabilities} request.
     * @return a WFSCapabilities XML document describing the capabilities of the service.
     *
     * @throws CstlServiceException
     */
    WFSCapabilities getCapabilities(final GetCapabilities request) throws CstlServiceException;

    /**
     * describe the structure of any feature type it can service.
     *
     * @param request A desribeFeatureType request contains typeNames.
     *
     * @return A marshallable object representing a xsd.
     * @throws CstlServiceException
     */
    Schema describeFeatureType(final DescribeFeatureType request) throws CstlServiceException;

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
    AbstractGML getGMLObject(final GetGmlObject grbi) throws CstlServiceException;

    ListStoredQueriesResponse listStoredQueries(final ListStoredQueries request)  throws CstlServiceException;

    DescribeStoredQueriesResponse describeStoredQueries(final DescribeStoredQueries request) throws CstlServiceException;

    /**
     * lock request on one or more instances of a feature type for the duration of a transaction
     *
     * @param gr a lockFeature request identifying which feature are to lock.
     *
     * @return An acknowledgment
     * @throws CstlServiceException
     */
    LockFeatureResponse lockFeature(final LockFeature gr) throws CstlServiceException;

    /**
     * Allow to insert, update, or remove feature instances.
     *
     * @param request A request containing feature to insert and filters identifying which feature have to be update/delete
     * @return
     * @throws CstlServiceException
     */
    TransactionResponse transaction(final Transaction request) throws CstlServiceException;

    Object getPropertyValue(final GetPropertyValue request) throws CstlServiceException;

    CreateStoredQueryResponse createStoredQuery(final CreateStoredQuery request) throws CstlServiceException;

    DropStoredQueryResponse dropStoredQuery(final DropStoredQuery request) throws CstlServiceException;
    
    List<ParameterExpression> getParameterForStoredQuery(final String queryId);
}
