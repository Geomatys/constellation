package org.constellation.admin;

import com.google.common.base.Optional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.constellation.admin.util.IOUtilities;
import org.constellation.api.ProviderType;
import org.constellation.business.IProviderBusiness;
import org.constellation.engine.register.CstlUser;
import org.constellation.engine.register.Data;
import org.constellation.engine.register.Provider;
import org.constellation.engine.register.Style;
import org.constellation.engine.register.repository.DatasetRepository;
import org.constellation.engine.register.repository.ProviderRepository;
import org.constellation.engine.register.repository.UserRepository;
import org.opengis.parameter.GeneralParameterValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProviderBusiness implements IProviderBusiness {

    @Inject
    private UserRepository userRepository;

    @Inject
    private ProviderRepository providerRepository;
    
    @Inject
    private DatasetRepository datasetRepository;

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
        final List<Provider> providers = providerRepository.findAll();
        for (Provider p : providers) {
            ids.add(p.getIdentifier());
        }
        return ids;
    }

    public void removeProvider(final String identifier) {
        datasetRepository.removeForProvider(identifier);
        providerRepository.deleteByIdentifier(identifier);
    }

    public void removeAll() {
        final List<Provider> providers = providerRepository.findAll();
        for (Provider p : providers) {
            datasetRepository.removeForProvider(p.getIdentifier());
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

    public Provider createProvider(final String identifier, final String parent, final ProviderType type, final String serviceName,
            final GeneralParameterValue config) throws IOException {
        Provider provider = new Provider();
        Optional<CstlUser> user = userRepository.findOne(securityManager.getCurrentUserLogin());
        if (user.isPresent()) {
            provider.setOwner(user.get().getId());
        }
        provider.setParent(parent);
        provider.setType(type.name());
        provider.setConfig(IOUtilities.writeParameter(config));
        provider.setIdentifier(identifier);
        // TODO very strange !!!!
        provider.setImpl(serviceName);
        return providerRepository.insert(provider);

    }
}
