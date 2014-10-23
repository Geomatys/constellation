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
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.admin.exception.ConstellationException;
import org.constellation.configuration.CstlConfigurationRuntimeException;
import org.constellation.configuration.DataBrief;
import org.constellation.dto.CoverageMetadataBean;
import org.constellation.dto.ParameterValues;
import org.constellation.engine.register.Data;
import org.constellation.engine.register.Provider;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;

import org.constellation.configuration.ConfigurationException;
import org.constellation.engine.register.Dataset;
import org.geotoolkit.process.coverage.statistics.ImageStatistics;

/**
 * @author Cédric Briançon (Geomatys)
 */
public interface IDataBusiness {
    
    void deleteData(QName qName, String id);

    Data create(QName name, String providerIdentifier, String type, boolean sensorable, boolean visible, String subType, String metadataXml);

    void removeDataFromProvider(String providerId);

    DataBrief getDataBrief(QName dataName, Integer id) throws ConstellationException;

    DataBrief getDataBrief(QName fullName, String providerIdentifier) throws ConstellationException;

    DefaultMetadata loadIsoDataMetadata(String providerID, QName qName)  throws ConstellationException;

    DefaultMetadata loadIsoDataMetadata(int dataId) throws ConstellationException;
    
    Dataset getDatasetForData(String providerID, QName qName) throws ConstellationException;

    Dataset getDatasetForData(int dataId) throws ConstellationException;

    void deleteAll();

    List<Data> searchOnMetadata(String search) throws IOException, ConstellationException;

    void updateDataVisibility(QName name, String providerIdentifier, boolean visibility) throws ConfigurationException;

    DataBrief getDataLayer(String layerAlias, String providerid);

    CoverageMetadataBean loadDataMetadata(String providerId, QName name, MarshallerPool instance);

    List<DataBrief> getDataBriefsFromMetadataId(String id);

    Provider getProvider(int id);

    void addDataToDomain(int dataId, int domainId);

    void removeDataFromDomain(int dataId, int domainId) throws CstlConfigurationRuntimeException;

    List<DataBrief> getDataBriefsFromDatasetId(Integer dataSetId);

    ParameterValues getVectorDataColumns(int id) throws DataStoreException;

    List<Data> findByDatasetId(final Integer datasetId);

    void updateMetadata(String providerId, QName dataName, Integer domainId, DefaultMetadata metadata) throws ConfigurationException;
    
    String getTemplate(final QName dataName, final String dataType) throws ConfigurationException;

    /**
     * Returns {@link Data} instance for given data id.
     * @param id given data id.
     * @return {@link Data} object.
     * @throws ConfigurationException
     */
    Data findById(final Integer id)throws ConfigurationException;

    /**
     * Get and parse data statistics.
     * @param dataId
     * @return ImageStatistics object or null if data is not a coverage or if Statistics were not computed.
     * @throws ConfigurationException
     */
    ImageStatistics getDataStatistics(final int dataId) throws ConfigurationException;

    /**
     * Run {@link org.constellation.business.IDataCoverageJob#asyncUpdateDataStatistics(int)}
     * on each coverage type data without computed statistics.
     */
    void computeEmptyDataStatistics();
}
