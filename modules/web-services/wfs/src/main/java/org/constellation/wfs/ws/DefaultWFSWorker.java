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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

// Constellation dependencies
import javax.xml.stream.XMLStreamException;
import org.constellation.ServiceDef;
import org.constellation.configuration.FormatURL;
import org.constellation.configuration.Layer;
import org.constellation.provider.FeatureLayerDetails;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.util.NameComparator;
import org.constellation.ws.CstlServiceException;
import static org.constellation.wfs.ws.WFSConstants.*;
import org.constellation.wfs.ws.rs.FeatureCollectionWrapper;

// Geotoolkit dependencies
import org.constellation.ws.LayerWorker;
import org.geotoolkit.util.logging.Logging;
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
import org.geotoolkit.feature.xml.XmlFeatureReader;
import org.geotoolkit.feature.xml.jaxp.JAXPStreamFeatureReader;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.IdentifiedObjects;
import org.geotoolkit.sld.xml.XMLUtilities;
import org.geotoolkit.xml.MarshallerPool;
import org.geotoolkit.xsd.xml.v2001.Schema;
import org.geotoolkit.filter.accessor.Accessors;
import org.geotoolkit.filter.accessor.PropertyAccessor;
import org.geotoolkit.filter.visitor.ListingPropertyVisitor;
import org.geotoolkit.filter.visitor.IsValidSpatialFilterVisitor;
import org.geotoolkit.gml.GeometrytoJTS;
import org.geotoolkit.gml.xml.AbstractGML;
import org.geotoolkit.gml.xml.v311.AbstractGeometryType;
import org.geotoolkit.gml.xml.v311.FeaturePropertyType;
import org.geotoolkit.ows.xml.AbstractOperationsMetadata;
import org.geotoolkit.ows.xml.AbstractServiceIdentification;
import org.geotoolkit.ows.xml.AbstractServiceProvider;
import org.geotoolkit.wfs.xml.GetPropertyValue;
import org.geotoolkit.wfs.xml.DescribeFeatureType;
import org.geotoolkit.wfs.xml.DescribeStoredQueries;
import org.geotoolkit.wfs.xml.DescribeStoredQueriesResponse;
import org.geotoolkit.wfs.xml.ListStoredQueries;
import org.geotoolkit.wfs.xml.ListStoredQueriesResponse;
import org.geotoolkit.wfs.xml.GetCapabilities;
import org.geotoolkit.wfs.xml.WFSCapabilities;
import org.geotoolkit.wfs.xml.ResultTypeType;
import org.geotoolkit.wfs.xml.WFSMarshallerPool;
import org.geotoolkit.wfs.xml.WFSXmlFactory;
import org.geotoolkit.wfs.xml.TransactionResponse;
import org.geotoolkit.wfs.xml.Transaction;
import org.geotoolkit.wfs.xml.Query;
import org.geotoolkit.wfs.xml.GetFeature;
import org.geotoolkit.wfs.xml.GetGmlObject;
import org.geotoolkit.wfs.xml.LockFeatureResponse;
import org.geotoolkit.wfs.xml.LockFeature;
import org.geotoolkit.wfs.xml.DeleteElement;
import org.geotoolkit.wfs.xml.InsertElement;
import org.geotoolkit.wfs.xml.IdentifierGenerationOptionType;
import org.geotoolkit.wfs.xml.Property;
import org.geotoolkit.wfs.xml.FeatureTypeList;
import org.geotoolkit.wfs.xml.UpdateElement;
import org.geotoolkit.wfs.xml.v110.FeatureCollectionType;
import org.geotoolkit.wfs.xml.v200.PropertyName;
import org.geotoolkit.wfs.xml.ValueCollection;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import org.geotoolkit.wfs.xml.*;

// GeoAPI dependencies
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.capability.FilterCapabilities;
import org.opengis.filter.identity.FeatureId;
import org.opengis.filter.sort.SortBy;
import org.opengis.geometry.Envelope;
import org.opengis.util.FactoryException;
import org.opengis.util.CodeList;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

