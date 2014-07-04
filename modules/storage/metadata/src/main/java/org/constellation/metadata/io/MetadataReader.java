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

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public interface MetadataReader {
    
    /**
     * Return a metadata object from the specified identifier.
     * 
     * @param identifier The metadata identifier.
     * @param mode An output schema mode: EBRIM, ISO_19115, DUBLINCORE and SENSORML supported.
     * 
     * @return A marshallable metadata object.
     * @throws MetadataIoException
     */
    Node getMetadata(final String identifier, final MetadataType mode) throws MetadataIoException;
    
    /**
     * Return true if the metadata exist.
     * 
     * @param identifier The metadata identifier.
     * 
     * @return true if the metadata exist
     * @throws MetadataIoException 
     */
    boolean existMetadata(final String identifier) throws MetadataIoException;
    
    /**
     * Return the number of metadata in the datasource.
     * 
     * @return the number of metadata in the datasource.
     * @throws MetadataIoException 
     */
    int getEntryCount() throws MetadataIoException;
    
    /**
     * @return all the entries from the database
     * @throws MetadataIoException
     */
    List<? extends Object> getAllEntries() throws MetadataIoException;

     /**
     * @return all the entries identifiers from the database
     * 
     * @throws org.constellation.metadata.io.MetadataIoException
     */
    List<String> getAllIdentifiers() throws MetadataIoException;

    Iterator<String> getIdentifierIterator() throws MetadataIoException;

    Iterator<? extends Object> getEntryIterator() throws MetadataIoException;

    boolean useEntryIterator();
    
    /**
     * Destroy all the resource used by this reader.
     */
    void destroy();

    /**
     * Remove a metadata from the cache.
     * 
     * @param identifier The metadata identifier.
     */
    void removeFromCache(final String identifier);
    
    /**
     * Remove all metadata from the cache.
     */
    void clearCache();
    
    
    /**
     * @return true is the cache mecanism is enabled.
     */
    boolean isCacheEnabled();

    /**
     * @return true is the cache mecanism is enabled.
     */
    boolean isThreadEnabled();

    /**
     * @return the list of supported data types.
     */
    List<MetadataType> getSupportedDataTypes();

    /**
     * @return A map of label / Skos concept URI.
     */
    Map<String, URI> getConceptMap();

    /**
     * Set the global level of log for information message of this reader.
     * @param logLevel
     */
    void setLogLevel(Level logLevel);
}
