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

package org.constellation.admin;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.ServiceDef;
import org.constellation.admin.exception.ConstellationException;
import org.constellation.admin.index.IndexEngine;
import org.constellation.admin.listener.DefaultDataBusinessListener;
import org.constellation.admin.listener.IDataBusinessListener;
import org.constellation.admin.util.ImageStatisticDeserializer;
import org.constellation.api.DataType;
import org.constellation.business.IDataBusiness;
import org.constellation.business.IDataCoverageJob;
import org.constellation.configuration.*;
import org.constellation.dto.CoverageMetadataBean;
import org.constellation.dto.FileBean;
import org.constellation.dto.ParameterValues;
import org.constellation.database.api.jooq.tables.pojos.CstlUser;
import org.constellation.database.api.jooq.tables.pojos.Data;
import org.constellation.database.api.jooq.tables.pojos.Dataset;
import org.constellation.database.api.jooq.tables.pojos.Layer;
import org.constellation.database.api.jooq.tables.pojos.Provider;
import org.constellation.database.api.jooq.tables.pojos.Service;
import org.constellation.database.api.jooq.tables.pojos.Style;
import org.constellation.database.api.repository.DataRepository;
import org.constellation.database.api.repository.DatasetRepository;
import org.constellation.database.api.repository.LayerRepository;
import org.constellation.database.api.repository.ProviderRepository;
import org.constellation.database.api.repository.SensorRepository;
import org.constellation.database.api.repository.StyleRepository;
import org.constellation.database.api.repository.UserRepository;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviders;
import org.constellation.token.TokenUtils;
import org.constellation.utils.GeotoolkitFileExtensionAvailable;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.metadata.ImageStatistics;
import org.geotoolkit.util.FileUtilities;
import org.opengis.feature.PropertyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.base.Optional;
import java.util.Collection;
import org.constellation.business.IMetadataBusiness;
import org.constellation.database.api.pojo.DataItem;
import org.constellation.database.api.repository.ServiceRepository;


/**
 * Business facade for data.
 *
 * @author Mehdi Sidhoum (Geomatys).
 * @version 0.9
 * @since 0.9
 */

@Component("cstlDataBusiness")
@DependsOn({"database-initer", "providerBusiness"})
@Primary
public class DataBusiness implements IDataBusiness {
    /**
     * Used for debugging purposes.
     */
    protected static final Logger LOGGER = LoggerFactory.getLogger(DataBusiness.class);
    /**
     * Injected user repository.
     */
    @Inject
    private UserRepository userRepository;
    /**
     * Injected data repository.
     */
    @Inject
    protected DataRepository dataRepository;
    /**
     * Injected layer repository.
     */
    @Inject
    private LayerRepository layerRepository;
    /**
     * Injected security manager.
     */
    @Inject
    private org.constellation.security.SecurityManager securityManager;
    /**
     * Injected style repository.
     */
    @Inject
    private StyleRepository styleRepository;
    /**
     * Injected provider repository.
     */
    @Inject
    private ProviderRepository providerRepository;
    /**
     * Injected dataset repository.
     */
    @Inject
    protected DatasetRepository datasetRepository;
    /**
     * Injected sensor repository.
     */
    @Inject
    private SensorRepository sensorRepository;
    /**
     * Injected metadata repository.
     */
    @Inject
    protected IMetadataBusiness metadataBusiness;
    /**
     * Injected lucene index engine.
     */
    @Inject
    protected IndexEngine indexEngine;

    /**
     * Injected data coverage job
     */
    @Inject
    private IDataCoverageJob dataCoverageJob;

    @Autowired(required = false)
    private IDataBusinessListener dataBusinessListener = new DefaultDataBusinessListener();