// W3c dependencies
import org.w3c.dom.Element;
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

    private boolean multipleVersionActivated = true;
    
    private static final WFSXmlFactory xmlFactory = new WFSXmlFactory();

    public DefaultWFSWorker(final String id, final File configurationDirectory) {
        super(id, configurationDirectory, ServiceDef.Specification.WFS);
        if (isStarted) {
            LOGGER.log(Level.INFO, "WFS worker {0} running", id);
        }
        
        //listen to changes on the providers to clear the getcapabilities cache
        LayerProviderProxy.getInstance().addPropertyListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                refreshUpdateSequence();
            }
        });
        
        final String multiVersProp = getProperty("multipleVersion");
        if (multiVersProp != null) {
            multipleVersionActivated = Boolean.parseBoolean(multiVersProp);
            LOGGER.log(Level.INFO, "Multiple version activated:{0}", multipleVersionActivated);
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
    public WFSCapabilities getCapabilities(final GetCapabilities request) throws CstlServiceException {
        LOGGER.log(logLevel, "GetCapabilities request proccesing");
        final long start = System.currentTimeMillis();
        // we verify the base attribute
        isWorking();
        verifyBaseRequest(request, false, true);
        final String currentVersion = actingVersion.version.toString();

        
        final WFSCapabilities inCapabilities = (WFSCapabilities) getStaticCapabilitiesObject(currentVersion, "WFS");
        
        //set the current updateSequence parameter
        final boolean returnUS = returnUpdateSequenceDocument(request.getUpdateSequence());
        if (returnUS) {
            return xmlFactory.buildWFSCapabilities(currentVersion, getCurrentUpdateSequence());
        }

        FeatureTypeList ftl              = null;
        AbstractOperationsMetadata om    = null;
        AbstractServiceProvider sp       = null;
        AbstractServiceIdentification si = null;
        final FilterCapabilities fc;

        if (request.getSections() == null || request.containsSection("featureTypeList")) {
            ftl = xmlFactory.buildFeatureTypeList(currentVersion);
            
            /*
             *  layer providers
             */
            final LayerProviderProxy namedProxy = LayerProviderProxy.getInstance();
            final Map<Name,Layer> layers        = getLayers();
            final List<Name> layerNames         = new ArrayList<Name>(layers.keySet());
            Collections.sort(layerNames, new NameComparator());
            for (final Name layerName : layerNames) {
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
                    final org.geotoolkit.wfs.xml.FeatureType ftt;
                    try {

                        final String defaultCRS;
                        if (type.getGeometryDescriptor() != null && type.getGeometryDescriptor().getCoordinateReferenceSystem() != null) {
                            final CoordinateReferenceSystem crs = type.getGeometryDescriptor().getCoordinateReferenceSystem();
                            //todo wait for martin fix
                            String id  = IdentifiedObjects.lookupIdentifier(crs, true);
                            if (id == null) {
                                id = IdentifiedObjects.getIdentifier(crs);
                            }

                            if (id != null) {
                                defaultCRS = "urn:x-ogc:def:crs:" + id.replaceAll(":", ":7.01:");
            //                    final String defaultCRS = IdentifiedObjects.lookupIdentifier(Citations.URN_OGC,
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
                        ftt = xmlFactory.buildFeatureType(
                                currentVersion,
                                Utils.getQnameFromName(layerName),
                                title,
                                defaultCRS,
                                DEFAULT_CRS,
                                toBBox(fld.getStore(), fld.getName(), currentVersion));

                        /*
                         * we apply the layer customization
                         */
                        ftt.setAbstract(configLayer.getAbstrac());
                        if (!configLayer.getKeywords().isEmpty()) {
                            ftt.addKeywords(configLayer.getKeywords());
                        }
                        FormatURL metadataURL = configLayer.getMetadataURL();
                        if (metadataURL != null) {
                            ftt.addMetadataURL(metadataURL.getOnlineResource().getValue(),
                                               metadataURL.getType(),
                                               metadataURL.getFormat());
                        }
                        if (!configLayer.getCrs().isEmpty()) {
                            ftt.setOtherCRS(configLayer.getCrs());
                        }

                        // we add the feature type description to the list
                        ftl.addFeatureType(ftt);
                    } catch (FactoryException ex) {
                        Logging.unexpectedException(LOGGER, ex);
                    }

                } else {
                    LOGGER.log(Level.WARNING, "The layer:{0} is not a feature layer", layerName);
                }
            }
        }
        //todo ...etc...--------------------------------------------------------

        if (request.getSections() == null || request.containsSection("operationsMetadata")) {
            if (currentVersion.equals("2.0.0")) {
                om =  OPERATIONS_METADATA_V200;
            } else {
                om = OPERATIONS_METADATA_V110;
            }
            final String url = getServiceUrl();
            if (url != null) {
                om.updateURL(url);
            }
        }
        if (request.getSections() == null || request.containsSection("serviceProvider")) {
            sp = inCapabilities.getServiceProvider();
        }
        if (request.getSections() == null || request.containsSection("serviceIdentification")) {
            si = inCapabilities.getServiceIdentification();
        }

        if (currentVersion.equals("2.0.0")) {
            fc = WFSConstants.FILTER_CAPABILITIES_V200;
        } else {
            fc = WFSConstants.FILTER_CAPABILITIES_V110;
        }    
        final WFSCapabilities result = xmlFactory.buildWFSCapabilities(currentVersion, si, sp, om, ftl, fc);

        LOGGER.log(logLevel, "GetCapabilities treated in {0}ms", (System.currentTimeMillis() - start));
        return result;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Schema describeFeatureType(final DescribeFeatureType request) throws CstlServiceException {
        LOGGER.log(logLevel, "DecribeFeatureType request proccesing");
        final long start = System.currentTimeMillis();
        
        // we verify the base attribute
        isWorking();
        verifyBaseRequest(request, false, false);
        final String currentVersion = actingVersion.version.toString();

        final String gmlVersion;
        if ("2.0.0".equals(currentVersion)) {
            gmlVersion = "3.2.1";
        } else {
            gmlVersion = "3.1.1";
        }
        final JAXBFeatureTypeWriter writer  = new JAXBFeatureTypeWriter(gmlVersion);
        final LayerProviderProxy namedProxy = LayerProviderProxy.getInstance();
        final List<QName> names             = request.getTypeName();
        final List<FeatureType> types       = new ArrayList<FeatureType>();
        final Map<Name,Layer> layers = getLayers();

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
                if (name == null) continue;

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
    private FeatureType getFeatureTypeFromLayer(final FeatureLayerDetails fld) throws DataStoreException {
        return fld.getStore().getFeatureType(fld.getName());
    }


    /**
     * {@inheritDoc }
     */
    @Override
    public Object getFeature(final GetFeature request) throws CstlServiceException {
        LOGGER.log(logLevel, "GetFeature request proccesing");
        final long start = System.currentTimeMillis();

        // we verify the base attribute
        isWorking();
        verifyBaseRequest(request, false, false);

        final String currentVersion                = actingVersion.version.toString();
        final LayerProviderProxy namedProxy        = LayerProviderProxy.getInstance();
        final String featureId                     = request.getFeatureId();
        final Integer maxFeatures                  = request.getCount();
        final List<FeatureCollection> collections  = new ArrayList<FeatureCollection>();
        final Map<String, String> schemaLocations  = new HashMap<String, String>();
        final Map<Name,Layer> layers               = getLayers();
        final Map<String, String> namespaceMapping = request.getPrefixMapping();
        if (request.getQuery() == null || request.getQuery().isEmpty()) {
            throw new CstlServiceException("You must specify a query!", MISSING_PARAMETER_VALUE);
        }

        for (final Query query : request.getQuery()) {
            final org.geotoolkit.ogc.xml.SortBy jaxbSortBy = query.getSortBy();
            final Filter jaxbFilter       = query.getFilter();
            final String srs              = query.getSrsName();
            final List<Object> properties = query.getPropertyNames();

            final List<QName> typeNames;
            if (featureId != null && query.getTypeNames().isEmpty()) {
                typeNames = Utils.getQNameListFromNameSet(layers.keySet());
            } else {
                typeNames = query.getTypeNames();
            }

            final List<String> requestPropNames = new ArrayList<String>();
            final List<SortBy> sortBys          = new ArrayList<SortBy>();

            //decode filter-----------------------------------------------------
            final Filter filter;
            if (featureId == null) {
                filter = extractJAXBFilter(jaxbFilter, Filter.INCLUDE, namespaceMapping, currentVersion);
            } else {
                filter = extractJAXBFilter(featureId, namespaceMapping, currentVersion);
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
                } else if (obj instanceof PropertyName) {
                    final PropertyName pName = (PropertyName) obj;
                    if (pName.getValue() != null) {
                        requestPropNames.add(pName.getValue().getLocalPart());
                    }
                }
            }

            //decode sort by----------------------------------------------------
            if (jaxbSortBy != null) {
                sortBys.addAll(visitJaxbSortBy(jaxbSortBy, namespaceMapping, currentVersion));
            }

            final QueryBuilder queryBuilder = new QueryBuilder();
            queryBuilder.setFilter(filter);
            queryBuilder.setCRS(crs);

            if (!sortBys.isEmpty()) {
                queryBuilder.setSortBy(sortBys.toArray(new SortBy[sortBys.size()]));
            }
            if (maxFeatures != null && maxFeatures != 0){
                queryBuilder.setMaxFeatures(maxFeatures);
            }

            for (QName typeName : typeNames) {

                final Name fullTypeName = Utils.getNameFromQname(typeName);
                if (layersContainsKey(fullTypeName) == null) {
                    throw new CstlServiceException(UNKNOW_TYPENAME + typeName, INVALID_PARAMETER_VALUE);
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
                        String describeRequest = url + "request=DescribeFeatureType&version=1.1.0&service=WFS";
                        describeRequest        = describeRequest + "&namespace=xmlns(" + prefix + "=" + namespace + ")";
                        describeRequest        = describeRequest + "&Typename=" + prefix + ':' + typeName.getLocalPart();
                        schemaLocations.put(namespace, describeRequest);
                    }
                }
            }

        }
        final String gmlVersion;
        if ("text/xml; subtype=gml/3.1.1".equals(request.getOutputFormat()) ||
            "text/gml; subtype=gml/3.1.1".equals(request.getOutputFormat())) {
            gmlVersion = "3.1.1";
        } else if ("text/xml; subtype=gml/3.2.1".equals(request.getOutputFormat()) ||
                   "text/xml; subtype=gml/3.2".equals(request.getOutputFormat())   ||
                   "application/gml+xml; version=3.2".equals(request.getOutputFormat())) {
            gmlVersion = "3.2.1";
        } else {
            throw new CstlServiceException("invalid outputFormat:" + request.getOutputFormat(), INVALID_PARAMETER_VALUE, "outputFormat");
        }
        

        /**
         * 3 possibility here :
         *    1) merge the collections
         *    2) return a collection of collection.
         *    3) id there is only one feature we return (change the return type in object)
         *
         * result TODO find an id and a member type
         */
        final FeatureCollection FeatureCollection;
	if (collections.size() > 1) {
            FeatureCollection = DataUtilities.sequence("collection-1", collections.toArray(new FeatureCollection[collections.size()]));
        } else if (collections.size() == 1) {
            FeatureCollection = collections.get(0);
        } else {
            FeatureCollection = DataUtilities.collection("collection-1", null);
        }
        if (request.getResultType() == ResultTypeType.HITS) {
            return xmlFactory.buildFeatureCollection(currentVersion, "collection-1", FeatureCollection.size(), org.geotoolkit.internal.jaxb.XmlUtilities.toXML(new Date()));
        }
        LOGGER.log(logLevel, "GetFeature treated in {0}ms", (System.currentTimeMillis() - start));
        return new FeatureCollectionWrapper(FeatureCollection, schemaLocations, gmlVersion, currentVersion);
    }
    
    private List<SortBy> visitJaxbSortBy(final org.geotoolkit.ogc.xml.SortBy jaxbSortby,final Map<String, String> namespaceMapping, final String version) {
        final XMLUtilities util = new XMLUtilities();
        if ("2.0.0".equals(version)) {
            return util.getTransformer200(namespaceMapping).visitSortBy((org.geotoolkit.ogc.xml.v200.SortByType)jaxbSortby);
        } else {
            return util.getTransformer110(namespaceMapping).visitSortBy((org.geotoolkit.ogc.xml.v110.SortByType)jaxbSortby);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public AbstractGML getGMLObject(final GetGmlObject grbi) throws CstlServiceException {
        throw new CstlServiceException("WFS get GML Object is not supported on this Constellation version.");
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public LockFeatureResponse lockFeature(final LockFeature gr) throws CstlServiceException {
        throw new CstlServiceException("WFS Lock is not supported on this Constellation version.");
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public TransactionResponse transaction(final Transaction request) throws CstlServiceException {
        LOGGER.log(logLevel, "Transaction request processing\n");
        final long startTime = System.currentTimeMillis();
        isWorking();
        verifyBaseRequest(request, true, false);

        // we prepare the report
        final String currentVersion                = actingVersion.version.toString();
        int totalInserted                          = 0;
        int totalUpdated                           = 0;
        int totalDeleted                           = 0;
        final LayerProviderProxy namedProxy        = LayerProviderProxy.getInstance();
        final List<Object> transactions            = request.getTransactionAction();
        final Map<String, String> inserted         = new LinkedHashMap<String, String>();
        final Map<String, String> namespaceMapping = request.getPrefixMapping();
        final XmlFeatureReader featureReader       = new JAXPStreamFeatureReader(getFeatureTypes());

        for (Object transaction: transactions) {

            /**
             * Features insertion.
             */
            if (transaction instanceof InsertElement) {
                final InsertElement insertRequest = (InsertElement)transaction;

                final String handle = insertRequest.getHandle();

                // we verify the input format
                if (insertRequest.getInputFormat() != null && !(insertRequest.getInputFormat().equals("text/xml; subtype=gml/3.1.1") 
                                                           ||   insertRequest.getInputFormat().equals("application/gml+xml; version=3.2"))) {
                    throw new CstlServiceException("This only input format supported are: text/xml; subtype=gml/3.1.1 and application/gml+xml; version=3.2",
                            INVALID_PARAMETER_VALUE, "inputFormat");
                }

                // what to do with the CRS ?
                final CoordinateReferenceSystem insertCRS = extractCRS(insertRequest.getSrsName());

                // what to do with that, which ones are supported ??
                final IdentifierGenerationOptionType idGen = insertRequest.getIdgen();

                for (Object featureObject : insertRequest.getFeature()) {
                    if (featureObject instanceof JAXBElement) {
                        featureObject = ((JAXBElement)featureObject).getValue();
                    }
                    try {
                        if (featureObject instanceof Node) {

                                featureObject = featureReader.read(featureObject);

                        } else if (featureObject instanceof FeatureCollectionType) {
                            final FeatureCollectionType xmlCollection = (FeatureCollectionType) featureObject;
                            final String id = xmlCollection.getId();
                            final List<Feature> features = new ArrayList<Feature>();
                            FeatureType ft = null;
                            for (FeaturePropertyType fprop : xmlCollection.getFeatureMember()) {
                                Feature feat = (Feature)featureReader.read(fprop.getUnknowFeature());
                                ft = feat.getType();
                                features.add(feat);
                            }
                            final FeatureCollection collection = DataUtilities.collection(id, ft);
                            collection.addAll(features);
                            featureObject = collection;
                        }
                    } catch (IllegalArgumentException ex) {
                        throw new CstlServiceException(ex.getMessage(), ex, INVALID_PARAMETER_VALUE);
                    } catch (IOException ex) {
                        throw new CstlServiceException(ex);
                    } catch (XMLStreamException ex) {
                        throw new CstlServiceException(ex);
                    }
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
                            if (featureObject instanceof JAXBElement) {
                                featureType = "JAXBElement<" + ((JAXBElement)featureObject).getValue().getClass().getName() + ">";
                            } else {
                                featureType = featureObject.getClass().getName();
                            }
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
                            inserted.put(fid.getID(), handle);// get the id of the inserted feature
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
            } else if (transaction instanceof DeleteElement) {

                final DeleteElement deleteRequest = (DeleteElement) transaction;

                //decode filter-----------------------------------------------------
                if (deleteRequest.getFilter() == null) {
                    throw new CstlServiceException("The filter must be specified.", MISSING_PARAMETER_VALUE, "filter");
                }
                final Filter filter = extractJAXBFilter(deleteRequest.getFilter(), Filter.EXCLUDE, namespaceMapping, currentVersion);

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
            } else if (transaction instanceof UpdateElement) {

                final UpdateElement updateRequest = (UpdateElement) transaction;

                // we verify the input format
                if (updateRequest.getInputFormat() != null && !(updateRequest.getInputFormat().equals("text/xml; subtype=gml/3.1.1") 
                                                           ||   updateRequest.getInputFormat().equals("application/gml+xml; version=3.2"))) {
                    throw new CstlServiceException("This only input format supported are: text/xml; subtype=gml/3.1.1 and application/gml+xml; version=3.2",
                            INVALID_PARAMETER_VALUE, "inputFormat");
                }

                //decode filter-----------------------------------------------------
                final Filter filter = extractJAXBFilter(updateRequest.getFilter(),Filter.EXCLUDE, namespaceMapping, currentVersion);

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
                    for (final Property updateProperty : updateRequest.getProperty()) {
                        final String updatePropertyValue = updateProperty.getLocalName();
                        final PropertyAccessor pa        = Accessors.getAccessor(Feature.class, updatePropertyValue, null);
                        if (pa == null || pa.get(ft, updatePropertyValue, null) == null) {
                            throw new CstlServiceException("The feature Type " + updateRequest.getTypeName() + " does not has such a property: " + updatePropertyValue, INVALID_PARAMETER_VALUE);
                        }
                        Object value;
                        if (updateProperty.getValue() instanceof Element) {
                            final String strValue = getXMLFromElementNSImpl((Element)updateProperty.getValue());
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
        
        final TransactionResponse response = xmlFactory.buildTransactionResponse(currentVersion,
                                                                                 totalInserted,
                                                                                 totalUpdated,
                                                                                 totalDeleted, 
                                                                                 inserted);
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
    private  String getXMLFromElementNSImpl(final Element elt) {
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
    private  StringBuilder getXMLFromNode(final Node node) {
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
     * Extract an OGC filter usable by the dataStore from the request filter
     * unmarshalled by JAXB.
     *
     * @param jaxbFilter an OGC JAXB filter.
     * @return An OGC filter
     * @throws CstlServiceException
     */
    private Filter extractJAXBFilter(final String featureId, final Map<String, String> namespaceMapping, final String currentVersion) throws CstlServiceException {
        if ("2.0.0".equals(currentVersion)) {
            return extractJAXBFilter(new org.geotoolkit.ogc.xml.v200.FilterType(new org.geotoolkit.ogc.xml.v200.ResourceIdType(featureId)), Filter.INCLUDE, namespaceMapping, currentVersion);
        } else {
            return extractJAXBFilter(new org.geotoolkit.ogc.xml.v110.FilterType(new org.geotoolkit.ogc.xml.v110.FeatureIdType(featureId)), Filter.INCLUDE, namespaceMapping, currentVersion);
        }
    }
    /**
     * Extract an OGC filter usable by the dataStore from the request filter
     * unmarshalled by JAXB.
     *
     * @param jaxbFilter an OGC JAXB filter.
     * @return An OGC filter
     * @throws CstlServiceException
     */
    private Filter extractJAXBFilter(final Filter jaxbFilter, final Filter defaultFilter, final Map<String, String> namespaceMapping, final String currentVersion) throws CstlServiceException {
        final XMLUtilities util = new XMLUtilities();
        final Filter filter;
        try {
            if (jaxbFilter != null) {
                if ("2.0.0".equals(currentVersion)) {
                    filter = util.getTransformer200(namespaceMapping).visitFilter((org.geotoolkit.ogc.xml.v200.FilterType)jaxbFilter);
                } else {
                    filter = util.getTransformer110(namespaceMapping).visitFilter((org.geotoolkit.ogc.xml.v110.FilterType)jaxbFilter);
                }
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
    private CoordinateReferenceSystem extractCRS(final String srsName) throws CstlServiceException {
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
    private void verifyFilterProperty(final FeatureType ft, final Filter filter) throws CstlServiceException {
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
     * what ? may not be wgs84 exactly ? why is there a CRS attribute on a wgs84 bbox ?
     */
    private static Object toBBox(final DataStore source, final Name groupName, final String version) throws CstlServiceException{
        try {
            Envelope env = source.getEnvelope(QueryBuilder.all(groupName));
            final CoordinateReferenceSystem epsg4326 = CRS.decode("urn:ogc:def:crs:OGC:2:84");
            if (env != null) {
                if (!CRS.equalsIgnoreMetadata(env.getCoordinateReferenceSystem(), epsg4326)) {
                    env = CRS.transform(env, epsg4326);
                }
                return xmlFactory.buildBBOX(version,
                       "urn:ogc:def:crs:OGC:2:84",
                       env.getMinimum(0),
                       env.getMinimum(1),
                       env.getMaximum(0),
                       env.getMaximum(1));
            } else {
                return xmlFactory.buildBBOX(version,"urn:ogc:def:crs:OGC:2:84", -180, -90, 180, 90);
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
    private void verifyBaseRequest(final RequestBase request, final boolean versionMandatory, final boolean getCapabilities) throws CstlServiceException {
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
                } else if (multipleVersionActivated && (request.getVersion().toString().equals("2.0.0") || request.getVersion().toString().equals("2.0"))) { 
                    this.actingVersion = ServiceDef.WFS_2_0_0;

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
    public List<FeatureType> getFeatureTypes() {
        final List<FeatureType> types       = new ArrayList<FeatureType>();
        final LayerProviderProxy namedProxy = LayerProviderProxy.getInstance();
        final Map<Name,Layer> layers        = getLayers();

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
    public void destroy() {
    }

    @Override
    public ListStoredQueriesResponse listStoredQueries(final ListStoredQueries request) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DescribeStoredQueriesResponse describeStoredQueries(final DescribeStoredQueries request) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ValueCollection getPropertyValue(final GetPropertyValue request) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CreateStoredQueryResponse createStoredQuery(CreateStoredQuery model) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DropStoredQueryResponse dropStoredQuery(DropStoredQuery model) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
