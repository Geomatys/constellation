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

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.constellation.cat.csw.v202.ElementSetType;
import org.constellation.ws.ServiceVersion;
import org.constellation.ws.WebServiceException;

/**
 *
 * @author Guilhem Legal
 */
public abstract class MetadataReader {
    
    public final static int DUBLINCORE = 0;
    public final static int ISO_19115  = 1;
    public final static int EBRIM      = 2;
    
    /**
     * A debugging logger
     */
    protected Logger logger = Logger.getLogger("org.constellation.metadata.io");
    
    /**
     * A service version used in exception launch.
     */
    protected ServiceVersion version;
    
    /**
     * A flag indicating if the cache mecanism is enabled or not.
     */
    private final boolean isCacheEnabled;
    
    /**
     * A map containing the metadata already extract from the database.
     */
    private final Map<String, Object> metadatas = new HashMap<String, Object>();
    
    public MetadataReader(boolean isCacheEnabled) {
        this.isCacheEnabled = isCacheEnabled;
    }
    
    /**
     * Return a metadata object from the specified identifier.
     * 
     * @param identifier The metadata identifier.
     * @param mode An output schema mode: EBRIM, ISO_19115 and DUBLINCORE supported.
     * @param type An elementSet: FULL, SUMMARY and BRIEF. (implies elementName == null)
     * @param elementName A list of QName describing the requested fields. (implies type == null)
     * 
     * @return A marshallable metadata object.
     * @throws java.sql.SQLException
     */
    public abstract Object getMetadata(String identifier, int mode, ElementSetType type, List<QName> elementName) throws SQLException, WebServiceException;
    
    /**
     * Add a metadata to the cache.
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
     * Set the version of the service (For execption report).
     * 
     * @param version
     */
    public final void setVersion(ServiceVersion version) {
        this.version = version;
    }
    
    /**
     * Return true is the cache mecanism is enbled.
     */
    public boolean isCacheEnabled() {
        return isCacheEnabled;
    }

}
