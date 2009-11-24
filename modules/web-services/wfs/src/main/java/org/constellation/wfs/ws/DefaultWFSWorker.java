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
import org.constellation.provider.FeatureLayerDetails;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.NamedLayerProviderProxy;
import org.constellation.ws.AbstractWorker;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.rs.WebService;

// Geotoolkit dependencies
import org.geotoolkit.data.FeatureSource;
import org.geotoolkit.data.collection.FeatureCollection;
import org.geotoolkit.data.collection.FeatureCollectionGroup;
import org.geotoolkit.data.query.DefaultQuery;
import org.geotoolkit.feature.xml.jaxb.JAXBFeatureTypeWriter;
import org.geotoolkit.feature.xml.Utils;
import org.geotoolkit.geometry.jts.JTSEnvelope2D;
import org.geotoolkit.gml.xml.v311.AbstractGMLEntry;
import org.geotoolkit.ogc.xml.v110.FilterType;
import org.geotoolkit.ogc.xml.v110.PropertyNameType;
import org.geotoolkit.ogc.xml.v110.SortByType;
import org.geotoolkit.ows.xml.AbstractDCP;
import org.geotoolkit.ows.xml.AbstractHTTP;
import org.geotoolkit.ows.xml.AbstractOnlineResourceType;
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

