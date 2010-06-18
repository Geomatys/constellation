/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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

//geotoolkit dependencies
import java.util.logging.Level;
import org.geotoolkit.csw.xml.v202.RecordPropertyType;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public interface CSWMetadataWriter extends MetadataWriter {

    /**
     * Update an object in the metadata database.
     *
     * @param metadataID The identifier of the metadata to Replace.
     * @param properties A List of property-value to replace in the specified metadata.
     */
    boolean updateMetadata(String metadataID, List<RecordPropertyType> properties) throws MetadataIoException;

    /**
     * Set the global level of log.
     *
     * @param logLevel
     */
    void setLogLevel(Level logLevel);
}
