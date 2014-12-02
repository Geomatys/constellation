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

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import org.constellation.business.IMetadataBusiness;
import org.constellation.engine.register.Data;
import org.constellation.engine.register.Dataset;
import org.constellation.engine.register.DatasetXCsw;
import org.constellation.engine.register.Service;
import org.constellation.engine.register.repository.DataRepository;
import org.constellation.engine.register.repository.DatasetRepository;
import org.constellation.engine.register.repository.ServiceRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Business facade for metadata.
 *
 * @author guilhem
 * @author Mehdi Sidhoum (Geomatys).
 * @since 0.9
 */
@Component
@Primary
public class MetadataBusiness implements IMetadataBusiness {
    /**
     * Injected data repository.
     */
    @Inject
    private DataRepository dataRepository;
    /**
     * Injected dataset repository.
     */
    @Inject
    private DatasetRepository datasetRepository;
    /**
     * Injected service repository.
     */
    @Inject
    private ServiceRepository serviceRepository;

    /**
     * Returns the xml as string representation of metadata for given metadata identifier.
     *
     * @param metadataId given metadata identifier
     * @param includeService flag that indicates if service repository will be requested.
     * @return String representation of metadata in xml.
     */
    @Override
    public String searchMetadata(final String metadataId, final boolean includeService)  {
        final Dataset dataset = datasetRepository.findByMetadataId(metadataId);
        if (dataset != null) {
            return dataset.getMetadataIso();
        }
        final Data data = dataRepository.findByMetadataId(metadataId);
        if (data != null && data.isIncluded()) {
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
    
    @Override
    public boolean updateMetadata(final String metadataId, final String xml)  {
        final Dataset dataset = datasetRepository.findByMetadataId(metadataId);
        if (dataset != null) {
            dataset.setMetadataIso(xml);
            datasetRepository.update(dataset);
            return true;
        }
        final Data data = dataRepository.findByMetadataId(metadataId);
        if (data != null) {
            data.setIsoMetadata(xml);
            dataRepository.update(data);
            return true;
        }
        final Service service = serviceRepository.findByMetadataId(metadataId);
        if (service != null) {
            service.setMetadataIso(xml);
            serviceRepository.update(service);
            return true;
        }
        
        // if the metadata is not yet present look for empty metadata object
        final Dataset dataset2 = datasetRepository.findByIdentifierWithEmptyMetadata(metadataId);
        if (dataset2 != null) {
            dataset2.setMetadataIso(xml);
            dataset2.setMetadataId(metadataId);
            datasetRepository.update(dataset2);
            return true;
        }
        
        // unsafe but no better way for now
        final Data data2 = dataRepository.findByIdentifierWithEmptyMetadata(metadataId);
        if (data2 != null) {
            data2.setIsoMetadata(xml);
            data2.setMetadataId(metadataId);
            dataRepository.update(data2);
            return true;
        }
        
        // no possible for service
        return false;
    }

    /**
     * Returns {@code true} if the xml metadata exists for given metadata identifier.
     *
     * @param metadataID given metadata identifier.
     * @param includeService flag that indicates if service repository will be requested.
     * @return boolean to indicates if metadata is present or not.
     */
    @Override
    public boolean existInternalMetadata(final String metadataID, final boolean includeService) {
        return searchMetadata(metadataID, includeService) != null;
    }

    /**
     * Returns a list of all metadata identifiers.
     *
     * @param includeService flag that indicates if service repository will be requested.
     * @return List of string identifiers.
     */
    @Override
    public List<String> getInternalMetadataIds(final boolean includeService) {
        final List<String> results = new ArrayList<>();
        final List<Dataset> datasets = datasetRepository.findAll();
        for (final Dataset record : datasets) {
            if (record.getMetadataIso() != null) {
                results.add(record.getMetadataId());
            }
        }
        if (includeService) {
            final List<Service> services = serviceRepository.findAll();
            for (final Service record : services) {
                if (record.getMetadataIso() != null) {
                    results.add(record.getMetadataId());
                }
            }
        }
        final List<Data> datas = dataRepository.findAll();
        for (final Data record : datas) {
            if (record.isIncluded() && record.getIsoMetadata() != null) {
                results.add(record.getMetadataId());
            }
        }
        return results;
    }

    /**
     * Returns all metadata stored in database.
     *
     * @param includeService given flag to include service's metadata
     * @return List of all metadata as string xml stored in database.
     */
    @Override
    public List<String> getAllMetadata(final boolean includeService) {
        final List<String> results = new ArrayList<>();
        final List<String> allIdentifiers = getInternalMetadataIds(includeService);
        for(final String identifier : allIdentifiers){
            final String metadataStr = searchMetadata(identifier, includeService);
            results.add(metadataStr);
        }
        return results;
    }
    
    @Override
    public List<String> getLinkedMetadataIDs(final String cswIdentifier) {
        final List<String> results = new ArrayList<>();
        final Service service = serviceRepository.findByIdentifierAndType(cswIdentifier, "csw");
        if (service != null) {
            final List<Data> datas    = dataRepository.getCswLinkedData(service.getId());
            for (Data data : datas) {
                if (data.getMetadataId() != null && data.isIncluded()) {
                    results.add(data.getMetadataId());
                }
            }
            
            for (DatasetXCsw dxc : datasetRepository.getCswLinkedDataset(service.getId())) {
                final Dataset ds = datasetRepository.findById(dxc.getDatasetId());
                if (ds.getMetadataId() != null) {
                    results.add(ds.getMetadataId());
                }
                if (dxc.isAllData()) {
                    final List<Data> subDatas = dataRepository.findByDatasetId(dxc.getDatasetId());
                    for (Data data : subDatas) {
                        if (data.getMetadataId() != null && data.isIncluded()) {
                            results.add(data.getMetadataId());
                        }
                    }
                }
            }
        }
        return results;
    }
    
    @Override
    public void linkMetadataIDToCSW(final String metadataId, final String cswIdentifier) {
        final Service service = serviceRepository.findByIdentifierAndType(cswIdentifier, "csw");
        if (service != null) {
            final Dataset dataset = datasetRepository.findByMetadataId(metadataId);
            if (dataset != null) {
                datasetRepository.addDatasetToCSW(service.getId(), dataset.getId(), false);
            }
            final Data data = dataRepository.findByMetadataId(metadataId);
            if (data != null) {
                dataRepository.addDataToCSW(service.getId(), data.getId());
            }
            /*final Service service = serviceRepository.findByMetadataId(metadataId);
            if (service != null) {
                service.setMetadataIso(xml);
                serviceRepository.update(service);
                return true;
            }*/

            // if the metadata is not yet present look for empty metadata object
            final Dataset dataset2 = datasetRepository.findByIdentifierWithEmptyMetadata(metadataId);
            if (dataset2 != null) {
                datasetRepository.addDatasetToCSW(service.getId(), dataset2.getId(), false);
            }

            // unsafe but no better way for now
            final Data data2 = dataRepository.findByIdentifierWithEmptyMetadata(metadataId);
            if (data2 != null) {
                dataRepository.addDataToCSW(service.getId(), data2.getId());
            }

            // no possible for service
        }
    }
    
    @Override
    public void unlinkMetadataIDToCSW(final String metadataId, final String cswIdentifier) {
        final Service service = serviceRepository.findByIdentifierAndType(cswIdentifier, "csw");
        if (service != null) {
            final Dataset dataset = datasetRepository.findByMetadataId(metadataId);
            if (dataset != null) {
                datasetRepository.removeDatasetFromCSW(service.getId(), dataset.getId());
            }
            final Data data = dataRepository.findByMetadataId(metadataId);
            if (data != null) {
                dataRepository.removeDataFromCSW(service.getId(), data.getId());
            }
            /*final Service service = serviceRepository.findByMetadataId(metadataId);
            if (service != null) {
                service.setMetadataIso(xml);
                serviceRepository.update(service);
                return true;
            }*/

            // if the metadata is not yet present look for empty metadata object
            final Dataset dataset2 = datasetRepository.findByIdentifierWithEmptyMetadata(metadataId);
            if (dataset2 != null) {
                datasetRepository.removeDatasetFromCSW(service.getId(), dataset2.getId());
            }

            // unsafe but no better way for now
            final Data data2 = dataRepository.findByIdentifierWithEmptyMetadata(metadataId);
            if (data2 != null) {
                dataRepository.removeDataFromCSW(service.getId(), data2.getId());
            }

            // no possible for service
        }
    }
}
