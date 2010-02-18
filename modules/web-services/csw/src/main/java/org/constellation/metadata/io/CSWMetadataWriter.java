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

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import org.constellation.ws.CstlServiceException;

//geotoolkit dependencies
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import org.geotoolkit.csw.xml.v202.RecordPropertyType;
import org.geotoolkit.lucene.index.AbstractIndexer;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public abstract class CSWMetadataWriter extends MetadataWriter {

    /**
     * An indexer lucene to add object into the index.
     */
    protected final AbstractIndexer indexer;

     /**
     * Build a new metadata writer.
     *
     * @param MDReader an MDWeb database reader.
     */
    public CSWMetadataWriter(AbstractIndexer indexer) throws CstlServiceException {
        super();
        this.indexer        = indexer;
    }

    /**
     * Update an object in the metadata database.
     *
     * @param metadataID The identifier of the metadata to Replace.
     * @param properties A List of property-value to replace in the specified metadata.
     */
    public abstract boolean updateMetadata(String metadataID, List<RecordPropertyType> properties) throws CstlServiceException;

    /**
     * Try to parse a date in a string.
     * If the string can not be parsed a CstlServiceException will be throw.
     *
     * @param dateValue the string representation of the date.
     * @return a Date object.
     *
     * @throws CstlServiceException if the string can not be parsed.
     */
    protected Date parseDate(String dateValue) throws CstlServiceException {
        // in the case of a timezone expressed like this +01:00 we must transform it in +0100
        if (dateValue.indexOf('.') != -1) {
            String msNtz = dateValue.substring(dateValue.indexOf('.'));
            if (msNtz.indexOf(':') != -1) {
                msNtz = msNtz.replace(":", "");
                dateValue = dateValue.substring(0, dateValue.indexOf('.'));
                dateValue = dateValue + msNtz;
            }
        }
        Date result = null;
        boolean success = true;
        try {
            result = dateFormat.get(0).parse((String) dateValue);
        } catch (ParseException ex) {
            success = false;
        }
        if (!success) {
            try {
               result = dateFormat.get(1).parse((String) dateValue);
            } catch (ParseException ex) {
                throw new CstlServiceException("There service was unable to parse the date:" + dateValue, INVALID_PARAMETER_VALUE);
            }
        }
        return result;
    }
    
    /**
     * Destoy all the resource and close connection.
     */
    @Override
    public void destroy() {
        if (indexer != null)
            indexer.destroy();
    }
}
