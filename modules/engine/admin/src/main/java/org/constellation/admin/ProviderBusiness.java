package org.constellation.admin;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.constellation.engine.register.Provider;
import org.constellation.engine.register.repository.ProviderRepository;
import org.springframework.stereotype.Component;

@Component
public class ProviderBusiness {

    @Inject
    ProviderRepository providerRepository;
    
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

}
