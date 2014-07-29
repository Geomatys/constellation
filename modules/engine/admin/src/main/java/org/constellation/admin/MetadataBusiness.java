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

import org.constellation.engine.register.Data;
import org.constellation.engine.register.Provider;
import org.constellation.engine.register.Service;
import org.constellation.engine.register.repository.DataRepository;
import org.constellation.engine.register.repository.ProviderRepository;
import org.constellation.engine.register.repository.ServiceRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author guilhem
 */
@Component
public class MetadataBusiness {
    
    @Inject
    private DataRepository dataRepository;
    
    @Inject
    private ProviderRepository providerRepository;
    
    @Inject
    private ServiceRepository serviceRepository;
 
    public String searchMetadata(final String metadataId, final boolean includeService)  {
        final Provider provider = providerRepository.findByMetadataId(metadataId);
        if (provider != null) {
            return provider.getMetadataIso();
        }
        
        final Data data = dataRepository.findByMetadataId(metadataId);
        if (data != null) {
            return data.getIsoMetadata();
        }
        
        if (includeService) {
            final Service service = serviceRepository.findByMetadataId(metadataId);
            if (service != null) {
                return service.getMetadataIso();
            }
        }
        return null;
    }
    
    public boolean existInternalMetadata(final String metadataID, final boolean includeService) {
        return searchMetadata(metadataID, includeService) != null;
    }
    
    public List<String> getInternalMetadataIds(final boolean includeService) {
        final List<String> results = new ArrayList<>();
        final List<Provider> providers = providerRepository.findAll();
        for (Provider record : providers) {
            if (record.getMetadataIso() != null) {
                results.add(record.getMetadataId());
            }
        }
        if (includeService) {
            final List<Service> services = serviceRepository.findAll();
            for (Service record : services) {
                if (record.getMetadataIso() != null) {
                    results.add(record.getMetadataId());
                }
            }
        }
        final List<Data> datas = dataRepository.findAll();
        for (Data record : datas) {
            if (record.isVisible() && record.getIsoMetadata() != null) {
                results.add(record.getMetadataId());
            }
        }
        
        return results;
    }
}