// GeoAPI dependencies
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.sort.SortBy;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class DefaultWFSWorker extends AbstractWorker implements WFSWorker{

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

        final String queryVersion = getCapab.getVersion().toString();

        //Generate the correct URL in the static part. ?TODO: clarify this.
        final WFSCapabilitiesType inCapabilities;
        try {
            inCapabilities = (WFSCapabilitiesType) getStaticCapabilitiesObject(
                    getServletContext().getRealPath("WEB-INF"), queryVersion);
        } catch (IOException e) {
            throw new CstlServiceException(e, NO_APPLICABLE_CODE);
        } catch (JAXBException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        }

        //types possible, providers gives this list-----------------------------
        final FeatureTypeListType ftl     = new FeatureTypeListType();
        final List<FeatureTypeType> types = new ArrayList<FeatureTypeType>();
        final LayerProviderProxy proxy    = LayerProviderProxy.getInstance();
        for (final String layerName : proxy.getKeys()) {
            final LayerDetails layer = proxy.get(layerName);
            if (layer instanceof FeatureLayerDetails){
                final FeatureLayerDetails fld = (FeatureLayerDetails) layer;
                final SimpleFeatureType type  = fld.getSource().getSchema();
                final FeatureTypeType ftt;
                try {

                    final String defaultCRS;
                    if (type.getGeometryDescriptor() != null && type.getGeometryDescriptor().getCoordinateReferenceSystem() != null) {
                        //todo wait for martin fix
                        String id  = CRS.lookupIdentifier(type.getGeometryDescriptor().getCoordinateReferenceSystem(), true);
                        defaultCRS = "urn:x-ogc:def:crs:" + id.replaceAll(":", ":7.01:");
    //                    final String defaultCRS = CRS.lookupIdentifier(Citations.URN_OGC,
    //                            type.getGeometryDescriptor().getCoordinateReferenceSystem(), true);
                    } else {
                        defaultCRS = "urn:x-ogc:def:crs:EPSG:7.01:4326";
                    }
                    ftt = new FeatureTypeType(
                            new QName(layerName, layerName),
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

        final NamedLayerProviderProxy namedProxy    = NamedLayerProviderProxy.getInstance();
        for (final Name layerName : namedProxy.getKeys()) {
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
                        defaultCRS = "urn:x-ogc:def:crs:" + id.replaceAll(":", ":7.01:");
    //                    final String defaultCRS = CRS.lookupIdentifier(Citations.URN_OGC,
    //                            type.getGeometryDescriptor().getCoordinateReferenceSystem(), true);
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
        ftl.setFeatureType(types);
        inCapabilities.setFeatureTypeList(ftl);

        //todo ...etc...--------------------------------------------------------
        inCapabilities.getOperationsMetadata();
        inCapabilities.setFilterCapabilities(null);
        inCapabilities.setServesGMLObjectTypeList(null);
        inCapabilities.setSupportsGMLObjectTypeList(null);
        inCapabilities.getSupportsGMLObjectTypeList();
        inCapabilities.getUpdateSequence();
        inCapabilities.getVersion();


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
            path = WebService.getSicadeDirectory();
         }
         if (fileName != null)
            return new File(path, fileName);
         else return path;
    }

    /**
     * update The URL in capabilities document with the service actual URL.
     */
    private void updateURL(final List<? extends AbstractDCP> dcpList, final String url) {
        for(AbstractDCP dcp: dcpList) {
            final AbstractHTTP http = dcp.getHTTP();
            List<? extends AbstractOnlineResourceType> types = http.getGetOrPost();
            for(AbstractOnlineResourceType aort : types){
                aort.setHref(url + "wfs?SERVICE=WFS&");
            }
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Schema describeFeatureType(final DescribeFeatureTypeType model) throws CstlServiceException {

        final JAXBFeatureTypeWriter writer;
        try {
            writer = new JAXBFeatureTypeWriter();
        } catch (JAXBException ex) {
            throw new CstlServiceException(ex);
        }
        final LayerProviderProxy proxy           = LayerProviderProxy.getInstance();
        final NamedLayerProviderProxy namedProxy = NamedLayerProviderProxy.getInstance();
        final List<QName> names                  = model.getTypeName();
        final List<FeatureType> types            = new ArrayList<FeatureType>();

        if (names.isEmpty()) {
            //search all types
            for (final String name : proxy.getKeys()) {
                final LayerDetails layer = proxy.get(name);
                if (layer == null || !(layer instanceof FeatureLayerDetails)) continue;

                final FeatureLayerDetails fld = (FeatureLayerDetails)layer;
                final SimpleFeatureType sft   = fld.getSource().getSchema();
                types.add(sft);
            }
            for (final Name name : namedProxy.getKeys()) {
                final LayerDetails layer = namedProxy.get(name);
                if (layer == null || !(layer instanceof FeatureLayerDetails)) continue;

                final FeatureLayerDetails fld = (FeatureLayerDetails)layer;
                final SimpleFeatureType sft   = fld.getSource().getSchema();
                types.add(sft);
            }
        } else {
            //search only the given list
            for (final QName name : names) {
                LayerDetails layer = proxy.get(name.getLocalPart());
                if (layer == null) {
                    layer = namedProxy.get(Utils.getNameFromQname(name));
                }
                if(layer == null || !(layer instanceof FeatureLayerDetails)) continue;

                final FeatureLayerDetails fld = (FeatureLayerDetails)layer;
                final SimpleFeatureType sft   = fld.getSource().getSchema();
                types.add(sft);
            }
        }

        return writer.getSchemaFromFeatureType(types);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public FeatureCollection getFeature(final GetFeatureType request) throws CstlServiceException {

        final LayerProviderProxy proxy            = LayerProviderProxy.getInstance();
        final NamedLayerProviderProxy namedProxy  = NamedLayerProviderProxy.getInstance();
        final XMLUtilities util                   = new XMLUtilities();
        final Integer maxFeatures                 = request.getMaxFeatures();
        final List<FeatureCollection> collections = new ArrayList<FeatureCollection>();
        
        for (final QueryType query : request.getQuery()) {
            final FilterType jaxbFilter   = query.getFilter();
            final SortByType jaxbSortBy   = query.getSortBy();
            final String srs              = query.getSrsName();
            final List<QName> typeNames   = query.getTypeName();
            final List<Object> properties = query.getPropertyNameOrXlinkPropertyNameOrFunction();
            
            final Filter filter;
            final CoordinateReferenceSystem crs;
            final List<String> propNames = new ArrayList<String>();
            final List<SortBy> sortBys   = new ArrayList<SortBy>();

            //decode filter-----------------------------------------------------
            if(jaxbFilter != null){
                filter = util.getTransformer110().visitFilter(jaxbFilter);
            }else{
                filter = Filter.INCLUDE;
            }

            //decode crs--------------------------------------------------------
            if(srs != null){
                try {
                    crs = CRS.decode(srs, true);
                    //todo use other properties to filter properly
                } catch (NoSuchAuthorityCodeException ex) {
                    throw new CstlServiceException(ex);
                } catch (FactoryException ex) {
                    throw new CstlServiceException(ex);
                }
            }else{
                crs = null;
            }

            //decode property names---------------------------------------------
            for (Object obj : properties) {
                if(obj instanceof JAXBElement){
                    obj = ((JAXBElement)obj).getValue();
                }

                if(obj instanceof PropertyNameType){
                    final PropertyName pn = util.getTransformer110().visitPropertyName((PropertyNameType) obj);
                    propNames.add(pn.getPropertyName());
                }
            }

            //decode sort by----------------------------------------------------
            if (jaxbSortBy != null) {
                sortBys.addAll(util.getTransformer110().visitSortBy(jaxbSortBy));
            }

            final DefaultQuery fsQuery = new DefaultQuery();
            fsQuery.setFilter(filter);
            fsQuery.setCoordinateSystem(crs);
            fsQuery.setCoordinateSystemReproject(crs);
            if(!propNames.isEmpty()){
                fsQuery.setPropertyNames(propNames);
            }
            if(!sortBys.isEmpty()){
                fsQuery.setSortBy(sortBys.toArray(new SortBy[sortBys.size()]));
            }
            if(maxFeatures != null){
                fsQuery.setMaxFeatures(maxFeatures);
            }

            for (QName typeName : typeNames) {
            FeatureLayerDetails layer = (FeatureLayerDetails)proxy.get(typeName.getLocalPart());
            if (layer == null) {
                layer = (FeatureLayerDetails)namedProxy.get(Utils.getNameFromQname(typeName));
            }
                try {
                    collections.add(layer.getSource().getFeatures(fsQuery));
                } catch (IOException ex) {
                    throw new CstlServiceException(ex);
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
	if (collections.size() > 0) {
	        return FeatureCollectionGroup.sequence("collection-1",collections.toArray(new FeatureCollection[collections.size()]));
        } else {
	        return new EmptyFeatureCollection(null);
	}
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
        return "text/xml";
    }

    /**
     * Extract the WGS84 BBOx from a featureSource.
     * what ? may not be wgs84 exactly ? why is there a crs attribut on a wgs84 bbox ?
     */
    private static WGS84BoundingBoxType toBBox(FeatureSource source) throws CstlServiceException{
        try {
            final JTSEnvelope2D env = source.getBounds();
            if (env != null) {
                WGS84BoundingBoxType bbox =  new WGS84BoundingBoxType(
                           env.getMinimum(0),
                           env.getMinimum(1),
                           env.getMaximum(0),
                           env.getMaximum(1));
                bbox.setCrs(CRS.lookupIdentifier(env.getCoordinateReferenceSystem(),true));
                return bbox;
            } else {
                return new WGS84BoundingBoxType(-180, -90, 180, 90);
            }


//            final CoordinateReferenceSystem EPSG4326 = CRS.decode("EPSG:4326");

//            if(CRS.equalsIgnoreMetadata(env.getCoordinateReferenceSystem(), EPSG4326)){
//               Envelope enveloppe = CRS.transform(env, EPSG4326);
//               return new WGS84BoundingBoxType(
//                       enveloppe.getMinimum(0),
//                       enveloppe.getMinimum(1),
//                       enveloppe.getMaximum(0),
//                       enveloppe.getMaximum(1));
//            }else{
//                return new WGS84BoundingBoxType(
//                       env.getMinimum(0),
//                       env.getMinimum(1),
//                       env.getMaximum(0),
//                       env.getMaximum(1));
//            }

        } catch (IOException ex) {
            throw new CstlServiceException(ex);
        }
//        catch (TransformException ex) {
//            throw new CstlServiceException(ex);
//        } 
        catch (FactoryException ex) {
            throw new CstlServiceException(ex);
        }
    }

}
