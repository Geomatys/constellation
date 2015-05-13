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
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.constellation.admin.dto.metadata.GroupStatBrief;
import org.constellation.admin.dto.metadata.OwnerStatBrief;
import org.constellation.admin.dto.metadata.User;
import org.constellation.configuration.ConfigurationException;
import org.constellation.dto.MetadataLists;
import org.constellation.engine.register.MetadataWithState;
import org.constellation.engine.register.jooq.tables.pojos.Metadata;

/**
 * @author Cédric Briançon (Geomatys)
 */
public interface IMetadataBusiness {
    
    /**
     * Returns the xml as string representation of metadata for given metadata identifier.
     *
     * @param metadataId given metadata identifier
     * @param includeService flag that indicates if service repository will be requested.
     * @param onlyPublished flag that indicates if it will return the unpublished metadata.
     * @return String representation of metadata in xml.
     */
    String searchMetadata(final String metadataId, final boolean includeService, final boolean onlyPublished);
    
    /**
     * Returns the metadata Pojo for given metadata identifier.
     *
     * @param metadataId given metadata identifier
     * @param includeService flag that indicates if service repository will be requested.
     * @param onlyPublished flag that indicates if it will return the unpublished metadata.
     * @return String representation of metadata in xml.
     */
    Metadata searchFullMetadata(final String metadataId, final boolean includeService, final boolean onlyPublished);

    /**
     * Returns {@code true} if the xml metadata exists for given metadata identifier.
     *
     * @param metadataID given metadata identifier.
     * @param includeService flag that indicates if service repository will be requested.
     * @param onlyPublished flag that indicates if it will return the unpublished metadata.
     * @return boolean to indicates if metadata is present or not.
     */
    boolean existInternalMetadata(final String metadataID, final boolean includeService, final boolean onlyPublished);

    /**
     * Returns a list of all metadata identifiers.
     *
     * @param includeService flag that indicates if service repository will be requested.
     * @param onlyPublished flag that indicates if it will return the unpublished metadata.
     * @return List of string identifiers.
     */
    List<String> getInternalMetadataIds(final boolean includeService, final boolean onlyPublished);
    
    int getInternalMetadataCount(final boolean includeService, final boolean onlyPublished);

    /**
     * Returns all metadata stored in database.
     *
     * @param includeService given flag to include service's metadata
     * @param onlyPublished flag that indicates if it will return the unpublished metadata.
     * @return List of all metadata as string xml stored in database.
     */
    List<String> getAllMetadata(final boolean includeService, final boolean onlyPublished);
    
    /**
     * Update or create a new Metadata object.
     * 
     * @param metadataId identifier of the metadata.
     * @param xml XML representation of the metadata.
     * 
     * @return The created/update Metadata pojo.
     * 
     * @throws org.constellation.configuration.ConfigurationException
     */
    Metadata updateMetadata(final String metadataId, final String xml) throws ConfigurationException;
    
    /**
     * Update or create a new Metadata object.
     * 
     * @param metadataId identifier of the metadata.
     * @param xml XML representation of the metadata.
     * @param owner User who owes the metadata.
     * 
     * @return The created/update Metadata pojo.
     * 
     * @throws org.constellation.configuration.ConfigurationException
     */
    Metadata updateMetadata(final String metadataId, final String xml, final Integer owner) throws ConfigurationException;
    
    /**
     * Update or create a new Metadata pojo.
     * 
     * @param metadataId identifier of the metadata.
     * @param metadata Marshallable geotk metadata.
     * 
     * @return The created/update Metadata pojo.
     * 
     * @throws org.constellation.configuration.ConfigurationException
     */
    Metadata updateMetadata(final String metadataId, final DefaultMetadata metadata) throws ConfigurationException;
    
    /**
     * Update or create a new Metadata pojo.
     * 
     * @param metadataId identifier of the metadata.
     * @param xml XML representation of the metadata.
     * @param dataID Identifier of the linked data (can be {@code null})
     * @param datasetID Identifier of the linked dataset (can be {@code null})
     * @param owner User who owes the metadata.
     * @return The created/update Metadata pojo.
     * 
     * @throws org.constellation.configuration.ConfigurationException
     */
    Metadata updateMetadata(final String metadataId, final String xml, final Integer dataID, final Integer datasetID, final Integer owner) throws ConfigurationException;
    
