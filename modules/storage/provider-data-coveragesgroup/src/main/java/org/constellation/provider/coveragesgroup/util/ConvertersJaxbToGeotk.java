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
package org.constellation.provider.coveragesgroup.util;

import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.ArgumentChecks;
import org.apache.sis.util.UnconvertibleObjectException;
import org.apache.sis.util.logging.Logging;
import org.constellation.ServiceDef;
import org.constellation.provider.*;
import org.constellation.util.DataReference;
import org.geotoolkit.client.Client;
import org.geotoolkit.coverage.AbstractCoverageReference;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.CoverageStore;
import org.geotoolkit.coverage.CoverageStoreFinder;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.coverage.io.GridCoverageWriter;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.data.FeatureStoreRuntimeException;
import org.geotoolkit.data.memory.WrapFeatureCollection;
import org.geotoolkit.data.query.Query;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.data.session.Session;
import org.geotoolkit.data.wfs.WebFeatureClient;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.feature.type.DefaultName;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.map.CoverageMapLayer;
import org.geotoolkit.map.EmptyMapLayer;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.security.BasicAuthenticationSecurity;
import org.geotoolkit.security.ClientSecurity;
import org.geotoolkit.style.DefaultStyleFactory;
import org.geotoolkit.style.MutableStyle;
import org.apache.sis.util.ArgumentChecks;
import org.apache.sis.util.UnconvertibleObjectException;
import org.apache.sis.util.logging.Logging;
import org.geotoolkit.version.VersionControl;
import org.geotoolkit.version.VersioningException;
import org.geotoolkit.wms.WebMapClient;
import org.geotoolkit.wms.xml.WMSVersion;
import org.geotoolkit.wmts.WebMapTileClient;
import org.geotoolkit.wmts.xml.WMTSVersion;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;


/**
 * Allow to convert a {@linkplain org.constellation.provider.coveragesgroup.xml.MapContext mapContext} from the
 * providers xml binding to a {@linkplain MapContext map context} from the map module.
 *
 * @author Cédric Briançon (Geomatys)
 * @author Quentin Boileau (Geomatys)
 */
public final class ConvertersJaxbToGeotk {

    private static final Logger LOGGER = Logging.getLogger(ConvertersJaxbToGeotk.class);
    public static final String ORIGINAL_CONFIG = "original_config";

    public static MapItem convertsMapLayer(final org.constellation.provider.coveragesgroup.xml.MapLayer mapLayer, final String login, final String password) {
        ArgumentChecks.ensureNonNull("mapLayer", mapLayer);
        ArgumentChecks.ensureNonNull("dataReference", mapLayer.getDataReference());

        final String dataReference = mapLayer.getDataReference().getValue();
        final DataReference ref = new DataReference(dataReference);

        Object obj;
        try {
            obj = convertsDataReferenceToCoverageReference(ref, login, password);
        } catch (UnconvertibleObjectException e) {
            obj = null;
        }

        //try in features
        if (obj == null) {
            try {
                obj = convertsDataReferenceToFeatureCollection(ref);
            } catch (UnconvertibleObjectException e) {
                obj = null;
                LOGGER.log(Level.WARNING, "No data found for the given DataReference {0}.",ref.getReference());
            }
        }

        MutableStyle style = null;
        if (mapLayer.getStyleReference() != null) {
            final String styleReference = mapLayer.getStyleReference().getValue();
            style = convertsDataReferenceToMutableStyle(styleReference);
        }

        final double opacity = (mapLayer.getOpacity() != null && mapLayer.getOpacity() <= 1 && mapLayer.getOpacity() >= 0) ? mapLayer.getOpacity() : 1;
        if (obj instanceof FeatureCollection) {
            final FeatureMapLayer layer = MapBuilder.createFeatureLayer((FeatureCollection) obj, style);
            layer.setOpacity(opacity);
            return layer;

        } else if (obj instanceof CoverageReference) {
            final CoverageMapLayer layer = MapBuilder.createCoverageLayer((CoverageReference) obj, style);
            layer.setOpacity(opacity);
            return layer;
        }

        final EmptyMapLayer emptyLayer = MapBuilder.createEmptyMapLayer();
        emptyLayer.setName(dataReference);
        emptyLayer.setOpacity(opacity);
        return emptyLayer;
    }

