/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2010, Geomatys
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.sis.util.logging.Logging;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public abstract class AbstractMetadataReader implements MetadataReader {
    
    public static final int DUBLINCORE  = 0;
    public static final int ISO_19115   = 1;
    public static final int EBRIM       = 2;
    public static final int SENSORML    = 3;
    public static final int NATIVE      = 4;
    public static final int ISO_19110   = 5;
    
    /**
     * A debugging logger
     */
    protected static final Logger LOGGER = Logging.getLogger("org.constellation.metadata.io");
    
    /**
     * A flag indicating if the cache mecanism is enabled or not.
     */
    private boolean cacheEnabled;

    /**
     * A flag indicating if the multi thread mecanism is enabled or not.
     */
    private boolean threadEnabled;
    
    /**
     * A map containing the metadata already extract from the database.
     */
    private final Map<String, Object> metadatas = new HashMap<>();

    /**
     * The default level for logging non essential informations (ToSee => finer)
     */
    protected Level logLevel = Level.INFO;

    /**
     * Initialize the metadata reader base attribute.
     * 
     * @param isCacheEnabled A flag indicating if the cache mecanism is enabled or not.
     * @param isThreadEnabled A flag indicating if the multi thread mecanism is enabled or not.
     */
    public AbstractMetadataReader(final boolean isCacheEnabled, final boolean isThreadEnabled) {
        this.cacheEnabled  = isCacheEnabled;
        this.threadEnabled = isThreadEnabled;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public abstract Object getMetadata(final String identifier, final int mode) throws MetadataIoException;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public abstract boolean existMetadata(final String identifier) throws MetadataIoException;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public abstract List<? extends Object> getAllEntries() throws MetadataIoException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract List<String> getAllIdentifiers() throws MetadataIoException;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void destroy();

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearCache() {
        metadatas.clear();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void removeFromCache(final String identifier) {
        metadatas.remove(identifier);
    }

    /**
     * Add a metadata to the cache.
     *
     * @param identifier The metadata identifier.
     * @param metadata The object to put in cache.
     */
    protected void addInCache(final String identifier, final Object metadata) {
        metadatas.put(identifier, metadata);
    }
    
    /**
     * Return a metadata from the cache if it present.
     * 
     * @param identifier The metadata identifier.
     */
    protected Object getFromCache(final String identifier) {
        return metadatas.get(identifier);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isThreadEnabled() {
        return threadEnabled;
    }

    /**
     * @param LogLevel the LogLevel to set
     */
    public void setLogLevel(final Level logLevel) {
        this.logLevel = logLevel;
    }

    /**
     * @param isCacheEnabled the isCacheEnabled to set
     */
    public void setIsCacheEnabled(final boolean isCacheEnabled) {
        this.cacheEnabled = isCacheEnabled;
    }

    /**
     * @param isThreadEnabled the isThreadEnabled to set
     */
    public void setIsThreadEnabled(final boolean isThreadEnabled) {
        this.threadEnabled = isThreadEnabled;
    }
}
