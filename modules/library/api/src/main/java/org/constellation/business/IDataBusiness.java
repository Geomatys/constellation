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
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.admin.exception.ConstellationException;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.DataBrief;
import org.constellation.dto.CoverageMetadataBean;
import org.constellation.dto.FileBean;
import org.constellation.dto.ParameterValues;
import org.constellation.database.api.jooq.tables.pojos.Data;
import org.constellation.database.api.jooq.tables.pojos.Dataset;
import org.constellation.database.api.jooq.tables.pojos.Provider;
import org.constellation.database.api.pojo.DataItem;
import org.geotoolkit.metadata.ImageStatistics;

/**
 * @author Cédric Briançon (Geomatys)
 */
public interface IDataBusiness {

    /**
     * Delete data from database and delete data's dataset if it's empty.
     * This should be called when a provider update his layer list and
     * one layer was removed by external modification.
     * Because this method delete data entry in Data table, every link to
     * this data should be cascaded.
     *
     * Do not use this method to remove a data, use {@link #removeData(Integer)} instead.
     *
     * @param name given data name.
     * @param providerIdentifier given provider identifier.
     * @throws org.constellation.configuration.ConfigurationException
     */
    void missingData(QName name, String providerIdentifier) throws ConfigurationException;

    /**
     * Set {@code updated} attribute to {@code false} in removed data and his children.
     * This may remove data/provider/dataset depending of the state of provider/dataset.
     *
     * @see #updateDataIncluded(int, boolean)
     * @param dataId
     * @throws org.constellation.configuration.ConfigurationException
     */
    void removeData(Integer dataId) throws ConfigurationException;

    /**
     * Proceed to create a new data for given parameters.
     *
     * TODO seems only used by Junit tests, should not be public in DataBusiness API.
     *
     * @param name data name to create.
     * @param providerIdentifier provider identifier.
     * @param type data type.
     * @param sensorable flag that indicates if data is sensorable.
     * @param included flag that indicates if data is included.
     * @param subType data subType.
     * @param metadataXml metadata of data.
     * @deprecated seems only used by Junit tests use {@link #create(javax.xml.namespace.QName, String, String, boolean, boolean, Boolean, String, String)}
     * instead.
     */
    Data create(QName name, String providerIdentifier, String type, boolean sensorable, boolean included, String subType, String metadataXml);

    /**
     * Proceed to create a new data for given parameters.
     * @param name data name to create.
     * @param providerIdentifier provider identifier.
     * @param type data type.
     * @param sensorable flag that indicates if data is sensorable.
     * @param included flag that indicates if data is included.
     * @param rendered flag that indicates if data is rendered (can be null).
     * @param subType data subType.
     * @param metadataXml metadata of data.
     */
    Data create(QName name, String providerIdentifier, String type, boolean sensorable, boolean included, Boolean rendered, String subType, String metadataXml);

    /**
     * Proceed to remove data for given provider.
     * Synchronized method.
     * @param providerId given provider identifier.
     * @throws org.constellation.configuration.ConfigurationException
     */
    void removeDataFromProvider(String providerId) throws ConfigurationException ;

    /**
     * Returns {@link DataBrief} for given data name and provider id as integer.
     *
     * @param dataName given data name.
     * @param providerId given data provider as integer.
     * @return {@link DataBrief}.
     * @throws ConstellationException is thrown if result fails.
     */
    DataBrief getDataBrief(QName dataName, Integer providerId) throws ConstellationException;

    /**
     * Returns {@link DataBrief} for given data name and provider identifier as string.
     *
     * @param fullName given data name.
     * @param providerIdentifier given data provider identifier.
     * @return {@link DataBrief}
     * @throws ConstellationException is thrown if result fails.
     */
    DataBrief getDataBrief(QName fullName, String providerIdentifier) throws ConstellationException;

    /**
     * Returns {@link DefaultMetadata} for given providerId and data name.
     * @param providerId given data provider id.
     * @param name given data name.
     * @return {@link DefaultMetadata}
     * @throws org.constellation.configuration.ConfigurationException is thrown for UnsupportedEncodingException or JAXBException.
     */
    DefaultMetadata loadIsoDataMetadata(String providerId, QName name)  throws ConfigurationException;

    Dataset getDatasetForData(String providerID, QName qName) throws ConstellationException;

    Dataset getDatasetForData(int dataId) throws ConstellationException;

    /**
     * Proceed to remove all data.
     * @throws org.constellation.configuration.ConfigurationException
     */
    void deleteAll() throws ConfigurationException ;

    /**
     * Search in the lucene index for data matching the supplied query.
     * 
     * @param query
     * 
     * @return A list
     * @throws IOException
     */
    List<Data> searchOnMetadata(String query) throws IOException, ConstellationException;

