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

package org.constellation.provider.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.apache.sis.storage.DataStore;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.admin.dao.DataRecord;
import org.constellation.admin.dao.ProviderRecord;
import org.constellation.admin.dao.StyleRecord;
import org.constellation.configuration.ConfigurationException;
import org.constellation.dto.CoverageMetadataBean;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.provider.*;
import org.constellation.util.MetadataMapBuilder;
import org.constellation.util.SimplyMetadataTreeNode;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.image.io.metadata.SpatialMetadata;
import org.geotoolkit.image.io.metadata.SpatialMetadataFormat;
import org.geotoolkit.style.MutableFeatureTypeStyle;
import org.geotoolkit.style.MutableRule;
import org.geotoolkit.style.MutableStyle;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.style.RasterSymbolizer;
import org.opengis.style.Symbolizer;
import org.w3c.dom.Node;

/**
 *
 * @author Ghuillem Legal (Geomatys)
 * @author Johann Sorel (Geomatys)
 */
public final class DefaultConfigurator implements Configurator {

    public DefaultConfigurator() {
    }

    @Override
    public List<Map.Entry<String, ParameterValueGroup>> getProviderConfigurations() throws ConfigurationException {
        final List<String> ids = ConfigurationEngine.getProviderIds();
        final List<Map.Entry<String,ParameterValueGroup>> entries = new ArrayList<>();
        for(String id : ids){
            ParameterValueGroup param = getProviderConfiguration(id);
            if(param!=null){
                entries.add(new AbstractMap.SimpleImmutableEntry<>(id, param));
            }
        }
        return entries;
    }
    
    @Override
    public List<ProviderInformation> getProviderInformations() throws ConfigurationException {
        final List<ProviderRecord> records = ConfigurationEngine.getProviders();
        final List<ProviderInformation> entries = new ArrayList<>();
        for(ProviderRecord record : records){
            ParameterValueGroup param = getProviderConfiguration(record.getIdentifier());
            if(param!=null){
                entries.add(new ProviderInformation(record.getIdentifier(), record.getImpl(), param));
            }
        }
        return entries;
    }
    
    @Override
    public ParameterValueGroup getProviderConfiguration(String providerId) throws ConfigurationException {
        final ProviderRecord record = ConfigurationEngine.getProvider(providerId);
        final String impl = record.getImpl();
        ProviderFactory factory = DataProviders.getInstance().getFactory(impl);
        if(factory==null) factory = StyleProviders.getInstance().getFactory(impl);
        if(factory==null) return null;
        try {
            ParameterValueGroup params = (ParameterValueGroup)record.getConfig(factory.getProviderDescriptor());
            return params;
        } catch (SQLException | IOException ex) {
            throw new ConfigurationException(ex);
        }
    }

    @Override
    public void addProviderConfiguration(String providerId, ParameterValueGroup config) throws ConfigurationException {
        
        Provider provider = DataProviders.getInstance().getProvider(providerId);
        if(provider==null) provider = StyleProviders.getInstance().getProvider(providerId);
        final ProviderRecord.ProviderType type = provider.getProviderType();
        final String factoryName = provider.getFactory().getName();
        
        final ProviderRecord pr = ConfigurationEngine.writeProvider(providerId, null, type, factoryName, config);
        try {
            checkDataUpdate(pr);
        } catch (SQLException | IOException ex) {
            throw new ConfigurationException(ex);
        }
    }

