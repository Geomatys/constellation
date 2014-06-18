package org.constellation.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import org.constellation.admin.dao.ProviderRecord;
import org.constellation.admin.util.IOUtilities;
import org.constellation.engine.register.Data;
import org.constellation.engine.register.Provider;
import org.constellation.engine.register.Style;
import org.constellation.engine.register.repository.ProviderRepository;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.springframework.stereotype.Component;

@Component
public class ProviderBusiness {

    @Inject
    ProviderRepository providerRepository;

    @Inject
    private org.constellation.security.SecurityManager securityManager;
    
    public List<Provider> getProviders() {
        return providerRepository.findAll();
    }
    
    public Provider getProvider(final String identifier) {
        return providerRepository.findByIdentifier(identifier);
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
}