    public static MapItem convertsMapItem(final org.constellation.provider.coveragesgroup.xml.MapItem mapItem, final String login, final String password) {
        final MapItem mi = MapBuilder.createItem();
        for (org.constellation.provider.coveragesgroup.xml.MapItem currentMapItem : mapItem.getMapItems()) {
            if (currentMapItem instanceof org.constellation.provider.coveragesgroup.xml.MapLayer) {
                final MapItem layer = convertsMapLayer((org.constellation.provider.coveragesgroup.xml.MapLayer)currentMapItem, login, password);
                if (layer != null) {
                    layer.setUserProperty(ORIGINAL_CONFIG, currentMapItem);
                    mi.items().add(layer);
                }
            } else {
                mi.items().add(convertsMapItem(currentMapItem, login, password));
            }
        }
        return mi;
    }

    public static MapContext convertsMapContext(final org.constellation.provider.coveragesgroup.xml.MapContext mapContext, final String login, final String password) {
        final MapContext mc = MapBuilder.createContext();
        mc.setName(mapContext.getName());
        mc.items().add(convertsMapItem(mapContext.getMapItem(), login, password));
        return mc;
    }

    /**
     * Get MutableStyle from DataReference or a StyleReference.
     * @param styleRefStr
     * @return MutableStyle from DataReference if found, or default MutableStyle.
     */
    private static MutableStyle convertsDataReferenceToMutableStyle(final String styleRefStr) {

        String providerID = null;
        String styleName = null;
        MutableStyle style = null;

        try {
            DataReference dataReference = new DataReference(styleRefStr);
            providerID = dataReference.getProviderOrServiceId();
            styleName = dataReference.getLayerId().getLocalPart();
        } catch (IllegalArgumentException ex) {
            //style reference is a simple StyleReference which contain only the name of the style.
            styleName = styleRefStr;
        }

        final Collection<StyleProvider> providers = StyleProviders.getInstance().getProviders();
        for (StyleProvider provider : providers) {
            if (providerID != null) {
                if (provider.getId().equals(providerID)) {
                    style = provider.get(styleName);
                    break;
                }
            } else {
                if (provider.getKeys().contains(styleName)) {
                    style = provider.get(styleName);
                    break;
                }
            }
        }
        return style != null ? style : new DefaultStyleFactory().style();
    }

