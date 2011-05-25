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

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public interface MetadataWriter {

    /**
     * Record an object in the metadata datasource.
     * 
     * @param obj The object to store in the datasource.
     * @return true if the storage succeed, false else.
     */
    boolean storeMetadata(final Object obj) throws MetadataIoException;

    /**
     * Delete an object in the metadata database.
     * @param metadataID The identifier of the metadata to delete.
     * @return true if the delete succeed, false else.
     */
    boolean deleteMetadata(final String metadataID) throws MetadataIoException;

    /**
     * Return true if the specified id is already used in the database.
     */
    boolean isAlreadyUsedIdentifier(final String metadataID) throws MetadataIoException;

    /**
     * Replace an object in the metadata datasource.
     *
     * @param metadataID The identifier of the metadata to Replace.
     * @param any The object to replace the matching metadata.
     */
     boolean replaceMetadata(String metadataID, Object any) throws MetadataIoException;

    /**
     * Return true if the Writer supports the delete mecanism.
     */
    boolean deleteSupported();

    /**
     * Return true if the Writer supports the update mecanism.
     */
    boolean updateSupported();

    /**
     * Destoy all the resource and close connection.
     */
    void destroy();
}