    private void checkDataUpdate(final ProviderRecord pr) throws SQLException, IOException{
        
        final List<DataRecord> list = pr.getData();
        final ProviderRecord.ProviderType type = pr.getType();
        if (type == ProviderRecord.ProviderType.LAYER) {
            final Provider provider = DataProviders.getInstance().getProvider(pr.getIdentifier());
            
            // Remove no longer existing layer.
            final Map<String, InputStream> metadata = new HashMap<>(0);
            for (final DataRecord data : list) {
                boolean found = false;
                for (final Object keyObj : provider.getKeys()) {
                    final Name key = (Name) keyObj;
                    if (data.getName().equals(key.getLocalPart())) {
                        found = true;
                        break;
                    } else if (key.getLocalPart().contains(data.getName()) && data.getProvider().getIdentifier().equalsIgnoreCase(provider.getId())) {
                        //save metadata
                        metadata.put(key.getLocalPart(), data.getMetadata());
                    }
                }
                if (!found) {
                    ConfigurationEngine.deleteData(data.getCompleteName(), provider.getId());
                }
            }
            // Add new layer.
            for (final Object keyObj : provider.getKeys()) {
                final Name key = (Name) keyObj;
                final QName name = new QName(key.getNamespaceURI(), key.getLocalPart());
                boolean found = false;
                for (final DataRecord data : list) {
                    if (name.equals(data.getCompleteName())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    DataRecord record = ConfigurationEngine.writeData(name, pr, provider.getDataType(), provider.isSensorAffectable());

                    // Subtype
                    final DataProvider dp = DataProviders.getInstance().getProvider(provider.getId());
                    final DataStore store = dp.getMainStore();
                    if (store instanceof FeatureStore) {
                        final FeatureStore fs = (FeatureStore)store;
                        FeatureType fType = null;
                        try {
                            fType = fs.getFeatureType(new DefaultName(name));
                        } catch (DataStoreException ex) {
                            LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
                        }
                        if (fType != null && fType.getGeometryDescriptor() != null && fType.getGeometryDescriptor().getType() != null &&
                                fType.getGeometryDescriptor().getType().getBinding() != null)
                        {
                            final String subtype = fType.getGeometryDescriptor().getType().getBinding().getSimpleName();
                            ConfigurationEngine.updateDataSubtype(name, provider.getId(), subtype);
                        } else {
                            // A feature that does not contain geometry, we hide it
                            ConfigurationEngine.updateDataVisibility(name, provider.getId(), false);
                        }
                    }

                    // Metadata
                    final InputStream currentMetadata = metadata.get(record.getName());
                    if (currentMetadata != null) {
                        StringWriter writer = new StringWriter();
                        IOUtils.copy(currentMetadata, writer);
                        StringReader reader = new StringReader(writer.toString());
                        record.setMetadata(reader);
                    } else {
                        final Data layer = (Data) provider.get(new DefaultName(name));
                        final Object origin = layer.getOrigin();
                        if (origin instanceof CoverageReference) {
                            final CoverageReference fcr = (CoverageReference) origin;
                            try {
                                int i = fcr.getImageIndex();
                                final GridCoverageReader reader = fcr.acquireReader();
                                final SpatialMetadata sm = reader.getCoverageMetadata(i);
                                if (sm != null) {
                                    final Node coverageRootNode = sm.getAsTree(SpatialMetadataFormat.GEOTK_FORMAT_NAME);
                                    MetadataMapBuilder.setCounter(0);
                                    final List<SimplyMetadataTreeNode> coverageMetadataList = MetadataMapBuilder.createSpatialMetadataList(coverageRootNode, null, 11, i);
                                    final CoverageMetadataBean coverageMetadataBean = new CoverageMetadataBean(coverageMetadataList);
                                    final MarshallerPool mp = GenericDatabaseMarshallerPool.getInstance();
                                    final Marshaller marshaller = mp.acquireMarshaller();
                                    final StringWriter sw = new StringWriter();
                                    marshaller.marshal(coverageMetadataBean, sw);
                                    mp.recycle(marshaller);
                                    final StringReader sr = new StringReader(sw.toString());
                                    record.setMetadata(sr);
                                }
                            } catch (CoverageStoreException | JAXBException e) {
                                LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
                            }
                        }
                    }
                }
            }
        } else {
            final Provider provider = StyleProviders.getInstance().getProvider(pr.getIdentifier());
            
            final List<StyleRecord> styles = pr.getStyles();
            // Remove no longer existing style.
            for (final StyleRecord style : styles) {
                if (provider.get(style.getName()) == null) {
                    ConfigurationEngine.deleteStyle(style.getName(), provider.getId());
                }
            }
            // Add not registered new data.
            for (final Object key : provider.getKeys()) {
                boolean found = false;
                for (final StyleRecord style : styles) {
                    if (key.equals(style.getName())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    StyleRecord.StyleType styleType = StyleRecord.StyleType.VECTOR;
                    MutableStyle style = (MutableStyle) provider.get(key);
                    fts:
                    for (MutableFeatureTypeStyle mutableFeatureTypeStyle : style.featureTypeStyles()) {
                        for (MutableRule mutableRule : mutableFeatureTypeStyle.rules()) {
                            for (Symbolizer symbolizer : mutableRule.symbolizers()) {
                                if (symbolizer instanceof RasterSymbolizer) {
                                    styleType = StyleRecord.StyleType.COVERAGE;
                                    break fts;
                                }
                            }
                        }
                    }
                    ConfigurationEngine.writeStyle((String) key, pr, styleType, style);
                }
            }
        }
    }
    
    @Override
    public void updateProviderConfiguration(String providerId, ParameterValueGroup config) throws ConfigurationException {
        final ProviderRecord pr = ConfigurationEngine.getProvider(providerId);
        try {
            pr.setConfig(config);
            checkDataUpdate(pr);
        } catch (SQLException | IOException ex) {
            throw new ConfigurationException(ex);
        }
    }

    @Override
    public void removeProviderConfiguration(String providerId) throws ConfigurationException {
        ConfigurationEngine.deleteProvider(providerId);
    }
        
}