    /**
     * Update or create a new Metadata pojo.
     * 
     * @param metadataId identifier of the metadata.
     * @param metadata Marshallable geotk metadata.
     * @param dataID Identifier of the linked data (can be {@code null}).
     * @param datasetID Identifier of the linked dataset (can be {@code null}).
     * @param owner User who owes the metadata.
     *
     * @return The created/update Metadata pojo.
     * @throws org.constellation.configuration.ConfigurationException
     */
    Metadata updateMetadata(final String metadataId, final DefaultMetadata metadata, final Integer dataID, final Integer datasetID, final Integer owner) throws ConfigurationException;
    
    /**
     * Returns all the metadata identifier associated with a csw service.
     *
     * @param cswIdentifier identifer of the CSW instance.
     * @param includeService given flag to include service's metadata
     * @param onlyPublished flag that indicates if it will return the unpublished metadata.
     * 
     * @return List of all metadata identifiers stored in database.
     */
    List<String> getLinkedMetadataIDs(final String cswIdentifier, final boolean includeService, final boolean onlyPublished);
    
    int getLinkedMetadataCount(final String cswIdentifier, final boolean includeService, final boolean onlyPublished);
    
    /**
     * Build a link beetween a CSW service and a metadata.
     * 
     * @param metadataId Identifier of the geotk metadata object.
     * @param cswIdentifier identifer of the CSW instance.
     * @throws org.constellation.configuration.ConfigurationException
     */
    void linkMetadataIDToCSW(final String metadataId, final String cswIdentifier) throws ConfigurationException;
    
    /**
     * Remove the link beetween a CSW service and a metadata.
     * 
     * @param metadataId Identifier of the geotk metadata object.
     * @param cswIdentifier identifer of the CSW instance.
     */
    void unlinkMetadataIDToCSW(final String metadataId, final String cswIdentifier);
    
    /**
     * Return {@code true} if the specified metadata is linked to the specified CSW service.
     * @param metadataID Identifier of the metadata pojo.
     * @param cswID identifer of the CSW instance.
     * 
     * @return {@code true} if the specified metadata is linked to the specified CSW service.
     */
    boolean isLinkedMetadataToCSW(final int metadataID, final int cswID);
    
    /**
     * Return {@code true} if the specified metadata is linked to the specified CSW service.
     * @param metadataID Identifier of the metadata pojo.
     * @param cswID identifer of the CSW instance.
     * @param includeService given flag to include service's metadata
     * @param onlyPublished flag that indicates if it will return the unpublished metadata.
     * 
     * @return {@code true} if the specified metadata is linked to the specified CSW service.
     */
    boolean isLinkedMetadataToCSW(final String metadataID, final String cswID, final boolean includeService, final boolean onlyPublished);
    
    /**
     * Return {@code true} if the specified metadata is linked to the specified CSW service.
     * @param metadataID Identifier of the geotk metadata object.
     * @param cswID  identifer of the CSW instance.
     * 
     * @return {@code true} if the specified metadata is linked to the specified CSW service.
     */
    boolean isLinkedMetadataToCSW(final String metadataID, final String cswID);
    
    MetadataLists getMetadataCodeLists();
    
    /**
     * Return the geotk metadata object the specified pojo identifier.
     * 
     * @param id identifier of the metadata pojo.
     * 
     * @return The geotk metadata object or {@code null} .
     * @throws org.constellation.configuration.ConfigurationException 
     */
    DefaultMetadata getMetadata(final int id) throws ConfigurationException;
    
    /**
     * Return the metadat pojo for the specified identifier.
     * 
     * @param id identifier of the metadata pojo.
     * 
     * @return The metadat pojo or {@code null}.
     */
    Metadata getMetadataById(final int id);
    
