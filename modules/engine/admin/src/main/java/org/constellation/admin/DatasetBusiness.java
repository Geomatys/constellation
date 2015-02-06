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

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.google.common.base.Optional;
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
import org.constellation.configuration.TargetNotFoundException;
import org.constellation.engine.register.CstlUser;
import org.constellation.engine.register.Data;
import org.constellation.engine.register.Dataset;
import org.constellation.engine.register.Metadata;
import org.constellation.engine.register.Provider;
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
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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
public class DatasetBusiness extends InternalCSWSynchronizer implements IDatasetBusiness {

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
    protected MetadataRepository metadataRepository;
    /**
     * Injected lucene index engine.
     */
    @Inject
    private IndexEngine indexEngine;


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
     * Get all dataset from dataset table.
     * @return list of {@link Dataset}.
     */
    @Override
    public List<Dataset> getAllDataset() {
        return datasetRepository.findAll();
    }

    /**
     * Get dataset for given identifier and domain id.
     *
     * @param datasetIdentifier given dataset identifier.
     * @param domainId domain id.
     * @return {@link Dataset}.
     */
    @Override
    public Dataset getDataset(final String datasetIdentifier, final int domainId) {
        return datasetRepository.findByIdentifierAndDomainId(datasetIdentifier, domainId);
    }

    /**
     * Get dataset for given identifier.
     *
     * @param identifier dataset identifier.
     * @return {@link Dataset}.
     */
    @Override
    public Dataset getDataset(final String identifier) {
        return datasetRepository.findByIdentifier(identifier);
    }

    /**
     * Create and insert then returns a new dataset for given parameters.
     * @param identifier dataset identifier.
     * @param metadataId metadata identifier.
     * @param metadataXml metadata content as xml string.
     * @param owner
     * @return {@link Dataset}.
     */
    @Override
    @Transactional
    public Dataset createDataset(final String identifier, final String metadataId, final String metadataXml, final Integer owner) {
        Dataset ds = new Dataset(identifier, owner, System.currentTimeMillis(), null,0);
        ds = datasetRepository.insert(ds);
        if (metadataId != null && metadataXml != null) {
            final Metadata metadata = new Metadata(metadataId, metadataXml, null, ds.getId(), null);
            metadataRepository.create(metadata);
        }
        return ds;
    }

    @Override
    @Transactional
    public Dataset createDataset(String identifier, DefaultMetadata metadata, Integer owner) throws ConfigurationException {
        String metadataString = null;
        String metadataId = null;
        if (metadata != null) {
            metadataId = metadata.getFileIdentifier();
            try {
                final MarshallerPool pool = getMarshallerPool();
                if(pool != null){
                    final Marshaller marshaller = pool.acquireMarshaller();
                    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    marshaller.marshal(metadata, outputStream);
                    pool.recycle(marshaller);
                    metadataString = outputStream.toString();
                }else {
                    metadataString = marshallMetadata(metadata);
                }
            } catch (JAXBException ex) {
                throw new ConfigurationException("Unable to marshall the dataset metadata", ex);
            }
        }
        return createDataset(identifier, metadataId, metadataString, owner);
    }
    /**
     * Get metadata for given dataset identifier and domain id.
     *
     * @param datasetIdentifier given dataset identifier.
     * @param domainId given domain id.
     * @return {@link org.apache.sis.metadata.iso.DefaultMetadata}.
     * @throws ConfigurationException for JAXBException
     */
    @Override
    public DefaultMetadata getMetadata(final String datasetIdentifier, final int domainId) throws ConfigurationException {
        final Dataset dataset = getDataset(datasetIdentifier, domainId);
        if (dataset != null) {
            final Metadata metaRecord = metadataRepository.findByDatasetId(dataset.getId());
            if (metaRecord != null) {
                final MarshallerPool pool = getMarshallerPool();
                final String metadataStr = metaRecord.getMetadataIso();
                if(metadataStr == null){
                    throw new ConfigurationException("Unable to get metadata for dataset identifier "+datasetIdentifier);
                }
                try {
                    final DefaultMetadata metadata;
                    if(pool != null) {
                        final Unmarshaller unmarshaller = pool.acquireUnmarshaller();
                        final byte[] byteArray = metadataStr.getBytes();
                        final ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
                        metadata = (DefaultMetadata) unmarshaller.unmarshal(bais);
                        pool.recycle(unmarshaller);
                    } else {
                        metadata = unmarshallMetadata(metadataStr);
                    }
                    metadata.prune();
                    return metadata;
                } catch (JAXBException ex) {
                    throw new ConfigurationException("Unable to unmarshall the dataset metadata", ex);
                }
            }
        }
        return null;
    }

