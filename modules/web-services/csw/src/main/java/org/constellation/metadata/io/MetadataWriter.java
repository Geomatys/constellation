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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

// constellation dependencies
import org.constellation.ws.CstlServiceException;

//geotoolkit dependencies
import org.geotoolkit.csw.xml.v202.RecordPropertyType;
import org.geotoolkit.lucene.index.AbstractIndexer;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

/**
 *
 * @author Guilhem Legal
 */
public abstract class MetadataWriter {

    /**
     * A debugging logger.
     */
    protected static final Logger LOGGER = Logger.getLogger("org.constellation.metadata.io");
    
    /**
     * Record the date format in the metadata.
     */
    protected final List<DateFormat> dateFormat = new ArrayList<DateFormat>();
    
    /**
     * An indexer lucene to add object into the index.
     */
    protected final AbstractIndexer indexer;

    /**
     * Build a new metadata writer.
     * 
     * @param MDReader an MDWeb database reader.
     */
    public MetadataWriter(AbstractIndexer indexer) throws CstlServiceException {
        this.indexer        = indexer;
        dateFormat.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"));
        dateFormat.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
    }

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
     * Record an object in the metadata datasource.
     * 
     * @param obj The object to store in the datasource.
     * @return true if the storage succeed, false else.
     */
    public abstract boolean storeMetadata(Object obj) throws CstlServiceException;

    /**
     * Delete an object in the metadata database.
     * @param metadataID The identifier of the metadata to delete.
     * @return true if the delete succeed, false else.
     */
    public abstract boolean deleteMetadata(String metadataID) throws CstlServiceException;


    /**
     * Replace an object in the metadata datasource.
     *
     * @param metadataID The identifier of the metadata to Replace.
     * @param any The object to replace the matching metadata.
     */
    public abstract boolean replaceMetadata(String metadataID, Object any) throws CstlServiceException;

    /**
     * Update an object in the metadata database.
     *
     * @param metadataID The identifier of the metadata to Replace.
     * @param properties A List of property-value to replace in the specified metadata.
     */
    public abstract boolean updateMetadata(String metadataID, List<RecordPropertyType> properties) throws CstlServiceException;

    /**
     * Return true if the Writer supports the delete mecanism.
     */
    public abstract boolean deleteSupported();

    /**
     * Return true if the Writer supports the update mecanism.
     */
    public abstract boolean updateSupported();

    
    /**
     * Destoy all the resource and close connection.
     */
    public void destroy() {
        if (indexer != null)
            indexer.destroy();
    }
}