    /**
     * Update the publication flag of a metadata.
     * 
     * @param id identifier of the metadata pojo.
     * @param newStatus new publication status to set.
     * 
     * @throws org.constellation.configuration.ConfigurationException 
     */
    void updatePublication(final int id, final boolean newStatus) throws ConfigurationException;
    
    /**
     * Update the publication flag for a list of metadata pojo.
     * 
     * @param ids List of metadata pojo identifier.
     * @param newStatus new publication status to set.
     * 
     * @throws org.constellation.configuration.ConfigurationException 
     */
    void updatePublication(final List<Integer> ids, final boolean newStatus) throws ConfigurationException;
    
    /**
     * Update the profile for a metadata pojo.
     * 
     * @param id metadata pojo identifier.
     * @param newProfile new profile to set.
     * 
     * @throws org.constellation.configuration.ConfigurationException 
     */
    void updateProfile(final Integer id, final String newProfile) throws ConfigurationException;
    
    /**
     * Update the validation flag of a metadata.
     * 
     * @param id identifier of the metadata pojo.
     * @param newStatus new validation status to set.
     * 
     */
    void updateValidation(final int id, final boolean newStatus);
    
    /**
     * Update the owner of a metadata.
     * 
     * @param id identifier of the metadata pojo.
     * @param newOwner new owner identifier to set.
     * 
     */
    void updateOwner(final int id, final int newOwner);
    
    /**
     * Delete a metadata pojo.
     * 
     * @param id identifier of the metadata pojo.
     * @throws org.constellation.configuration.ConfigurationException 
     */
    void deleteMetadata(final int id) throws ConfigurationException;
    
    /**
     * Delete the linked metadata pojo for the specified data.
     * 
     * @param dataId identifier of the data pojo.
     * @throws org.constellation.configuration.ConfigurationException 
     */
    void deleteDataMetadata(final int dataId) throws ConfigurationException;
    
    /**
     * Delete the linked metadata pojo for the specified dataszt.
     * 
     * @param datasetId identifier of the dataset pojo.
     * @throws org.constellation.configuration.ConfigurationException 
     */
    void deleteDatasetMetadata(final int datasetId) throws ConfigurationException;
    
    /**
     * Delete a list of metadata pojo.
     * 
     * @param ids List of metadata pojo identifiers.
     * @throws org.constellation.configuration.ConfigurationException 
     */
    void deleteMetadata(final List<Integer> ids) throws ConfigurationException;
    
    /**
     * Return a percentage of the metadata completion (related to the profile linked to the metadata pojo).
     * The metadata pojo is retrieve from the linked specified data.
     * 
     * @param dataId identifier of the data.
     * 
     * @return an integer representing the percentage of completion or {@code null} if the data has no linked metadata.
     */
    Integer getCompletionForData(final int dataId);
    
    /**
     * Return a percentage of the metadata completion (related to the profile linked to the metadata pojo).
     * The metadata pojo is retrieve from the linked specified dataset.
     * 
     * @param datasetId identifier of the dataset.
     * 
     * @return an integer representing the percentage of completion or {@code null} if the dataset has no linked metadata.
     */
    Integer getCompletionForDataset(final int datasetId);
    
    /**
     * Return the geotk metadata object linked with the specified data.
     * 
     * @param dataId identifier of the data.
     * 
     * @return The geotk metadata object or {@code null} if there is no metadata linked to the specified data.
     * @throws org.constellation.configuration.ConfigurationException 
     */
    DefaultMetadata getIsoMetadataForData(final int dataId) throws ConfigurationException;
    
    /**
     * Return the geotk metadata object linked with the specified dataset.
     * 
     * @param datasetId identifier of the dataset.
     * 
     * @return The geotk metadata object or {@code null} if there is no metadata linked to the specified dataset.
     * @throws org.constellation.configuration.ConfigurationException 
     */
    DefaultMetadata getIsoMetadataForDataset(final int datasetId) throws ConfigurationException;
    