    /**
     * Returns {@link Node} that represents the metadata document of dataset.
     * @param datasetIdentifier the given dataset identifier.
     * @param domainId domain id.
     * @return {@link Node}
     * @throws ConfigurationException
     */
    @Override
    public Node getMetadataNode(final String datasetIdentifier, int domainId) throws ConfigurationException {
        final Dataset dataset = getDataset(datasetIdentifier, domainId);
        if (dataset != null) {
            final Metadata metaRecord = metadataRepository.findByDatasetId(dataset.getId());
            if (metaRecord != null) {
                return getNodeFromString(metaRecord.getMetadataIso());
            }
        }
        return null;
    }

    /**
     * Convert iso metadata string xml to w3c document.
     *
     * @param metadataStr the given metadata xml as string.
     * @return {@link Node} that represents the metadata in w3c document format.
     * @throws ConfigurationException
     */
    protected Node getNodeFromString(final String metadataStr) throws ConfigurationException {
        if(metadataStr == null) {
            return null;
        }
        try {
            final InputSource source = new InputSource(new StringReader(metadataStr));
            final DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            final Document document = docBuilder.parse(source);
            return document.getDocumentElement();
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new ConfigurationException("Unable to get w3c node for metadata of dataset!", ex);
        }
    }

    /**
     * Proceed to update metadata for given dataset identifier.
     *
     * @param datasetIdentifier given dataset identifier.
     * @param domainId given domain id.
     * @param metadata metadata as {@link org.apache.sis.metadata.iso.DefaultMetadata} to update.
     * @throws ConfigurationException
     */
    @Override
    @Transactional
    public void updateMetadata(final String datasetIdentifier, final Integer domainId,
                               final DefaultMetadata metadata) throws ConfigurationException {
        String metadataString = null;
        try {
            final MarshallerPool pool = getMarshallerPool();
            if(pool != null) {
                final Marshaller marshaller = pool.acquireMarshaller();
                final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                marshaller.marshal(metadata, outputStream);
                pool.recycle(marshaller);
                metadataString = outputStream.toString();
            }else {
                metadataString = marshallMetadata(metadata);
            }
        } catch (JAXBException ex) {
            throw new ConfigurationException("Unable to marshall the dataset metadata", ex);
        }
        
        final Dataset dataset = datasetRepository.findByIdentifierAndDomainId(datasetIdentifier, domainId);
        if (dataset != null) {
            Metadata metadataRecord = metadataRepository.findByDatasetId(dataset.getId());
            if (metadataRecord != null) {
                metadataRecord.setMetadataIso(metadataString);
                metadataRecord.setMetadataId(metadata.getFileIdentifier());
                metadataRepository.update(metadataRecord);
            } else {
                metadataRecord = new Metadata(metadata.getFileIdentifier(), metadataString, null, dataset.getId(), null);
                metadataRepository.create(metadataRecord);
            }
            indexEngine.addMetadataToIndexForDataset(metadata, dataset.getId());
            // update internal CSW index
            updateInternalCSWIndex(metadata.getFileIdentifier(), domainId, true);
        } else {
            throw new TargetNotFoundException("Dataset :" + datasetIdentifier + " not found");
        }
    }

    @Override
    @Transactional
    public void updateMDCompletion(final Integer datasetID, final Integer rating) {
        final Dataset dataset = datasetRepository.findById(datasetID);
        dataset.setMdCompletion(rating);
        datasetRepository.update(dataset);
    }

