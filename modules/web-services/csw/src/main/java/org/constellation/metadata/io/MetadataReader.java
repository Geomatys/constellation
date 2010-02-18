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

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.xml.namespace.QName;

/// geotoolkit dependencies
import org.geotoolkit.csw.xml.DomainValues;
import org.geotoolkit.csw.xml.ElementSetType;

// Constellation dependencies
import org.constellation.ws.CstlServiceException;

/**
 *
 * @author Guilhem Legal
 */
public abstract class MetadataReader {
    
    public static final int DUBLINCORE = 0;
    public static final int ISO_19115  = 1;
    public static final int EBRIM      = 2;
    public static final int SENSORML   = 3;
    
    /**
     * A debugging logger
     */
    protected static final Logger LOGGER = Logger.getLogger("org.constellation.metadata.io");
    
    /**
     * A flag indicating if the cache mecanism is enabled or not.
     */
    private final boolean isCacheEnabled;

    /**
     * A flag indicating if the multi thread mecanism is enabled or not.
     */
    private final boolean isThreadEnabled;
    
    /**
     * A map containing the metadata already extract from the database.
     */
    private final Map<String, Object> metadatas = new HashMap<String, Object>();

    /**
     * A date formatter used to display the Date object for dublin core translation.
     */
    protected DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    /**
     * Initialize the metadata reader base attribute.
     * 
     * @param isCacheEnabled A flag indicating if the cache mecanism is enabled or not.
     * @param isThreadEnabled A flag indicating if the multi thread mecanism is enabled or not.
     */
    public MetadataReader(boolean isCacheEnabled, boolean isThreadEnabled) {
        this.isCacheEnabled  = isCacheEnabled;
        this.isThreadEnabled = isThreadEnabled;
    }
    
    /**
     * Return a metadata object from the specified identifier.
     * 
     * @param identifier The metadata identifier.
     * @param mode An output schema mode: EBRIM, ISO_19115, DUBLINCORE and SENSORML supported.
     * @param type An elementSet: FULL, SUMMARY and BRIEF. (implies elementName == null)
     * @param elementName A list of QName describing the requested fields. (implies type == null)
     * 
     * @return A marshallable metadata object.
     * @throws CstlServiceException
     */
    public abstract Object getMetadata(String identifier, int mode, ElementSetType type, List<QName> elementName) throws CstlServiceException;
    
    /**
     * Return a list of values for each specific fields specified as a coma separated String.
     */
    public abstract List<DomainValues> getFieldDomainofValues(String propertyNames) throws CstlServiceException;
    
    /**
     * Execute a SQL query and return the result as a List of identifier;
     * 
     * @param query
     * @return
     * @throws CstlServiceException
     */
    public abstract List<String> executeEbrimSQLQuery(String sqlQuery) throws CstlServiceException;
    
    /**
     * Return all the entries from the database
     */
    public abstract List<? extends Object> getAllEntries() throws CstlServiceException;

     /**
     * Return all the entries identifiers from the database
     */
    public abstract List<String> getAllIdentifiers() throws CstlServiceException;
    
    /**
     * Return the list of supported data types.
     */
    public abstract List<Integer> getSupportedDataTypes();

    /**
     * Return the list of QName for additional queryable element.
     */
    public abstract List<QName> getAdditionalQueryableQName();

    /**
     * Return the list of path for the additional queryable element.
     */
    public abstract Map<String, List<String>> getAdditionalQueryablePathMap();

    /**
     * Return the list of Additional queryable element.
     */
    public abstract Map<String, URI> getConceptMap();

    /**
     * Destroy all the resource used by this reader.
     */
    public abstract void destroy();

    /**
     * Remove a metadata from the cache.
     * 
     * @param identifier The metadata identifier.
     */
    public void removeFromCache(String identifier) {
        if (isCacheEnabled)
            metadatas.remove(identifier);
    }

    /**
     * Add a metadata to the cache.
     *
     * @param identifier The metadata identifier.
     * @param metadata The object to put in cache.
     */
    protected void addInCache(String identifier,  Object metadata) {
        if (isCacheEnabled)
            metadatas.put(identifier, metadata);
    }
    
    /**
     * Return a metadata from the cache if it present.
     * 
     * @param identifier The metadata identifier.
     */
    protected Object getFromCache(String identifier) {
        return metadatas.get(identifier);
    }
    
    /**
     * Return true is the cache mecanism is enabled.
     */
    public boolean isCacheEnabled() {
        return isCacheEnabled;
    }

    /**
     * Return true is the cache mecanism is enabled.
     */
    public boolean isThreadEnabled() {
        return isThreadEnabled;
    }
}
