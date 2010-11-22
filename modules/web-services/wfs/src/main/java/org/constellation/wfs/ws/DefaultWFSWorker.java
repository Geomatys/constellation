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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

// Constellation dependencies
import org.apache.xerces.dom.ElementNSImpl;
import org.constellation.ServiceDef;
import org.constellation.configuration.FormatURL;
import org.constellation.configuration.Layer;
import org.constellation.provider.FeatureLayerDetails;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.ws.CstlServiceException;
import static org.constellation.wfs.ws.WFSConstants.*;

// Geotoolkit dependencies
import org.constellation.ws.LayerWorker;
import org.geotoolkit.data.DataStore;
import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.data.DataUtilities;
import org.geotoolkit.feature.SchemaException;
import org.geotoolkit.ows.xml.RequestBase;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.factory.HintsPending;
import org.geotoolkit.feature.FeatureTypeUtilities;
import org.geotoolkit.feature.xml.jaxb.JAXBFeatureTypeWriter;
import org.geotoolkit.feature.xml.Utils;
import org.geotoolkit.gml.xml.v311.AbstractGMLEntry;
import org.geotoolkit.ogc.xml.v110.FilterType;
import org.geotoolkit.ogc.xml.v110.SortByType;
import org.geotoolkit.ows.xml.v100.WGS84BoundingBoxType;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.sld.xml.XMLUtilities;
import org.geotoolkit.util.collection.UnmodifiableArrayList;
import org.geotoolkit.wfs.xml.v110.DescribeFeatureTypeType;
import org.geotoolkit.wfs.xml.v110.FeatureTypeListType;
import org.geotoolkit.wfs.xml.v110.FeatureTypeType;
import org.geotoolkit.wfs.xml.v110.GetCapabilitiesType;
import org.geotoolkit.wfs.xml.v110.GetFeatureType;
import org.geotoolkit.wfs.xml.v110.GetGmlObjectType;
import org.geotoolkit.wfs.xml.v110.LockFeatureResponseType;
import org.geotoolkit.wfs.xml.v110.LockFeatureType;
import org.geotoolkit.wfs.xml.v110.QueryType;
import org.geotoolkit.wfs.xml.v110.TransactionResponseType;
import org.geotoolkit.wfs.xml.v110.TransactionType;
import org.geotoolkit.wfs.xml.v110.WFSCapabilitiesType;
import org.geotoolkit.xml.MarshallerPool;
import org.geotoolkit.xsd.xml.v2001.Schema;
import org.geotoolkit.filter.accessor.Accessors;
import org.geotoolkit.filter.accessor.PropertyAccessor;
import org.geotoolkit.filter.visitor.ListingPropertyVisitor;
import org.geotoolkit.filter.visitor.IsValidSpatialFilterVisitor;
import org.geotoolkit.gml.GeometrytoJTS;
import org.geotoolkit.gml.xml.v311.AbstractGeometryType;
import org.geotoolkit.ogc.xml.v110.FeatureIdType;
import org.geotoolkit.ows.xml.v100.KeywordsType;
import org.geotoolkit.ows.xml.v100.OperationsMetadata;
import org.geotoolkit.util.logging.Logging;
import org.geotoolkit.wfs.xml.v110.DeleteElementType;
import org.geotoolkit.wfs.xml.v110.FeatureCollectionType;
import org.geotoolkit.wfs.xml.v110.IdentifierGenerationOptionType;
import org.geotoolkit.wfs.xml.v110.InsertElementType;
import org.geotoolkit.wfs.xml.v110.InsertResultsType;
import org.geotoolkit.wfs.xml.v110.InsertedFeatureType;
import org.geotoolkit.wfs.xml.v110.PropertyType;
import org.geotoolkit.wfs.xml.v110.ResultTypeType;
import org.geotoolkit.wfs.xml.v110.TransactionSummaryType;
import org.geotoolkit.wfs.xml.v110.UpdateElementType;
import org.geotoolkit.wfs.xml.WFSMarshallerPool;
import org.geotoolkit.wfs.xml.v110.MetadataURLType;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

// GeoAPI dependencies
import org.geotoolkit.ows.xml.v100.ServiceIdentification;
import org.geotoolkit.ows.xml.v100.ServiceProvider;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.identity.FeatureId;
import org.opengis.filter.sort.SortBy;
import org.opengis.geometry.Envelope;
import org.opengis.util.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.CodeList;

