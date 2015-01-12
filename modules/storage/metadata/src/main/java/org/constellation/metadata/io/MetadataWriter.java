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
package org.constellation.metadata.io;

import org.w3c.dom.Node;

import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public interface MetadataWriter {

    /**
     * Record an object in the metadata datasource.
     * 
     * @param obj The object to store in the datasource.
     * @return true if the storage succeed, false else.
     * @throws org.constellation.metadata.io.MetadataIoException
     */
    boolean storeMetadata(final Node obj) throws MetadataIoException;

    /**
     * Delete an object in the metadata database.
     * @param metadataID The identifier of the metadata to delete.
     * @return true if the delete succeed, false else.
     * @throws org.constellation.metadata.io.MetadataIoException
     */
    boolean deleteMetadata(final String metadataID) throws MetadataIoException;

    /**
     * Return true if the specified id is already used in the database.
     * @param metadataID
     * @return
     * @throws org.constellation.metadata.io.MetadataIoException
     */
    boolean isAlreadyUsedIdentifier(final String metadataID) throws MetadataIoException;

    /**
     * Replace an object in the metadata datasource.
     *
     * @param metadataID The identifier of the metadata to Replace.
     * @param any The object to replace the matching metadata.
     * @return
     * @throws org.constellation.metadata.io.MetadataIoException
     */
     boolean replaceMetadata(String metadataID, Node any) throws MetadataIoException;

    /**
     * Return true if the Writer supports the delete mecanism.
     * @return
     */
    boolean deleteSupported();

    /**
     * Return true if the Writer supports the update mecanism.
     * @return
     */
    boolean updateSupported();

    /**
     * Destoy all the resource and close connection.
     */
    void destroy();

    /**
     * Update an object in the metadata database.
     *
     * @param metadataID The identifier of the metadata to Replace.
     * @param properties A List of property-value to replace in the specified metadata.
     * @return
     * @throws org.constellation.metadata.io.MetadataIoException
     */
    boolean updateMetadata(String metadataID, Map<String, Object> properties) throws MetadataIoException;

    /**
     * Set the global level of log.
     *
     * @param logLevel
     */
    void setLogLevel(Level logLevel);
    
    boolean canImportInternalData();
    
    void linkInternalMetadata(final String metadataID) throws MetadataIoException;
}