    /**
     * Update the CSW services index linked with the specified metadata pojos.
     * 
     * @param metadatas List of metadata pojos.
     * @param update If {@code false} indicates that the metadata must be removed from the indexes.
     * 
     * @throws org.constellation.configuration.ConfigurationException 
     */
    void updateInternalCSWIndex(final List<MetadataWithState> metadatas, final boolean update) throws ConfigurationException;
    
    /**
     * Return the template name for the specified dataset.
     * 
     * @param datasetId identifier of the dataset.
     * @param dataType Type of the dataset (VECTOR, COVERAGE, ..)
     * 
     * @return The template name for the specified dataset.
     * @throws org.constellation.configuration.ConfigurationException 
     */
    String getDatasetTemplate(final String datasetId, final String dataType) throws ConfigurationException;
    
    /**
     * Return the template name for the specified data.
     * 
     * @param dataName identifier of the data.
     * @param dataType Type of the dataset (VECTOR, COVERAGE, ..)
     * 
     * @return The template name for the specified data.
     * @throws org.constellation.configuration.ConfigurationException 
     */
    String getDataTemplate(final QName dataName, final String dataType) throws ConfigurationException;
    
    /**
     * Duplicate a metadata pojo. Update the fileIdentifier and title of the geotk metadata.
     * if (the specified newTitle is null, the new title of the metadata will be "old title" + "(1)".
     * 
     * @param id identifier of the metadata pojo.
     * @param newTitle the new tittle to apply to the metadata object (can be {@code null}).
     * 
     * @return the new pojo created.
     * @throws org.constellation.configuration.ConfigurationException 
     */
    Metadata duplicateMetadata(final int id, final String newTitle) throws ConfigurationException;
    
    /**
     * Count the number of metadata stored in the database.
     *
     * @param filterMap Filters which is optional.
     *
     * @return The total count of metadata.
     */
    int countTotal(final Map<String,Object> filterMap);
    
    int[] countInCompletionRange(final Map<String,Object> filterMap);
    
    /**
     * Count the number of metadata stored in the database whith the specified publication flag.
     * 
     * @param status Publication flag value.
     * @param filterMap Filters which is optional.
     * 
     * @return The total count of metadata with the specified publication flag.
     */
    int countPublished(final boolean status,final Map<String,Object> filterMap);

    /**
     * Returns map of distribution of used profiles.
     * @param filterMap optional filters
     * @return Map
     */
    Map<String,Integer> getProfilesCount(final Map<String,Object> filterMap);
    
    /**
     * Count the number of metadata stored in the database whith the specified validation flag.
     * 
     * @param status Validation flag value.
     * @param filterMap Filters which is optional.
     *
     * @return The total count of metadata with the specified validation flag.
     */
    int countValidated(final boolean status,final Map<String,Object> filterMap);
    
    /**
     * Unmarshall an xml metadata into geotk object.
     * 
     * @param metadata
     * @return
     * @throws org.constellation.configuration.ConfigurationException 
     */
    Object unmarshallMetadata(final String metadata) throws ConfigurationException;
    
    /**
     * Marshall a geotk metadata object into a String.
     * 
     * @param metadata
     * @return
     * @throws org.constellation.configuration.ConfigurationException 
     */
    String marshallMetadata(final Object metadata) throws ConfigurationException;
    
    String getTemplateFromMetadata(DefaultMetadata meta);
    
    void askForValidation(final int metadataID);
    
    void denyValidation(final int metadataID, final String comment);
    
    void acceptValidation(final int metadataID);
    
    Map<Integer, List> filterAndGet(final Map<String,Object> filterMap, final Map.Entry<String,String> sortEntry,final int pageNumber,final int rowsPerPage);
    
    Map<Integer,String> filterAndGetWithoutPagination(final Map<String,Object> filterMap);
    
    List<OwnerStatBrief> getOwnerStatBriefs(final Map<String, Object> filter);
    
    List<GroupStatBrief> getGroupStatBriefs(final Map<String, Object> filter);
    
    List<User> getUsers();
    
    User getUser(int id);
    
    boolean isSpecialMetadataFormat(File metadataFile);
    
    DefaultMetadata getMetadataFromSpecialFormat(File metadataFile) throws ConfigurationException;
}
