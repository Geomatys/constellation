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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.base.Optional;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.MarshallerPool;
import org.apache.sis.xml.XML;
import org.constellation.ServiceDef;
import org.constellation.admin.exception.ConstellationException;
import org.constellation.admin.index.IndexEngine;
import org.constellation.admin.util.ImageStatisticDeserializer;
import org.constellation.api.DataType;
import org.constellation.business.IDataBusiness;
import org.constellation.business.IDataCoverageJob;
import org.constellation.configuration.*;
import org.constellation.dto.CoverageMetadataBean;
import org.constellation.dto.ParameterValues;
import org.constellation.engine.register.*;
import org.constellation.engine.register.Layer;
import org.constellation.engine.register.repository.*;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviders;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.process.coverage.statistics.ImageStatistics;
import org.opengis.feature.PropertyType;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.util.Locales;
import org.apache.sis.util.iso.Types;
import org.constellation.dto.MetadataLists;
import org.opengis.metadata.citation.DateType;
import org.opengis.metadata.constraint.Classification;
import org.opengis.metadata.constraint.Restriction;
import org.opengis.metadata.identification.KeywordType;
import org.opengis.metadata.identification.TopicCategory;
import org.opengis.metadata.maintenance.MaintenanceFrequency;
import org.opengis.metadata.maintenance.ScopeCode;
import org.opengis.metadata.spatial.GeometricObjectType;


/**
 * Business facade for data.
 *
 * @author Mehdi Sidhoum (Geomatys).
 * @version 0.9
 * @since 0.9
 */

@Profile("standard")
@Component
@DependsOn({"database-initer", "providerBusiness"})
@Primary
public class DataBusiness extends InternalCSWSynchronizer implements IDataBusiness {
    /**
     * Used for debugging purposes.
     */
    protected static final Logger LOGGER = Logging.getLogger(DataBusiness.class);
    /**
     * Injected user repository.
     */
    @Inject
    private UserRepository userRepository;
    /**
     * Injected domain repository.
     */
    @Inject
    private DomainRepository domainRepository;
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
    private DatasetRepository datasetRepository;
    /**
     * Injected sensor repository.
     */
    @Inject
    private SensorRepository sensorRepository;
    /**
     * Injected lucene index engine.
     */
    @Inject
    private IndexEngine indexEngine;

    /**
     * Injected data coverage job
     */
    @Inject
    private IDataCoverageJob dataCoverageJob;

    /**
     * Return the {@linkplain Provider provider} for the given {@linkplain Data data} identifier.
     *
     * @param dataId {@link Data} identifier
     * @return a {@linkplain Provider provider}
     */
    @Override
    public Provider getProvider(int dataId) {
        final Data d = dataRepository.findById(dataId);
        return providerRepository.findOne(d.getProvider());
    }

    /**
     * Returns {@link DefaultMetadata} for given providerId and data name.
     * @param providerId given data provider id.
     * @param name given data name.
     * @return {@link DefaultMetadata}
     * @throws ConstellationException is thrown for UnsupportedEncodingException or JAXBException.
     */
    @Override
    public DefaultMetadata loadIsoDataMetadata(final String providerId,
                                               final QName name) throws ConstellationException{
        DefaultMetadata metadata = null;
        final Data data = dataRepository.findDataFromProvider(name.getNamespaceURI(), name.getLocalPart(), providerId);
        final MarshallerPool pool = getMarshallerPool();
        try {
            final String metadataStr = data.getIsoMetadata();
            if (data != null && metadataStr != null) {
                if(pool != null){
                    final InputStream sr = new ByteArrayInputStream(metadataStr.getBytes("UTF-8"));
                    final Unmarshaller m = pool.acquireUnmarshaller();
                    metadata = (DefaultMetadata) m.unmarshal(sr);
                    pool.recycle(m);
                }else {
                    metadata = unmarshallMetadata(metadataStr);
                }
            }
        } catch (UnsupportedEncodingException | JAXBException e) {
            throw new ConstellationException(e);
        }
        return metadata;
    }

