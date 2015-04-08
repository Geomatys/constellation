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

/**
 * @author Cédric Briançon (Geomatys)
 */
public interface IDatasetBusiness {
    
    /**
     * Create and insert then returns a new dataset for given parameters.
     * @param identifier dataset identifier.
     * @param metadataId metadata identifier.
     * @param metadataXml metadata content as xml string.
     * @param owner
     * @return {@link Dataset}.
     */
    Dataset createDataset(String identifier, String metadataId, String metadataXml, Integer owner) throws ConfigurationException;
    
    Dataset createDataset(String identifier, DefaultMetadata metadataXml, Integer owner) throws ConfigurationException;

    /**
     * Proceed to update metadata for given dataset identifier.
     *
     * @param datasetIdentifier given dataset identifier.
     * @param metadata metadata as {@link org.apache.sis.metadata.iso.DefaultMetadata} to update.
     * @throws ConfigurationException
     */
    void updateMetadata(final String datasetIdentifier, final DefaultMetadata metadata) throws ConfigurationException;

    /**
     * Get metadata for given dataset identifier.
     *
     * @param datasetIdentifier given dataset identifier.
     * @return {@link org.apache.sis.metadata.iso.DefaultMetadata}.
     * @throws ConfigurationException for JAXBException
     */
    DefaultMetadata getMetadata(final String datasetIdentifier) throws ConfigurationException;

    /**
     * Proceed to extract metadata from reader and fill additional info
     * then save metadata in dataset.
     *
     * @param providerId given provider identifier.
     * @param dataType data type vector or raster.
     * @throws ConfigurationException
     */
    void saveMetadata(final String providerId, final String dataType) throws ConfigurationException;
    
    void removeDataset(final String datasetIdentifier) throws ConfigurationException;

    /**
     * Get dataset for given identifier.
     *
     * @param identifier dataset identifier.
     * @return {@link Dataset}.
     */
    Dataset getDataset(String identifier);

    /**
     * Get all dataset from dataset table.
     * @return list of {@link Dataset}.
     */
    List<Dataset> getAllDataset();

    /**
     * Proceed to link data to dataset.
     *
     * @param dataset given dataset.
     * @param datasFromProviderId given data to link.
     */
    void linkDataTodataset(Dataset dataset, List<Data> datasFromProviderId);

    /**
     * Search and returns result as list of {@link Dataset} for given query string.
     * @param queryString the lucene query.
     * @return list of {@link Dataset}
     * @throws org.constellation.admin.exception.ConstellationException
     * @throws IOException
     */
    List<Dataset> searchOnMetadata(String queryString) throws IOException, ConstellationException;
    
    void addProviderDataToDataset(final String datasetId, final String providerId) throws ConfigurationException;
    
    DataSetBrief getDatasetBrief(final Integer dataSetId, List<DataBrief> children);

}
