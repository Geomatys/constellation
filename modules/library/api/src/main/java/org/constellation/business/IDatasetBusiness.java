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

import org.apache.sis.metadata.iso.DefaultMetadata;
import org.constellation.admin.exception.ConstellationException;
import org.constellation.configuration.ConfigurationException;
import org.constellation.engine.register.Data;
import org.constellation.engine.register.Dataset;
import org.w3c.dom.Node;

import java.io.IOException;
import java.util.List;

/**
 * @author Cédric Briançon (Geomatys)
 */
public interface IDatasetBusiness {
    
    Dataset createDataset(String identifier, String metadataId, String metadataXml, Integer owner);

    Dataset getDataset(String datasetId, int domainId);

    void updateMetadata(final String datasetIdentifier, final Integer domainId,
                        final DefaultMetadata metadata) throws ConfigurationException;

    DefaultMetadata getMetadata(final String datasetIdentifier, final int domainId) throws ConfigurationException;

    Dataset getDataset(String datasetId);

    List<Dataset> getAllDataset();

    Node getMetadataNode(String identifier, int domainId) throws ConfigurationException;

    void linkDataTodataset(Dataset dataset, List<Data> datasFromProviderId);

    List<Dataset> searchOnMetadata(String search) throws IOException, ConstellationException;
    
    void addProviderDataToDataset(final String datasetId, final String providerId) throws ConfigurationException;
    
}
