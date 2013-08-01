/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.metadata.io;

// J2SE dependencies
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.sis.util.logging.Logging;

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
     * @param MDReader an MDWeb database reader.
     */
    public AbstractMetadataWriter() throws MetadataIoException {
    }

    /**
     * Record an object in the metadata dataSource.
     * 
     * @param obj The object to store in the dataSource.
     * @return true if the storage succeed, false else.
     */
    @Override
    public abstract boolean storeMetadata(final Object obj) throws MetadataIoException;

    /**
     * Delete an object in the metadata database.
     * @param metadataID The identifier of the metadata to delete.
     * @return true if the delete succeed, false else.
     */
    @Override
    public abstract boolean deleteMetadata(final String metadataID) throws MetadataIoException;


    /**
     * Replace an object in the metadata dataSource.
     *
     * @param metadataID The identifier of the metadata to Replace.
     * @param any The object to replace the matching metadata.
     */
    @Override
    public abstract boolean replaceMetadata(final String metadataID, final Object any) throws MetadataIoException;

    /**
     * Return true if the Writer supports the delete mecanism.
     */
    @Override
    public abstract boolean deleteSupported();

    /**
     * Return true if the Writer supports the update mecanism.
     */
    @Override
    public abstract boolean updateSupported();

    /**
     * Destroy all the resource and close connection.
     */
    @Override
    public abstract void destroy();

    /**
     * @param LogLevel the LogLevel to set
     */
    public void setLogLevel(final Level logLevel) {
        this.logLevel = logLevel;
    }
}
