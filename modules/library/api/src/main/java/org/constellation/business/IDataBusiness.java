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

import java.io.File;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.admin.exception.ConstellationException;
import org.constellation.configuration.CstlConfigurationRuntimeException;
import org.constellation.configuration.DataBrief;
import org.constellation.dto.CoverageMetadataBean;
import org.constellation.dto.FileBean;
import org.constellation.dto.ParameterValues;
import org.constellation.engine.register.Data;
import org.constellation.engine.register.Provider;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.List;
import javax.xml.bind.JAXBException;

import org.constellation.configuration.ConfigurationException;
import org.constellation.dto.MetadataLists;
import org.constellation.engine.register.Dataset;
import org.geotoolkit.process.coverage.statistics.ImageStatistics;

/**
 * @author Cédric Briançon (Geomatys)
 */
public interface IDataBusiness {
    
    void deleteData(QName qName, String id);

    Data create(QName name, String providerIdentifier, String type, boolean sensorable, boolean included, String subType, String metadataXml);

    Data create(QName name, String providerIdentifier, String type, boolean sensorable, boolean included, Boolean rendered, String subType, String metadataXml);

    void removeDataFromProvider(String providerId);

    DataBrief getDataBrief(QName dataName, Integer id) throws ConstellationException;

    DataBrief getDataBrief(QName fullName, String providerIdentifier) throws ConstellationException;

    DefaultMetadata loadIsoDataMetadata(String providerID, QName qName)  throws ConstellationException;

    DefaultMetadata loadIsoDataMetadata(int dataId) throws ConstellationException;
    
    Dataset getDatasetForData(String providerID, QName qName) throws ConstellationException;

    Dataset getDatasetForData(int dataId) throws ConstellationException;

    void deleteAll();

    List<Data> searchOnMetadata(String search) throws IOException, ConstellationException;

    void updateDataIncluded(int dataId, boolean included) throws ConfigurationException;

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
     * Give subfolder list from a server file path
     *
     * @param path server file path
     * @param filtered {@code True} if we want to keep only known files.
     * @param onlyXML flag to list only xml files used list metadata xml.
     * @return the file list
     */
    List<FileBean> getFilesFromPath(final String path, final boolean filtered, final boolean onlyXML) throws ConstellationException;

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
    
    MetadataLists getMetadataCodeLists();

    /**
     * Update {@link org.constellation.engine.register.Data#isRendered()} attribute that define
     * if a data is Rendered or Geophysic.
     *
     * @param fullName data name
     * @param providerIdentifier provider identifier name
     * @param isRendered if true data is Rendered, otherwise it's Geophysic
     */
    void updateDataRendered(QName fullName, String providerIdentifier, boolean isRendered);

    /**
     * Update {@link org.constellation.engine.register.Data#datasetId} attribute.
     *
     * @param fullName data name
     * @param providerIdentifier provider identifier name
     * @param datasetId dataset Id value to set
     */
    void updateDataDataSetId(QName fullName, String providerIdentifier, Integer datasetId);

    /**
     * Update hidden for data
     * @param dataId
     * @param value
     */
    void updateHidden(final int dataId, boolean value);

    void linkDataToData(final int dataId, final int childId);

    List<Data> getDataLinkedData(final int dataId);
    
    String marshallMetadata(final DefaultMetadata metadata) throws JAXBException;
    
    DefaultMetadata unmarshallMetadata(final String metadata) throws JAXBException;

    DefaultMetadata unmarshallMetadata(final File metadata) throws JAXBException;

    void uploadCleaner();
}
