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

import java.util.List;
import javax.xml.namespace.QName;

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
     * @param elementName A list of QName describing the requested fields. (implies type == null)
     * 
     * @return A marshallable metadata object.
     * @throws MetadataIoException
     */
    Object getMetadata(String identifier, int mode, List<QName> elementName) throws MetadataIoException;
    
    /**
     * Return all the entries from the database
     */
    List<? extends Object> getAllEntries() throws MetadataIoException;

     /**
     * Return all the entries identifiers from the database
     */
    List<String> getAllIdentifiers() throws MetadataIoException;
    
    /**
     * Destroy all the resource used by this reader.
     */
    void destroy();

    /**
     * Remove a metadata from the cache.
     * 
     * @param identifier The metadata identifier.
     */
    void removeFromCache(String identifier);
    
    
    /**
     * Return true is the cache mecanism is enabled.
     */
    boolean isCacheEnabled();

    /**
     * Return true is the cache mecanism is enabled.
     */
    boolean isThreadEnabled();
}
