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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.MarshallerPool;
import org.apache.sis.xml.XML;
import org.constellation.admin.exception.ConstellationException;
import org.constellation.admin.index.IndexEngine;
import org.constellation.admin.listener.DefaultDataBusinessListener;
import org.constellation.admin.listener.IDataBusinessListener;
import org.constellation.admin.util.MetadataUtilities;
import org.constellation.business.IDatasetBusiness;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.DataBrief;
import org.constellation.configuration.DataSetBrief;
import org.constellation.configuration.TargetNotFoundException;
import org.constellation.engine.register.jooq.tables.pojos.CstlUser;
import org.constellation.engine.register.jooq.tables.pojos.Data;
import org.constellation.engine.register.jooq.tables.pojos.Dataset;
import org.constellation.engine.register.jooq.tables.pojos.Provider;
import org.constellation.engine.register.repository.DataRepository;
import org.constellation.engine.register.repository.DatasetRepository;
import org.constellation.engine.register.repository.MetadataRepository;
import org.constellation.engine.register.repository.ProviderRepository;
import org.constellation.engine.register.repository.UserRepository;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviders;
import org.constellation.security.SecurityManagerHolder;
import org.constellation.utils.CstlMetadatas;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.temporal.object.TemporalUtilities;
import org.geotoolkit.util.FileUtilities;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.NoSuchIdentifierException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Optional;
import org.constellation.business.IMetadataBusiness;

/**
 *
 * Business facade for dataset.
 *
 * @author Guilhem Legal (Geomatys)
 * @author Mehdi Sidhoum (Geomatys).
 * @since 0.9
 */
@Component("cstlDatasetBusiness")
@Primary
public class DatasetBusiness implements IDatasetBusiness {

    /**
     * Used for debugging purposes.
     */
    protected static final Logger LOGGER = Logging.getLogger(DatasetBusiness.class);

    /**
     * w3c document builder factory.
     */
    protected final DocumentBuilderFactory dbf;

    /**
     * Injected user repository.
     */
    @Inject
    protected UserRepository userRepository;

    /**
     * Injected dataset repository.
     */
    @Inject
    protected DatasetRepository datasetRepository;
    /**
     * Injected data repository.
     */
    @Inject
    protected DataRepository dataRepository;
    /**
     * Injected provider repository.
     */
    @Inject
    private ProviderRepository providerRepository;
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
    
    @Autowired(required = false)
    private IDataBusinessListener dataBusinessListener = new DefaultDataBusinessListener();

