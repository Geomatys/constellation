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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

// Constellation dependencies
import org.constellation.ServiceDef;
import org.constellation.provider.FeatureLayerDetails;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.configuration.ConfigDirectory;
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
import org.geotoolkit.wfs.xml.v110.FeatureCollectionType;
import org.geotoolkit.wfs.xml.v110.ResultTypeType;
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

    /**
     * The default logger.
     */
    private static final Logger LOGGER = Logger.getLogger("org.constellation.wfs");

    private final List<String> standardCRS = new ArrayList<String>();

    /**
     * The web service unmarshaller, which will use the web service name space.
     */
    private final MarshallerPool marshallerPool;

    /**
     * A map containing the Capabilities Object already loaded from file.
     */
    private final Map<String,Object> capabilities = new HashMap<String,Object>();


    /**
     * The current version of the service.
     */
    private ServiceVersion actingVersion = new ServiceVersion(ServiceType.WFS, "1.1.0");

    private Map<String, String> schemaLocations;

    private String outputFormat = "text/xml";
    
    public DefaultWFSWorker(final MarshallerPool marshallerPool) {
        this.marshallerPool = marshallerPool;

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

    public DefaultWFSWorker() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public WFSCapabilitiesType getCapabilities(final GetCapabilitiesType getCapab) throws CstlServiceException {
        LOGGER.info("GetCapabilities request proccesing");
        long start = System.currentTimeMillis();

        // we verify the base attribute
        verifyBaseRequest(getCapab, false, true);

        if (getCapab.getAcceptFormats() != null && getCapab.getAcceptFormats().getOutputFormat() != null && getCapab.getAcceptFormats().getOutputFormat().size() > 0) {
            outputFormat =  getCapab.getAcceptFormats().getOutputFormat().get(0);
        } else {
            outputFormat = "application/xml";
        }
        
        final WFSCapabilitiesType inCapabilities;
        try {
            inCapabilities = (WFSCapabilitiesType) getStaticCapabilitiesObject(
                    getServletContext().getRealPath("WEB-INF"), actingVersion.toString());
        } catch (IOException e) {
            throw new CstlServiceException(e, NO_APPLICABLE_CODE);
        } catch (JAXBException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        }

        final String url = getUriContext().getBaseUri().toString();

        OGCWebService.updateOWSURL(inCapabilities.getOperationsMetadata().getOperation(), url, "WFS");

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

        inCapabilities.setFeatureTypeList(new FeatureTypeListType(null, types));

        //todo ...etc...--------------------------------------------------------
        inCapabilities.getOperationsMetadata();
        //inCapabilities.setFilterCapabilities(null);
        inCapabilities.setServesGMLObjectTypeList(null);
        inCapabilities.setSupportsGMLObjectTypeList(null);
        inCapabilities.getSupportsGMLObjectTypeList();
        inCapabilities.getUpdateSequence();
        inCapabilities.getVersion();


        LOGGER.info("GetCapabilities treated in " + (System.currentTimeMillis() - start) + "ms");
        return inCapabilities;
    }

    /**
     * Returns the file where to read the capabilities document for each service.
     * If no such file is found, then this method returns {@code null}.
     *
     * @param home    The home directory, where to search for configuration files.
     * @param version The version of the GetCapabilities.
     * @return The capabilities Object, or {@code null} if none.
     *
     * @throws JAXBException
     * @throws IOException
     */
    private Object getStaticCapabilitiesObject(final String home, final String version) throws JAXBException, IOException {
        final String fileName = "WFSCapabilities" + version + ".xml";
        final File changeFile = getFile("change.properties", home);
        final Properties p = new Properties();

        // if the flag file is present we load the properties
        if (changeFile != null && changeFile.exists()) {
            final FileInputStream in = new FileInputStream(changeFile);
            p.load(in);
            in.close();
        } else {
            p.put("update", "false");
        }

        //Look if the template capabilities is already in cache.
        Object response = capabilities.get(fileName);
        final boolean update = p.getProperty("update").equals("true");

        if (response == null || update) {
            if (update) {
                LOGGER.info("updating metadata");
            }

            final File f = getFile(fileName, home);
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = marshallerPool.acquireUnmarshaller();
                // If the file is not present in the configuration directory, take the one in resource.
                if (!f.exists()) {
                    final InputStream in = getClass().getResourceAsStream(fileName);
                    response = unmarshaller.unmarshal(in);
                    in.close();
                } else {
                    response = unmarshaller.unmarshal(f);
                }

                if(response instanceof JAXBElement){
                    response = ((JAXBElement)response).getValue();
                }

                capabilities.put(fileName, response);

            } finally {
                if (unmarshaller != null) {
                    marshallerPool.release(unmarshaller);
                }
            }

            //this.setLastUpdateSequence(System.currentTimeMillis());
            p.put("update", "false");

            // if the flag file is present we store the properties
            if (changeFile != null && changeFile.exists()) {
                final FileOutputStream out = new FileOutputStream(changeFile);
                p.store(out, "updated from WebService");
                out.close();
            }
        }

        return response;
    }

    /**
     * Return a file located in the home directory. In this implementation, it should be
     * the WEB-INF directory of the deployed service.
     *
     * @param fileName The name of the file requested.
     * @return The specified file.
     */
    private File getFile(final String fileName, final String home) {
         File path;
         if (home == null || !(path = new File(home)).isDirectory()) {
            path = ConfigDirectory.getConfigDirectory();
         }
         if (fileName != null)
            return new File(path, fileName);
         else return path;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Schema describeFeatureType(final DescribeFeatureTypeType request) throws CstlServiceException {
        LOGGER.info("DecribeFeatureType request proccesing");
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

        LOGGER.info("DescribeFeatureType treated in " + (System.currentTimeMillis() - start) + "ms");
        return writer.getSchemaFromFeatureType(types);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Object getFeature(final GetFeatureType request) throws CstlServiceException {
        LOGGER.info("GetFeature request proccesing");
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
            
            final Filter filter;
            final CoordinateReferenceSystem crs;
            final List<String> requestPropNames = new ArrayList<String>();
            final List<SortBy> sortBys          = new ArrayList<SortBy>();

            //decode filter-----------------------------------------------------
            try {
                if (jaxbFilter != null) {
                    filter = util.getTransformer110().visitFilter(jaxbFilter);
                } else {
                    filter = Filter.INCLUDE;
                }
            } catch(Exception ex) {
                throw new CstlServiceException(ex);
            }

            //decode crs--------------------------------------------------------
            if (srs != null){
                try {
                    crs = CRS.decode(srs, true);
                    //todo use other properties to filter properly
                } catch (NoSuchAuthorityCodeException ex) {
                    throw new CstlServiceException(ex);
                } catch (FactoryException ex) {
                    throw new CstlServiceException(ex);
                }
            } else {
                crs = null;
            }

            //decode property names---------------------------------------------
            for (Object obj : properties) {
                if (obj instanceof JAXBElement){
                    obj = ((JAXBElement)obj).getValue();
                }
                
                if (obj instanceof String) {
                    String pName = (String) obj;
                    if (pName.indexOf(":") != -1) {
                        pName = pName.substring(pName.indexOf(":") + 1);
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
            if(maxFeatures != null){
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
        LOGGER.info("GetFeature treated in " + (System.currentTimeMillis() - start) + "ms");
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
    public TransactionResponseType transaction(TransactionType t) throws CstlServiceException {
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
     * Extract the WGS84 BBOx from a featureSource.
     * what ? may not be wgs84 exactly ? why is there a crs attribut on a wgs84 bbox ?
     */
    private static WGS84BoundingBoxType toBBox(FeatureSource source) throws CstlServiceException{
        try {
            final JTSEnvelope2D env = source.getBounds();
//            if (env != null) {
//                if(CRS.equalsIgnoreMetadata(env.getCoordinateReferenceSystem(), EPSG4326)){
//                Envelope enveloppe = CRS.transform(env, EPSG4326);
//                WGS84BoundingBoxType bbox =  new WGS84BoundingBoxType(
//                           env.getMinimum(0),
//                           env.getMinimum(1),
//                           env.getMaximum(0),
//                           env.getMaximum(1));
//                // fixed value by the standard
//                bbox.setCrs("urn:ogc:def:crs:OGC:2:84");
//                return bbox;
//            } else {
//                return new WGS84BoundingBoxType(-180, -90, 180, 90);
//            }

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
}
