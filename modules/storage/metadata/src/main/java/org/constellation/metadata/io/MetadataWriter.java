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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author Guilhem Legal (Geomatys)
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
     * Build a new metadata writer.
     * 
     * @param MDReader an MDWeb database reader.
     */
    public MetadataWriter() throws MetadataIoException {
        dateFormat.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"));
        dateFormat.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
    }

    /**
     * Record an object in the metadata datasource.
     * 
     * @param obj The object to store in the datasource.
     * @return true if the storage succeed, false else.
     */
    public abstract boolean storeMetadata(Object obj) throws MetadataIoException;

    /**
     * Delete an object in the metadata database.
     * @param metadataID The identifier of the metadata to delete.
     * @return true if the delete succeed, false else.
     */
    public abstract boolean deleteMetadata(String metadataID) throws MetadataIoException;


    /**
     * Replace an object in the metadata datasource.
     *
     * @param metadataID The identifier of the metadata to Replace.
     * @param any The object to replace the matching metadata.
     */
    public abstract boolean replaceMetadata(String metadataID, Object any) throws MetadataIoException;

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
    public abstract void destroy();
}