    /**
     * Update data {@code included} attribute.
     * If data {@code included} is set to {@code false}, all layers using this data are deleted,
     * data is removed from all CSW and reload them, delete data provider if all siblings data also have
     * their {@code included} attribute set as {@code false}.
     * This may also delete data's dataset if it's empty.
     *
     * @param dataId the given data Id.
     * @param included value to set
     * @throws org.constellation.configuration.ConfigurationException
     */
    void updateDataIncluded(int dataId, boolean included) throws ConfigurationException;

    /**
     * Returns {@link DataBrief} for given layer alias and data provider identifier.
     *
     * @param layerAlias given layer name.
     * @param providerId given data provider identifier.
     * @return {@link DataBrief}.
     * @throws ConstellationException is thrown if result fails.
     */
    DataBrief getDataLayer(String layerAlias, String providerId);

    /**
     * Load a metadata for given data provider id and data name.
     *
     * @param providerId given data provider.
     * @param name given data name.
     * @param pool marshaller pool.
     * @return {@link CoverageMetadataBean}
     * @throws ConstellationException is thrown for JAXBException.
     */
    CoverageMetadataBean loadDataMetadata(String providerId, QName name, MarshallerPool pool);

    /**
     * Returns a list of {@link DataBrief} for given metadata identifier.
     *
     * @param metadataId given metadata identifier.
     * @return list of {@link DataBrief}.
     */
    List<DataBrief> getDataBriefsFromMetadataId(String metadataId);

    /**
     * Return the {@linkplain Provider provider} for the given {@linkplain Data data} identifier.
     *
     * @param dataId {@link Data} identifier
     * @return a {@linkplain Provider provider}
     */
    Provider getProvider(int dataId);

    /**
     * Returns a list of {@link DataBrief} for given dataSet id.
     *
     * @param datasetId the given dataSet id.
     * @return the list of {@link DataBrief}.
     */
    List<DataBrief> getDataBriefsFromDatasetId(Integer datasetId);

    /**
     * Returns list of {@link Data} for given style id.
     *
     * @param styleId the given style id.
     * @return the list of {@link Data}.
     */
    List<Data> findByStyleId(final Integer styleId);

    /**
     * Returns a list of {@link DataBrief} for given style id.
     *
     * @param styleId the given style id.
     * @return the list of {@link DataBrief}.
     */
    List<DataBrief> getDataBriefsFromStyleId(final Integer styleId);

    /**
     * Returns a list of light {@link DataBrief} for given style id.
     * Output DataBrief contain only :
     * <ul>
     *     <li>id</li>
     *     <li>name</li>
     *     <li>namespace</li>
     *     <li>provider</li>
     *     <li>type</li>
     *     <li>subtype</li>
     * </ul>
     *
     * @param styleId the given style id.
     * @return the list of light {@link DataBrief}.
     */
    List<DataBrief> getDataRefsFromStyleId(final Integer styleId);

    ParameterValues getVectorDataColumns(int id) throws DataStoreException;

    /**
     * Returns list of {@link Data} for given dataSet id.
     *
     * @param datasetId the given dataSet id.
     * @return the list of {@link Data}.
     */
    List<Data> findByDatasetId(final Integer datasetId);

    void updateMetadata(String providerId, QName dataName, DefaultMetadata metadata) throws ConfigurationException;

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
     * @throws org.constellation.configuration.ConfigurationException
     */
    Data findById(final Integer id)throws ConfigurationException;

    /**
     * Get and parse data statistics.
     * @param dataId
     * @return ImageStatistics object or null if data is not a coverage or if Statistics were not computed.
     * @throws org.constellation.configuration.ConfigurationException
     */
    ImageStatistics getDataStatistics(final int dataId) throws ConfigurationException;

    /**
     * Run {@link org.constellation.business.IDataCoverageJob#asyncUpdateDataStatistics(int)}
     * on each coverage type data without computed statistics.
     * @param isInit flag that define if it's a startup call.
     *               If true, statistic of all data in ERROR and PENDING will be also computed
     */
    void computeEmptyDataStatistics(boolean isInit);

    /**
     * Search for data without statistics
     */
    void updateDataStatistics();

    /**
     * Update {@link org.constellation.database.api.Data#isRendered()} attribute that define
     * if a data is Rendered or Geophysic.
     *
     * @param fullName data name
     * @param providerIdentifier provider identifier name
     * @param isRendered if true data is Rendered, otherwise it's Geophysic
     */
    void updateDataRendered(QName fullName, String providerIdentifier, boolean isRendered);

    /**
     * Update {@link org.constellation.database.api.Data#datasetId} attribute.
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

    /**
     * Returns count of all data
     * @param includeInvisibleData flag that indicates if the count will includes hidden data.
     * @return int
     */
    Integer getCountAll(boolean includeInvisibleData);

    void linkDataToData(final int dataId, final int childId);

    List<Data> getDataLinkedData(final int dataId);
    
    void uploadCleaner();
    
    boolean existsById(int dataId);
    
    List<DataItem> fetchByDatasetId(int datasetId);

    List<DataItem> fetchByDatasetIds(Collection<Integer> datasetIds);
}