    public static CoverageReference convertsDataReferenceToCoverageReference(final DataReference source, final String login, final String password) throws UnconvertibleObjectException {
        final String dataType = source.getDataType();
        final Date dataVersion = source.getDataVersion();

        if (dataType.equals(DataReference.PROVIDER_STYLE_TYPE)) {
            throw new UnconvertibleObjectException("Style provider not supported.");
        }

        CoverageReference coverageReference = null;
        final Name layerName = source.getLayerId();

        /*
         * Search in Provider layers
         */
        if (dataType.equals(DataReference.PROVIDER_LAYER_TYPE)) {
            final String providerID = source.getProviderOrServiceId();

            boolean providerFound = false;
            boolean providerLayerFound = false;

            //find provider
            final Collection<DataProvider> providers = DataProviders.getInstance().getProviders();
            for (DataProvider provider : providers) {
                if (provider.getId().equals(providerID)) {
                    providerFound = true;

                    //open store with provider config
                    ParameterValueGroup config = provider.getSource();
                    config = config.groups("choice").get(0);
                    ParameterValueGroup factoryconfig = null;
                    for(GeneralParameterValue val : config.values()){
                        if(val instanceof ParameterValueGroup){
                            factoryconfig = (ParameterValueGroup) val;
                            break;
                        }
                    }
                    try {
                        final CoverageStore store = CoverageStoreFinder.open(factoryconfig);
                        if (store != null && store.handleVersioning()) {
                            final VersionControl control = store.getVersioning(layerName);
                            if (control.isVersioned() && dataVersion != null) {
                                coverageReference = store.getCoverageReference(layerName, control.getVersion(dataVersion));
                            }
                        }
                        if (store != null && coverageReference == null) {
                            coverageReference = store.getCoverageReference(layerName);
                        }

                    } catch (VersioningException e) {
                        throw new UnconvertibleObjectException(e.getMessage(), e);
                    } catch (DataStoreException e) {
                        throw new UnconvertibleObjectException(e.getMessage(), e);
                    }

                    if (coverageReference != null) {
                        providerLayerFound = true;
                        break;
                    }
                }
            }

            if (!providerFound) {
                throw new UnconvertibleObjectException("Provider id "+providerID+" not found.");
            }
            if (!providerLayerFound) {
                throw new UnconvertibleObjectException("Layer name "+layerName+" not found.");
            }


            /*
             * Search in Services
             */
        } else if (dataType.equals(DataReference.SERVICE_TYPE)) {
            final String serviceURL = source.getServiceURL();
            final String serviceSpec = source.getServiceSpec();

            if (serviceSpec.equalsIgnoreCase("WMS") || serviceSpec.equalsIgnoreCase("WMTS")) {

                if (serviceURL != null) {
                    try {

                        final ClientSecurity security;
                        if (login != null && password != null) {
                            security = new BasicAuthenticationSecurity(login, password);
                        } else {
                            security = null;
                        }

                        final Client server;
                        if(serviceSpec.equalsIgnoreCase("WMS")){
                            server = new WebMapClient(new URL(serviceURL), security, WMSVersion.v130);
                        }else{
                            server = new WebMapTileClient(new URL(serviceURL), security, WMTSVersion.v100);
                        }

                        if (server instanceof CoverageStore) {
                            final CoverageStore coverageStore = (CoverageStore) server;
                            if (coverageStore != null && coverageStore.handleVersioning()) {
                                final VersionControl control = coverageStore.getVersioning(layerName);
                                if (control.isVersioned() && dataVersion != null) {
                                    coverageReference = coverageStore.getCoverageReference(layerName,  control.getVersion(dataVersion));
                                }
                            }
                            if (coverageStore != null && coverageReference == null) {
                                coverageReference = coverageStore.getCoverageReference(layerName);
                            }

                        } else {
                            throw new UnconvertibleObjectException("Server is not a coverageStore.");
                        }

                    } catch (VersioningException e) {
                        throw new UnconvertibleObjectException(e.getMessage(), e);
                    } catch (DataStoreException ex) {
                        throw new UnconvertibleObjectException(ex);
                    } catch (MalformedURLException ex) {
                        throw new UnconvertibleObjectException(ex);
                    }
                } else {
                    throw new UnconvertibleObjectException("Service URL unknow.");
                }
            } else {
                throw new UnconvertibleObjectException("Service specification should be a WMS or WMTS service.");
            }
        }

        if (coverageReference == null) {
            throw new UnconvertibleObjectException("Data no found for : "+source.getReference());
        }

        return new CoverageReferenceWrapper(source.getReference(), coverageReference);
    }

