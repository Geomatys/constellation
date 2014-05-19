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

// J2SE dependencies
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.sis.util.logging.Logging;
import org.w3c.dom.Node;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public abstract class AbstractMetadataWriter implements MetadataWriter {

    /**
     * A debugging logger.
     */
    protected static final Logger LOGGER = Logging.getLogger("org.constellation.metadata.io");
    
    public static final int INSERTED = 0;

    public static final int REPLACED = 1;

    /**
     * The default level for logging non essential informations (ToSee => finer)
     */
    protected Level logLevel = Level.INFO;
    
    /**
     * Build a new metadata writer.
     * 
     * @throws org.constellation.metadata.io.MetadataIoException
     */
    public AbstractMetadataWriter() throws MetadataIoException {
    }

    /**
     * Record an object in the metadata dataSource.
     * 
     * @param obj The object to store in the dataSource.
     * @return true if the storage succeed, false else.
     * @throws org.constellation.metadata.io.MetadataIoException
     */
    @Override
    public abstract boolean storeMetadata(final Node obj) throws MetadataIoException;

    /**
     * Delete an object in the metadata database.
     * @param metadataID The identifier of the metadata to delete.
     * @return true if the delete succeed, false else.
     * @throws org.constellation.metadata.io.MetadataIoException
     */
    @Override
    public abstract boolean deleteMetadata(final String metadataID) throws MetadataIoException;


    /**
     * Replace an object in the metadata dataSource.
     *
     * @param metadataID The identifier of the metadata to Replace.
     * @param any The object to replace the matching metadata.
     * @return true if the replacing succeed.
     * 
     * @throws org.constellation.metadata.io.MetadataIoException
     */
    @Override
    public abstract boolean replaceMetadata(final String metadataID, final Node any) throws MetadataIoException;

    /**
     * Update an object in the metadata database.
     *
     * @param metadataID The identifier of the metadata to Replace.
     * @param properties A List of property-value to replace in the specified metadata.
     * @return true if the update succeed.
     * 
     * @throws org.constellation.metadata.io.MetadataIoException
     */
    @Override
    public abstract boolean updateMetadata(final String metadataID, final Map<String , Object> properties) throws MetadataIoException;
    
    /**
     * @return true if the Writer supports the delete mecanism.
     */
    @Override
    public abstract boolean deleteSupported();

    /**
     * @return true if the Writer supports the update mecanism.
     */
    @Override
    public abstract boolean updateSupported();

    /**
     * Destroy all the resource and close connection.
     */
    @Override
    public abstract void destroy();

    /**
     * @param logLevel the LogLevel to set
     */
    @Override
    public void setLogLevel(final Level logLevel) {
        this.logLevel = logLevel;
    }
}