// W3c dependencies
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class DefaultWFSWorker extends LayerWorker implements WFSWorker {

    /**
     * Base known CRS.
     */
    private final static List<String> DEFAULT_CRS = new ArrayList<String>();
    static {
        DEFAULT_CRS.add("urn:x-ogc:def:crs:EPSG:7.01:4326");
        DEFAULT_CRS.add("urn:x-ogc:def:crs:EPSG:7.01:3395");
    }
    /**
     * The current version of the service.
     */
    private ServiceDef actingVersion = ServiceDef.WFS_1_1_0;

    /**
     * Current map with namespace - xsd location. << ISSUE multiThread
     */
    private Map<String, String> schemaLocations;

    /**
     * Current mapping between prefix and namespace << ISSUE multiThread
     */
    private Map<String, String> namespaceMapping;

    /**
     * Current outputFormat requested (default value is text/xml) << ISSUE multiThread
     */
    private String outputFormat = "text/xml";

    public DefaultWFSWorker(String id, File configurationDirectory) {
        super(id, configurationDirectory);
        if (isStarted) {
            LOGGER.log(Level.INFO, "WFS worker {0} running", id);
        }
    }

    @Override
    protected MarshallerPool getMarshallerPool() {
        return WFSMarshallerPool.getInstance();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public WFSCapabilitiesType getCapabilities(final GetCapabilitiesType request) throws CstlServiceException {
        LOGGER.log(logLevel, "GetCapabilities request proccesing");
        final long start = System.currentTimeMillis();

        // we verify the base attribute
        isWorking();
        verifyBaseRequest(request, false, true);

        outputFormat = request.getFirstAcceptFormat();
        if (outputFormat == null) {
            outputFormat = "application/xml";
        }
        
        final WFSCapabilitiesType inCapabilities;
        try {
            inCapabilities = (WFSCapabilitiesType) getStaticCapabilitiesObject(actingVersion.version.toString(), "WFS");
        } catch (JAXBException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        }

        FeatureTypeListType ftl  = null;
        OperationsMetadata om    = null;
        ServiceProvider sp       = null;
        ServiceIdentification si = null;
        
        if (request.getSections() == null || request.containsSection("featureTypeList")) {
            final List<FeatureTypeType> types = new ArrayList<FeatureTypeType>();

            /*
             *  layer providers
             */
            final LayerProviderProxy namedProxy    = LayerProviderProxy.getInstance();
            for (final Name layerName : layers.keySet()) {
                final LayerDetails layer = namedProxy.get(layerName);
                final Layer configLayer  = layers.get(layerName);

                if (layer instanceof FeatureLayerDetails) {
                    final FeatureLayerDetails fld = (FeatureLayerDetails) layer;
                    final FeatureType type;
                    try {
                        type  = getFeatureTypeFromLayer(fld);
                    } catch (DataStoreException ex) {
                        LOGGER.severe("error while getting featureType for:" + fld.getName() + 
                                "\ncause:" + ex.getMessage());
                        continue;
                    }
                    final FeatureTypeType ftt;
                    try {

                        final String defaultCRS;
                        if (type.getGeometryDescriptor() != null && type.getGeometryDescriptor().getCoordinateReferenceSystem() != null) {
                            final CoordinateReferenceSystem crs = type.getGeometryDescriptor().getCoordinateReferenceSystem();
                            //todo wait for martin fix
                            String id  = CRS.lookupIdentifier(crs, true);
                            if (id == null) {
                                id = CRS.getDeclaredIdentifier(crs);
                            }

                            if (id != null) {
                                defaultCRS = "urn:x-ogc:def:crs:" + id.replaceAll(":", ":7.01:");
            //                    final String defaultCRS = CRS.lookupIdentifier(Citations.URN_OGC,
            //                            type.getGeometryDescriptor().getCoordinateReferenceSystem(), true);
                            } else {
                                defaultCRS = "urn:x-ogc:def:crs:EPSG:7.01:4326";
                            }
                        } else {
                            defaultCRS = "urn:x-ogc:def:crs:EPSG:7.01:4326";
                        }
                        final String title;
                        if (configLayer.getTitle() != null) {
                            title = configLayer.getTitle();
                        } else {
                            title = fld.getName().getLocalPart();
                        }
                        ftt = new FeatureTypeType(
                                Utils.getQnameFromName(layerName),
                                title,
                                defaultCRS,
                                DEFAULT_CRS,
                                UnmodifiableArrayList.wrap(new WGS84BoundingBoxType[]{toBBox(fld.getStore(), fld.getName())}));

                        /*
                         * we apply the layer customization
                         */
                        ftt.setAbstract(configLayer.getAbstrac());
                        if (!configLayer.getKeywords().isEmpty()) {
                            ftt.getKeywords().add(new KeywordsType(configLayer.getKeywords(), null));
                        }
                        FormatURL metadataURL = configLayer.getMetadataURL();
                        if (metadataURL != null) {
                            ftt.getMetadataURL().add(new MetadataURLType(metadataURL.getOnlineResource().getValue(),
                                                                         metadataURL.getType(),
                                                                         metadataURL.getFormat()));
                        }
                        if (!configLayer.getCrs().isEmpty()) {
                            ftt.setOtherSRS(configLayer.getCrs());
                        }

                        // we add the feature type description to the list
                        types.add(ftt);
                    } catch (FactoryException ex) {
                        Logging.unexpectedException(LOGGER, ex);
                    }

                } else {
                    LOGGER.log(Level.WARNING, "The layer:{0} is not a feature layer", layerName);
                }
            }

            ftl = new FeatureTypeListType(null, types);
        }
        //todo ...etc...--------------------------------------------------------

        if (request.getSections() == null || request.containsSection("operationsMetadata")) {
            final String url = getServiceUrl();
            if (url != null) {
                OPERATIONS_METADATA.updateURL(url, "WFS");
            }
            om = OPERATIONS_METADATA;
        }
        if (request.getSections() == null || request.containsSection("serviceProvider")) {
            sp = inCapabilities.getServiceProvider();
        }
        if (request.getSections() == null || request.containsSection("serviceIdentification")) {
            si = inCapabilities.getServiceIdentification();
        }

        final WFSCapabilitiesType result = new WFSCapabilitiesType("1.1.0", si, sp, om, ftl, WFSConstants.FILTER_CAPABILITIES);
        result.setServesGMLObjectTypeList(null);
        result.setSupportsGMLObjectTypeList(null);

        LOGGER.log(logLevel, "GetCapabilities treated in {0}ms", (System.currentTimeMillis() - start));
        return result;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Schema describeFeatureType(final DescribeFeatureTypeType request) throws CstlServiceException {
        LOGGER.log(logLevel, "DecribeFeatureType request proccesing");
        final long start = System.currentTimeMillis();

        // we verify the base attribute
        isWorking();
        verifyBaseRequest(request, false, false);

        String requestOutputFormat                = request.getOutputFormat();
        if (requestOutputFormat == null) {
            requestOutputFormat = "text/xml; subtype=gml/3.1.1";
        }
        outputFormat = requestOutputFormat;
        
        final JAXBFeatureTypeWriter writer;
        try {
            writer = new JAXBFeatureTypeWriter();
        } catch (JAXBException ex) {
            throw new CstlServiceException(ex);
        }
        final LayerProviderProxy namedProxy = LayerProviderProxy.getInstance();
        final List<QName> names             = request.getTypeName();
        final List<FeatureType> types       = new ArrayList<FeatureType>();

        if (names.isEmpty()) {
            //search all types
            for (final Name name : layers.keySet()) {
                final LayerDetails layer = namedProxy.get(name);
                if (!(layer instanceof FeatureLayerDetails)) continue;

                try {
                    types.add(getFeatureTypeFromLayer((FeatureLayerDetails)layer));
                } catch (DataStoreException ex) {
                    LOGGER.log(Level.WARNING, "error while getting featureType for:{0}", layer.getName());
                }
            }
        } else {

            //search only the given list
            for (final QName name : names) {
                final Name n = Utils.getNameFromQname(name);
                if (layersContainsKey(n) == null) {
                    throw new CstlServiceException(UNKNOW_TYPENAME + name);
                }

                final LayerDetails layer = namedProxy.get(n);
                
                if(!(layer instanceof FeatureLayerDetails)) {
                    throw new CstlServiceException(UNKNOW_TYPENAME + name);
                }

                try {
                    types.add(getFeatureTypeFromLayer((FeatureLayerDetails)layer));
                } catch (DataStoreException ex) {
                    LOGGER.log(Level.WARNING, "error while getting featureType for:{0}", layer.getName());
                }
            }
        }

        final int size = types.size();
        for (int i = 0; i < size; i++) {
            try {
                types.set(i, FeatureTypeUtilities.excludePrimaryKeyFields(types.get(i)));
            } catch (SchemaException ex) {
                LOGGER.log(Level.SEVERE, "error while excluding primary keys", ex);
            }
        }
        LOGGER.log(logLevel, "DescribeFeatureType treated in {0}ms", (System.currentTimeMillis() - start));
        return writer.getSchemaFromFeatureType(types);
    }

    /**
     * Extract a FeatureType from a FeatureLayerDetails
     * 
     * @param fld A feature layer object.
     * @return A Feature type.
     */
    private FeatureType getFeatureTypeFromLayer(FeatureLayerDetails fld) throws DataStoreException {
        return fld.getStore().getFeatureType(fld.getName());
    }


    /**
     * {@inheritDoc }
     */
    @Override
    public Object getFeature(final GetFeatureType request) throws CstlServiceException {
        LOGGER.log(logLevel, "GetFeature request proccesing");
        final long start = System.currentTimeMillis();

        // we verify the base attribute
        isWorking();
        verifyBaseRequest(request, false, false);

        // we verify the outputFormat requested (default text/xml; subtype=gml/3.1.1)
        String requestOutputFormat = request.getOutputFormat();
        if (requestOutputFormat == null) {
            requestOutputFormat = "text/xml; subtype=gml/3.1.1";
        }
        outputFormat = requestOutputFormat;

        final LayerProviderProxy namedProxy       = LayerProviderProxy.getInstance();
        final String featureId                    = request.getFeatureId();
        final XMLUtilities util                   = new XMLUtilities();
        final Integer maxFeatures                 = request.getMaxFeatures();
        final List<FeatureCollection> collections = new ArrayList<FeatureCollection>();
        schemaLocations                           = new HashMap<String, String>();

        if (request.getQuery() == null || request.getQuery().isEmpty()) {
            throw new CstlServiceException("You must specify a query!", MISSING_PARAMETER_VALUE);
        }
        
        for (final QueryType query : request.getQuery()) {
            final FilterType jaxbFilter   = query.getFilter();
            final SortByType jaxbSortBy   = query.getSortBy();
            final String srs              = query.getSrsName();
            final List<Object> properties = query.getPropertyNameOrXlinkPropertyNameOrFunction();

            final List<QName> typeNames;
            if (featureId != null && query.getTypeName().isEmpty()) {
                typeNames = Utils.getQNameListFromNameSet(layers.keySet());
            } else {
                typeNames = query.getTypeName();
            }
            
            final List<String> requestPropNames = new ArrayList<String>();
            final List<SortBy> sortBys          = new ArrayList<SortBy>();

            //decode filter-----------------------------------------------------
            final Filter filter;
            if (featureId == null) {
                filter = extractJAXBFilter(jaxbFilter, Filter.INCLUDE);
            } else {
                filter = extractJAXBFilter(new FilterType(new FeatureIdType(featureId)), Filter.INCLUDE);
            }

            //decode crs--------------------------------------------------------
            final CoordinateReferenceSystem crs = extractCRS(srs);

            //decode property names---------------------------------------------
            for (Object obj : properties) {
                if (obj instanceof JAXBElement){
                    obj = ((JAXBElement)obj).getValue();
                }
                
                if (obj instanceof String) {
                    String pName  = (String) obj;
                    final int pos = pName.lastIndexOf(':');
                    if (pos != -1) {
                        pName = pName.substring(pos + 1);
                    }
                    requestPropNames.add(pName);
                }
            }

            //decode sort by----------------------------------------------------
            if (jaxbSortBy != null) {
                sortBys.addAll(util.getTransformer110(namespaceMapping).visitSortBy(jaxbSortBy));
            }

            final QueryBuilder queryBuilder = new QueryBuilder();
            queryBuilder.setFilter(filter);
            queryBuilder.setCRS(crs);

            if (!sortBys.isEmpty()) {
                queryBuilder.setSortBy(sortBys.toArray(new SortBy[sortBys.size()]));
            }
            if (maxFeatures != null){
                queryBuilder.setMaxFeatures(maxFeatures);
            }

            for (QName typeName : typeNames) {

                final Name fullTypeName = Utils.getNameFromQname(typeName);
                if (layersContainsKey(fullTypeName) == null) {
                    throw new CstlServiceException(UNKNOW_TYPENAME + typeName);
                }
                final LayerDetails layerD = namedProxy.get(fullTypeName);

                if (!(layerD instanceof FeatureLayerDetails)) continue;

                final FeatureLayerDetails layer = (FeatureLayerDetails) layerD;

                final FeatureType ft;
                try {
                    ft = getFeatureTypeFromLayer(layer);
                } catch (DataStoreException ex) {
                    throw new CstlServiceException(ex);
                }
                // we ensure that the property names are contained in the feature type and add the mandatory attribute to the list
                if (!requestPropNames.isEmpty()) {
                    final List<Name> propertyNames = new ArrayList<Name>();
                    for (PropertyDescriptor pdesc : ft.getDescriptors()) {
                        final Name propName = pdesc.getName();

                        if (!pdesc.isNillable()) {
                            if (!propertyNames.contains(propName)) {
                                propertyNames.add(propName);
                            }
                        } else if (requestPropNames.contains(propName.getLocalPart())) {
                            propertyNames.add(propName);
                        }
                        
                        requestPropNames.remove(propName.getLocalPart());
                    }
                    // if the requestPropNames is not empty there is unKnown propertyNames
                    if (!requestPropNames.isEmpty()) {
                        throw new CstlServiceException("The feature Type " + typeName + " does not have such a property:" + requestPropNames.get(0), INVALID_PARAMETER_VALUE);
                    }
                    queryBuilder.setProperties(propertyNames.toArray(new Name[propertyNames.size()]));
                } else  {
                    queryBuilder.setProperties((Name[])null);
                }

                queryBuilder.setTypeName(ft.getName());
                queryBuilder.setHints(new Hints(HintsPending.FEATURE_HIDE_ID_PROPERTY, Boolean.TRUE));

                // we verify that all the properties contained in the filter are known by the feature type.
                verifyFilterProperty(ft, filter);

                final FeatureCollection<Feature> collection = layer.getStore().createSession(false).getFeatureCollection(queryBuilder.buildQuery());
                collections.add(collection);
                

                // we write The SchemaLocation
                final String namespace = typeName.getNamespaceURI();
                if (schemaLocations.containsKey(namespace)) {
                    LOGGER.severe("TODO multiple typeName schemaLocation");

                } else {
                    final String prefix = typeName.getPrefix();
                    final String url    = getServiceUrl();
                    if (getServiceUrl() != null) {
                        String describeRequest = url + "wfs?request=DescribeFeatureType&version=1.1.0&service=WFS";
                        describeRequest        = describeRequest + "&namespace=xmlns(" + prefix + "=" + namespace + ")";
                        describeRequest        = describeRequest + "&Typename=" + prefix + ':' + typeName.getLocalPart();
                        schemaLocations.put(namespace, describeRequest);
                    }
                }
            }

        }

        /**
         * 3 possibilité ici :
         *    1) mergé les collections
         *    2) retourné une collection de collection.
         *    3) si il n'y a qu'un feature on le retourne (changer le type de retour en object)
         *
         * result TODO find an id and a member type
         */
        final FeatureCollection response;
	if (collections.size() > 1) {
            response = DataUtilities.sequence("collection-1", collections.toArray(new FeatureCollection[collections.size()]));
        } else if (collections.size() == 1) {
            response = collections.get(0);
        } else {
            response = DataUtilities.collection("collection-1", null);
        }
        if (request.getResultType() == ResultTypeType.HITS) {
            final FeatureCollectionType collection = new FeatureCollectionType(response.size(), org.geotoolkit.internal.jaxb.XmlUtilities.toXML(new Date()));
            collection.setId("collection-1");
            return collection;

        }
        LOGGER.log(logLevel, "GetFeature treated in {0}ms", (System.currentTimeMillis() - start));
        return response;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public AbstractGMLEntry getGMLObject(GetGmlObjectType grbi) throws CstlServiceException {
        throw new CstlServiceException("WFS get GML Object is not supported on this Constellation version.");
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public LockFeatureResponseType lockFeature(LockFeatureType gr) throws CstlServiceException {
        throw new CstlServiceException("WFS Lock is not supported on this Constellation version.");
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public TransactionResponseType transaction(TransactionType request) throws CstlServiceException {
        LOGGER.log(logLevel, "Transaction request processing\n");
        final long startTime = System.currentTimeMillis();
        isWorking();
        verifyBaseRequest(request, true, false);

        // we prepare the report
        int totalInserted                        = 0;
        int totalUpdated                         = 0;
        int totalDeleted                         = 0;
        final LayerProviderProxy namedProxy      = LayerProviderProxy.getInstance();
        InsertResultsType insertResults          = null;
        final List<Object> transactions          = request.getInsertOrUpdateOrDelete();
        final List<InsertedFeatureType> inserted = new ArrayList<InsertedFeatureType>();
        for (Object transaction: transactions) {

            /**
             * Features insertion.
             */
            if (transaction instanceof InsertElementType) {
                final InsertElementType insertRequest = (InsertElementType)transaction;

                final String handle = insertRequest.getHandle();

                // we verify the input format
                if (insertRequest.getInputFormat() != null && !insertRequest.getInputFormat().equals("text/xml; subtype=gml/3.1.1")) {
                    throw new CstlServiceException("This only input format supported is: text/xml; subtype=gml/3.1.1",
                            INVALID_PARAMETER_VALUE, "inputFormat");
                }

                // what to do with the CRS ?
                final CoordinateReferenceSystem insertCRS = extractCRS(insertRequest.getSrsName());

                // what to do with that, whitch ones are supported ??
                final IdentifierGenerationOptionType idGen = insertRequest.getIdgen();

                for (Object featureObject : insertRequest.getFeature()) {
                    final Name typeName;
                    final Collection<? extends Feature> featureCollection;

                    if (featureObject instanceof Feature) {
                        final Feature feature = (Feature) featureObject;
                        typeName = feature.getType().getName();
                        featureCollection = Collections.singleton(feature);
                    } else if (featureObject instanceof FeatureCollection) {
                        featureCollection = (FeatureCollection) featureObject;
                        typeName = ((FeatureCollection)featureCollection).getFeatureType().getName();
                    } else {
                        final String featureType;
                        if (featureObject == null) {
                            featureType = "null";
                        } else {
                            featureType = featureObject.getClass().getName();
                        }
                        throw new CstlServiceException("Unexpected Object to insert:" + featureType);
                    }

                    if (layersContainsKey(typeName) == null) {
                        throw new CstlServiceException(UNKNOW_TYPENAME + typeName);
                    }
                    final FeatureLayerDetails layer = (FeatureLayerDetails) namedProxy.get(typeName);
                    try {
                        final List<FeatureId> features = layer.getStore().addFeatures(typeName, featureCollection);

                        for (FeatureId fid : features) {
                            final String id = fid.getID(); // get the id of the inserted feature
                            inserted.add(new InsertedFeatureType(new FeatureIdType(id), handle));
                            totalInserted++;
                            LOGGER.finer("fid inserted: " + fid + " total:" + totalInserted);
                        }
                    } catch (DataStoreException ex) {
                        Logging.unexpectedException(LOGGER, ex);
                    } catch (ClassCastException ex) {
                        Logging.unexpectedException(LOGGER, ex);
                        throw new CstlServiceException("The specified Datastore does not suport the write operations.");
                    }
                }

            /**
             * Features remove.
             */
            } else if (transaction instanceof DeleteElementType) {
                
                final DeleteElementType deleteRequest = (DeleteElementType) transaction;

                //decode filter-----------------------------------------------------
                if (deleteRequest.getFilter() == null) {
                    throw new CstlServiceException("The filter must be specified.", MISSING_PARAMETER_VALUE, "filter");
                }
                final Filter filter = extractJAXBFilter(deleteRequest.getFilter(), Filter.EXCLUDE);

                final Name typeName = Utils.getNameFromQname(deleteRequest.getTypeName());
                if (layersContainsKey(typeName) == null) {
                    throw new CstlServiceException(UNKNOW_TYPENAME + typeName);
                }
                final FeatureLayerDetails layer = (FeatureLayerDetails)namedProxy.get(typeName);
                try {
                    final FeatureType ft = getFeatureTypeFromLayer(layer);

                    // we verify that all the properties contained in the filter are known by the feature type.
                    verifyFilterProperty(ft, filter);

                    // we extract the number of feature deleted
                    final QueryBuilder queryBuilder = new QueryBuilder(layer.getName());
                    queryBuilder.setFilter(filter);
                    totalDeleted = totalDeleted + (int) layer.getStore().getCount(queryBuilder.buildQuery());

                    layer.getStore().removeFeatures(layer.getName(), filter);
                } catch (DataStoreException ex) {
                    throw new CstlServiceException(ex);
                } catch (ClassCastException ex) {
                    Logging.unexpectedException(LOGGER, ex);
                    throw new CstlServiceException("The specified Datastore does not suport the delete operations.");
                }

            /**
             * Features updates.
             */
            } else if (transaction instanceof UpdateElementType) {

                final UpdateElementType updateRequest = (UpdateElementType) transaction;

                // we verify the input format
                if (updateRequest.getInputFormat() != null && !updateRequest.getInputFormat().equals("text/xml; subtype=gml/3.1.1")) {
                    throw new CstlServiceException("The only input format supported is: text/xml; subtype=gml/3.1.1",
                            INVALID_PARAMETER_VALUE, "inputFormat");
                }
                
                //decode filter-----------------------------------------------------
                final Filter filter = extractJAXBFilter(updateRequest.getFilter(),Filter.EXCLUDE);

                //decode crs--------------------------------------------------------
                final CoordinateReferenceSystem crs = extractCRS(updateRequest.getSrsName());

                final Name typeName = Utils.getNameFromQname(updateRequest.getTypeName());
                if (layersContainsKey(typeName) == null) {
                    throw new CstlServiceException(UNKNOW_TYPENAME + typeName);
                }
                final FeatureLayerDetails layer = (FeatureLayerDetails)namedProxy.get(typeName);
                try {
                    final FeatureType ft = getFeatureTypeFromLayer(layer);
                    if (ft == null) {
                        throw new CstlServiceException("Unable to find the featuretype:" + layer.getName());
                    }

                    final Map<PropertyDescriptor,Object> values = new HashMap<PropertyDescriptor, Object>();

                    // we verify that the update property are contained in the feature type
                    for (final PropertyType updateProperty : updateRequest.getProperty()) {
                        final String updatePropertyValue = updateProperty.getName().getLocalPart();
                        final PropertyAccessor pa        = Accessors.getAccessor(Feature.class, updatePropertyValue, null);
                        if (pa == null || pa.get(ft, updatePropertyValue, null) == null) {
                            throw new CstlServiceException("The feature Type " + updateRequest.getTypeName() + " does not has such a property: " + updatePropertyValue, INVALID_PARAMETER_VALUE);
                        }
                        Object value;
                        if (updateProperty.getValue() instanceof ElementNSImpl) {
                            final String strValue = getXMLFromElementNSImpl((ElementNSImpl)updateProperty.getValue());
                            value = null;
                            LOGGER.finer(">> updating : "+ updatePropertyValue +"   => " + strValue);
                        } else {
                            value = updateProperty.getValue();
                            if (value instanceof AbstractGeometryType) {
                                try {
                                    value = GeometrytoJTS.toJTS((AbstractGeometryType) value);
                                } catch (NoSuchAuthorityCodeException ex) {
                                    Logging.unexpectedException(LOGGER, ex);
                                } catch (FactoryException ex) {
                                    Logging.unexpectedException(LOGGER, ex);
                                } catch (IllegalArgumentException ex) {
                                    throw new CstlServiceException(ex);
                                }
                            }
                            LOGGER.finer(">> updating : "+ updatePropertyValue +"   => " + value);
                            if (value != null) {
                                LOGGER.log(Level.FINER, "type : {0}", value.getClass());
                            }
                        }
                        values.put(ft.getDescriptor(updatePropertyValue), value);
                        
                    }

                    // we verify that all the properties contained in the filter are known by the feature type.
                    verifyFilterProperty(ft, filter);

                    // we extract the number of feature update
                    final QueryBuilder queryBuilder = new QueryBuilder(layer.getName());
                    queryBuilder.setFilter(filter);
                    totalUpdated = totalUpdated + (int) layer.getStore().getCount(queryBuilder.buildQuery());

                    layer.getStore().updateFeatures(layer.getName(), filter, values);
                } catch (DataStoreException ex) {
                    throw new CstlServiceException(ex);
                }

                
            } else {
                String className = " null object";
                if (transaction != null) {
                    className = transaction.getClass().getName();
                }
                throw new CstlServiceException("This kind of transaction is not supported by the service: " + className,
                                              INVALID_PARAMETER_VALUE, "transaction");
            }

        }
        if (inserted.size() > 0) {
            insertResults = new InsertResultsType(inserted);
        }
        final TransactionSummaryType summary = new TransactionSummaryType(totalInserted,
                                                                          totalUpdated,
                                                                          totalDeleted);

        final TransactionResponseType response = new TransactionResponseType(summary, null, insertResults, actingVersion.version.toString());
        LOGGER.log(logLevel, "Transaction request processed in {0} ms", (System.currentTimeMillis() - startTime));
        
        return response;
    }

    /**
     * Extract the a XML string from a W3C Element.
     *
     * @param node An W3c Xml Element.
     *
     * @return a string containing the xml representation.
     */
    private  String getXMLFromElementNSImpl(ElementNSImpl elt) {
        final StringBuilder s = new StringBuilder();
        s.append('<').append(elt.getLocalName()).append('>');
        final Node node = elt.getFirstChild();
        s.append(getXMLFromNode(node));

        s.append("</").append(elt.getLocalName()).append('>');
        return s.toString();
    }

    /**
     * Extract the a XML string from a W3C node.
     *
     * @param node An W3c Xml node.
     *
     * @return a string builder containing the xml.
     */
    private  StringBuilder getXMLFromNode(Node node) {
        final StringBuilder temp = new StringBuilder();
        if (!node.getNodeName().equals("#text")){
            temp.append("<").append(node.getNodeName());
            final NamedNodeMap attrs = node.getAttributes();
            for(int i=0;i<attrs.getLength();i++){
                temp.append(" ").append(attrs.item(i).getNodeName()).append("=\"").append(attrs.item(i).getTextContent()).append("\" ");
            }
            temp.append(">");
        }
        if (node.hasChildNodes()) {
            final NodeList nodes = node.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                temp.append(getXMLFromNode(nodes.item(i)));
            }
        }
        else{
            temp.append(node.getTextContent());
        }
        if (!node.getNodeName().equals("#text")) temp.append("</").append(node.getNodeName()).append(">");
        return temp;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public MediaType getOutputFormat() {
        if (outputFormat.equals("text/xml; subtype=gml/3.1.1")) {
            return new MediaType("text", "xml; subtype=gml/3.1.1");
        } else {
            return MediaType.valueOf(outputFormat);
        }
    }

    /**
     * Extract an OGC filter usable by the datastore from the request filter
     * unmarshalled by JAXB.
     *
     * @param jaxbFilter an OGC JAXB filter.
     * @return An OGC filter
     * @throws CstlServiceException
     */
    private Filter extractJAXBFilter(FilterType jaxbFilter, Filter defaultFilter) throws CstlServiceException {
        final XMLUtilities util = new XMLUtilities();
        final Filter filter;
        try {
            if (jaxbFilter != null) {
                filter = util.getTransformer110(namespaceMapping).visitFilter(jaxbFilter);
                //System.out.println("jaxbFIlter:" + filter);
            } else {
                filter = defaultFilter;
            }
        } catch (Exception ex) {
            throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE);
        }
        return filter;
    }

    /**
     * Return a coordinate reference system from an identifier.
     *
     * @param srsName a CRS identifier.
     * @return
     * @throws CstlServiceException
     */
    private CoordinateReferenceSystem extractCRS(String srsName) throws CstlServiceException {
        final CoordinateReferenceSystem crs;
        if (srsName != null) {
            try {
                crs = CRS.decode(srsName, true);
                //todo use other properties to filter properly
            } catch (NoSuchAuthorityCodeException ex) {
                throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE);
            } catch (FactoryException ex) {
                throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE);
            }
        } else {
            crs = null;
        }
        return crs;
    }

    /**
     * Verify that all the property contained in the filter are known by the featureType
     *
     * @param ft A featureType.
     * @param filter An OGC filter.
     *
     * @throws CstlServiceException if one of the propertyName in the filter is not present in the featureType.
     */
    private void verifyFilterProperty(FeatureType ft, Filter filter) throws CstlServiceException {
        final Collection<String> filterProperties = (Collection<String>) filter.accept(ListingPropertyVisitor.VISITOR, null);
        if (filterProperties != null) {
            for (String filterProperty : filterProperties) {

                if (filterProperty.startsWith("@")){
                    //this property in an id property, we won't find it in the feature type
                    //but it always exist on the features
                    continue;
                }
                final PropertyAccessor pa = Accessors.getAccessor(Feature.class, filterProperty, null);
                if (pa == null || pa.get(ft, filterProperty, null) == null) {
                    throw new CstlServiceException("The feature Type " + ft.getName() + " does not has such a property: " + filterProperty, INVALID_PARAMETER_VALUE, "filter");
                }
            }
        }
        
        if (!((Boolean)filter.accept(new IsValidSpatialFilterVisitor(ft), null))) {
            throw new CstlServiceException("The filter try to apply spatial operators on non-spatial property", INVALID_PARAMETER_VALUE, "filter");
        }
    }
    
    /**
     * Extract the WGS84 BBOx from a featureSource.
     * what ? may not be wgs84 exactly ? why is there a crs attribut on a wgs84 bbox ?
     */
    private static WGS84BoundingBoxType toBBox(DataStore source, Name groupName) throws CstlServiceException{
        try {
            Envelope env = source.getEnvelope(QueryBuilder.all(groupName));
            final CoordinateReferenceSystem epsg4326 = CRS.decode("urn:ogc:def:crs:OGC:2:84");
            if (env != null) {
                if (!CRS.equalsIgnoreMetadata(env.getCoordinateReferenceSystem(), epsg4326)) {
                    env = CRS.transform(env, epsg4326);
                }
                return new WGS84BoundingBoxType(
                       "urn:ogc:def:crs:OGC:2:84",
                       env.getMinimum(0),
                       env.getMinimum(1),
                       env.getMaximum(0),
                       env.getMaximum(1));
            } else {
                return new WGS84BoundingBoxType("urn:ogc:def:crs:OGC:2:84", -180, -90, 180, 90);
            }

        } catch (DataStoreException ex) {
            throw new CstlServiceException(ex);
        } catch (TransformException ex) {
            throw new CstlServiceException(ex);
        } catch (FactoryException ex) {
            throw new CstlServiceException(ex);
        }
    }

    /**
     * Verify that the bases request attributes are correct.
     *
     * @param request an object request with the base attribute (all except GetCapabilities request);
     */
    private void verifyBaseRequest(final RequestBase request, boolean versionMandatory, boolean getCapabilities) throws CstlServiceException {
        if (request != null) {
            if (request.getService() != null) {
                if (request.getService().isEmpty()) {
                  // we let pass (CITE test)
                } else if (!request.getService().equalsIgnoreCase("WFS"))  {
                    throw new CstlServiceException("service must be \"WFS\"!",
                                                  INVALID_PARAMETER_VALUE, "service");
                }
            } else {
                throw new CstlServiceException("service must be specified!",
                                              MISSING_PARAMETER_VALUE, "service");
            }
            if (request.getVersion() != null) {
                if (request.getVersion().toString().equals("1.1.0") || request.getVersion().toString().equals("1.1") || 
                        request.getVersion().toString().isEmpty()  || request.getVersion().toString().equals("1.0.0") ) { // hack for openScale accept 1.0.0
                    this.actingVersion = ServiceDef.WFS_1_1_0;

                } else {
                    CodeList code;
                    if (getCapabilities) {
                        code = VERSION_NEGOTIATION_FAILED;
                    } else {
                        code = INVALID_PARAMETER_VALUE;
                    }
                    throw new CstlServiceException("version must be \"1.1.0\"!", code, "version");
                }
            } else {
                if (versionMandatory) {
                    throw new CstlServiceException("version must be specified!", MISSING_PARAMETER_VALUE, "version");
                } else {
                    this.actingVersion = ServiceDef.WFS_1_1_0;
                }
            }
         } else {
            throw new CstlServiceException("The request is null!", NO_APPLICABLE_CODE);
         }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Map<String, String> getSchemaLocations() {
        return schemaLocations;
    }


    /**
     * {@inheritDoc }
     */
    @Override
    public List<FeatureType> getFeatureTypes() {
        final List<FeatureType> types       = new ArrayList<FeatureType>();
        final LayerProviderProxy namedProxy = LayerProviderProxy.getInstance();

        //search all types
        for (final Name name : layers.keySet()) {
            final LayerDetails layer = namedProxy.get(name);
            if (!(layer instanceof FeatureLayerDetails)) continue;

            try {
                types.add(getFeatureTypeFromLayer((FeatureLayerDetails)layer));
            } catch (DataStoreException ex) {
                LOGGER.severe("DataStore exception while getting featureType");
            }
        }
        return types;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void setprefixMapping(Map<String, String> namespaceMapping) {
       this.namespaceMapping = namespaceMapping;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void destroy() {
    }

}