    public static FeatureCollection convertsDataReferenceToFeatureCollection(final DataReference source) throws UnconvertibleObjectException {
        final String dataType = source.getDataType();
        final Date dataVersion = source.getDataVersion();

        if (dataType.equals(DataReference.PROVIDER_STYLE_TYPE)) {
            throw new UnconvertibleObjectException("Style provider not supported.");
        }

        FeatureCollection featureColl = null;
        final Name layerName = source.getLayerId();

        // build query
        final QueryBuilder builder = new QueryBuilder();
        builder.setTypeName(layerName);
        if (dataVersion != null) {
            builder.setVersionDate(dataVersion);
        }
        final Query query =  builder.buildQuery();
        /*
         * Search in Provider layers
         */
        if (dataType.equals(DataReference.PROVIDER_LAYER_TYPE)) {
            final String providerID = source.getProviderOrServiceId();

            boolean providerFound = false;
            boolean providerLayerFound = false;

            //find provider
            final Collection<DataProvider> providers = DataProviders.getInstance().getProviders();
            for (DataProvider provider : providers) {
                if (provider.getId().equals(providerID)) {
                    providerFound = true;

                    final Data layerDetails = provider.get(layerName);
                    if (layerDetails != null && layerDetails instanceof FeatureData) {

                        final FeatureData fLayerDetail = (FeatureData) layerDetails;
                        final FeatureStore store = fLayerDetail.getStore();
                        if (store != null) {
                            final Session session = store.createSession(true);
                            featureColl = session.getFeatureCollection(query);
                            providerLayerFound = true;
                            break;
                        }
                    }
                }
            }

            if (!providerFound) {
                throw new UnconvertibleObjectException("Provider id "+providerID+" not found.");
            }
            if (!providerLayerFound) {
                throw new UnconvertibleObjectException("Layer name "+layerName+" not found.");
            }


            /*
             * Search in Services
             */
        } else if (dataType.equals(DataReference.SERVICE_TYPE)) {
            final String serviceURL = source.getServiceURL();
            final String serviceSpec = source.getServiceSpec();
            final String serviceId = source.getProviderOrServiceId();

            if (serviceSpec.equalsIgnoreCase("WFS")) {
                if (serviceURL != null) {
                    try {

                        final Client server = new WebFeatureClient(new URL(serviceURL), ServiceDef.WFS_1_1_0.version.toString());
                        if (server instanceof FeatureStore) {
                            final FeatureStore datastore = (FeatureStore) server;
                            final Session session = datastore.createSession(true);
                            featureColl = session.getFeatureCollection(query);
                        } else {
                            throw new UnconvertibleObjectException("Server is not a datastore.");
                        }
                    } catch (MalformedURLException ex) {
                        throw new UnconvertibleObjectException(ex);
                    }
                } else {
                    throw new UnconvertibleObjectException("Service URL unknow.");
                }
            } else {
                throw new UnconvertibleObjectException("Service specification should be a WFS service.");
            }
        }

        if (featureColl == null) {
            throw new UnconvertibleObjectException("Data no found for : "+source.getReference());
        }

        return new ReferenceCollectionWrapper(featureColl, source.getReference());
    }

    /**
     * Private internal class that wrap a FeatureCollection into another with a specified identifier.
     */
    private static class ReferenceCollectionWrapper extends WrapFeatureCollection {

        private String referenceId;

        public ReferenceCollectionWrapper(final FeatureCollection<?> originalFC, final String collectionId) {
            super(originalFC);
            this.referenceId = collectionId;
        }

        @Override
        public String getID() {
            return referenceId;
        }

        @Override
        protected Feature modify(Feature original) throws FeatureStoreRuntimeException {
            return original;
        }
    }

    /**
     * Class that wrap a CoverageReference into another with a specified identifier.
     *
     * @author Quentin Boileau (Geomatys)
     */
    private static class CoverageReferenceWrapper extends AbstractCoverageReference {

        private final CoverageReference reference;
        private final String name;

        public CoverageReferenceWrapper(final String name, final CoverageReference ref) {
            super(ref.getStore(), new DefaultName(null, name));
            this.name = name;
            this.reference = ref;
        }

        @Override
        public GridCoverageReader acquireReader() throws CoverageStoreException {
            return reference.acquireReader();
        }

        @Override
        public GridCoverageWriter acquireWriter() throws CoverageStoreException {
            return reference.acquireWriter();
        }

        @Override
        public boolean isWritable() throws DataStoreException {
            return reference.isWritable();
        }

        @Override
        public Image getLegend() throws DataStoreException {
            return reference.getLegend();
        }

        @Override
        public int getImageIndex() {
            return reference.getImageIndex();
        }
    }
}
