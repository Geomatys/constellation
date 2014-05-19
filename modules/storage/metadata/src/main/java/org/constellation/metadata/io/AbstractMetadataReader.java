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

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.sis.util.logging.Logging;
import org.w3c.dom.Node;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public abstract class AbstractMetadataReader implements MetadataReader {
    
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
    public abstract Node getMetadata(final String identifier, final MetadataType mode) throws MetadataIoException;
    
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
     * @param logLevel the LogLevel to set
     */
    @Override
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, URI> getConceptMap() {
        return new HashMap<>();
    }

    @Override
    public Iterator<Node> getEntryIterator() throws MetadataIoException {
        throw new UnsupportedOperationException("not supported by this implementation");
    }

    @Override
    public boolean useEntryIterator() {
        return false;
    }
}
