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

import java.util.List;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.constellation.configuration.ConfigurationException;
import org.constellation.dto.MetadataLists;
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

    /**
     * Returns all metadata stored in database.
     *
     * @param includeService given flag to include service's metadata
     * @param onlyPublished flag that indicates if it will return the unpublished metadata.
     * @return List of all metadata as string xml stored in database.
     */
    List<String> getAllMetadata(final boolean includeService, final boolean onlyPublished);
    
    boolean updateMetadata(final String metadataId, final String xml) throws ConfigurationException;
    
    boolean updateMetadata(final String metadataId, final String xml, final Integer dataID, final Integer datasetID) throws ConfigurationException;
    
    /**
     * Returns all the metadata associated with a csw service.
     *
     * @param cswIdentifier identifer of the CSW instance.
     * @param includeService given flag to include service's metadata
     * @param onlyPublished flag that indicates if it will return the unpublished metadata.
     * @return List of all metadata as string xml stored in database.
     */
    List<String> getLinkedMetadataIDs(final String cswIdentifier, final boolean includeService, final boolean onlyPublished);
    
    void linkMetadataIDToCSW(final String metadataId, final String cswIdentifier);
    
    void unlinkMetadataIDToCSW(final String metadataId, final String cswIdentifier);
    
    boolean isLinkedMetadataToCSW(final int metadataID, final int cswID);
    
    MetadataLists getMetadataCodeLists();
    
    DefaultMetadata getMetadata(final int id) throws ConfigurationException;
    
    Metadata getMetadataById(final int id);
    
    void updatePublication(final int id, final boolean newStatus) throws ConfigurationException;
    
    void updatePublication(final List<Integer> ids, final boolean newStatus) throws ConfigurationException;
    
    void updateValidation(final int id, final boolean newStatus);
    
    void updateOwner(final int id, final int newOwner);
    
    void deleteMetadata(final int id) throws ConfigurationException;
    
    void deleteMetadata(final List<Integer> ids) throws ConfigurationException;
    
    Integer getCompletionForData(final int dataId);
    
    Integer getCompletionForDataset(final int datasetId);
    
    DefaultMetadata getIsoMetadataForData(final int dataId) throws ConfigurationException;
    
    DefaultMetadata getIsoMetadataForDataset(final int datasetId) throws ConfigurationException;
    
    void updateInternalCSWIndex(final List<Metadata> metadatas, final boolean update) throws ConfigurationException;
}
