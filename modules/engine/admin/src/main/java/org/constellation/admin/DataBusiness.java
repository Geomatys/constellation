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

import com.google.common.base.Optional;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.ServiceDef;
import org.constellation.admin.exception.ConstellationException;
import org.constellation.admin.index.IndexEngine;
import org.constellation.configuration.CstlConfigurationRuntimeException;
import org.constellation.configuration.DataBrief;
import org.constellation.configuration.ServiceProtocol;
import org.constellation.configuration.StyleBrief;
import org.constellation.dto.CoverageMetadataBean;
import org.constellation.engine.register.CstlUser;
import org.constellation.engine.register.Data;
import org.constellation.engine.register.Dataset;
import org.constellation.engine.register.Domain;
import org.constellation.engine.register.Provider;
import org.constellation.engine.register.Service;
import org.constellation.engine.register.Style;
import org.constellation.engine.register.repository.DataRepository;
import org.constellation.engine.register.repository.DatasetRepository;
import org.constellation.engine.register.repository.DomainRepository;
import org.constellation.engine.register.repository.LayerRepository;
import org.constellation.engine.register.repository.ProviderRepository;
import org.constellation.engine.register.repository.SensorRepository;
import org.constellation.engine.register.repository.ServiceRepository;
import org.constellation.engine.register.repository.StyleRepository;
import org.constellation.engine.register.repository.UserRepository;
import org.constellation.utils.ISOMarshallerPool;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business facade for data.
 *
 * @author Mehdi Sidhoum (Geomatys).
 * @version 0.9
 * @since 0.9
 */

@Component
public class DataBusiness {
    /**
     * Used for debugging purposes.
     */
    private static final Logger LOGGER = Logging.getLogger(DataBusiness.class);
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
    private DataRepository dataRepository;
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
     * Injected service repository.
     */
    @Inject
    private ServiceRepository serviceRepository;
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
     * Return the {@linkplain Provider provider} for the given {@linkplain Data data} identifier.
     *
     * @param dataId {@link Data} identifier
     * @return a {@linkplain Provider provider}
     */
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
    public DefaultMetadata loadIsoDataMetadata(final String providerId,
                                               final QName name) throws ConstellationException{
        DefaultMetadata metadata = null;
        final Data data = dataRepository.findByNameAndNamespaceAndProviderIdentifier(name.getLocalPart(), name.getNamespaceURI(), providerId);
        final MarshallerPool pool = ISOMarshallerPool.getInstance();
        try {
            if (data != null && data.getIsoMetadata() != null) {
                final InputStream sr = new ByteArrayInputStream(data.getIsoMetadata().getBytes("UTF-8"));
                final Unmarshaller m = pool.acquireUnmarshaller();
                metadata = (DefaultMetadata) m.unmarshal(sr);
                pool.recycle(m);
            }
        } catch (UnsupportedEncodingException | JAXBException e) {
            throw new ConstellationException(e);
        }
        return metadata;
    }

