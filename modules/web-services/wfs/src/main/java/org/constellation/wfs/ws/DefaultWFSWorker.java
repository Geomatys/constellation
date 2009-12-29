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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

// Constellation dependencies
import org.constellation.ServiceDef;
import org.constellation.provider.FeatureLayerDetails;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.ws.AbstractWorker;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.ServiceType;
import org.constellation.ws.ServiceVersion;
import org.constellation.ws.rs.OGCWebService;

// Geotoolkit dependencies
import org.geotoolkit.wfs.xml.RequestBase;
import org.geotoolkit.data.FeatureSource;
import org.geotoolkit.data.collection.FeatureCollection;
import org.geotoolkit.data.collection.FeatureCollectionGroup;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.feature.xml.jaxb.JAXBFeatureTypeWriter;
import org.geotoolkit.feature.xml.Utils;
import org.geotoolkit.geometry.jts.JTSEnvelope2D;
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
import org.geotoolkit.data.store.EmptyFeatureCollection;
import org.geotoolkit.filter.accessor.Accessors;
import org.geotoolkit.filter.accessor.PropertyAccessor;
import org.geotoolkit.filter.visitor.ListingPropertyVisitor;
import org.geotoolkit.ogc.xml.v110.FeatureIdType;
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
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

// GeoAPI dependencies
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.opengis.referencing.operation.TransformException;
import org.opengis.util.CodeList;