     /**
     * Injected service repository.
     */
    @Inject
    private ServiceRepository serviceRepository;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Provider getProvider(int dataId) {
        final Data d = dataRepository.findById(dataId);
        return providerRepository.findOne(d.getProvider());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DefaultMetadata loadIsoDataMetadata(final String providerId,
                                               final QName name) throws ConfigurationException {
        final Data data = dataRepository.findDataFromProvider(name.getNamespaceURI(), name.getLocalPart(), providerId);
        return metadataBusiness.getIsoMetadataForData(data.getId());
    }


    @Override
    public Dataset getDatasetForData(final String providerId, final QName name) throws ConstellationException{
        final Data data = dataRepository.findDataFromProvider(name.getNamespaceURI(), name.getLocalPart(), providerId);
        if (data != null && data.getDatasetId() != null) {
            return datasetRepository.findById(data.getDatasetId());
        }
        return null;
    }

    @Override
    public Dataset getDatasetForData(final int dataId) throws ConstellationException{
        final Data data = dataRepository.findById(dataId);
        if (data != null && data.getDatasetId() != null) {
            return datasetRepository.findById(data.getDatasetId());
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Data> searchOnMetadata(final String query) throws IOException, ConstellationException {
        final List<Data> result = new ArrayList<>();
        final Set<Integer> ids;
        try {
            ids = indexEngine.searchOnMetadata(query, "dataId");
        } catch( ParseException ex) {
            throw new ConstellationException(ex);
        }
        for (final Integer dataId : ids){
            final Data d = dataRepository.findById(dataId);
            if(d!=null){
                result.add(d);
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CoverageMetadataBean loadDataMetadata(final String providerId,
                                                 final QName name,
                                                 final MarshallerPool pool) throws ConstellationException {
        try {
            final Data data = dataRepository.findDataFromProvider(name.getNamespaceURI(), name.getLocalPart(), providerId);
            if (data != null && data.getMetadata() != null) {
                final InputStream sr = new ByteArrayInputStream(data.getMetadata().getBytes());
                final Unmarshaller m = pool.acquireUnmarshaller();
                final CoverageMetadataBean metadata = (CoverageMetadataBean) m.unmarshal(sr);
                pool.recycle(m);
                return metadata;
            }
        } catch (JAXBException ex) {
            LOGGER.warn("An error occurred while updating service database", ex);
            throw new ConstellationException(ex);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataBrief getDataBrief(QName dataName,Integer providerId) throws ConstellationException {
        final Data data = dataRepository.findByNameAndNamespaceAndProviderId(dataName.getLocalPart(), dataName.getNamespaceURI(), providerId);
        final List<Data> datas = new ArrayList<>();
        datas.add(data);
        final List<DataBrief> dataBriefs = getDataBriefFrom(datas);
        if (dataBriefs != null && dataBriefs.size() == 1) {
            return dataBriefs.get(0);
        }
        throw new ConstellationException(new Exception("Problem : DataBrief Construction is null or multiple"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataBrief getDataBrief(final QName fullName,
                                  final String providerIdentifier) throws ConstellationException {
        final Data data = dataRepository.findDataFromProvider(fullName.getNamespaceURI(), fullName.getLocalPart(), providerIdentifier);
        final List<Data> datas = new ArrayList<>();
        if (data != null) {
            datas.add(data);
            final List<DataBrief> dataBriefs = getDataBriefFrom(datas);
            if (dataBriefs != null && dataBriefs.size() == 1) {
                return dataBriefs.get(0);
            }
        }
        throw new ConstellationException(new Exception("Problem : DataBrief Construction is null or multiple"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DataBrief> getDataBriefsFromMetadataId(final String metadataId) {
        final List<Data> datas = findByMetadataId(metadataId);
        return getDataBriefFrom(datas);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataBrief getDataLayer(final String layerAlias,
                                  final String providerId) throws ConstellationException {
        final Data data = layerRepository.findDatasFromLayerAlias(layerAlias, providerId);
        final List<Data> datas = new ArrayList<>();
        datas.add(data);
        final List<DataBrief> dataBriefs = getDataBriefFrom(datas);
        if (dataBriefs != null && !dataBriefs.isEmpty()){
            return dataBriefs.get(0);
        }
        throw new ConstellationException(new Exception("Problem : DataBrief Construction is null or multiple"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Data> findByDatasetId(final Integer datasetId) {
        return dataRepository.findByDatasetId(datasetId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DataBrief> getDataBriefsFromDatasetId(final Integer datasetId) {
        final List<Data> dataList = findByDatasetId(datasetId);
        return getDataBriefFrom(dataList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Data> findByStyleId(final Integer styleId) {
        return dataRepository.getFullDataByLinkedStyle(styleId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DataBrief> getDataBriefsFromStyleId(final Integer styleId) {
        final List<Data> dataList = dataRepository.getRefDataByLinkedStyle(styleId);
        if (dataList != null) {
            return getDataBriefFrom(dataList);
        }
        return new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DataBrief> getDataRefsFromStyleId(final Integer styleId) {
        final List<Data> dataList = dataRepository.getRefDataByLinkedStyle(styleId);
        if (dataList != null) {
            return toDataRef(dataList);
        }
        return new ArrayList<>();
    }


    /**
     * Returns a list of {@link Data} for given metadata identifier.
     * @param metadataId given metadata identifier.
     * @return list of {@link Data}.
     */
    private List<Data> findByMetadataId(final String metadataId) {
        List<Data> dataResult   = new ArrayList<>();
        final Dataset dataset   = datasetRepository.findByMetadataId(metadataId);
        final Data data         = dataRepository.findByMetadataId(metadataId);
        final Service service   = serviceRepository.findByMetadataId(metadataId);
        if (dataset != null){
            dataResult = dataRepository.findByDatasetId(dataset.getId());
        } else if (service!= null) {
            dataResult = serviceRepository.findDataByServiceId(service.getId());
        } else if (data != null) {
            dataResult.add(data);
        }
        return dataResult;
    }

    /**
     * Convert a Data to DataBrief using only fields :
     * <ul>
     *     <li>id</li>
     *     <li>name</li>
     *     <li>namespace</li>
     *     <li>provider</li>
     *     <li>type</li>
     *     <li>subtype</li>
     * </ul>
     * @param dataList data to convert
     * @return
     */
    protected List<DataBrief> toDataRef(List<Data> dataList) {
        final List<DataBrief> dataBriefs = new ArrayList<>();

        for (final Data data : dataList) {
            final String providerId = getProviderIdentifier(data.getProvider());
            final DataBrief db = new DataBrief();
            db.setId(data.getId());
            db.setName(data.getName());
            db.setNamespace(data.getNamespace());
            db.setProvider(providerId);
            db.setType(data.getType());
            db.setSubtype(data.getSubtype());
            dataBriefs.add(db);
        }
        return dataBriefs;
    }

    /**
     * Convert a list of {@link Data} to list of {@link DataBrief}.
     * @param datas given list of {@link Data}.
     * @return the list of {@link DataBrief}.
     */
    protected List<DataBrief> getDataBriefFrom(final List<Data> datas) {
        final List<DataBrief> dataBriefs = new ArrayList<>();
        for (final Data data : datas) {
            final List<Style> styles = styleRepository.findByData(data);

            final DataBrief db = new DataBrief();
            db.setId(data.getId());
            final Optional<CstlUser> user = userRepository.findById(data.getOwner());
            if (user != null && user.isPresent()) {
                final CstlUser cstlUser = user.get();
                if(cstlUser!=null){
                    db.setOwner(cstlUser.getLogin());
                }
            }
            final String providerId = getProviderIdentifier(data.getProvider());
            db.setName(data.getName());
            db.setNamespace(data.getNamespace());
            db.setDate(new Date(data.getDate()));
            db.setProvider(providerId);
            db.setDatasetId(data.getDatasetId());
            db.setType(data.getType());
            db.setSubtype(data.getSubtype());
            db.setSensorable(data.getSensorable());
            db.setTargetSensor(sensorRepository.getLinkedSensors(data));
            db.setStatsResult(data.getStatsResult());
            db.setStatsState(data.getStatsState());
            db.setRendered(data.getRendered());
            db.setMdCompletion(metadataBusiness.getCompletionForData(data.getId()));

            final List<Data> linkedDataList = getDataLinkedData(data.getId());
            for(final Data d : linkedDataList){
                if("pyramid".equalsIgnoreCase(d.getSubtype()) &&
                        !d.getRendered()){
                    final String pyramidProvId = getProviderIdentifier(d.getProvider());
                    db.setPyramidConformProviderId(pyramidProvId);
                    break;
                }
            }
            //if the data is a pyramid itself. we need to fill the property to enable the picto of pyramided data.
            if("pyramid".equalsIgnoreCase(data.getSubtype()) && !data.getRendered()){
                db.setPyramidConformProviderId(providerId);
            }

            final List<StyleBrief> styleBriefs = new ArrayList<>(0);
            for (final Style style : styles) {
                final StyleBrief sb = new StyleBrief();
                sb.setId(style.getId());
                sb.setType(style.getType());
                sb.setProvider(getProviderIdentifier(style.getProvider()));
                sb.setDate(new Date(style.getDate()));
                sb.setName(style.getName());

                final Optional<CstlUser> userStyle = userRepository.findById(style.getOwner());
                if (userStyle!=null && userStyle.isPresent()) {
                    final CstlUser cstlUser = userStyle.get();
                    if(cstlUser!=null){
                        sb.setOwner(cstlUser.getLogin());
                    }
                }
                styleBriefs.add(sb);
            }
            db.setTargetStyle(styleBriefs);

            final List<Service> services = serviceRepository.findByDataId(data.getId());
            for(final Data d : linkedDataList){
                final List<Service> servicesLinked = serviceRepository.findByDataId(d.getId());
                services.addAll(servicesLinked);
            }
            
            // add csw link
            
            
            //use HashSet to avoid duplicated objects.
            final Set<ServiceProtocol> serviceProtocols = new HashSet<>();
            for (final Service service : services) {
                final List<String> protocol = new ArrayList<>();
                final ServiceDef.Specification spec = ServiceDef.Specification.valueOf(service.getType().toUpperCase());
                protocol.add(spec.name());
                protocol.add(spec.fullName);
                final ServiceProtocol sp = new ServiceProtocol(service.getIdentifier(), protocol);
                serviceProtocols.add(sp);
            }
            db.setTargetService(new ArrayList<>(serviceProtocols));
            dataBriefs.add(db);
        }
        return dataBriefs;
    }

    /**
     * Returns provider identifier for given provider id.
     * @param providerId given provider id.
     * @return provider identifier as string.
     */
    private String getProviderIdentifier(final int providerId) {
        return providerRepository.findOne(providerId).getIdentifier();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void missingData(final QName name, final String providerIdentifier) throws ConfigurationException {
        final Provider provider = providerRepository.findByIdentifier(providerIdentifier);
        if (provider != null) {
            final Data data = dataRepository.findByNameAndNamespaceAndProviderId(name.getLocalPart(), name.getNamespaceURI(), provider.getId());
            if (data != null) {
                // remove data metadata from index
                indexEngine.removeDataMetadataFromIndex(data.getId());

                // delete data entry
                metadataBusiness.deleteDataMetadata(data.getId());
                dataBusinessListener.preDataDelete(data);
                dataRepository.delete(data.getId());
                dataBusinessListener.postDataDelete(data);

                // Relevant erase dataset when the is no more data in it. fr now we remove it
                deleteDatasetIfEmpty(data.getDatasetId());
            }
        }
    }

    protected void deleteDatasetIfEmpty(Integer datasetID) throws ConfigurationException {
        if (datasetID != null) {
            List<Data> datas = dataRepository.findAllByDatasetId(datasetID);
            if (datas.isEmpty()) {
                indexEngine.removeDatasetMetadataFromIndex(datasetID);
                metadataBusiness.deleteDatasetMetadata(datasetID);
                datasetRepository.remove(datasetID);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void removeData(Integer dataId) throws ConfigurationException {
        final List<Data> linkedDataList = getDataLinkedData(dataId);
        for(final Data d : linkedDataList){
            updateDataIncluded(d.getId(), false);
        }
        updateDataIncluded(dataId, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteAll() throws ConfigurationException {
        final List<Data> datas = dataRepository.findAll();
        for (final Data data : datas) {
            indexEngine.removeDataMetadataFromIndex(data.getId());
            metadataBusiness.deleteDataMetadata(data.getId());
            dataBusinessListener.preDataDelete(data);
            dataRepository.delete(data.getId());
            dataBusinessListener.postDataDelete(data);
            // Relevant erase dataset when the is no more data in it. fr now we remove it
            deleteDatasetIfEmpty(data.getDatasetId());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Data create(final QName name, final String providerIdentifier,
                       final String type, final boolean sensorable,
                       final boolean included, final String subType, final String metadataXml) {
        return create(name, providerIdentifier, type, sensorable, included, null, subType, metadataXml);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Data create(QName name, String providerIdentifier, String type, boolean sensorable, boolean included, Boolean rendered, String subType, String metadataXml) {
        final Provider provider = providerRepository.findByIdentifier(providerIdentifier);
        if (provider != null) {
            Data data = new Data();
            data.setDate(new Date().getTime());
            data.setName(name.getLocalPart());
            data.setNamespace(name.getNamespaceURI());
            final Optional<CstlUser> user = userRepository.findOne(securityManager.getCurrentUserLogin());
            if (user.isPresent()) {
                data.setOwner(user.get().getId());
            }
            data.setProvider(provider.getId());
            data.setSensorable(sensorable);
            data.setType(type);
            data.setSubtype(subType);
            data.setIncluded(included);
            data.setMetadata(metadataXml);
            data.setRendered(rendered);
            data = dataRepository.create(data);
            dataBusinessListener.postDataCreate(data);
            return data;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void updateDataIncluded(final int dataId, boolean included) throws ConfigurationException {
        final Data data = dataRepository.findById(dataId);
        data.setIncluded(included);
        dataRepository.update(data);

        final int providerID = data.getProvider();
        final int dataID = data.getId();
        if (!included) {
            // 1. remove layer involving the data
            for (Layer layer : layerRepository.findByDataId(dataID)) {
                layerRepository.delete(layer.getId());
            }

            // 2. unlink from csw
            dataRepository.removeDataFromAllCSW(dataID);

            // 3. cleanup provider if empty
            boolean remove = true;
            List<Data> providerData = dataRepository.findByProviderId(providerID);
            for (Data pdata : providerData) {
                if (pdata.getIncluded()) {
                    remove = false;
                    break;
                }
            }
            if (remove) {
                //notify pre delete
                for (Data pdata : providerData) {
                    dataBusinessListener.preDataDelete(pdata);
                    // remove metadata
                    metadataBusiness.deleteDataMetadata(pdata.getId());
                }

                final Provider p = providerRepository.findOne(providerID);
                final DataProvider dp = DataProviders.getInstance().getProvider(p.getIdentifier());
                DataProviders.getInstance().removeProvider(dp);
                providerRepository.delete(providerID);

                //notify post delete
                for (Data pdata : providerData) {
                    dataBusinessListener.postDataDelete(pdata);
                }

                // delete associated files in integrated folder. the file name (p.getIdentifier()) is a folder.
                final File provDir = ConfigDirectory.getDataIntegratedDirectory(p.getIdentifier());
                FileUtilities.deleteDirectory(provDir);
            }

            // Relevant erase dataset when there is no more data in it. for now we remove it
            deleteDatasetIfEmpty(data.getDatasetId());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public synchronized void removeDataFromProvider(final String providerId) throws ConfigurationException {
        final Provider p = providerRepository.findByIdentifier(providerId);
        if (p != null) {
            final List<Data> datas = dataRepository.findByProviderId(p.getId());
            for (final Data data : datas) {
                indexEngine.removeDataMetadataFromIndex(data.getId());
                dataBusinessListener.preDataDelete(data);
                metadataBusiness.deleteDataMetadata(data.getId());
                dataRepository.delete(data.getId());
                dataBusinessListener.postDataDelete(data);
                // Relevant erase dataset when the is no more data in it. fr now we remove it
                deleteDatasetIfEmpty( data.getDatasetId());
            }
        }
    }

    @Override
    public ParameterValues getVectorDataColumns(int id) throws DataStoreException {
        final Provider provider = getProvider(id);
        final DataProvider dataProvider = DataProviders.getInstance().getProvider(provider.getIdentifier());
        if (!(dataProvider.getMainStore() instanceof FeatureStore)) {
            throw new DataStoreException("Not a vector data requested");
        }

        final List<String> colNames = new ArrayList<>();
        final String dataName = dataRepository.findById(id).getName();
        final FeatureStore store = (FeatureStore) dataProvider.getMainStore();
        final org.opengis.feature.FeatureType ft = store.getFeatureType(dataName);
        for (final PropertyType prop : ft.getProperties(true)) {
            colNames.add(prop.getName().toString());
        }

        final ParameterValues values = new ParameterValues();
        final HashMap<String, String> mapVals = new HashMap<>();
        for (final String colName : colNames) {
            mapVals.put(colName, colName);
        }
        values.setValues(mapVals);
        return values;
    }

    @Override
    @Transactional
    public void updateMetadata(String providerId, QName dataName, DefaultMetadata metadata) throws ConfigurationException {
        final Data data = dataRepository.findDataFromProvider(dataName.getNamespaceURI(), dataName.getLocalPart(), providerId);
        if (data != null) {
            metadataBusiness.updateMetadata(metadata.getFileIdentifier(), metadata, data.getId(), null, null);
            
            indexEngine.addMetadataToIndexForData(metadata, data.getId());
        } else {
            throw new TargetNotFoundException("Data :" + dataName + " in provider:" + providerId +  " not found");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ImageStatistics getDataStatistics(final int dataId) throws ConfigurationException {

        final Data data = dataRepository.findById(dataId);
        if (data != null && DataType.COVERAGE.name().equals(data.getType()) &&
                (data.getRendered() == null || !data.getRendered())) {
            try {
                final String state = data.getStatsState();
                final String result = data.getStatsResult();

                if (state != null) {
                    switch (state) {
                        case "PARTIAL" : //fall through
                        case "COMPLETED" :
                            return deserializeImageStatistics(result);
                        case "PENDING" : return null;
                        case "ERROR" :
                            //can have partial statistics even if an error occurs.
                            if (result != null && result.startsWith("{")) {
                                return deserializeImageStatistics(result);
                            } else {
                                return null;
                            }
                    }
                }

            } catch (IOException e) {
                throw new ConfigurationException("Invalid statistic JSON format for data : "+dataId, e);
            }
        }
        return null;
    }

    private ImageStatistics deserializeImageStatistics(String state) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final SimpleModule module = new SimpleModule();
        module.addDeserializer(ImageStatistics.class, new ImageStatisticDeserializer()); //custom deserializer
        mapper.registerModule(module);
        return mapper.readValue(state, ImageStatistics.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Data findById(final Integer id) throws ConfigurationException {
        return dataRepository.findById(id);
    }

    /**
     * {@inheritDoc}
     */
    public Integer getCountAll(boolean includeInvisibleData) {
        return dataRepository.countAll(includeInvisibleData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Scheduled(cron = "1 * * * * *")
    public void updateDataStatistics() {
        String propertyValue = Application.getProperty(AppProperty.DATA_AUTO_ANALYSE);
        boolean doAnalysis = propertyValue == null ? false : Boolean.valueOf(propertyValue);
        if (doAnalysis) {
            computeEmptyDataStatistics(false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void computeEmptyDataStatistics(boolean isInit) {
        final List<Data> dataList = dataRepository.findStatisticLess();

        List<Integer> dataWithoutStats = new ArrayList<>();
        for (final Data data : dataList) {

            //compute statistics only on coverage data not rendered and without previous statistic computed.
            if (DataType.COVERAGE.name().equals(data.getType()) && !"pyramid".equalsIgnoreCase(data.getSubtype()) &&
                    (data.getRendered() == null || !data.getRendered())) {

                String state = data.getStatsState();
                if (isInit) {
                    //rerun statistic for error and pending states
                    if ("PENDING".equalsIgnoreCase(state) || "ERROR".equalsIgnoreCase(state)) {
                        data.setStatsState(null);
                        data.setStatsResult(null);
                        SpringHelper.executeInTransaction(new TransactionCallbackWithoutResult() {
                            @Override
                            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                                dataRepository.update(data);
                            }
                        });
                        dataWithoutStats.add(data.getId());
                    }
                }

                if (state == null || state.isEmpty()) {
                    dataWithoutStats.add(data.getId());
                }
            }
        }

        for (Integer dataId : dataWithoutStats) {
            dataCoverageJob.asyncUpdateDataStatistics(dataId);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    @Transactional
    public void updateDataRendered(final QName fullName, final String providerIdentifier, boolean isRendered) {
        final Data data = dataRepository.findDataFromProvider(fullName.getNamespaceURI(),
                fullName.getLocalPart(),
                providerIdentifier);
        data.setRendered(isRendered);
        dataRepository.update(data);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    @Transactional
    public void updateDataDataSetId(final QName fullName, final String providerIdentifier, final Integer datasetId) {
        final Data data = dataRepository.findDataFromProvider(fullName.getNamespaceURI(),
                fullName.getLocalPart(),
                providerIdentifier);
        data.setDatasetId(datasetId);
        dataRepository.update(data);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    @Transactional
    public void updateHidden(final int dataId, boolean value) {
        final Data data = dataRepository.findById(dataId);
        data.setHidden(value);
        dataRepository.update(data);
    }


    @Override
    //FIXME RESTORE cleaning mechanism @Scheduled(fixedDelay=5*60*1000)
    public void uploadCleaner() {
        LOGGER.debug("Cleaner");
        java.nio.file.Path uploadDirectory = ConfigDirectory.getUploadDirectory();
        File[] listFiles = uploadDirectory.toFile().listFiles();
        if (listFiles != null) {
            for (File file : listFiles) {
                if (TokenUtils.isExpired(file.getName())) {
                    LOGGER.info(file.getName() + " expired");
                    FileUtilities.deleteDirectory(file);
                }
            }
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<FileBean> getFilesFromPath(final String path, final boolean filtered, final boolean onlyXML) throws ConstellationException {
        final List<FileBean> listBean = new ArrayList<>();
        final Set<String> extensions = GeotoolkitFileExtensionAvailable.getAvailableFileExtension().keySet();
        final File[] children;
        if (Paths.get(path).toFile().exists()) {
            final File nextRoot = new File(path);
            children = nextRoot.listFiles();
        }else{
            throw new ConstellationException("path does not exists!");
        }

        //loop on subfiles/folders to create bean
        if (children != null) {
            for (final File child : children) {
                final FileBean bean = new FileBean(child.getName(),
                        child.isDirectory(),
                        child.getAbsolutePath(),
                        child.getParentFile().getAbsolutePath());
                if (!child.isDirectory() || !filtered) {
                    final int lastIndexPoint = child.getName().lastIndexOf('.');
                    final String extension = child.getName().substring(lastIndexPoint + 1);
                    if(onlyXML) {
                        if ("xml".equalsIgnoreCase(extension)) {
                            listBean.add(bean);
                        }
                    }else {
                        if (extensions.contains(extension.toLowerCase())) {
                            listBean.add(bean);
                        }
                    }
                } else {
                    listBean.add(bean);
                }
            }
        }
        Collections.sort(listBean);
        return listBean;
    }

    @Transactional
    @Override
    public void linkDataToData(final int dataId, final int childId) {
        dataRepository.linkDataToData(dataId, childId);
    }

    @Transactional
    @Override
    public List<Data> getDataLinkedData(final int dataId){
        return dataRepository.getDataLinkedData(dataId);
    }

    @Override
    public List<DataItem> fetchByDatasetId(int datasetId) {
        return dataRepository.fetchByDatasetId(datasetId);
    }

    @Override
    public List<DataItem> fetchByDatasetIds(Collection<Integer> datasetIds) {
        return dataRepository.fetchByDatasetIds(datasetIds);
    }

    @Override
    public boolean existsById(int dataId) {
        return dataRepository.existsById(dataId);
    }
}
