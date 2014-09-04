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
import java.util.List;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.TargetNotFoundException;
import org.constellation.engine.register.Data;
import org.constellation.engine.register.Dataset;
import org.constellation.engine.register.repository.DataRepository;
import org.constellation.engine.register.repository.DatasetRepository;
import org.constellation.utils.ISOMarshallerPool;
import org.springframework.stereotype.Component;

/**
 *
 * Business facade for dataset.
 *
 * @author Guilhem Legal (Geomatys)
 * @author Mehdi Sidhoum (Geomatys).
 * @since 0.9
 */
@Component
public class DatasetBusiness {

    /**
     * Injected dataset repository.
     */
    @Inject
    private DatasetRepository datasetRepository;
    /**
     * Injected data repository.
     */
    @Inject
    private DataRepository dataRepository;

    /**
     * Get all dataset from dataset table.
     * @return list of {@link Dataset}.
     */
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
    public Dataset getDataset(final String datasetIdentifier, final int domainId) {
        return datasetRepository.findByIdentifierAndDomainId(datasetIdentifier, domainId);
    }

    /**
     * Get dataset for given identifier.
     *
     * @param identifier dataset identifier.
     * @return {@link Dataset}.
     */
    public Dataset getDataset(final String identifier) {
        return datasetRepository.findByIdentifier(identifier);
    }

    /**
     * Create and insert then returns a new dataset for given parameters.
     * @param identifier dataset identifier.
     * @param providerId provider id.
     * @param metadataId metadata identifier.
     * @param metadataXml metadata content as xml string.
     * @return {@link Dataset}.
     */
    public Dataset createDataset(final String identifier, final int providerId,
                                 final String metadataId, final String metadataXml) {
        final Dataset ds = new Dataset(identifier, providerId, metadataId, metadataXml);
        return datasetRepository.insert(ds);
    }

    /**
     * Get metadata for given dataset identifier and domain id.
     *
     * @param datasetIdentifier given dataset identifier.
     * @param domainId given domain id.
     * @return {@link org.apache.sis.metadata.iso.DefaultMetadata}.
     * @throws ConfigurationException for JAXBException
     */
    public DefaultMetadata getMetadata(String datasetIdentifier, int domainId) throws ConfigurationException {
        final Dataset dataset = getDataset(datasetIdentifier, domainId);
        final MarshallerPool pool = ISOMarshallerPool.getInstance();
        try {
            final Unmarshaller unmarshaller = pool.acquireUnmarshaller();
            final byte[] byteArray = dataset.getMetadataIso().getBytes();
            final ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
            final DefaultMetadata metadata = (DefaultMetadata) unmarshaller.unmarshal(bais);
            pool.recycle(unmarshaller);
            metadata.prune();
            return metadata;
        } catch (JAXBException ex) {
            throw new ConfigurationException("Unable to unmarshall the dataset metadata", ex);
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
    public void updateMetadata(final String datasetIdentifier, final Integer domainId,
                               final DefaultMetadata metadata) throws ConfigurationException {
        String metadataString = null;
        try {
            final MarshallerPool pool = ISOMarshallerPool.getInstance();
            final Marshaller marshaller = pool.acquireMarshaller();
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            marshaller.marshal(metadata, outputStream);
            metadataString = outputStream.toString();
        } catch (JAXBException ex) {
            throw new ConfigurationException("Unable to marshall the dataset metadata", ex);
        }
        
        final Dataset dataset = datasetRepository.findByIdentifierAndDomainId(datasetIdentifier, domainId);
        if (dataset != null) {
            dataset.setMetadataIso(metadataString);
            dataset.setMetadataId(metadata.getFileIdentifier());
            datasetRepository.update(dataset);
        } else {
            throw new TargetNotFoundException("Dataset :" + datasetIdentifier + " not found");
        }
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
    public void updateMetadata(final String datasetIdentifier, final Integer domainId,
                               final String metaId, final String metadataXml) throws ConfigurationException {
        final Dataset dataset = datasetRepository.findByIdentifierAndDomainId(datasetIdentifier, domainId);
        if (dataset != null) {
            dataset.setMetadataIso(metadataXml);
            dataset.setMetadataId(metaId);
            datasetRepository.update(dataset);
        } else {
            throw new TargetNotFoundException("Dataset :" + datasetIdentifier + " not found");
        }
    }

    /**
     * Proceed to link data to dataset.
     *
     * @param ds given dataset.
     * @param datas given data to link.
     */
    public void linkDataTodataset(final Dataset ds, final List<Data> datas) {
        for (final Data data : datas) {
            data.setDatasetId(ds.getId());
            dataRepository.update(data);
        }
    }
}