    /**
     * Returns {@link DefaultMetadata} for given dataId.
     * @param dataId given data id.
     * @return {@link DefaultMetadata}
     * @throws ConstellationException is thrown for UnsupportedEncodingException or JAXBException.
     */
    @Override
    public DefaultMetadata loadIsoDataMetadata(final int dataId) throws ConstellationException{
        DefaultMetadata metadata = null;
        final Data data = dataRepository.findById(dataId);
        final MarshallerPool pool = getMarshallerPool();
        try {
            final String metadataStr = data.getIsoMetadata();
            if (data != null && metadataStr != null) {
                if(pool != null){
                    final InputStream sr = new ByteArrayInputStream(metadataStr.getBytes("UTF-8"));
                    final Unmarshaller m = pool.acquireUnmarshaller();
                    metadata = (DefaultMetadata) m.unmarshal(sr);
                    pool.recycle(m);
                } else {
                    metadata = unmarshallMetadata(metadataStr);
                }
            }
        } catch (UnsupportedEncodingException | JAXBException e) {
            throw new ConstellationException(e);
        }
        return metadata;
    }
    
    @Override
    public Dataset getDatasetForData(final String providerId, final QName name) throws ConstellationException{
        final Data data = dataRepository.findDataFromProvider(name.getNamespaceURI(), name.getLocalPart(), providerId);
        if (data != null) {
            return datasetRepository.findById(data.getDatasetId());
        }
        return null;
    }

    @Override
    public Dataset getDatasetForData(final int dataId) throws ConstellationException{
        final Data data = dataRepository.findById(dataId);
        if (data != null) {
            return datasetRepository.findById(data.getDatasetId());
        }
        return null;
    }