    /**
     * Proceed to update metadata for given dataset identifier.
     *
     * @param datasetIdentifier given dataset identifier.
     * @param domainId domain id.
     * @param metaId metadata identifier.
     * @param metadataXml metadata as xml string content.
     * @throws ConfigurationException
     */
    @Transactional
    public void updateMetadata(final String datasetIdentifier, final Integer domainId,
                               final String metaId, final String metadataXml) throws ConfigurationException {
        final Dataset dataset = datasetRepository.findByIdentifierAndDomainId(datasetIdentifier, domainId);
        if (dataset != null) {
            Metadata metadataRecord = metadataRepository.findByDatasetId(dataset.getId());
            if (metadataRecord != null) {
                metadataRecord.setMetadataIso(metadataXml);
                metadataRecord.setMetadataId(metaId);
                metadataRepository.update(metadataRecord);
            } else {
                metadataRecord = new Metadata(metaId, metadataXml, null, dataset.getId(), null);
                metadataRepository.create(metadataRecord);
            }
        } else {
            throw new TargetNotFoundException("Dataset :" + datasetIdentifier + " not found");
        }
    }

    /**
     * Proceed to extract metadata from reader and fill additional info
     * then save metadata in dataset.
     *
     * @param providerId given provider identifier.
     * @param dataType data type vector or raster.
     * @throws ConfigurationException
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

        final DefaultMetadata templateMetadata = MetadataUtilities.getTemplateMetadata(prop);

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
        try{
            uploadedMetadata = getMetadata(providerId,-1);
        }catch(Exception ex){
            uploadedMetadata = null;
        }
        if(uploadedMetadata != null){
            try {
                mergedMetadata = MetadataUtilities.mergeMetadata(uploadedMetadata,mergedMetadata);
            } catch (NoSuchIdentifierException | ProcessException ex) {
                LOGGER.log(Level.WARNING, "error while merging built metadata with uploaded metadata!", ex);
            }
        }
        mergedMetadata.prune();

        //Save metadata
        updateMetadata(providerId, -1, mergedMetadata);
    }

    /**
     * Proceed to link data to dataset.
     *
     * @param ds given dataset.
     * @param datas given data to link.
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
     * Search and returns result as list of {@link Dataset} for given query string.
     * @param queryString the lucene query.
     * @return list of {@link Dataset}
     * @throws org.constellation.admin.exception.ConstellationException
     * @throws IOException
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
    public void removeDataset(String datasetIdentifier, int domainId) throws ConfigurationException {
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
                Metadata meta = metadataRepository.findByDataId(data.getId());
                if (meta != null) {
                    updateInternalCSWIndex(meta.getMetadataId(), domainId, false);
                }
                dataRepository.removeDataFromAllCSW(data.getId());
            }

            // 2. cleanup provider if empty
            for (Integer providerID : involvedProvider) {
                boolean remove = true;
                List<Data> providerData = dataRepository.findByProviderId(providerID);
                for (Data pdata : providerData) {
                    if (pdata.isIncluded()) {
                        remove = false;
                        break;
                    }
                }
                if (remove) {
                    //notify pre delete
                    for (Data pdata : providerData) {
                        dataBusinessListener.preDataDelete(pdata);
                    }

                    final Provider p = providerRepository.findOne(providerID);
                    final DataProvider dp = DataProviders.getInstance().getProvider(p.getIdentifier());
                    DataProviders.getInstance().removeProvider(dp);
                    providerRepository.delete(providerID);

                    //notify post delete
                    for (Data pdata : providerData) {
                        dataBusinessListener.postDataDelete(pdata);
                    }

                    final File provDir = ConfigDirectory.getDataIntegratedDirectory(p.getIdentifier());
                    FileUtilities.deleteDirectory(provDir);
                }
            }
            // 3. remove internal csw link
            datasetRepository.removeDatasetFromAllCSW(ds.getId());
            
            // 4. remove dataset
            indexEngine.removeDatasetMetadataFromIndex(ds.getId());
            datasetRepository.remove(ds.getId());
            
            // update internal CSW index
            final Metadata meta = metadataRepository.findByDatasetId(ds.getId());
            if (meta != null) {
                updateInternalCSWIndex(meta.getMetadataId(), domainId, false);
            }
        }
    }
    
    @Override
    public String getTemplate(final String datasetId, final String dataType) throws ConfigurationException {
        //get template name
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
    
    protected MarshallerPool getMarshallerPool() {
        return null; //in constellation this should always return null, since this method can be overrided by sub-project.
    }

    protected String marshallMetadata(final DefaultMetadata metadata) throws JAXBException {
        return XML.marshal(metadata);
    }

    protected DefaultMetadata unmarshallMetadata(final String metadata) throws JAXBException {
        return (DefaultMetadata) XML.unmarshal(metadata);
    }
}
