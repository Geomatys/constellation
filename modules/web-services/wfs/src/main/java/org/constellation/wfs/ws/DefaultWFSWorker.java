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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
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
import org.constellation.util.QNameComparator;
import org.constellation.ws.CstlServiceException;
import static org.constellation.wfs.ws.WFSConstants.*;
import org.constellation.wfs.ws.rs.FeatureCollectionWrapper;
import org.constellation.wfs.ws.rs.ValueCollectionWrapper;

// Geotoolkit dependencies
import org.constellation.ws.LayerWorker;
import org.constellation.ws.UnauthorizedException;
import org.geotoolkit.util.logging.Logging;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.data.FeatureStoreUtilities;
import org.geotoolkit.feature.SchemaException;
import org.geotoolkit.ows.xml.RequestBase;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.memory.GenericReprojectFeatureIterator;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.factory.HintsPending;
import org.geotoolkit.feature.FeatureTypeUtilities;
import org.geotoolkit.feature.xml.jaxb.JAXBFeatureTypeWriter;
import org.geotoolkit.feature.xml.Utils;
import org.geotoolkit.feature.xml.jaxp.JAXPStreamFeatureReader;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.IdentifiedObjects;
import org.geotoolkit.sld.xml.StyleXmlIO;
import org.geotoolkit.xml.MarshallerPool;
import org.geotoolkit.xsd.xml.v2001.Schema;
import org.geotoolkit.filter.accessor.Accessors;
import org.geotoolkit.filter.accessor.PropertyAccessor;
import org.geotoolkit.filter.visitor.FillCrsVisitor;
import org.geotoolkit.filter.visitor.ListingPropertyVisitor;
import org.geotoolkit.filter.visitor.IsValidSpatialFilterVisitor;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.gml.GeometrytoJTS;
import org.geotoolkit.gml.xml.AbstractGML;
import org.geotoolkit.gml.xml.DirectPosition;
import org.geotoolkit.gml.xml.v311.AbstractGeometryType;
import org.geotoolkit.gml.xml.v311.FeaturePropertyType;
import org.geotoolkit.ogc.xml.XMLFilter;
import org.geotoolkit.ogc.xml.XMLLiteral;
import org.geotoolkit.ogc.xml.v200.BBOXType;
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
import org.geotoolkit.wfs.xml.StoredQueryDescription;
import org.geotoolkit.wfs.xml.StoredQueries;
import org.geotoolkit.wfs.xml.WFSCapabilities;
import org.geotoolkit.wfs.xml.ResultTypeType;
import org.geotoolkit.wfs.xml.WFSMarshallerPool;
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

import static org.geotoolkit.wfs.xml.WFSXmlFactory.*;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import org.geotoolkit.wfs.xml.*;
import org.geotoolkit.wfs.xml.v200.ObjectFactory;
import org.geotoolkit.wfs.xml.v200.QueryExpressionTextType;
import org.geotoolkit.wfs.xml.v200.QueryType;
import org.geotoolkit.wfs.xml.v200.StoredQueryDescriptionType;

