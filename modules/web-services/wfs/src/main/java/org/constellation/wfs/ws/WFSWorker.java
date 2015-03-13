/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.constellation.wfs.ws;


import org.constellation.ws.CstlServiceException;
import org.constellation.ws.Worker;
import org.geotoolkit.gml.xml.AbstractGML;
import org.geotoolkit.wfs.xml.CreateStoredQuery;
import org.geotoolkit.wfs.xml.CreateStoredQueryResponse;
import org.geotoolkit.wfs.xml.DescribeFeatureType;
import org.geotoolkit.wfs.xml.DescribeStoredQueries;
import org.geotoolkit.wfs.xml.DescribeStoredQueriesResponse;
import org.geotoolkit.wfs.xml.DropStoredQuery;
import org.geotoolkit.wfs.xml.DropStoredQueryResponse;
import org.geotoolkit.wfs.xml.GetCapabilities;
import org.geotoolkit.wfs.xml.GetFeature;
import org.geotoolkit.wfs.xml.GetGmlObject;
import org.geotoolkit.wfs.xml.GetPropertyValue;
import org.geotoolkit.wfs.xml.ListStoredQueries;
import org.geotoolkit.wfs.xml.ListStoredQueriesResponse;
import org.geotoolkit.wfs.xml.LockFeature;
import org.geotoolkit.wfs.xml.LockFeatureResponse;
import org.geotoolkit.wfs.xml.ParameterExpression;
import org.geotoolkit.wfs.xml.Transaction;
import org.geotoolkit.wfs.xml.TransactionResponse;
import org.geotoolkit.wfs.xml.WFSCapabilities;
import org.geotoolkit.xsd.xml.v2001.Schema;

import java.util.List;

// Constellation dependencies
// Geotoolkit dependencies

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
    Object describeFeatureType(final DescribeFeatureType request) throws CstlServiceException;

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
    
    Schema getXsd(final WFSConstants.GetXSD request) throws CstlServiceException;
}