    /**
     * Search and returns result as list of {@link Data} for given query string.
     * @param queryString the lucene query.
     * @return list of {@link Data}
     * @throws ParseException
     * @throws IOException
     */
    public List<Data> searchOnMetadata(final String queryString) throws ParseException, IOException {
        final List<Data> result = new ArrayList<>();
        final Set<Integer> ids = indexEngine.searchOnMetadata(queryString);
        for (final Integer dataId : ids){
            result.add(dataRepository.findById(dataId));
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
        final StringWriter sw = new StringWriter();
        try {
            final Marshaller marshaller = ISOMarshallerPool.getInstance().acquireMarshaller();
            marshaller.marshal(metadata, sw);
        } catch (JAXBException ex) {
            throw new ConstellationException(ex);
        }
        final Data data = dataRepository.findByNameAndNamespaceAndProviderIdentifier(name.getLocalPart(), name.getNamespaceURI(), providerId);
        data.setIsoMetadata(sw.toString());
        data.setMetadataId(metadata.getFileIdentifier());
        dataRepository.update(data);
        indexEngine.addMetadataToIndex(metadata, data.getId());
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
    public CoverageMetadataBean loadDataMetadata(final String providerIdentifier,
                                                 final QName name,
                                                 final MarshallerPool pool) throws ConstellationException {
        CoverageMetadataBean metadata = null;
        try {
            final Data data = dataRepository.findByNameAndNamespaceAndProviderIdentifier(name.getLocalPart(), name.getNamespaceURI(), providerIdentifier);
            if (data != null && data.getMetadata() != null) {
                final InputStream sr = new ByteArrayInputStream(data.getMetadata().getBytes());
                final Unmarshaller m = pool.acquireUnmarshaller();
                metadata = (CoverageMetadataBean) m.unmarshal(sr);
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
    public DataBrief getDataBrief(final QName fullName,
                                  final String providerIdentifier) throws ConstellationException {
        final Data data = dataRepository.findByNameAndNamespaceAndProviderIdentifier(fullName.getLocalPart(), fullName.getNamespaceURI(), providerIdentifier);
        final List<Data> datas = new ArrayList<>();
        datas.add(data);
        final List<DataBrief> dataBriefs = getDataBriefFrom(datas);
        if (dataBriefs != null && dataBriefs.size() == 1) {
            return dataBriefs.get(0);
        }
        throw new ConstellationException(new Exception("Problem : DataBrief Construction is null or multiple"));
    }

    /**
     * Returns a list of {@link DataBrief} for given metadata identifier.
     *
     * @param metadataId given metadata identifier.
     * @return list of {@link DataBrief}.
     */
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
            if (user.isPresent()) {
                db.setOwner(user.get().getLogin());
            }
            db.setName(data.getName());
            db.setNamespace(data.getNamespace());
            db.setDate(new Date(data.getDate()));
            db.setProvider(getProviderIdentifier(data.getProvider()));
            db.setType(data.getType());
            db.setSubtype(data.getSubtype());
            db.setSensorable(data.isSensorable());
            db.setTargetSensor(sensorRepository.getLinkedSensors(data));

            final List<StyleBrief> styleBriefs = new ArrayList<>(0);
            for (final Style style : styles) {
                final StyleBrief sb = new StyleBrief();
                sb.setId(style.getId());
                sb.setType(style.getType());
                sb.setProvider(getProviderIdentifier(style.getProvider()));
                sb.setDate(new Date(style.getDate()));
                sb.setName(style.getName());
                sb.setOwner(style.getOwner());
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
    public void deleteData(final QName name, final String providerIdentifier) {
        final Provider provider = providerRepository.findByIdentifier(providerIdentifier);
        if (provider != null) {
            dataRepository.delete(name.getNamespaceURI(), name.getLocalPart(), provider.getId());
        }
    }

    /**
     * Proceed to remove data for provider identifier.
     * @param providerIdentifier given provider identifier.
     */
    public void deleteDataForProvider(final String providerIdentifier) {
        final Provider provider = providerRepository.findByIdentifier(providerIdentifier);
        if (provider != null) {
            final List<Data> datas = dataRepository.findByProviderId(provider.getId());
            for (final Data data : datas) {
                dataRepository.delete(data.getId());
            }
        }
    }

    /**
     * Proceed to remove all data.
     */
    public void deleteAll() {
        final List<Data> datas = dataRepository.findAll();
        for (final Data data : datas) {
            dataRepository.delete(data.getId());
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
    public void create(final QName name, final String providerIdentifier,
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
            dataRepository.create(data);
        }
    }

    /**
     * Update data visibility for given data name and provider identifier.
     * @param name data name.
     * @param providerIdentifier provider identifier.
     * @param visibility value to set
     */
    public void updateDataVisibility(final QName name,
                                     final String providerIdentifier,
                                     boolean visibility) {
        final Data data = dataRepository.findByNameAndNamespaceAndProviderIdentifier(name.getLocalPart(),
                name.getNamespaceURI(),
                providerIdentifier);
        data.setVisible(visibility);
        dataRepository.update(data);
    }

    /**
     * Proceed to add a data domain.
     * @param dataId given data Id.
     * @param domainId given domain Id.
     */
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
    public synchronized void removeDataFromProvider(final String providerID) {
        final Provider p = providerRepository.findByIdentifier(providerID);
        if (p != null) {
            final List<Data> datas = dataRepository.findByProviderId(p.getId());
            for (final Data data : datas) {
                dataRepository.delete(data.getId());
            }
        }
    }
}
