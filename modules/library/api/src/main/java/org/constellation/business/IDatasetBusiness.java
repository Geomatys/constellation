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
package org.constellation.business;

import java.io.IOException;
import java.util.List;

import org.apache.sis.metadata.iso.DefaultMetadata;
import org.constellation.admin.exception.ConstellationException;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.DataBrief;
import org.constellation.configuration.DataSetBrief;
import org.constellation.engine.register.jooq.tables.pojos.Data;
import org.constellation.engine.register.jooq.tables.pojos.Dataset;
import org.w3c.dom.Node;

/**
 * @author Cédric Briançon (Geomatys)
 */
public interface IDatasetBusiness {
    
    Dataset createDataset(String identifier, String metadataId, String metadataXml, Integer owner) throws ConfigurationException;
    
    Dataset createDataset(String identifier, DefaultMetadata metadataXml, Integer owner) throws ConfigurationException;

    Dataset getDataset(String datasetId, int domainId);

    void updateMetadata(final String datasetIdentifier,
                        final DefaultMetadata metadata) throws ConfigurationException;

    DefaultMetadata getMetadata(final String datasetIdentifier, final int domainId) throws ConfigurationException;

    void saveMetadata(final String providerIdentifier, final String dataType) throws ConfigurationException;
    
    void removeDataset(final String datasetIdentifier, final int domainId) throws ConfigurationException;

    Dataset getDataset(String datasetId);

    List<Dataset> getAllDataset();

    void linkDataTodataset(Dataset dataset, List<Data> datasFromProviderId);

    List<Dataset> searchOnMetadata(String search) throws IOException, ConstellationException;
    
    void addProviderDataToDataset(final String datasetId, final String providerId) throws ConfigurationException;
    
    DataSetBrief getDatasetBrief(final Integer dataSetId, List<DataBrief> children);

}