// GeoAPI dependencies
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.feature.type.PropertyType;
import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.BinaryLogicOperator;
import org.opengis.filter.Filter;
import org.opengis.filter.capability.FilterCapabilities;
import org.opengis.filter.identity.FeatureId;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.spatial.BinarySpatialOperator;
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

    private List<StoredQueryDescription> storedQueries = new ArrayList<StoredQueryDescription>();

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

        // loading stored queries
       loadStoredQueries();
    }

    private void loadStoredQueries() {
        // loading stored queries
        if (configurationDirectory != null) {
            final File sqFile = new File(configurationDirectory, "StoredQueries.xml");
            if (sqFile.exists()) {
                Unmarshaller unmarshaller = null;
                try {
                    unmarshaller = getMarshallerPool().acquireUnmarshaller();
                    Object obj   = unmarshaller.unmarshal(sqFile);
                    if (obj instanceof StoredQueries) {
                        StoredQueries candidate = (StoredQueries) obj;
                        this.storedQueries = candidate.getStoredQuery();
                    } else {
                        LOGGER.log(Level.WARNING, "The storedQueries File does not contains proper object");
                    }
                } catch (JAXBException ex) {
                    LOGGER.log(Level.WARNING, "JAXBExeception while unmarshalling the stored queries File", ex);
                } finally {
                    if (unmarshaller != null) {
                        getMarshallerPool().release(unmarshaller);
                    }
                }
            }
        }
        // we verify if the identifier query is loaded (if not we load it)
       boolean found = false;
       for (StoredQueryDescription squery : storedQueries) {
           if ("identifierQuery".equals(squery.getId())) {
               found = true;
               break;
           }
       }
       if (!found) {
           final List<QName> typeNames = Utils.getQNameListFromNameSet(getLayers().keySet());
           Collections.sort(typeNames, new QNameComparator());
           final QueryType query = new QueryType(IDENTIFIER_FILTER, typeNames, "2.0.0");
           final QueryExpressionTextType queryEx = new QueryExpressionTextType("urn:ogc:def:queryLanguage:OGC-WFS::WFS_QueryExpression", null, typeNames);
           final ObjectFactory factory = new ObjectFactory();
           queryEx.getContent().add(factory.createQuery(query));
           final StoredQueryDescriptionType idQ = new StoredQueryDescriptionType("identifierQuery", "Identifier query" , "filter on feature identifier", IDENTIFIER_PARAM, queryEx);
           storedQueries.add(idQ);
       }
    }

    private void storedQueries() {
        // loading stored queries
        if (configurationDirectory != null) {
            final File sqFile = new File(configurationDirectory, "StoredQueries.xml");
            Marshaller marshaller = null;
            try {
                marshaller = getMarshallerPool().acquireMarshaller();
                marshaller.marshal(new StoredQueries(storedQueries), sqFile);

            } catch (JAXBException ex) {
                LOGGER.log(Level.WARNING, "JAXBExeception while marshalling the stored queries File", ex);
            } finally {
                if (marshaller != null) {
                    getMarshallerPool().release(marshaller);
                }
            }

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
        if (inCapabilities == null) {
            throw new CstlServiceException("Unable to find the capabilities skeleton", NO_APPLICABLE_CODE);
        }

        //set the current updateSequence parameter
        final boolean returnUS = returnUpdateSequenceDocument(request.getUpdateSequence());
        if (returnUS) {
            return buildWFSCapabilities(currentVersion, getCurrentUpdateSequence());
        }

        FeatureTypeList ftl              = null;
        AbstractOperationsMetadata om    = null;
        AbstractServiceProvider sp       = null;
        AbstractServiceIdentification si = null;
        final FilterCapabilities fc;

        if (request.getSections() == null || request.containsSection("featureTypeList")) {
            ftl = buildFeatureTypeList(currentVersion);

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

                        final String defaultCRS = getCRSCode(type);
                        final String title;
                        if (configLayer.getTitle() != null) {
                            title = configLayer.getTitle();
                        } else {
                            title = fld.getName().getLocalPart();
                        }
                        ftt = buildFeatureType(
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
        final WFSCapabilities result = buildWFSCapabilities(currentVersion, si, sp, om, ftl, fc);

        LOGGER.log(logLevel, "GetCapabilities treated in {0}ms", (System.currentTimeMillis() - start));
        return result;
    }

    private String getCRSCode(FeatureType type) throws FactoryException{

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
//                final String defaultCRS = IdentifiedObjects.lookupIdentifier(Citations.URN_OGC,
//                        type.getGeometryDescriptor().getCoordinateReferenceSystem(), true);
            } else {
                defaultCRS = "urn:x-ogc:def:crs:EPSG:7.01:4326";
            }
        } else {
            defaultCRS = "urn:x-ogc:def:crs:EPSG:7.01:4326";
        }
        return defaultCRS;
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
                if (!(layer instanceof FeatureLayerDetails)) {continue;}

                try {
                    types.add(getFeatureTypeFromLayer((FeatureLayerDetails)layer));
                } catch (DataStoreException ex) {
                    LOGGER.log(Level.WARNING, "error while getting featureType for:{0}", layer.getName());
                }
            }
        } else {

            //search only the given list
            for (final QName name : names) {
                if (name == null) {continue;}

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
                    LOGGER.log(Level.WARNING, "error while getting featureType for:"+ layer.getName(), ex);
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


    private List<? extends Query> extractStoredQueries(final FeatureRequest request) throws CstlServiceException {
        final List<? extends Query> queries = request.getQuery();
        for (StoredQuery storedQuery : request.getStoredQuery()) {
            StoredQueryDescription description = null;
            final List<? extends Parameter> parameters = storedQuery.getParameter();
            for (StoredQueryDescription desc : storedQueries) {
                if (desc.getId().equals(storedQuery.getId())) {
                    description = desc;
                    break;
                }
            }
            if (description == null) {
                throw new CstlServiceException("Unknow stored query: " + storedQuery.getId(), INVALID_PARAMETER_VALUE, "storedQuery");
            } else {
                for (QueryExpressionText queryEx : description.getQueryExpressionText()) {
                    for (Object content : queryEx.getContent()) {
                        if (content instanceof JAXBElement) {
                            content = ((JAXBElement)content).getValue();
                        }
                        if (content instanceof Query) {
                            final Query query = (Query)content;
                            applyParameterOnQuery(query.getFilter(), parameters);
                            ((List)queries).add(query);
                        } else {
                            throw new CstlServiceException("unexpected query object: " + content, INVALID_PARAMETER_VALUE, "storedQuery");
                        }
                    }
                }
            }
        }
        return queries;
    }

    private List<String> extractPropertyNames(final List<Object> properties) {
        final List<String> requestPropNames = new ArrayList<String>();
        for (Object obj : properties) {
            if (obj instanceof JAXBElement) {
                obj = ((JAXBElement) obj).getValue();
            }
            if (obj instanceof String) {
                String pName = (String) obj;
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
        return requestPropNames;
    }

    private void putSchemaLocation(final QName typeName, final Map<String, String> schemaLocations) {
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

    private Name[] verifyPropertyNames(final QName typeName, final FeatureType ft, final List<String> requestPropNames) throws CstlServiceException {
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
            return propertyNames.toArray(new Name[propertyNames.size()]);
        } else {
            return null;
        }
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
        final String featureId                     = request.getFeatureId();
        final int maxFeatures                      = request.getCount();
        final Integer startIndex                   = request.getStartIndex();
        final List<FeatureCollection> collections  = new ArrayList<FeatureCollection>();
        final Map<String, String> schemaLocations  = new HashMap<String, String>();
        final Map<Name,Layer> layers               = getLayers();
        final Map<String, String> namespaceMapping = request.getPrefixMapping();
        if ((request.getQuery() == null || request.getQuery().isEmpty()) && (request.getStoredQuery() == null || request.getStoredQuery().isEmpty())) {
            throw new CstlServiceException("You must specify a query!", MISSING_PARAMETER_VALUE);
        }
        final List<? extends Query> queries = extractStoredQueries(request);

        for (final Query query : queries) {

            final List<QName> typeNames;
            if (featureId != null && query.getTypeNames().isEmpty()) {
                typeNames = Utils.getQNameListFromNameSet(layers.keySet());
            } else {
                typeNames = query.getTypeNames();
            }

            //decode filter-----------------------------------------------------
            final Filter filter = extractJAXBFilter(featureId, query.getFilter(), Filter.INCLUDE, namespaceMapping, currentVersion);

            //decode crs--------------------------------------------------------
            final CoordinateReferenceSystem queryCRS = extractCRS(query.getSrsName());

            //decode property names---------------------------------------------
            final List<String> requestPropNames = extractPropertyNames(query.getPropertyNames());

            //decode sort by----------------------------------------------------
            final List<SortBy> sortBys = visitJaxbSortBy(query.getSortBy(), namespaceMapping, currentVersion);


            final QueryBuilder queryBuilder = new QueryBuilder();
            queryBuilder.setCRS(queryCRS);

            if (!sortBys.isEmpty()) {
                queryBuilder.setSortBy(sortBys.toArray(new SortBy[sortBys.size()]));
            }
            if (maxFeatures != 0){
                queryBuilder.setMaxFeatures(maxFeatures);
            }
            if (startIndex != 0){
                queryBuilder.setStartIndex(startIndex);
            }

            for (QName typeName : typeNames) {

                final Name fullTypeName = Utils.getNameFromQname(typeName);
                if (layersContainsKey(fullTypeName) == null) {
                    throw new CstlServiceException(UNKNOW_TYPENAME + typeName, INVALID_PARAMETER_VALUE);
                }
                final LayerDetails layerD = getLayerReference(fullTypeName);

                if (!(layerD instanceof FeatureLayerDetails)) {continue;}

                final FeatureLayerDetails layer = (FeatureLayerDetails) layerD;

                final FeatureType ft;
                try {
                    ft = getFeatureTypeFromLayer(layer);
                } catch (DataStoreException ex) {
                    throw new CstlServiceException(ex);
                }
                // we ensure that the property names are contained in the feature type and add the mandatory attribute to the list
                queryBuilder.setProperties(verifyPropertyNames(typeName, ft, requestPropNames));
                queryBuilder.setTypeName(ft.getName());
                queryBuilder.setHints(new Hints(HintsPending.FEATURE_HIDE_ID_PROPERTY, Boolean.TRUE));
                queryBuilder.setFilter(fillFilterCrs(ft, filter));

                // we verify that all the properties contained in the filter are known by the feature type.
                verifyFilterProperty(ft, filter);


                FeatureCollection collection = layer.getStore().createSession(false).getFeatureCollection(queryBuilder.buildQuery());
                if (!collection.isEmpty()) {
                    if(queryCRS == null){
                        try {
                            //ensure axes are in the declared order, since we use urn epsg, we must comply
                            //to proper epsg axis order
                            final String defaultCRS = getCRSCode(ft);
                            final CoordinateReferenceSystem rcrs = CRS.decode(defaultCRS);
                            if(!CRS.equalsIgnoreMetadata(rcrs, ft.getCoordinateReferenceSystem())){
                                collection = GenericReprojectFeatureIterator.wrap(collection, CRS.decode(defaultCRS));
                            }
                        } catch (FactoryException ex) {
                            Logger.getLogger(DefaultWFSWorker.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
                        }
                    }

                    collections.add(collection);
                    // we write The SchemaLocation
                    putSchemaLocation(typeName, schemaLocations);
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
         *    3) if there is only one feature we return (change the return type in object)
         *
         * result TODO find an id and a member type
         */
        final FeatureCollection featureCollection;
	if (collections.size() > 1) {
            featureCollection = FeatureStoreUtilities.sequence("collection-1", collections.toArray(new FeatureCollection[collections.size()]));
        } else if (collections.size() == 1) {
            featureCollection = collections.get(0);
        } else {
            featureCollection = FeatureStoreUtilities.collection("collection-1", null);
        }
        if (request.getResultType() == ResultTypeType.HITS) {
            return buildFeatureCollection(currentVersion, "collection-1", featureCollection.size(), org.geotoolkit.internal.jaxb.XmlUtilities.toXML(new Date()));
        }
        LOGGER.log(logLevel, "GetFeature treated in {0}ms", (System.currentTimeMillis() - start));
        return new FeatureCollectionWrapper(featureCollection, schemaLocations, gmlVersion, currentVersion);
    }

    @Override
    public Object getPropertyValue(final GetPropertyValue request) throws CstlServiceException {
        LOGGER.log(logLevel, "GetPropertyValue request processing\n");
        final long startTime = System.currentTimeMillis();
        isWorking();
        verifyBaseRequest(request, true, false);

        final Map<String, String> namespaceMapping = request.getPrefixMapping();
        final String currentVersion                = actingVersion.version.toString();
        final Map<Name,Layer> layers               = getLayers();
        final List<? extends Query> queries        = extractStoredQueries(request);
        final Integer maxFeatures                  = request.getCount();
        final Map<String, String> schemaLocations  = new HashMap<String, String>();
        final List<FeatureCollection> collections  = new ArrayList<FeatureCollection>();

        for (final Query query : queries) {

            final List<QName> typeNames;
            if (query.getTypeNames().isEmpty()) {
                typeNames = Utils.getQNameListFromNameSet(layers.keySet());
            } else {
                typeNames = query.getTypeNames();
            }

            //decode filter-----------------------------------------------------
            final Filter filter = extractJAXBFilter(null, query.getFilter(), Filter.INCLUDE, namespaceMapping, currentVersion);

            //decode crs--------------------------------------------------------
            final CoordinateReferenceSystem crs = extractCRS(query.getSrsName());

            //decode property names---------------------------------------------
            final List<String> requestPropNames = extractPropertyNames(query.getPropertyNames());

            //decode sort by----------------------------------------------------
             final List<SortBy> sortBys = visitJaxbSortBy(query.getSortBy(), namespaceMapping, currentVersion);


            final QueryBuilder queryBuilder = new QueryBuilder();
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
                final LayerDetails layerD = getLayerReference(fullTypeName);

                if (!(layerD instanceof FeatureLayerDetails)) {continue;}

                final FeatureLayerDetails layer = (FeatureLayerDetails) layerD;

                final FeatureType ft;
                try {
                    ft = getFeatureTypeFromLayer(layer);
                } catch (DataStoreException ex) {
                    throw new CstlServiceException(ex);
                }
                // we ensure that the property names are contained in the feature type and add the mandatory attribute to the list
                queryBuilder.setProperties(verifyPropertyNames(typeName, ft, requestPropNames));
                queryBuilder.setFilter(fillFilterCrs(ft, filter));

                queryBuilder.setTypeName(ft.getName());
                queryBuilder.setHints(new Hints(HintsPending.FEATURE_HIDE_ID_PROPERTY, Boolean.TRUE));

                // we verify that all the properties contained in the filter are known by the feature type.
                verifyFilterProperty(ft, filter);

                collections.add(layer.getStore().createSession(false).getFeatureCollection(queryBuilder.buildQuery()));

                // we write The SchemaLocation
                putSchemaLocation(typeName, schemaLocations);
            }
        }

        /**
         * 3 possibility here :
         *    1) merge the collections
         *    2) return a collection of collection.
         *    3) if there is only one feature we return (change the return type in object)
         *
         * result TODO find an id and a member type
         */
        final FeatureCollection featureCollection;
	if (collections.size() > 1) {
            featureCollection = FeatureStoreUtilities.sequence("collection-1", collections.toArray(new FeatureCollection[collections.size()]));
        } else if (collections.size() == 1) {
            featureCollection = collections.get(0);
        } else {
            featureCollection = FeatureStoreUtilities.collection("collection-1", null);
        }

        LOGGER.log(logLevel, "GetPropertyValue request processed in {0} ms", (System.currentTimeMillis() - startTime));
        if (request.getResultType() == ResultTypeType.HITS) {
            return buildValueCollection(currentVersion, featureCollection.size(), org.geotoolkit.internal.jaxb.XmlUtilities.toXML(new Date()));
        }
        return new ValueCollectionWrapper(featureCollection, request.getValueReference(), "3.2.1");
    }

    private List<SortBy> visitJaxbSortBy(final org.geotoolkit.ogc.xml.SortBy jaxbSortby,final Map<String, String> namespaceMapping, final String version) {
        if (jaxbSortby != null) {
            final StyleXmlIO util = new StyleXmlIO();
            if ("2.0.0".equals(version)) {
                return util.getTransformer200(namespaceMapping).visitSortBy((org.geotoolkit.ogc.xml.v200.SortByType)jaxbSortby);
            } else {
                return util.getTransformer110(namespaceMapping).visitSortBy((org.geotoolkit.ogc.xml.v110.SortByType)jaxbSortby);
            }
        }
        return new ArrayList<SortBy>();
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
        if (transactionSecurized && !org.constellation.ws.security.SecurityManager.isAuthenticated()) {
            throw new UnauthorizedException("You must be authentified to perform an registerSensor request.");
        }
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
        final JAXPStreamFeatureReader featureReader= new JAXPStreamFeatureReader(getFeatureTypes());

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
                            final FeatureCollection collection = FeatureStoreUtilities.collection(id, ft);
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
                    FeatureCollection featureCollection;

                    if (featureObject instanceof Feature) {
                        final Feature feature = (Feature) featureObject;
                        typeName = feature.getType().getName();
                        featureCollection = FeatureStoreUtilities.collection(feature);
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
                        final CoordinateReferenceSystem trueCrs = layer.getStore().getFeatureType(typeName).getCoordinateReferenceSystem();
                        if(trueCrs != null && !CRS.equalsIgnoreMetadata(trueCrs, featureCollection.getFeatureType().getCoordinateReferenceSystem())){
                            featureCollection = GenericReprojectFeatureIterator.wrap(featureCollection, trueCrs);
                        }

                        final List<FeatureId> features = layer.getStore().addFeatures(typeName, featureCollection);

                        for (FeatureId fid : features) {
                            inserted.put(fid.getID(), handle);// get the id of the inserted feature
                            totalInserted++;
                            LOGGER.log(Level.FINER, "fid inserted: {0} total:{1}", new Object[]{fid, totalInserted});
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
                    queryBuilder.setFilter(fillFilterCrs(ft, filter));
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
                        final String updatePropertyName = updateProperty.getLocalName();
                        final PropertyAccessor pa = Accessors.getAccessor(Feature.class, updatePropertyName, null);
                        if (pa == null || pa.get(ft, updatePropertyName, null) == null) {
                            throw new CstlServiceException("The feature Type " + updateRequest.getTypeName() + " does not has such a property: " + updatePropertyName, INVALID_PARAMETER_VALUE);
                        }
                        final PropertyType propertyType = ft.getDescriptor(updatePropertyName).getType();

                        Object value;
                        if (updateProperty.getValue() instanceof Element) {
                            final String strValue = getXMLFromElementNSImpl((Element)updateProperty.getValue());
                            value = null;
                            LOGGER.log(Level.FINER, ">> updating : {0}   => {1}", new Object[]{updatePropertyName, strValue});
                        } else {
                            value = updateProperty.getValue();
                            if (value instanceof AbstractGeometryType) {
                                try {
                                    final String defaultCRS = getCRSCode(ft);
                                    final CoordinateReferenceSystem exposedCrs = CRS.decode(defaultCRS);
                                    final CoordinateReferenceSystem trueCrs = ((GeometryType)propertyType).getCoordinateReferenceSystem();

                                    value = GeometrytoJTS.toJTS((AbstractGeometryType) value);
                                    if(!CRS.equalsIgnoreMetadata(exposedCrs, trueCrs)){
                                        value = JTS.transform((Geometry)value, CRS.findMathTransform(exposedCrs, trueCrs));
                                    }

                                } catch (NoSuchAuthorityCodeException ex) {
                                    Logging.unexpectedException(LOGGER, ex);
                                } catch (TransformException ex) {
                                    Logging.unexpectedException(LOGGER, ex);
                                } catch (FactoryException ex) {
                                    Logging.unexpectedException(LOGGER, ex);
                                } catch (IllegalArgumentException ex) {
                                    throw new CstlServiceException(ex);
                                }
                            }else if(value instanceof DirectPosition){
                                final DirectPosition dp = (DirectPosition) value;
                                value = new GeometryFactory().createPoint(new Coordinate(dp.getOrdinate(0), dp.getOrdinate(1)));
                            }else if(value instanceof String){
                                value = featureReader.readValue((String)value, propertyType);
                            }
                            LOGGER.log(Level.FINER, ">> updating : {0} => {1}", new Object[]{updatePropertyName, value});
                            if (value != null) {
                                LOGGER.log(Level.FINER, "type : {0}", value.getClass());
                            }
                        }
                        values.put(ft.getDescriptor(updatePropertyName), value);

                    }

                    // we verify that all the properties contained in the filter are known by the feature type.
                    verifyFilterProperty(ft, filter);

                    // we extract the number of feature update
                    final QueryBuilder queryBuilder = new QueryBuilder(layer.getName());
                    queryBuilder.setFilter(fillFilterCrs(ft, filter));
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

        final TransactionResponse response = buildTransactionResponse(currentVersion,
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
        if (!node.getNodeName().equals("#text")) {temp.append("</").append(node.getNodeName()).append(">");}
        return temp;
    }

    private Filter extractJAXBFilter(final String featureId,final Filter jaxbFilter, final Filter defaultFilter, final Map<String, String> namespaceMapping, final String currentVersion) throws CstlServiceException {
        if (featureId == null) {
            return extractJAXBFilter(jaxbFilter, Filter.INCLUDE, namespaceMapping, currentVersion);
        } else {
            return extractJAXBFilter(featureId, namespaceMapping, currentVersion);
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
        final StyleXmlIO util = new StyleXmlIO();
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
                crs = CRS.decode(srsName, false);
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
     * Ensure crs is set on all geometric elements and with correct crs.
     */
    private Filter fillFilterCrs(FeatureType ft, Filter filter){
        try {
            final String defaultCRS = getCRSCode(ft);
            final CoordinateReferenceSystem exposedCrs = CRS.decode(defaultCRS);
            final CoordinateReferenceSystem trueCrs = ft.getCoordinateReferenceSystem();

            if(CRS.equalsIgnoreMetadata(trueCrs, exposedCrs)){
                return filter;
            }else{
                filter = (Filter) filter.accept(FillCrsVisitor.VISITOR, exposedCrs);
                filter = (Filter) filter.accept(new CrsAdjustFilterVisitor(exposedCrs, trueCrs), null);

                return filter;
            }

        } catch (FactoryException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
        return filter;
    }

    /**
     * Extract the WGS84 BBOx from a featureSource.
     * what ? may not be wgs84 exactly ? why is there a CRS attribute on a wgs84 bbox ?
     */
    private static Object toBBox(final FeatureStore source, final Name groupName, final String version) throws CstlServiceException{
        try {
            Envelope env = source.getEnvelope(QueryBuilder.all(groupName));
            final CoordinateReferenceSystem epsg4326 = CRS.decode("urn:ogc:def:crs:OGC:2:84");
            if (env != null) {
                if (!CRS.equalsIgnoreMetadata(env.getCoordinateReferenceSystem(), epsg4326)) {
                    env = CRS.transform(env, epsg4326);
                }
                return buildBBOX(version,
                       "urn:ogc:def:crs:OGC:2:84",
                       env.getMinimum(0),
                       env.getMinimum(1),
                       env.getMaximum(0),
                       env.getMaximum(1));
            } else {
                return buildBBOX(version,"urn:ogc:def:crs:OGC:2:84", -180, -90, 180, 90);
            }

        } catch (DataStoreException ex) {
            throw new CstlServiceException(ex);
        } catch (TransformException ex) {
            throw new CstlServiceException(ex);
        } catch (FactoryException ex) {
            throw new CstlServiceException(ex);
        }
    }

    private static void applyParameterOnQuery(final Filter filter, final List<? extends Parameter> parameters) throws CstlServiceException {
        if (filter instanceof XMLFilter) {
            final Object filterObject = ((XMLFilter)filter).getFilterObject();

            if (filterObject instanceof BinarySpatialOperator) {
                final BinarySpatialOperator binary = (BinarySpatialOperator) filterObject;
                if (binary.getExpression2() != null && binary.getExpression2() instanceof XMLLiteral) {
                    final XMLLiteral lit = (XMLLiteral) binary.getExpression2();
                    if (lit.getValue() instanceof String) {
                        String s = (String)lit.getValue();
                        for (Parameter param : parameters) {
                            if (s.indexOf('$' + param.getName()) != -1) {
                                s = s.replace('$' + param.getName(), (String)param.getContent().get(0));
                            }
                        }
                        lit.getContent().clear();
                        lit.setContent(s);
                    }
                }

            } else if (filterObject instanceof BBOXType) {
                final BBOXType bb = (BBOXType) filterObject;
                if (bb.getAny() != null && bb.getAny() instanceof String) {
                    String s = (String)bb.getAny();
                    for (Parameter param : parameters) {
                        if (s.indexOf('$' + param.getName()) != -1) {
                            bb.setAny(param.getContent().get(0));
                        }
                    }
                }

            } else if (filterObject instanceof BinaryComparisonOperator) {
                final BinaryComparisonOperator binary = (BinaryComparisonOperator) filterObject;
                if (binary.getExpression2() != null && binary.getExpression2() instanceof XMLLiteral) {
                    final XMLLiteral lit = (XMLLiteral) binary.getExpression2();
                    if (lit.getValue() instanceof String) {
                        String s = (String)lit.getValue();
                        for (Parameter param : parameters) {
                            if (s.indexOf('$' + param.getName()) != -1) {
                                s = s.replace('$' + param.getName(), (String)param.getContent().get(0));
                            }
                        }
                        lit.getContent().clear();
                        lit.setContent(s);
                    }
                }

            } else if (filterObject instanceof BinaryLogicOperator) {
                final BinaryLogicOperator binary = (BinaryLogicOperator) filterObject;
                for (Filter child : binary.getChildren()) {
                    applyParameterOnQuery(child, parameters);
                }
            } else {
                throw new CstlServiceException("Unimplemented filter implementation:" + filterObject.getClass().getName(), NO_APPLICABLE_CODE);
            }
        } else {
            throw new CstlServiceException("Expected filter implementation", NO_APPLICABLE_CODE);
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

    private List<FeatureType> getFeatureTypes() {
        final List<FeatureType> types       = new ArrayList<FeatureType>();
        final LayerProviderProxy namedProxy = LayerProviderProxy.getInstance();
        final Map<Name,Layer> layers        = getLayers();

        //search all types
        for (final Name name : layers.keySet()) {
            final LayerDetails layer = namedProxy.get(name);
            if (!(layer instanceof FeatureLayerDetails)) {continue;}



            try {
                //fix feature type to define the exposed crs : true EPSG axis order
                final FeatureType baseType = getFeatureTypeFromLayer((FeatureLayerDetails)layer);
                final String crsCode = getCRSCode(baseType);
                final CoordinateReferenceSystem exposedCrs = CRS.decode(crsCode);
                final FeatureType exposedType = FeatureTypeUtilities.transform(baseType, exposedCrs);
                types.add(exposedType);
            } catch (Exception ex) {
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
    public ListStoredQueriesResponse listStoredQueries(final ListStoredQueries request) throws CstlServiceException {
        LOGGER.log(logLevel, "ListStoredQueries request processing\n");
        final long startTime = System.currentTimeMillis();
        isWorking();
        verifyBaseRequest(request, true, false);

        final String currentVersion              = actingVersion.version.toString();

        final ListStoredQueriesResponse response = buildListStoredQueriesResponse(currentVersion, storedQueries);
        LOGGER.log(logLevel, "ListStoredQueries request processed in {0} ms", (System.currentTimeMillis() - startTime));
        return response;

    }

    @Override
    public DescribeStoredQueriesResponse describeStoredQueries(final DescribeStoredQueries request) throws CstlServiceException {
        LOGGER.log(logLevel, "DescribeStoredQueries request processing\n");
        final long startTime = System.currentTimeMillis();
        isWorking();
        verifyBaseRequest(request, true, false);

        final List<StoredQueryDescription> storedQueryList = new ArrayList<StoredQueryDescription>();
        final String currentVersion              = actingVersion.version.toString();
        for (String id : request.getStoredQueryId()) {
            for (StoredQueryDescription description : storedQueries) {
                if (description.getId().equals(id)) {
                    storedQueryList.add(description);
                }
            }
        }
        final DescribeStoredQueriesResponse response = buildDescribeStoredQueriesResponse(currentVersion, storedQueryList);
        LOGGER.log(logLevel, "DescribeStoredQueries request processed in {0} ms", (System.currentTimeMillis() - startTime));
        return response;
    }

    @Override
    public CreateStoredQueryResponse createStoredQuery(final CreateStoredQuery request) throws CstlServiceException {
        LOGGER.log(logLevel, "CreateStoredQuery request processing\n");
        final long startTime = System.currentTimeMillis();
        isWorking();
        verifyBaseRequest(request, true, false);
        final String currentVersion  = actingVersion.version.toString();

        storedQueries.addAll(request.getStoredQueryDefinition());
        storedQueries();

        final CreateStoredQueryResponse response = buildCreateStoredQueryResponse(currentVersion, "OK");
        LOGGER.log(logLevel, "CreateStoredQuery request processed in {0} ms", (System.currentTimeMillis() - startTime));
        return response;
    }

    @Override
    public DropStoredQueryResponse dropStoredQuery(final DropStoredQuery request) throws CstlServiceException {
        LOGGER.log(logLevel, "dropStoredQuery request processing\n");
        final long startTime = System.currentTimeMillis();
        isWorking();
        verifyBaseRequest(request, true, false);
        final String currentVersion  = actingVersion.version.toString();

        StoredQueryDescription candidate = null;
        for (StoredQueryDescription sq : storedQueries) {
            if (sq.getId().equals(request.getId())) {
                candidate = sq;
            }
        }
        if (candidate == null) {
            throw new CstlServiceException("Unexisting Stored query: " + request.getId(), INVALID_PARAMETER_VALUE);
        } else  {
            storedQueries.remove(candidate);
        }
        storedQueries();

        final DropStoredQueryResponse response = buildDropStoredQueryResponse(currentVersion, "OK");
        LOGGER.log(logLevel, "dropStoredQuery request processed in {0} ms", (System.currentTimeMillis() - startTime));
        return response;
    }
}