    /**
     * Creates a new instance of {@link DatasetBusiness}.
     */
    public DatasetBusiness() {
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Dataset> getAllDataset() {
        return datasetRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dataset getDataset(final String identifier) {
        return datasetRepository.findByIdentifier(identifier);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Dataset createDataset(final String identifier, final String metadataXml, final Integer owner) throws ConfigurationException {
        Dataset ds = new Dataset();
        ds.setIdentifier(identifier);
        ds.setOwner(owner);
        ds.setDate(System.currentTimeMillis());

        ds = datasetRepository.insert(ds);
        if (metadataXml != null) {
            final DefaultMetadata meta = unmarshallMetadata(metadataXml);
            updateMetadata(identifier, meta);
        }
        return ds;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DefaultMetadata getMetadata(final String datasetIdentifier) throws ConfigurationException {
        final Dataset dataset = getDataset(datasetIdentifier);
        if (dataset != null) {
            return metadataBusiness.getIsoMetadataForDataset(dataset.getId());
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void updateMetadata(final String datasetIdentifier,
                               final DefaultMetadata metadata) throws ConfigurationException {
        
        final Dataset dataset = datasetRepository.findByIdentifier(datasetIdentifier);
        if (dataset != null) {
            metadataBusiness.updateMetadata(metadata.getFileIdentifier(), metadata, null, dataset.getId(), null);
            
            indexEngine.addMetadataToIndexForDataset(metadata, dataset.getId());
        } else {
            throw new TargetNotFoundException("Dataset :" + datasetIdentifier + " not found");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void saveMetadata(final String providerId, final String dataType) throws ConfigurationException {
        final DataProvider dataProvider = DataProviders.getInstance().getProvider(providerId);
        DefaultMetadata extractedMetadata;
        String crsName = null;
        if (dataType != null) {
            switch (dataType) {
                case "raster":
                    try {
                        extractedMetadata = MetadataUtilities.getRasterMetadata(dataProvider);
                        crsName = MetadataUtilities.getRasterCRSName(dataProvider);
                    } catch (DataStoreException e) {
                        LOGGER.log(Level.WARNING, "Error when trying to get raster metadata", e);
                        extractedMetadata = new DefaultMetadata();
                    }
                    break;
                case "vector":
                    try {
                        extractedMetadata = MetadataUtilities.getVectorMetadata(dataProvider);
                        crsName = MetadataUtilities.getVectorCRSName(dataProvider);
                    } catch (DataStoreException | TransformException e) {
                        LOGGER.log(Level.WARNING, "Error when trying to get metadata for a shape file", e);
                        extractedMetadata = new DefaultMetadata();
                    }
                    break;
                default:
                    extractedMetadata = new DefaultMetadata();
            }
        } else {
            extractedMetadata = new DefaultMetadata();
        }
        //Update metadata
        final Properties prop = ConfigurationBusiness.getMetadataTemplateProperties();
        final String metadataID = CstlMetadatas.getMetadataIdForDataset(providerId);
        prop.put("fileId", metadataID);
        prop.put("dataTitle", metadataID);
        prop.put("dataAbstract", "");
        final String dateIso = TemporalUtilities.toISO8601(new Date());
        prop.put("isoCreationDate", dateIso);
        prop.put("creationDate", dateIso);
        if("raster".equalsIgnoreCase(dataType)){
            prop.put("dataType", "grid");
        }else if("vector".equalsIgnoreCase(dataType)){
            prop.put("dataType", "vector");
        }

        if(crsName != null){
            prop.put("srs", crsName);
        }

        // get current user name and email and store into metadata contact.
        final String login = SecurityManagerHolder.getInstance().getCurrentUserLogin();
        final Optional<CstlUser> optUser = userRepository.findOne(login);
        if(optUser!=null && optUser.isPresent()){
            final CstlUser user = optUser.get();
            if (user != null) {
                prop.put("contactName", user.getFirstname()+" "+user.getLastname());
                prop.put("contactEmail", user.getEmail());
            }
        }

        //fill in keywords all data name of dataset children.
        final Dataset dataset = getDataset(providerId);
        if(dataset!= null){
            final List<Data> dataList = dataRepository.findAllByDatasetId(dataset.getId());
            if(dataList != null){
                final List<String> keywords = new ArrayList<>();
                for(final Data d : dataList){
                    final String dataName = d.getName();
                    if(!keywords.contains(dataName)){
                        keywords.add(dataName);
                    }
                }
                if(!keywords.isEmpty()){
                    prop.put("keywords",keywords);
                }
            }
        }

        final DefaultMetadata templateMetadata = MetadataUtilities.getTemplateMetadata(prop, "org/constellation/engine/template/mdTemplDataset.xml", getMarshallerPool());

        DefaultMetadata mergedMetadata;
        if (extractedMetadata != null) {
            mergedMetadata = new DefaultMetadata();
            try {
                mergedMetadata = MetadataUtilities.mergeMetadata(templateMetadata, extractedMetadata);
            } catch (NoSuchIdentifierException | ProcessException ex) {
                LOGGER.log(Level.WARNING, "error while merging metadata", ex);
            }
        } else {
            mergedMetadata = templateMetadata;
        }

        //merge with uploaded metadata
        DefaultMetadata uploadedMetadata;
        try {
            uploadedMetadata = getMetadata(providerId);
        } catch (Exception ex) {
            uploadedMetadata = null;
        }
        if (uploadedMetadata != null) {
            try {
                mergedMetadata = MetadataUtilities.mergeMetadata(uploadedMetadata,mergedMetadata);
            } catch (NoSuchIdentifierException | ProcessException ex) {
                LOGGER.log(Level.WARNING, "error while merging built metadata with uploaded metadata!", ex);
            }
        }
        mergedMetadata.prune();

        //Save metadata
        updateMetadata(providerId, mergedMetadata);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void linkDataTodataset(final Dataset ds, final List<Data> datas) {
        for (final Data data : datas) {
            data.setDatasetId(ds.getId());
            dataRepository.update(data);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Dataset> searchOnMetadata(final String queryString) throws IOException, ConstellationException {
        final List<Dataset> result = new ArrayList<>();
        final Set<Integer> ids;
        try {
            ids = indexEngine.searchOnMetadata(queryString, "datasetId");
        } catch( ParseException ex) {
            throw new ConstellationException(ex);
        }
        for (final Integer datasetId : ids){
            final Dataset d = datasetRepository.findById(datasetId);
            if(d!=null){
                result.add(d);
            }
        }
        return result;
    }

    @Override
    @Transactional
    public void addProviderDataToDataset(final String datasetId, final String providerId) throws ConfigurationException {
        final Dataset ds = datasetRepository.findByIdentifier(datasetId);
        if (ds != null) {
            final Provider p = providerRepository.findByIdentifier(providerId);
            if (p != null) {
                final List<Data> datas = dataRepository.findByProviderId(p.getId());
                for (Data data : datas) {
                    data.setDatasetId(ds.getId());
                    dataRepository.update(data);
                }
            } else {
                throw new TargetNotFoundException("Unable to find a profile: " + providerId);
            }
        } else {
            throw new TargetNotFoundException("Unable to find a dataset: " + datasetId);
        }
    }

    @Override
    @Transactional
    public void removeDataset(String datasetIdentifier) throws ConfigurationException {
        final Dataset ds = datasetRepository.findByIdentifier(datasetIdentifier);
        if (ds != null) {
            final Set<Integer> involvedProvider = new HashSet<>();
            final Set<Data> linkedData = new HashSet<>();

            // 1. hide data
            linkedData.addAll(dataRepository.findAllByDatasetId(ds.getId()));

            //find provider with same identifier as dataset
            final Provider linkedProvider = providerRepository.findByIdentifier(ds.getIdentifier());
            if (linkedProvider != null) {
                final List<Data> providerData = dataRepository.findByProviderId(linkedProvider.getId());
                linkedData.addAll(providerData);
                if (providerData.isEmpty()) {
                    //handle empty provider
                    involvedProvider.add(linkedProvider.getId());
                }
            }

            for (Data data : linkedData) {
                data.setIncluded(false);
                data.setDatasetId(null);
                dataRepository.update(data);
                involvedProvider.add(data.getProvider());
                metadataBusiness.deleteDataMetadata(data.getId());
                dataRepository.removeDataFromAllCSW(data.getId());
            }

            // 2. cleanup provider if empty
            for (Integer providerID : involvedProvider) {
                boolean remove = true;
                List<Data> providerData = dataRepository.findByProviderId(providerID);
                for (Data pdata : providerData) {
                    if (pdata.getIncluded()) {
                        remove = false;
                        break;
                    }
                }
                if (remove) {
                    final Provider p = providerRepository.findOne(providerID);
                    final DataProvider dp = DataProviders.getInstance().getProvider(p.getIdentifier());

                    //will remove provider from cache, delete data and delete provider from database.
                    DataProviders.getInstance().removeProvider(dp);

                    final File provDir = ConfigDirectory.getDataIntegratedDirectory(p.getIdentifier());
                    FileUtilities.deleteDirectory(provDir);
                }
            }
            // 3. remove internal csw link
            datasetRepository.removeDatasetFromAllCSW(ds.getId());
            
            // 4. remove metadata
            metadataBusiness.deleteDatasetMetadata(ds.getId());
            
            // 5. remove dataset
            indexEngine.removeDatasetMetadataFromIndex(ds.getId());
            datasetRepository.remove(ds.getId());
        }
    }
    
    protected MarshallerPool getMarshallerPool() {
        return null; //in constellation this should always return null, since this method can be overrided by sub-project.
    }

    @Deprecated
    protected String marshallMetadata(final DefaultMetadata metadata) throws ConfigurationException {
        try {
            final MarshallerPool pool = getMarshallerPool();
            if (pool != null) {
                final Marshaller marshaller = pool.acquireMarshaller();
                final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                marshaller.marshal(metadata, outputStream);
                pool.recycle(marshaller);
                return outputStream.toString();
            } else {
                return XML.marshal(metadata);
            }
        } catch (JAXBException ex) {
            throw new ConfigurationException("Unable to marshall the dataset metadata", ex);
        }
    }

    @Deprecated
    protected DefaultMetadata unmarshallMetadata(final String metadataStr) throws ConfigurationException {
        try {
            final MarshallerPool pool = getMarshallerPool();
            final DefaultMetadata metadata;
            if(pool != null) {
                final Unmarshaller unmarshaller = pool.acquireUnmarshaller();
                final byte[] byteArray = metadataStr.getBytes();
                final ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
                metadata = (DefaultMetadata) unmarshaller.unmarshal(bais);
                pool.recycle(unmarshaller);
            } else {
                metadata = (DefaultMetadata) XML.unmarshal(metadataStr);
            }
            return metadata;
        } catch (JAXBException ex) {
            throw new ConfigurationException("Unable to unmarshall the dataset metadata", ex);
        }
    }

    @Override
    public DataSetBrief getDatasetBrief(Integer dataSetId, List<DataBrief> children) {
        final Dataset dataset = datasetRepository.findById(dataSetId);
        Integer completion = metadataBusiness.getCompletionForDataset(dataSetId);
        String type = null;
        if (!children.isEmpty()) {
            type = children.get(0).getType();
        }
        final Optional<CstlUser> optUser = userRepository.findById(dataset.getOwner());
        String owner = null;
        if(optUser!=null && optUser.isPresent()){
            final CstlUser user = optUser.get();
            if(user != null){
                owner = user.getLogin();
            }
        }
        final DataSetBrief dsb = new DataSetBrief(dataset.getId(),
                dataset.getIdentifier(),
                type, owner, children,
                dataset.getDate(),
                completion);

        return dsb;
    }
}
