/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.constellation.admin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.engine.register.Dataset;
import org.constellation.engine.register.repository.DatasetRepository;
import org.constellation.utils.ISOMarshallerPool;
import org.springframework.stereotype.Component;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Component
public class DatasetBusiness {
    
    @Inject
    private DatasetRepository datasetRepository;
    
    public Dataset getDataset(String datasetIdentifier, int domainId) {
        return datasetRepository.findByIdentifierAndDomainId(datasetIdentifier, domainId);
    }
    
    public Dataset getDataset(final String identifier) {
        return datasetRepository.findByIdentifier(identifier);
    }
    
    public Dataset createDataset(final String identifier, final int providerId, final String metadataId, final String metadataXml) {
        final Dataset ds = new Dataset(identifier, providerId, metadataId, metadataXml);
        return datasetRepository.insert(ds);
    }
    
    public DefaultMetadata getMetadata(String providerId, int domainId) throws JAXBException {
        final Dataset dataset = getDataset(providerId, domainId);
        final MarshallerPool pool = ISOMarshallerPool.getInstance();
        final Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        final DefaultMetadata metadata = (DefaultMetadata) unmarshaller.unmarshal(new ByteArrayInputStream(dataset.getMetadataIso()
                .getBytes()));
        pool.recycle(unmarshaller);
        metadata.prune();
        return metadata;
    }

    public void updateMetadata(String providerIdentifier, Integer domainId, DefaultMetadata metadata) throws JAXBException {
        final MarshallerPool pool = ISOMarshallerPool.getInstance();
        final Marshaller marshaller = pool.acquireMarshaller();
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        marshaller.marshal(metadata, outputStream);
        final String metadataString = outputStream.toString();
        final Dataset dataset = datasetRepository.findByIdentifierAndDomainId(providerIdentifier, domainId);
        dataset.setMetadataIso(metadataString);
        dataset.setMetadataId(metadata.getFileIdentifier());
        datasetRepository.update(dataset);
    }

    public void updateMetadata(String providerIdentifier, Integer domainId, String metaId, String metadataXml) throws JAXBException {
        final Dataset dataset = datasetRepository.findByIdentifierAndDomainId(providerIdentifier, domainId);
        dataset.setMetadataIso(metadataXml);
        dataset.setMetadataId(metaId);
        datasetRepository.update(dataset);
    }
}