/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class DefaultWFSWorker extends AbstractWorker implements WFSWorker {

    private final List<String> standardCRS = new ArrayList<String>();

    /**
     * The current version of the service.
     */
    private ServiceVersion actingVersion = new ServiceVersion(ServiceType.WFS, "1.1.0");

    private Map<String, String> schemaLocations;

    private String outputFormat = "text/xml";

    public DefaultWFSWorker(final MarshallerPool marshallerPool) {
        super(marshallerPool);

        //todo wait for martin fix
        standardCRS.add("urn:x-ogc:def:crs:EPSG:7.01:4326");
        standardCRS.add("urn:x-ogc:def:crs:EPSG:7.01:3395");

//        try{
//            standardCRS.add(CRS.lookupIdentifier(Citations.URN_OGC, CRS.decode("CRS:84"), true));
//            standardCRS.add(CRS.lookupIdentifier(Citations.URN_OGC, CRS.decode("EPSG:4326"), true));
//            standardCRS.add(CRS.lookupIdentifier(Citations.URN_OGC, CRS.decode("EPSG:3395"), true));
//        }catch(FactoryException ex){
//            LOGGER.log(Level.SEVERE, "Could not find urn identifiers : " + ex.getLocalizedMessage(),ex);
//        }


    }

    /**
     * {@inheritDoc }
     */
    @Override
    public WFSCapabilitiesType getCapabilities(final GetCapabilitiesType request) throws CstlServiceException {
        LOGGER.log(logLevel, "GetCapabilities request proccesing");
        long start = System.currentTimeMillis();

        // we verify the base attribute
        verifyBaseRequest(request, false, true);

        if (request.getAcceptFormats() != null && request.getAcceptFormats().getOutputFormat() != null && request.getAcceptFormats().getOutputFormat().size() > 0) {
            outputFormat =  request.getAcceptFormats().getOutputFormat().get(0);
        } else {
            outputFormat = "application/xml";
        }
        
        final WFSCapabilitiesType inCapabilities;
        try {
            String deployedDir = null;
            if (getServletContext() != null) {
                deployedDir = getServletContext().getRealPath("WEB-INF");
            }
            inCapabilities = (WFSCapabilitiesType) getStaticCapabilitiesObject(deployedDir, actingVersion.toString(), "WFS");
        } catch (IOException e) {
            throw new CstlServiceException(e, NO_APPLICABLE_CODE);
        } catch (JAXBException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        }

        if (getUriContext() != null) {
            final String url = getUriContext().getBaseUri().toString();
            OGCWebService.updateOWSURL(inCapabilities.getOperationsMetadata().getOperation(), url, "WFS");
        }

        final WFSCapabilitiesType result = new WFSCapabilitiesType("1.1.0");


        if (request.getSections() == null || request.getSections().getSection().contains("featureTypeList")) {
            final List<FeatureTypeType> types = new ArrayList<FeatureTypeType>();

            /*
             *  layer providers
             */
            final LayerProviderProxy namedProxy    = LayerProviderProxy.getInstance();
            for (final Name layerName : namedProxy.getKeys(ServiceDef.Specification.WFS.fullName)) {
                final LayerDetails layer = namedProxy.get(layerName);
                if (layer instanceof FeatureLayerDetails){
                    final FeatureLayerDetails fld = (FeatureLayerDetails) layer;
                    final SimpleFeatureType type  = fld.getSource().getSchema();
                    final FeatureTypeType ftt;
                    try {

                        final String defaultCRS;
                        if (type.getGeometryDescriptor() != null && type.getGeometryDescriptor().getCoordinateReferenceSystem() != null) {
                            //todo wait for martin fix
                            String id  = CRS.lookupIdentifier(type.getGeometryDescriptor().getCoordinateReferenceSystem(), true);
                            if (id == null) {
                                id = CRS.getDeclaredIdentifier(type.getGeometryDescriptor().getCoordinateReferenceSystem());
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
                        ftt = new FeatureTypeType(
                                Utils.getQnameFromName(layerName),
                                fld.getName(),
                                defaultCRS,
                                standardCRS,
                                UnmodifiableArrayList.wrap(new WGS84BoundingBoxType[]{toBBox(fld.getSource())}));
                        types.add(ftt);
                    } catch (FactoryException ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                    }

                }
            }

            result.setFeatureTypeList(new FeatureTypeListType(null, types));
        } else {
            result.setFeatureTypeList(null);
        }
        //todo ...etc...--------------------------------------------------------
        if (request.getSections() != null && !request.getSections().getSection().contains("operationsMetadata")) {
            result.setOperationsMetadata(null);
        } else {
            result.setOperationsMetadata(inCapabilities.getOperationsMetadata());
        }
        if (request.getSections() != null && !request.getSections().getSection().contains("serviceProvider")) {
            result.setServiceProvider(null);
        } else {
            result.setServiceProvider(inCapabilities.getServiceProvider());
        }
        if (request.getSections() != null && !request.getSections().getSection().contains("serviceIdentification")) {
            result.setServiceIdentification(null);
        } else {
            result.setServiceIdentification(inCapabilities.getServiceIdentification());
        }
        result.setServesGMLObjectTypeList(null);
        result.setSupportsGMLObjectTypeList(null);

        result.setFilterCapabilities(inCapabilities.getFilterCapabilities());

        LOGGER.log(logLevel, "GetCapabilities treated in " + (System.currentTimeMillis() - start) + "ms");
        return result;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Schema describeFeatureType(final DescribeFeatureTypeType request) throws CstlServiceException {
        LOGGER.log(logLevel, "DecribeFeatureType request proccesing");
        long start = System.currentTimeMillis();

        // we verify the base attribute
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
            for (final Name name : namedProxy.getKeys(ServiceDef.Specification.WFS.fullName)) {
                final LayerDetails layer = namedProxy.get(name, ServiceDef.Specification.WFS.fullName);
                if (layer == null || !(layer instanceof FeatureLayerDetails)) continue;

                final FeatureLayerDetails fld = (FeatureLayerDetails)layer;
                final SimpleFeatureType sft   = fld.getSource().getSchema();
                types.add(sft);
            }
        } else {
            //search only the given list
            for (final QName name : names) {
                LayerDetails layer = namedProxy.get(Utils.getNameFromQname(name), ServiceDef.Specification.WFS.fullName);
                
                if(layer == null || !(layer instanceof FeatureLayerDetails)) {
                    throw new CstlServiceException("The specified TypeNames does not exist:" + name);
                }

                final FeatureLayerDetails fld = (FeatureLayerDetails)layer;
                final SimpleFeatureType sft   = fld.getSource().getSchema();
                types.add(sft);
            }
        }

        LOGGER.log(logLevel, "DescribeFeatureType treated in " + (System.currentTimeMillis() - start) + "ms");
        return writer.getSchemaFromFeatureType(types);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Object getFeature(final GetFeatureType request) throws CstlServiceException {
        LOGGER.log(logLevel, "GetFeature request proccesing");
        long start = System.currentTimeMillis();

        // we verify the base attribute
        verifyBaseRequest(request, false, false);

        String requestOutputFormat                = request.getOutputFormat();
        if (requestOutputFormat == null) {
            requestOutputFormat = "text/xml; subtype=gml/3.1.1";
        }
        outputFormat = requestOutputFormat;

        final LayerProviderProxy namedProxy       = LayerProviderProxy.getInstance();
        final XMLUtilities util                   = new XMLUtilities();
        final Integer maxFeatures                 = request.getMaxFeatures();
        final List<FeatureCollection> collections = new ArrayList<FeatureCollection>();
        schemaLocations                           = new HashMap<String, String>();

        if (request.getQuery() == null || request.getQuery().size() == 0) {
            throw new CstlServiceException("You must specify a query!", MISSING_PARAMETER_VALUE);
        }
        
        for (final QueryType query : request.getQuery()) {
            final FilterType jaxbFilter   = query.getFilter();
            final SortByType jaxbSortBy   = query.getSortBy();
            final String srs              = query.getSrsName();
            final List<QName> typeNames   = query.getTypeName();
            final List<Object> properties = query.getPropertyNameOrXlinkPropertyNameOrFunction();
            
            final List<String> requestPropNames = new ArrayList<String>();
            final List<SortBy> sortBys          = new ArrayList<SortBy>();

            //decode filter-----------------------------------------------------
            final Filter filter = extractJAXBFilter(jaxbFilter);

            //decode crs--------------------------------------------------------
            final CoordinateReferenceSystem crs = extractCRS(srs);

            //decode property names---------------------------------------------
            for (Object obj : properties) {
                if (obj instanceof JAXBElement){
                    obj = ((JAXBElement)obj).getValue();
                }
                
                if (obj instanceof String) {
                    String pName = (String) obj;
                    int pos = pName.indexOf(':');
                    if (pos != -1) {
                        pName = pName.substring(pos + 1);
                    }
                    requestPropNames.add(pName);
                }
            }

            //decode sort by----------------------------------------------------
            if (jaxbSortBy != null) {
                sortBys.addAll(util.getTransformer110().visitSortBy(jaxbSortBy));
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

                FeatureLayerDetails layer = (FeatureLayerDetails)namedProxy.get(Utils.getNameFromQname(typeName), ServiceDef.Specification.WFS.fullName);

                if (layer == null) {
                    throw new CstlServiceException("The specified TypeNames does not exist:" + typeName);
                }
                
                FeatureType ft = layer.getSource().getSchema();

                if (!requestPropNames.isEmpty()) {
                    List<String> propertyNames = new ArrayList<String>();
                    for (PropertyDescriptor pdesc : ft.getDescriptors()) {
                        String propName = pdesc.getName().getLocalPart();

                        if (!pdesc.isNillable()) {
                            if (!propertyNames.contains(propName)) {
                                propertyNames.add(propName);
                            }
                        } else if (requestPropNames.contains(propName)) {
                            propertyNames.add(propName);
                        }
                        
                        if (requestPropNames.contains(propName)) {
                                requestPropNames.remove(propName);
                        }
                    }
                    // if the requestPropNames is not empty there is unKnown propertyNames
                    if (!requestPropNames.isEmpty()) {
                        throw new CstlServiceException("The feature Type " + typeName + " does not has such a property:" + requestPropNames.get(0), INVALID_PARAMETER_VALUE);
                    }

                    queryBuilder.setProperties(propertyNames.toArray(new String[propertyNames.size()]));
                } else  {
                    queryBuilder.setProperties(null);
                }

                queryBuilder.setTypeName(ft.getName());
                Collection<String> filterProperties =  (Collection<String>) filter.accept(ListingPropertyVisitor.VISITOR, null);
                for (String filterProperty : filterProperties) {
                    PropertyAccessor pa = Accessors.getAccessor(FeatureType.class, filterProperty, null);
                    if (pa == null || pa.get(ft, filterProperty, null) == null) {
                        throw new CstlServiceException("The feature Type " + typeName + " does not has such a property:" + filterProperty, INVALID_PARAMETER_VALUE);
                    }
                }

                try {
                    collections.add(layer.getSource().getFeatures(queryBuilder.buildQuery()));
                } catch (IOException ex) {
                    throw new CstlServiceException(ex);
                }

                // we write The SchemaLocation
                String namespace = typeName.getNamespaceURI();
                if (schemaLocations.containsKey(namespace)) {
                    LOGGER.severe("TODO multiple typeName schemaLocation");

                } else {
                    String prefix          = typeName.getPrefix();
                    if (getUriContext() != null) {
                        String describeRequest = getUriContext().getBaseUri().toString() + "wfs?request=DescribeFeatureType&version=1.1.0&service=WFS";
                        describeRequest        = describeRequest + "&namespace=xmlns(" + prefix + "=" + namespace + ")";
                        describeRequest        = describeRequest + "&Typename=" + prefix + ':' + typeName.getLocalPart() + "";
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
        FeatureCollection response;
	if (collections.size() > 0) {
	        response = FeatureCollectionGroup.sequence("collection-1",collections.toArray(new FeatureCollection[collections.size()]));
        } else {
	        response = new EmptyFeatureCollection(null);
	}
        if (request.getResultType() == ResultTypeType.HITS) {
            FeatureCollectionType collection = new FeatureCollectionType(response.size(), org.geotoolkit.internal.jaxb.XmlUtilities.toXML(new Date()));
            collection.setId("collection-1");
            return collection;

        }
        LOGGER.log(logLevel, "GetFeature treated in " + (System.currentTimeMillis() - start) + "ms");
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
        LOGGER.log(logLevel, "Transaction request processing" + '\n');
        final long startTime = System.currentTimeMillis();
        verifyBaseRequest(request, true, false);

        // we prepare the report
        int totalInserted                   = 0;
        int totalUpdated                    = 0;
        int totalDeleted                    = 0;
        final LayerProviderProxy namedProxy = LayerProviderProxy.getInstance();
        InsertResultsType insertResults     = null;
        final List<Object> transactions     = request.getInsertOrUpdateOrDelete();

        for (Object transaction: transactions) {

            /**
             * Features insertion.
             */
            if (transaction instanceof InsertElementType) {
                final InsertElementType insertRequest = (InsertElementType)transaction;

                String handle = insertRequest.getHandle();

                // we verify the input format
                if (insertRequest.getInputFormat() != null && !insertRequest.getInputFormat().equals("text/xml; subtype=gml/3.1.1")) {
                    throw new CstlServiceException("This only input format supported is: text/xml; subtype=gml/3.1.1",
                            INVALID_PARAMETER_VALUE, "inputFormat");
                }

                // what to do with the SRSName ?
                String srsName = insertRequest.getSrsName();

                // what to do with that, whitch ones are supported ??
                IdentifierGenerationOptionType idGen = insertRequest.getIdgen();

                List<InsertedFeatureType> inserted = new ArrayList<InsertedFeatureType>();
                for (Object feature : insertRequest.getFeature()) {
                    System.out.println("feature:" + feature);
                    // TODO datastore.storeFeature(feature);
                    String fid = null; // get the id of the inserted feature
                    inserted.add(new InsertedFeatureType(new FeatureIdType(fid), handle));
                    totalInserted++;
                }
                insertResults = new InsertResultsType(inserted);

            /**
             * Features remove.
             */
            } else if (transaction instanceof DeleteElementType) {
                
                final DeleteElementType deleteRequest = (DeleteElementType) transaction;

                //decode filter-----------------------------------------------------
                final Filter filter = extractJAXBFilter(deleteRequest.getFilter());

                final QueryBuilder queryBuilder = new QueryBuilder();
                queryBuilder.setFilter(filter);

                FeatureLayerDetails layer = (FeatureLayerDetails)namedProxy.get(Utils.getNameFromQname(deleteRequest.getTypeName()), ServiceDef.Specification.WFS.fullName);
                if (layer == null) {
                    throw new CstlServiceException("The specified TypeNames does not exist:" + deleteRequest.getTypeName());
                }

                FeatureType ft = layer.getSource().getSchema();
                queryBuilder.setTypeName(ft.getName());
                
                FeatureCollection fc;
                try {
                    fc = layer.getSource().getFeatures(queryBuilder.buildQuery());
                } catch (IOException ex) {
                    throw new CstlServiceException(ex);
                }

                // todo delete the selected feature
                totalDeleted++;


            /**
             * Features updates.
             */
            } else if (transaction instanceof UpdateElementType) {

                UpdateElementType updateRequest = (UpdateElementType) transaction;

                // we verify the input format
                if (updateRequest.getInputFormat() != null && !updateRequest.getInputFormat().equals("text/xml; subtype=gml/3.1.1")) {
                    throw new CstlServiceException("This only input format supported is: text/xml; subtype=gml/3.1.1",
                            INVALID_PARAMETER_VALUE, "inputFormat");
                }
                
                //decode filter-----------------------------------------------------
                final Filter filter = extractJAXBFilter(updateRequest.getFilter());

                //decode crs--------------------------------------------------------
                final CoordinateReferenceSystem crs = extractCRS(updateRequest.getSrsName());

                final QueryBuilder queryBuilder = new QueryBuilder();
                queryBuilder.setFilter(filter);
                queryBuilder.setCRS(crs);

                FeatureLayerDetails layer = (FeatureLayerDetails)namedProxy.get(Utils.getNameFromQname(updateRequest.getTypeName()), ServiceDef.Specification.WFS.fullName);

                if (layer == null) {
                    throw new CstlServiceException("The specified TypeNames does not exist:" + updateRequest.getTypeName());
                }

                FeatureType ft = layer.getSource().getSchema();

                queryBuilder.setTypeName(ft.getName());
                
                for (PropertyType updateProperty : updateRequest.getProperty()) {
                    String updatePropertyValue = updateProperty.getName().getLocalPart();
                    PropertyAccessor pa = Accessors.getAccessor(FeatureType.class, updatePropertyValue, null);
                    if (pa == null || pa.get(ft, updatePropertyValue, null) == null) {
                        throw new CstlServiceException("The feature Type " + updateRequest.getTypeName() + " does not has such a property:" + updatePropertyValue, INVALID_PARAMETER_VALUE);
                    }
                }
                FeatureCollection fc;
                try {
                    fc = layer.getSource().getFeatures(queryBuilder.buildQuery());
                } catch (IOException ex) {
                    throw new CstlServiceException(ex);
                }

                // TODO update the selected feature

                totalUpdated++;
                    
            } else {
                String className = " null object";
                if (transaction != null) {
                    className = transaction.getClass().getName();
                }
                throw new CstlServiceException("This kind of transaction is not supported by the service: " + className,
                                              INVALID_PARAMETER_VALUE, "transaction");
            }

        }
        // todo

        final TransactionSummaryType summary = new TransactionSummaryType(totalInserted,
                                                                          totalUpdated,
                                                                          totalDeleted);

        final TransactionResponseType response = new TransactionResponseType(summary, null, insertResults, actingVersion.toString());
        LOGGER.log(logLevel, "Transaction request processed in " + (System.currentTimeMillis() - startTime) + " ms");
        
        throw new CstlServiceException("WFS-T is not supported on this Constellation version.");
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getOutputFormat() {
        return outputFormat;
    }

    /**
     * Extract an OGC filter usable by the datastore from the request filter
     * unmarshalled by JAXB.
     *
     * @param jaxbFilter an OGC JAXB filter.
     * @return
     * @throws CstlServiceException
     */
    private Filter extractJAXBFilter(FilterType jaxbFilter) throws CstlServiceException {
        final XMLUtilities util = new XMLUtilities();
        final Filter filter;
        try {
            if (jaxbFilter != null) {
                filter = util.getTransformer110().visitFilter(jaxbFilter);
            } else {
                filter = Filter.INCLUDE;
            }
        } catch (Exception ex) {
            throw new CstlServiceException(ex);
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
                throw new CstlServiceException(ex);
            } catch (FactoryException ex) {
                throw new CstlServiceException(ex);
            }
        } else {
            crs = null;
        }
        return crs;
    }
    
    /**
     * Extract the WGS84 BBOx from a featureSource.
     * what ? may not be wgs84 exactly ? why is there a crs attribut on a wgs84 bbox ?
     */
    private static WGS84BoundingBoxType toBBox(FeatureSource source) throws CstlServiceException{
        try {
            final JTSEnvelope2D env = source.getBounds();
            final CoordinateReferenceSystem EPSG4326 = CRS.decode("urn:ogc:def:crs:OGC:2:84");
            if (env != null && !env.isEmpty()) {
                if (CRS.equalsIgnoreMetadata(env.getCoordinateReferenceSystem(), EPSG4326)) {
                   return new WGS84BoundingBoxType(
                           "urn:ogc:def:crs:OGC:2:84",
                           env.getMinimum(0),
                           env.getMinimum(1),
                           env.getMaximum(0),
                           env.getMaximum(1));
                } else {
                    Envelope enveloppe = CRS.transform(env, EPSG4326);
                    return new WGS84BoundingBoxType(
                            "urn:ogc:def:crs:OGC:2:84",
                           enveloppe.getMinimum(0),
                           enveloppe.getMinimum(1),
                           enveloppe.getMaximum(0),
                           enveloppe.getMaximum(1));
                }
            } else {
                return new WGS84BoundingBoxType("urn:ogc:def:crs:OGC:2:84", -180, -90, 180, 90);
            }

        } catch (IOException ex) {
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
                if (request.getService().equals("")) {
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
                        request.getVersion().toString().equals("")  || request.getVersion().toString().equals("1.0.0") ) { // hack for openScale accept 1.0.0
                    this.actingVersion = new ServiceVersion(ServiceType.WFS, "1.1.0");

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
                    this.actingVersion = new ServiceVersion(ServiceType.WFS, "1.1.0");
                }
            }
         } else {
            throw new CstlServiceException("The request is null!", NO_APPLICABLE_CODE);
         }
    }

    @Override
    public Map<String, String> getSchemaLocations() {
        return schemaLocations;
    }

    @Override
    public List<FeatureType> getFeatureTypes() {
        final List<FeatureType> types       = new ArrayList<FeatureType>();
        final LayerProviderProxy namedProxy = LayerProviderProxy.getInstance();

        //search all types
        for (final Name name : namedProxy.getKeys(ServiceDef.Specification.WFS.fullName)) {
            final LayerDetails layer = namedProxy.get(name, ServiceDef.Specification.WFS.fullName);
            if (layer == null || !(layer instanceof FeatureLayerDetails)) continue;

            final FeatureLayerDetails fld = (FeatureLayerDetails)layer;
            final SimpleFeatureType sft   = fld.getSource().getSchema();
            types.add(sft);
        }
        return types;
    }
}
