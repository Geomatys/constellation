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
import java.io.StringWriter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import org.apache.sis.storage.DataStore;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.UnconvertibleObjectException;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.admin.SpringHelper;
import org.constellation.admin.exception.ConstellationException;
import org.constellation.api.DataType;
import org.constellation.api.PropertyConstants;
import org.constellation.api.ProviderType;
import org.constellation.api.StyleType;
import org.constellation.business.IDataBusiness;
import org.constellation.business.IDataCoverageJob;
import org.constellation.business.IDatasetBusiness;
import org.constellation.business.IProviderBusiness;
import org.constellation.business.IStyleBusiness;
import org.constellation.configuration.ConfigurationException;
import org.constellation.dto.CoverageMetadataBean;
import org.constellation.engine.register.Dataset;
import org.constellation.engine.register.Style;
import org.constellation.engine.register.repository.DatasetRepository;
import org.constellation.engine.register.repository.PropertyRepository;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.provider.Data;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviders;
import org.constellation.provider.Provider;
import org.constellation.provider.ProviderFactory;
import org.constellation.provider.StyleProviders;
import org.constellation.util.MetadataMapBuilder;
import org.constellation.util.ParamUtilities;
import org.constellation.util.SimplyMetadataTreeNode;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.PyramidalCoverageReference;
import org.geotoolkit.coverage.grid.ViewType;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.feature.type.DefaultName;
import org.geotoolkit.feature.type.FeatureType;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.image.io.metadata.SpatialMetadata;
import org.geotoolkit.image.io.metadata.SpatialMetadataFormat;
import org.geotoolkit.style.MutableFeatureTypeStyle;
import org.geotoolkit.style.MutableRule;
import org.geotoolkit.style.MutableStyle;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.style.RasterSymbolizer;
import org.opengis.style.Symbolizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Node;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @author Johann Sorel (Geomatys)
 */
public final class DefaultConfigurator implements Configurator {

    @Autowired
    private IProviderBusiness providerBusiness;

    @Autowired
    private IDataBusiness dataBusiness;

    @Autowired
    private IStyleBusiness styleBusiness;

    @Autowired
    private IDataCoverageJob dataCoverageJob;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private IDatasetBusiness datasetBusiness;

    public DefaultConfigurator() {
        SpringHelper.injectDependencies(this);
    }

    @Override
    public List<Map.Entry<String, ParameterValueGroup>> getProviderConfigurations() throws ConfigurationException {
        final List<String> ids = providerBusiness.getProviderIds();
        final List<Map.Entry<String,ParameterValueGroup>> entries = new ArrayList<>();
        for (String id : ids) {
            try {
                ParameterValueGroup param = getProviderConfiguration(id);
                if (param != null) {
                    entries.add(new AbstractMap.SimpleImmutableEntry<>(id, param));
                }
            } catch (ConfigurationException ex) {
                LOGGER.log(Level.WARNING, "error while getting configuration for provider " + id, ex);
            }
        }
        return entries;
    }

    @Override
    public List<ProviderInformation> getProviderInformations() throws ConfigurationException {
        final List<org.constellation.engine.register.Provider> records = providerBusiness.getProviders();
        final List<ProviderInformation> entries = new ArrayList<>();
        for (org.constellation.engine.register.Provider record : records) {
            try {
                final ParameterValueGroup param = getProviderConfiguration(record.getIdentifier());
                if (param != null) {
                    entries.add(new ProviderInformation(record.getIdentifier(), record.getImpl(), param));
                }
            } catch (ConfigurationException ex) {
                LOGGER.log(Level.WARNING, "error while getting configuration for provider " + record.getIdentifier(), ex);
            }
        }
        return entries;
    }

    @Override
    public ParameterValueGroup getProviderConfiguration(String providerId) throws ConfigurationException {
        final org.constellation.engine.register.Provider record = providerBusiness.getProvider(providerId);
        final String impl = record.getImpl();
        ProviderFactory factory = DataProviders.getInstance().getFactory(impl);
        if(factory==null) factory = StyleProviders.getInstance().getFactory(impl);
        if(factory==null) return null;
        try {
            ParameterValueGroup params = (ParameterValueGroup) ParamUtilities.readParameter(record.getConfig(), factory.getProviderDescriptor());
            return params;
        } catch (IOException | UnconvertibleObjectException ex) {
            throw new ConfigurationException("Error while reading provider configuration for:" + providerId, ex);
        }
    }

