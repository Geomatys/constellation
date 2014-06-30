package org.constellation.admin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.admin.dao.ProviderRecord;
import org.constellation.admin.util.IOUtilities;
import org.constellation.engine.register.Data;
import org.constellation.engine.register.MetadataIOUtils;
import org.constellation.engine.register.Provider;
import org.constellation.engine.register.Style;
import org.constellation.engine.register.repository.ProviderRepository;
import org.constellation.utils.ISOMarshallerPool;
import org.opengis.parameter.GeneralParameterValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProviderBusiness {

    @Inject
    ProviderRepository providerRepository;

    @Autowired
    private org.constellation.security.SecurityManager securityManager;
    
    public List<Provider> getProviders() {
        return providerRepository.findAll();
    }
    
    public Provider getProvider(final String identifier) {
        return providerRepository.findByIdentifier(identifier);
    }

    public Provider getProvider(String providerIdentifier, int domainId) {
        return providerRepository.findByIdentifierAndDomainId(providerIdentifier, domainId);
    }

    public Provider getProvider(final int id) {
        return providerRepository.findOne(id);
    }
    
    public List<String> getProviderIds() {
        final List<String> ids = new ArrayList<>();
        final List<Provider> providers =  providerRepository.findAll();
        for (Provider p : providers) {
            ids.add(p.getIdentifier());
        }
        return ids;
    }
    
    public void removeProvider(final String identifier) {
        providerRepository.deleteByIdentifier(identifier);
    }
    
    public void removeAll() {
        final List<Provider> providers = providerRepository.findAll();
        for (Provider p : providers) {
            providerRepository.delete(p.getId());
        }
    }
    
    public List<Provider> getProviderChildren(final String identifier) {
        return providerRepository.findChildren(identifier);
    }

    public List<Integer> getProviderIdsForDomain(int domainId) {
        return providerRepository.getProviderIdsForDomain(domainId);
    }

    public List<Data> getDatasFromProviderId(Integer id) {
        return providerRepository.findDatasByProviderId(id);
    }

    public void updateParent(String providerIdentifier, String newParentIdentifier) {
        final Provider provider = getProvider(providerIdentifier);
        provider.setParent(newParentIdentifier);
        providerRepository.update(provider);
    }

    public List<Style> getStylesFromProviderId(Integer providerId) {
        return providerRepository.findStylesByProviderId(providerId);
    }

    public Provider createProvider(final String identifier, final String parent,
                                   final ProviderRecord.ProviderType type, final String serviceName, final GeneralParameterValue config) throws IOException {
        String login = securityManager.getCurrentUserLogin();
        Provider provider = new Provider();
        provider.setOwner(login);
        provider.setParent(parent);
        provider.setType(type.name());
        provider.setConfig(IOUtilities.writeParameter(config));
        provider.setIdentifier(identifier);
        // TODO very strange !!!!
        provider.setImpl(serviceName);
        return providerRepository.insert(provider);

    }


    public DefaultMetadata getMetadata(String providerId, int domainId) throws JAXBException {
        final Provider provider = getProvider(providerId, domainId);
        final MarshallerPool pool = ISOMarshallerPool.getInstance();
        final Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        final DefaultMetadata metadata = (DefaultMetadata)unmarshaller.unmarshal(new ByteArrayInputStream(provider.getMetadata().getBytes()));
        pool.recycle(unmarshaller);
        metadata.prune();
        return metadata;
    }

    public void updateMetadata(String providerIdentifier, Integer domainId, DefaultMetadata metadata) throws JAXBException {
        final MarshallerPool pool = ISOMarshallerPool.getInstance();
        final Marshaller marshaller = pool.acquireMarshaller();
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        marshaller.marshal(metadata,outputStream);
        final String metadataString = outputStream.toString();
        final Provider provider = providerRepository.findByIdentifierAndDomainId(providerIdentifier, domainId);
        provider.setMetadata(metadataString);
        provider.setMetadataId(metadata.getFileIdentifier());
        providerRepository.update(provider);
    }
    
    public void updateMetadata(String providerIdentifier, Integer domainId, String metaId, String metadataXml) throws JAXBException {
        final Provider provider = providerRepository.findByIdentifierAndDomainId(providerIdentifier, domainId);
        provider.setMetadata(metadataXml);
        provider.setMetadataId(metaId);
        providerRepository.update(provider);
    }
}