    /**
     * Search and returns result as list of {@link Data} for given query string.
     * @param queryString the lucene query.
     * @return list of {@link Data}
     * @throws ConstellationException
     * @throws IOException
     */
    @Override
    public List<Data> searchOnMetadata(final String queryString) throws IOException, ConstellationException {
        final List<Data> result = new ArrayList<>();
        final Set<Integer> ids;
        try {
            ids = indexEngine.searchOnMetadata(queryString, "dataId");
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
     * Proceed to save metadata for given data provider id and data name.
     * @param providerId data provider id.
     * @param name data name.
     * @param metadata given {@link DefaultMetadata} to save.
     * @throws ConstellationException is thrown for JAXBException.
     */
    public void saveMetadata(final String providerId,
                             final QName name,
                             final DefaultMetadata metadata) throws ConstellationException {
        final String metadataStr;
        try {
            final MarshallerPool pool = getMarshallerPool();
            if(pool != null) {
                final Marshaller marshaller = pool.acquireMarshaller();
                final StringWriter sw = new StringWriter();
                marshaller.marshal(metadata, sw);
                pool.recycle(marshaller);
                metadataStr = sw.toString();
            } else {
                metadataStr = marshallMetadata(metadata);
            }
        } catch (JAXBException ex) {
            throw new ConstellationException(ex);
        }
        final Data data = dataRepository.findDataFromProvider(name.getNamespaceURI(), name.getLocalPart(), providerId);
        data.setIsoMetadata(metadataStr);
        data.setMetadataId(metadata.getFileIdentifier());
        dataRepository.update(data);
        indexEngine.addMetadataToIndexForData(metadata, data.getId());
    }

    /**
     * Load a metadata for given data provider id and data name.
     *
     * @param providerIdentifier given data provider.
     * @param name given data name.
     * @param pool marshaller pool.
     * @return {@link CoverageMetadataBean}
     * @throws ConstellationException is thrown for JAXBException.
     */
    @Override
    public CoverageMetadataBean loadDataMetadata(final String providerIdentifier,
                                                 final QName name,
                                                 final MarshallerPool pool) throws ConstellationException {
        try {
            final Data data = dataRepository.findDataFromProvider(name.getNamespaceURI(), name.getLocalPart(), providerIdentifier);
            if (data != null && data.getMetadata() != null) {
                final InputStream sr = new ByteArrayInputStream(data.getMetadata().getBytes());
                final Unmarshaller m = pool.acquireUnmarshaller();
                final CoverageMetadataBean metadata = (CoverageMetadataBean) m.unmarshal(sr);
                pool.recycle(m);
                return metadata;
            }
        } catch (JAXBException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while updating service database", ex);
            throw new ConstellationException(ex);
        }
        return null;
    }

    /**
     * Returns {@link DataBrief} for given data name and provider id as integer.
     *
     * @param fullName given data name.
     * @param providerId given data provider as integer.
     * @return {@link DataBrief}.
     * @throws ConstellationException is thrown if result fails.
     */
    @Override
    public DataBrief getDataBrief(QName fullName,Integer providerId) throws ConstellationException {
        final Data data = dataRepository.findByNameAndNamespaceAndProviderId(fullName.getLocalPart(),fullName.getNamespaceURI(), providerId);
        final List<Data> datas = new ArrayList<>();
        datas.add(data);
        final List<DataBrief> dataBriefs = getDataBriefFrom(datas);
        if (dataBriefs != null && dataBriefs.size() == 1) {
            return dataBriefs.get(0);
        }
        throw new ConstellationException(new Exception("Problem : DataBrief Construction is null or multiple"));
    }

    /**
     * Returns {@link DataBrief} for given data name and provider identifier as string.
     *
     * @param fullName given data name.
     * @param providerIdentifier given data provider identifier.
     * @return {@link DataBrief}
     * @throws ConstellationException is thrown if result fails.
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
     * Returns a list of {@link DataBrief} for given metadata identifier.
     *
     * @param metadataId given metadata identifier.
     * @return list of {@link DataBrief}.
     */
    @Override
    public List<DataBrief> getDataBriefsFromMetadataId(final String metadataId) {
        final List<Data> datas = findByMetadataId(metadataId);
        return getDataBriefFrom(datas);
    }

    /**
     * Returns {@link DataBrief} for given layer alias and data provider identifier.
     *
     * @param layerAlias given layer name.
     * @param dataProviderIdentifier given data provider identifier.
     * @return {@link DataBrief}.
     * @throws ConstellationException is thrown if result fails.
     */
    @Override
    public DataBrief getDataLayer(final String layerAlias,
                                  final String dataProviderIdentifier) throws ConstellationException {
        final Data data = layerRepository.findDatasFromLayerAlias(layerAlias, dataProviderIdentifier);
        final List<Data> datas = new ArrayList<>();
        datas.add(data);
        final List<DataBrief> dataBriefs = getDataBriefFrom(datas);
        if (dataBriefs != null && !dataBriefs.isEmpty()){
            return dataBriefs.get(0);
        }
        throw new ConstellationException(new Exception("Problem : DataBrief Construction is null or multiple"));
    }

    /**
     * Returns list of {@link Data} for given dataSet id.
     *
     * @param datasetId the given dataSet id.
     * @return the list of {@link Data}.
     */
    public List<Data> findByDatasetId(final Integer datasetId) {
        return dataRepository.findByDatasetId(datasetId);
    }

    /**
     * Returns a list of {@link DataBrief} for given dataSet id.
     *
     * @param datasetId the given dataSet id.
     * @return the list of {@link DataBrief}.
     */
    @Override
    public List<DataBrief> getDataBriefsFromDatasetId(final Integer datasetId) {
        final List<Data> dataList = findByDatasetId(datasetId);
        return getDataBriefFrom(dataList);
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
     * Convert a list of {@link Data} to list of {@link DataBrief}.
     * @param datas given list of {@link Data}.
     * @return the list of {@link DataBrief}.
     */
    private List<DataBrief> getDataBriefFrom(final List<Data> datas) {
        final List<DataBrief> dataBriefs = new ArrayList<>();
        for (final Data data : datas) {
            final List<Style> styles = styleRepository.findByData(data);
            final List<Service> services = serviceRepository.findByDataId(data.getId());

            final DataBrief db = new DataBrief();
            db.setId(data.getId());
            final Optional<CstlUser> user = userRepository.findById(data.getOwner());
            if (user != null && user.isPresent()) {
                final CstlUser cstlUser = user.get();
                if(cstlUser!=null){
                    db.setOwner(cstlUser.getLogin());
                }
            }
            db.setName(data.getName());
            db.setNamespace(data.getNamespace());
            db.setDate(new Date(data.getDate()));
            db.setProvider(getProviderIdentifier(data.getProvider()));
            db.setType(data.getType());
            db.setSubtype(data.getSubtype());
            db.setSensorable(data.isSensorable());
            db.setTargetSensor(sensorRepository.getLinkedSensors(data));
            db.setStats(data.getStats());

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

            final List<ServiceProtocol> serviceProtocols = new ArrayList<>(0);
            for (final Service service : services) {
                final List<String> protocol = new ArrayList<>(0);
                final ServiceDef.Specification spec = ServiceDef.Specification.valueOf(service.getType().toUpperCase());
                protocol.add(spec.name());
                protocol.add(spec.fullName);
                final ServiceProtocol sp = new ServiceProtocol(service.getIdentifier(), protocol);
                serviceProtocols.add(sp);
            }
            db.setTargetService(serviceProtocols);
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
     * Proceed to remove data for given data name and provider identifier.
     * @param name given data name.
     * @param providerIdentifier given provider identifier.
     */
    @Override
    public void deleteData(final QName name, final String providerIdentifier) {
        final Provider provider = providerRepository.findByIdentifier(providerIdentifier);
        if (provider != null) {
            final Data d = dataRepository.findByNameAndNamespaceAndProviderId(name.getLocalPart(), name.getNamespaceURI(), provider.getId());
            if (d != null) {
                indexEngine.removeDataMetadataFromIndex(d.getId());
                dataRepository.delete(d.getId());
                // Relevant erase dataset when the is no more data in it. fr now we remove it
                deleteDatasetIfEmpty(d.getDatasetId());
            }
        }
    }
    
    @Transactional("txManager")
    private void deleteDatasetIfEmpty(Integer datasetID) {
        if (datasetID != null) {
            List<Data> datas = dataRepository.findAllByDatasetId(datasetID);
            if (datas.isEmpty()) {
                indexEngine.removeDatasetMetadataFromIndex(datasetID);
                datasetRepository.remove(datasetID);
            }
        }
    }

    /**
     * Proceed to remove all data.
     */
    @Override
    public void deleteAll() {
        final List<Data> datas = dataRepository.findAll();
        for (final Data data : datas) {
            indexEngine.removeDataMetadataFromIndex(data.getId());
            dataRepository.delete(data.getId());
            // Relevant erase dataset when the is no more data in it. fr now we remove it
            deleteDatasetIfEmpty(data.getDatasetId());
        }
    }

    /**
     * Proceed to create a new data for given parameters.
     * @param name data name to create.
     * @param providerIdentifier provider identifier.
     * @param type data type.
     * @param sensorable flag that indicates if data is sensorable.
     * @param visible flag that indicates if data is visible.
     * @param subType data subType.
     * @param metadata metadata of data.
     */
    @Override
    public Data create(final QName name, final String providerIdentifier,
                       final String type, final boolean sensorable,
                       final boolean visible, final String subType, final String metadata) {
        final Provider provider = providerRepository.findByIdentifier(providerIdentifier);
        if (provider != null) {
            final Data data = new Data();
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
            data.setVisible(visible);
            data.setMetadata(metadata);
            return dataRepository.create(data);
        }
        return null;
    }

    /**
     * Update data visibility for given data name and provider identifier.
     * @param name data name.
     * @param providerIdentifier provider identifier.
     * @param visibility value to set
     * @throws org.constellation.configuration.ConfigurationException
     */
    @Override
    public void updateDataVisibility(final QName name,
                                     final String providerIdentifier,
                                     boolean visibility) throws ConfigurationException {
        final Data data = dataRepository.findDataFromProvider(name.getNamespaceURI(), name.getLocalPart(), providerIdentifier);
        data.setVisible(visibility);
        dataRepository.update(data);
        
        if (!visibility) {
            // 1. remove layer involving the data
            for (Layer layer : layerRepository.findByDataId(data.getId())) {
                layerRepository.delete(layer.getId());
            }

            // 2. cleanup provider if empty
            boolean remove = true;
            for (Data pdata : dataRepository.findByProviderId(data.getProvider())) {
                if (pdata.isVisible()) {
                    remove = false;
                    break;
                }
            }
            if (remove) {
                providerRepository.delete(data.getProvider());
            }
            
            // Relevant erase dataset when there is no more data in it. for now we remove it
            deleteDatasetIfEmpty(data.getDatasetId());
            
            // update internal CSW index
            updateInternalCSWIndex(data.getMetadataId(), 1, false); // TODO DOMAIN ID
        }
    }

    /**
     * Proceed to add a data domain.
     * @param dataId given data Id.
     * @param domainId given domain Id.
     */
    @Override
    public void addDataToDomain(final int dataId, final int domainId) {
        domainRepository.addDataToDomain(dataId, domainId);
    }

    /**
     * proceed to remove data from domain.
     * synchronized method.
     * @param dataId given data id.
     * @param domainId given domain id.
     * @throws CstlConfigurationRuntimeException
     */
    @Transactional("txManager")
    @Override
    public synchronized void removeDataFromDomain(final int dataId,
                                                  final int domainId)throws CstlConfigurationRuntimeException {
        final List<Domain> findByLinkedService = domainRepository.findByLinkedData(dataId);
        if (findByLinkedService.size() == 1) {
            throw new CstlConfigurationRuntimeException("Could not unlink last domain from a data").withErrorCode("error.data.lastdomain");
        }
        domainRepository.removeDataFromDomain(dataId, domainId);
    }

    /**
     * Proceed to remove data for given provider.
     * Synchronized method.
     * @param providerID given provider identifier.
     */
    @Transactional("txManager")
    @Override
    public synchronized void removeDataFromProvider(final String providerID) {
        final Provider p = providerRepository.findByIdentifier(providerID);
        if (p != null) {
            final List<Data> datas = dataRepository.findByProviderId(p.getId());
            for (final Data data : datas) {
                indexEngine.removeDataMetadataFromIndex(data.getId());
                dataRepository.delete(data.getId());
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
    public void updateMetadata(String providerId, QName dataName, Integer domainId, DefaultMetadata metadata) throws ConfigurationException {
        final String metadataString;
        try {
            final MarshallerPool pool = getMarshallerPool();
            if(pool != null) {
                final Marshaller marshaller = pool.acquireMarshaller();
                final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                marshaller.marshal(metadata, outputStream);
                pool.recycle(marshaller);
                metadataString = outputStream.toString();
            } else {
                metadataString = marshallMetadata(metadata);
            }
        } catch (JAXBException ex) {
            throw new ConfigurationException("Unable to marshall the dataset metadata", ex);
        }
        
        final Data data = dataRepository.findDataFromProvider(dataName.getNamespaceURI(), dataName.getLocalPart(), providerId);
        if (data != null) {
            data.setIsoMetadata(metadataString);
            data.setMetadataId(metadata.getFileIdentifier());
            dataRepository.update(data);
            indexEngine.addMetadataToIndexForData(metadata, data.getId());
            // update internal CSW index
            updateInternalCSWIndex(metadata.getFileIdentifier(), domainId, true);
        } else {
            throw new TargetNotFoundException("Data :" + dataName + " in provider:" + providerId +  " not found");
        }
    }
    
    @Override
    public String getTemplate(final QName dataName, final String dataType) throws ConfigurationException {
        final String templateName;
        if ("vector".equalsIgnoreCase(dataType)) {
            //vector template
            templateName = "profile_default_vector";
        } else if ("raster".equalsIgnoreCase(dataType)) {
            //raster template
            templateName = "profile_default_raster";
        } else {
            //default template is import
            templateName = "profile_import";
        }
        return templateName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ImageStatistics getDataStatistics(final int dataId) throws ConfigurationException {

        final Data data = dataRepository.findById(dataId);
        if (data != null && DataType.COVERAGE.name().equals(data.getType())) {
            try {
                final String stats = data.getStats();
                if (stats != null && !stats.equalsIgnoreCase("PENDING")) {
                    final ObjectMapper mapper = new ObjectMapper();
                    final SimpleModule module = new SimpleModule();
                    module.addDeserializer(ImageStatistics.class, new ImageStatisticDeserializer()); //custom deserializer
                    mapper.registerModule(module);
                    return mapper.readValue(stats, ImageStatistics.class);
                } else {
                    Future<ImageStatistics> futureStats = dataCoverageJob.asyncUpdateDataStatistics(data.getId());
                    return futureStats.get();
                }
            } catch (IOException | InterruptedException | ExecutionException e) {
                throw new ConfigurationException("Invalid statistic JSON format for data : "+dataId, e);
            }
        }
        return null;
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
    @Override
    public void computeEmptyDataStatistics() {
        final List<Data> dataList = dataRepository.findAll();
        for (Data data : dataList) {
            String stats = data.getStats();
            if (DataType.COVERAGE.name().equals(data.getType()) &&
                    (stats == null || stats.isEmpty() || stats.equalsIgnoreCase("PENDING") )) {
                dataCoverageJob.asyncUpdateDataStatistics(data.getId());
            }
        }
    }

    protected MarshallerPool getMarshallerPool() {
        return null; //in constellation this should always return null, since this method can be overrided by sub-project.
    }

    protected String marshallMetadata(final DefaultMetadata metadata) throws JAXBException {
        return XML.marshal(metadata);
    }

    protected DefaultMetadata unmarshallMetadata(final String metadata) throws JAXBException {
        return (DefaultMetadata) XML.unmarshal(metadata);
    }

    @Override
    public MetadataLists getMetadataCodeLists() {
        final MetadataLists mdList = new MetadataLists();

        //for role codes
        final List<String> roleCodes = new LinkedList<>();
        for (final org.opengis.metadata.citation.Role role : org.opengis.metadata.citation.Role.values()) {
            final String standardName = Types.getStandardName(role.getClass());
            final String code = role.identifier()!=null?role.identifier():role.name();
            final String codeListName = standardName+"."+code;
            roleCodes.add(codeListName);
        }
        Collections.sort(roleCodes);
        mdList.setRoleCodes(roleCodes);

        //for keyword type codes
        final List<String> keywordTypesCodes = new LinkedList<>();
        for (final KeywordType ktype : KeywordType.values()) {
            final String standardName = Types.getStandardName(ktype.getClass());
            final String code = ktype.identifier()!=null?ktype.identifier():ktype.name();
            final String codeListName = standardName+"."+code;
            keywordTypesCodes.add(codeListName);
        }
        Collections.sort(keywordTypesCodes);
        mdList.setKeywordTypeCodes(keywordTypesCodes);

        //for locale codes
        final List<String> localeCodes = new LinkedList<>();
        for (final Locale locale : Locales.ALL.getAvailableLanguages()) {
            localeCodes.add("LanguageCode."+locale.getISO3Language());
        }
        Collections.sort(localeCodes);
        mdList.setLocaleCodes(localeCodes);

        //for topic category codes
        final List<String> topicCategoryCodes = new LinkedList<>();
        for (final TopicCategory tc : TopicCategory.values()) {
            final String standardName = Types.getStandardName(tc.getClass());
            final String code = tc.identifier()!=null? tc.identifier(): tc.name();
            final String codeListName = standardName+"."+code;
            topicCategoryCodes.add(codeListName);
        }
        Collections.sort(topicCategoryCodes);
        mdList.setTopicCategoryCodes(topicCategoryCodes);

        //for date type codes
        final List<String> dateTypeCodes = new LinkedList<>();
        for (final DateType dateType : DateType.values()) {
            final String standardName = Types.getStandardName(dateType.getClass());
            final String code = dateType.identifier()!=null? dateType.identifier(): dateType.name();
            final String codeListName = standardName+"."+code;
            dateTypeCodes.add(codeListName);
        }
        Collections.sort(dateTypeCodes);
        mdList.setDateTypeCodes(dateTypeCodes);

        //for maintenanceFrequency codes
        final List<String> maintenanceFrequencyCodes = new LinkedList<>();
        for (final MaintenanceFrequency cl : MaintenanceFrequency.values()) {
            final String standardName = Types.getStandardName(cl.getClass());
            final String code = cl.identifier()!=null? cl.identifier(): cl.name();
            final String codeListName = standardName+"."+code;
            maintenanceFrequencyCodes.add(codeListName);
        }
        Collections.sort(maintenanceFrequencyCodes);
        mdList.setMaintenanceFrequencyCodes(maintenanceFrequencyCodes);

        //for GeometricObjectType codes
        final List<String> geometricObjectTypeCodes = new LinkedList<>();
        for (final GeometricObjectType got : GeometricObjectType.values()) {
            final String standardName = Types.getStandardName(got.getClass());
            final String code = got.identifier()!=null? got.identifier(): got.name();
            final String codeListName = standardName+"."+code;
            geometricObjectTypeCodes.add(codeListName);
        }
        Collections.sort(geometricObjectTypeCodes);
        mdList.setGeometricObjectTypeCodes(geometricObjectTypeCodes);

        //for Classification codes
        final List<String> classificationCodes = new LinkedList<>();
        for (final Classification cl : Classification.values()) {
            final String standardName = Types.getStandardName(cl.getClass());
            final String code = cl.identifier()!=null? cl.identifier(): cl.name();
            final String codeListName = standardName+"."+code;
            classificationCodes.add(codeListName);
        }
        Collections.sort(classificationCodes);
        mdList.setClassificationCodes(classificationCodes);

        // for characterSet codes
        final List<String> characterSetCodes = new LinkedList<>();
        final Set<String> keys = Charset.availableCharsets().keySet();
        final List<String> keep = Arrays.asList("UTF-8","UTF-16","UTF-32",
                "ISO-8859-1","ISO-8859-13","ISO-8859-15",
                "ISO-8859-2","ISO-8859-3","ISO-8859-4",
                "ISO-8859-5","ISO-8859-6","ISO-8859-7",
                "ISO-8859-8","ISO-8859-9","Shift_JIS",
                "EUC-JP","EUC-KR","US-ASCII","Big5","GB2312");
        keep.retainAll(keys);
        for (final String c : keep) {
            characterSetCodes.add(c);
        }
        Collections.sort(characterSetCodes);
        mdList.setCharacterSetCodes(characterSetCodes);

        //for Restriction codes
        final List<String> restrictionCodes = new LinkedList<>();
        for (final Restriction cl : Restriction.values()) {
            final String standardName = Types.getStandardName(cl.getClass());
            final String code = cl.identifier()!=null? cl.identifier(): cl.name();
            final String codeListName = standardName+"."+code;
            restrictionCodes.add(codeListName);
        }
        Collections.sort(restrictionCodes);
        mdList.setRestrictionCodes(restrictionCodes);
        
        //for Scope codes
        final List<String> scopeCodes = new LinkedList<>();
        for (final ScopeCode cl : ScopeCode.values()) {
            final String standardName = Types.getStandardName(cl.getClass());
            final String code = cl.identifier()!=null? cl.identifier(): cl.name();
            final String codeListName = standardName+"."+code;
            scopeCodes.add(codeListName);
        }
        Collections.sort(scopeCodes);
        mdList.setScopeCodes(scopeCodes);
        
        return mdList;
    }
}