    @Override
    @Transactional
    public void addProviderConfiguration(final String providerId,
                                         final ParameterValueGroup config,
                                         final Integer datasetId) throws ConfigurationException {
        addProviderConfiguration(providerId, config, datasetId,true);
    }

    @Override
    @Transactional
    public void addProviderConfiguration(final String providerId,
                                         final ParameterValueGroup config,
                                         final Integer datasetId,
                                         final boolean createDatasetIfNull)
            throws ConfigurationException {

        Provider provider = DataProviders.getInstance().getProvider(providerId);
        if(provider==null){
            provider = StyleProviders.getInstance().getProvider(providerId);
        }
        final ProviderType type = provider.getProviderType();
        final String factoryName = provider.getFactory().getName();

        try {
            final org.constellation.engine.register.Provider pr = providerBusiness.storeProvider(providerId, null, type, factoryName, config);
            checkDataUpdate(pr, datasetId, createDatasetIfNull);
        } catch ( IOException ex) {
            throw new ConfigurationException(ex);
        }
    }

    private void checkDataUpdate(final org.constellation.engine.register.Provider pr,
                                 final Integer datasetId) throws IOException, ConfigurationException{
        checkDataUpdate(pr,datasetId,true);
    }

    /**
     *
     * @param pr given provider
     * @param datasetId given dataset identifier to attach to data.
     * @param createDatasetIfNull flag that indicates if a dataset will be created in case of given datasetId is null.
     * @throws IOException
     */
    private void checkDataUpdate(final org.constellation.engine.register.Provider pr,
                                 Integer datasetId,
                                 final boolean createDatasetIfNull) throws IOException, ConfigurationException{

        final List<org.constellation.engine.register.Data> list = providerBusiness.getDatasFromProviderId(pr.getId());
        final String type = pr.getType();
        if (type.equals(ProviderType.LAYER.name())) {
            final DataProvider provider = DataProviders.getInstance().getProvider(pr.getIdentifier());

            if (datasetId == null) {
                final Dataset dataset = datasetRepository.findByIdentifier(pr.getIdentifier());
                if (dataset == null) {
                    if(createDatasetIfNull) {
                        datasetId = datasetBusiness.createDataset(pr.getIdentifier(), null, null, pr.getOwner()).getId();
                    }
                }else {
                    datasetId = dataset.getId();
                }
            }

            // Remove no longer existing layer.
            final Map<String, String> metadata = new HashMap<>(0);
            for (final org.constellation.engine.register.Data data : list) {
                boolean found = false;
                for (final Object keyObj : provider.getKeys()) {
                    final Name key = (Name) keyObj;
                    if (data.getName().equals(key.getLocalPart())) {
                        found = true;
                        break;
                    } else if (key.getLocalPart().contains(data.getName()) &&
                            providerBusiness.getProvider(data.getProvider()).getIdentifier().equalsIgnoreCase(provider.getId())) {
                        //save metadata
                        metadata.put(key.getLocalPart(), data.getMetadata());
                    }
                }
                if (!found) {
                    dataBusiness.missingData(new QName(data.getNamespace(), data.getName()), provider.getId());
                }
            }

            //check if layer analysis is required
            String propertyValue = propertyRepository.getValue(PropertyConstants.DATA_ANALYSE_KEY, null);
            boolean doAnalysis = propertyValue == null ? false : Boolean.valueOf(propertyValue);

            // Add new layer.
            for (final Name key : provider.getKeys()) {
                final QName name = new QName(key.getNamespaceURI(), key.getLocalPart());
                boolean found = false;
                for (final org.constellation.engine.register.Data data : list) {
                    if (name.equals(new QName(data.getNamespace(),data.getName()))) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    // Subtype and included
                    String subType  = null;
                    boolean included = true;
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
                        if (fType != null && fType.getGeometryDescriptor() != null &&
                                fType.getGeometryDescriptor().getType() != null &&
                                fType.getGeometryDescriptor().getType().getBinding() != null) {
                            subType = fType.getGeometryDescriptor().getType().getBinding().getSimpleName();
                        } else {
                            // A feature that does not contain geometry, we hide it
                            included = false;
                        }
                    }

                    // Metadata
                    String metadataXml = null;
                    final String currentMetadata = metadata.get(name.getLocalPart());
                    if (currentMetadata != null) {
                        metadataXml = currentMetadata;
                    } else {
                        final Data layer = (Data) provider.get(new DefaultName(name));
                        final Object origin = layer.getOrigin();
                        if (origin instanceof CoverageReference) {
                            final CoverageReference fcr = (CoverageReference) origin;
                            try {
                                int i = fcr.getImageIndex();
                                final GridCoverageReader reader = fcr.acquireReader();
                                final SpatialMetadata sm = reader.getCoverageMetadata(i);
                                fcr.recycle(reader);
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
                                    metadataXml = sw.toString();
                                }
                            } catch (CoverageStoreException | JAXBException e) {
                                LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
                            }
                        }
                    }

                    // find if data is rendered
                    Boolean rendered = null;
                    if (DataType.COVERAGE.equals(provider.getDataType())) {
                        Data providerData = provider.get(key);
                        Object origin = providerData.getOrigin();
                        if (origin instanceof PyramidalCoverageReference) {
                            try {
                                ViewType packMode = ((PyramidalCoverageReference) origin).getPackMode();
                                if (ViewType.RENDERED.equals(packMode)) {
                                    rendered = Boolean.TRUE;
                                } else {
                                    rendered = Boolean.FALSE;
                                }
                            } catch (DataStoreException e) {
                                LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
                            }
                            subType = "pyramid";
                        } else {
                            rendered = Boolean.FALSE;
                        }
                    }

                    //dataBusiness.create(name, pr.getIdentifier(), provider.getDataType().name(), provider.isSensorAffectable(), visible, subType, metadataXml);
                    //do not save the coverage metadata in database, this metadata is obsolete, the full iso metadata is stored later.
                    org.constellation.engine.register.Data data = dataBusiness.create(
                            name, pr.getIdentifier(), provider.getDataType().name(),
                            provider.isSensorAffectable(), included, rendered, subType, null);

                    if(datasetId != null) {
                        dataBusiness.updateDataDataSetId(name, pr.getIdentifier(), datasetId);
                    }
                }
            }
        } else {
            final Provider provider = StyleProviders.getInstance().getProvider(pr.getIdentifier());
            final List<Style> styles = providerBusiness.getStylesFromProviderId(pr.getId());
            // Remove no longer existing style.
            for (final Style style : styles) {
                if (provider.get(style.getName()) == null) {
                    try {
                        styleBusiness.deleteStyle(provider.getId(),style.getName());
                    } catch (ConfigurationException e) {
                        throw new ConstellationException(e);
                    }
                }
            }
            // Add not registered new data.
            for (final Object key : provider.getKeys()) {
                boolean found = false;
                for (final Style style : styles) {
                    if (key.equals(style.getName())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    StyleType styleType = StyleType.VECTOR;
                    MutableStyle style = (MutableStyle) provider.get(key);
                    fts:
                    for (MutableFeatureTypeStyle mutableFeatureTypeStyle : style.featureTypeStyles()) {
                        for (MutableRule mutableRule : mutableFeatureTypeStyle.rules()) {
                            for (Symbolizer symbolizer : mutableRule.symbolizers()) {
                                if (symbolizer instanceof RasterSymbolizer) {
                                    styleType = StyleType.COVERAGE;
                                    break fts;
                                }
                            }
                        }

                    }

                    styleBusiness.writeStyle((String) key, pr.getId(), styleType, style);
                }
            }
        }
    }

    @Override
    @Transactional
    public void updateProviderConfiguration(String providerId, ParameterValueGroup config) throws ConfigurationException {
        final org.constellation.engine.register.Provider pr = providerBusiness.getProvider(providerId);
        if (pr != null) {
            try {
                final String configString = ParamUtilities.writeParameter(config);
                pr.setConfig(configString);
                checkDataUpdate(pr, null, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //TODO throw exception ? log message ? create new provider ? do nothing ?
    }

    @Override
    @Transactional
    public void removeProviderConfiguration(String providerId) throws ConfigurationException {
        dataBusiness.removeDataFromProvider(providerId);
        providerBusiness.removeProvider(providerId);
    }

}
