/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2009, Geomatys
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

package org.constellation.wfs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import org.constellation.provider.FeatureLayerDetails;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.data.DefaultFeatureCollection;
import org.geotoolkit.data.FeatureSource;
import org.geotoolkit.data.collection.FeatureCollection;
import org.geotoolkit.data.collection.FeatureIterator;
import org.geotoolkit.data.query.DefaultQuery;
import org.geotoolkit.geometry.jts.JTSEnvelope2D;
import org.geotoolkit.gml.xml.v311.AbstractGMLEntry;
import org.geotoolkit.ogc.xml.v110.FilterType;
import org.geotoolkit.ogc.xml.v110.SortByType;
import org.geotoolkit.ows.xml.v100.AddressType;
import org.geotoolkit.ows.xml.v100.CodeType;
import org.geotoolkit.ows.xml.v100.ContactType;
import org.geotoolkit.ows.xml.v100.KeywordsType;
import org.geotoolkit.ows.xml.v100.OnlineResourceType;
import org.geotoolkit.ows.xml.v100.ResponsiblePartySubsetType;
import org.geotoolkit.ows.xml.v100.ServiceIdentification;
import org.geotoolkit.ows.xml.v100.ServiceProvider;
import org.geotoolkit.ows.xml.v100.TelephoneType;
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
import org.geotoolkit.xsd.xml.v2001.FormChoice;
import org.geotoolkit.xsd.xml.v2001.Import;
import org.geotoolkit.xsd.xml.v2001.Schema;
import org.geotoolkit.xsd.xml.v2001.TopLevelComplexType;
import org.geotoolkit.xsd.xml.v2001.TopLevelElement;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class WFSWorker {

    /**
     * The default logger.
     */
    private static final Logger LOGGER = Logger.getLogger("org.constellation.wfs");

    private static final String CSTL_NAMESPACE = "http://constellation-sdi.org";
    private static final String CSTL_PREFIX = "cstl";

    private final List<String> standardCRS = new ArrayList<String>();
    private final ServiceIdentification serviceIdentification;
    private final ServiceProvider serviceProvider;



    public WFSWorker() {

        standardCRS.add("CRS:84");
        standardCRS.add("EPSG:4326");
        standardCRS.add("EPSG:3395");

        serviceIdentification = new ServiceIdentification(
                "WFS",
                "Constellation WFS Service.",
                new KeywordsType("Constellation","WFS"),
                new CodeType("what's that ??"),
                UnmodifiableArrayList.wrap(new String[]{"1.1.0"}),
                "None",
                "None");
        serviceProvider = new ServiceProvider(
                "Constellation",
                new OnlineResourceType("http://constellation.codehaus.org"),
                new ResponsiblePartySubsetType(
                    "Vincent Heurteaux",
                    "PDG",
                    new ContactType(
                        new TelephoneType("04 67 54 87 30",""),
                        new AddressType(
                            "24 rue Pierre Renaudel",
                            "ARLES",
                            "Bouches du rhone",
                            "13200", 
                            "France",
                            "vincent.heurteaux@geomatys.fr"),
                        new OnlineResourceType("http://constellation.codehaus.org"),
                        "9h - 19h",
                        "none"),
                    new CodeType("")
                    )
                );
    }

    public WFSCapabilitiesType getCapabilities(final GetCapabilitiesType model) throws CstlServiceException {
        final WFSCapabilitiesType template = new WFSCapabilitiesType();

        template.setServiceIdentification(serviceIdentification);
        template.setServiceProvider(serviceProvider);

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
                    ftt = new FeatureTypeType(
                            new QName(layerName, layerName),
                            fld.getName(),
                            CRS.lookupIdentifier(type.getGeometryDescriptor().getCoordinateReferenceSystem(), true),
                            standardCRS,
                            UnmodifiableArrayList.wrap(new WGS84BoundingBoxType[]{toBBox(fld.getSource())}));
                    types.add(ftt);
                } catch (FactoryException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }

            }
        }
        ftl.setFeatureType(types);
        template.setFeatureTypeList(ftl);

        //todo ...etc...--------------------------------------------------------
        template.getOperationsMetadata();
        template.setFilterCapabilities(null);
        template.setServesGMLObjectTypeList(null);
        template.setSupportsGMLObjectTypeList(null);
        template.getSupportsGMLObjectTypeList();
        template.getUpdateSequence();
        template.getVersion();

        return template;
    }

    public Schema describeFeatureType(final DescribeFeatureTypeType model) throws CstlServiceException {
        final Schema schema = new Schema();
        schema.setTargetNamespace("http://constellation-sdi.org");
        schema.setElementFormDefault(FormChoice.QUALIFIED);
        schema.setVersion("0.1");
        

        //todo fill the schema correctly
        final Import gmlImport = new Import();
        gmlImport.setNamespace("http://www.opengis.net/gml");
        gmlImport.setSchemaLocation("../gml/3.1.1/base/gml.xsd");
        schema.getIncludeOrImportOrRedefine().add(gmlImport);

        final LayerProviderProxy proxy = LayerProviderProxy.getInstance();
        final List<QName> names = model.getTypeName();

        if (names.isEmpty()){
            //search all types
            for(final String name : proxy.getKeys()){
                final LayerDetails layer = proxy.get(name);
                if(layer == null || !(layer instanceof FeatureLayerDetails)) continue;

                final FeatureLayerDetails fld = (FeatureLayerDetails)layer;
                final SimpleFeatureType sft = fld.getSource().getSchema();

                final TopLevelElement element = new TopLevelElement();
                element.setName(sft.getTypeName());
                element.setType(new QName(CSTL_NAMESPACE, sft.getTypeName(), CSTL_PREFIX));
                element.setSubstitutionGroup(new QName("http://www.opengis.net/gml", "_Feature", "gml"));

                schema.getIncludeOrImportOrRedefine().add(element);

                final TopLevelComplexType type = new TopLevelComplexType();
                type.setName(sft.getTypeName());
                //todo implement a utility method to transform a feature type in complex type
                //schema.getSimpleTypeOrComplexTypeOrGroup().add(...);
                schema.getSimpleTypeOrComplexTypeOrGroup().add(type);
            }
        } else {
            //search only the given list
            for(final QName name : names){
                final LayerDetails layer = proxy.get(name.getLocalPart());
                if(layer == null || !(layer instanceof FeatureLayerDetails)) continue;

                final FeatureLayerDetails fld = (FeatureLayerDetails)layer;
                final SimpleFeatureType sft   = fld.getSource().getSchema();

                final TopLevelElement element = new TopLevelElement();
                element.setName(sft.getTypeName());
                element.setType(new QName(CSTL_NAMESPACE, sft.getTypeName(), CSTL_PREFIX));
                element.setSubstitutionGroup(new QName("http://www.opengis.net/gml", "_Feature", "gml"));

                schema.getIncludeOrImportOrRedefine().add(element);

                final TopLevelComplexType type = new TopLevelComplexType();
                type.setName(sft.getTypeName());
                //todo implement a utility method to transform a feature type in complex type
                //schema.getSimpleTypeOrComplexTypeOrGroup().add(...);
                schema.getSimpleTypeOrComplexTypeOrGroup().add(type);
            }
        }

        return schema;
    }

    public FeatureCollection getFeature(final GetFeatureType request) throws CstlServiceException {

        final LayerProviderProxy proxy = LayerProviderProxy.getInstance();
        final XMLUtilities util = new XMLUtilities();

        final List<FeatureCollection> collections = new ArrayList<FeatureCollection>();
        
        for (final QueryType query : request.getQuery()) {
            final FilterType jaxbFilter   = query.getFilter();
            final SortByType jaxbSortBy   = query.getSortBy();
            final String srs              = query.getSrsName();
            final String typeName         = query.getTypeName().get(0).getLocalPart();
            final List<Object> properties = query.getPropertyNameOrXlinkPropertyNameOrFunction();

            
            final Filter filter;
            final FeatureLayerDetails layer = (FeatureLayerDetails)proxy.get(typeName);
            try {
                filter = util.readFilter(jaxbFilter, org.geotoolkit.sld.xml.Specification.Filter.V_1_1_0);
            } catch (JAXBException ex) {
                throw new CstlServiceException(ex);
            }

            //todo use other properties to filter properly

            final DefaultQuery fsQuery = new DefaultQuery();
            fsQuery.setFilter(filter);
            try {
                collections.add(layer.getSource().getFeatures(fsQuery));
            } catch (IOException ex) {
                throw new CstlServiceException(ex);
            }

        }

        /**
         * 2 possibilité ici :
         *    1) mergé les collections
         *    2) retourné une collection de collection.
         *
         * result TODO find an id and a member type
         */
        FeatureCollection <SimpleFeatureType, SimpleFeature> result = new DefaultFeatureCollection("", null);
        for (FeatureCollection collection: collections) {
            FeatureIterator<SimpleFeature> iterator = collection.features();
            while (iterator.hasNext()) {
                result.add(iterator.next());
            }
        }
        return result;
    }

    public AbstractGMLEntry getGMLObject(GetGmlObjectType grbi) throws CstlServiceException {
        throw new CstlServiceException("WFS get GML Object is not supported on this Constellation version.");
    }

    public LockFeatureResponseType lockFeature(LockFeatureType gr) throws CstlServiceException {
        throw new CstlServiceException("WFS Lock is not supported on this Constellation version.");
    }

    public TransactionResponseType transaction(TransactionType t) throws CstlServiceException {
        throw new CstlServiceException("WFS-T is not supported on this Constellation version.");
    }

    public String getOutputFormat() {
        return "text/xml";
    }

    /**
     * Extract the WGS84 BBOx from a featureSource.
     */
    private static WGS84BoundingBoxType toBBox(FeatureSource source) throws CstlServiceException{
        try {
            final JTSEnvelope2D env = source.getBounds();

            final CoordinateReferenceSystem EPSG4326 = CRS.decode("EPSG:4326");

            if(CRS.equalsIgnoreMetadata(env.getCoordinateReferenceSystem(), EPSG4326)){
               Envelope enveloppe = CRS.transform(env, EPSG4326);
               return new WGS84BoundingBoxType(
                       enveloppe.getMinimum(0),
                       enveloppe.getMinimum(1),
                       enveloppe.getMaximum(0),
                       enveloppe.getMaximum(1));
            }else{
                return new WGS84BoundingBoxType(
                       env.getMinimum(0),
                       env.getMinimum(1),
                       env.getMaximum(0),
                       env.getMaximum(1));
            }

        } catch (IOException ex) {
            throw new CstlServiceException(ex);
        } catch (TransformException ex) {
            throw new CstlServiceException(ex);
        } catch (FactoryException ex) {
            throw new CstlServiceException(ex);
        }
    }

}
