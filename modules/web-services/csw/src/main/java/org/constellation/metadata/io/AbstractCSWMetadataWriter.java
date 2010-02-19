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
import org.geotoolkit.csw.xml.v202.RecordPropertyType;
import org.geotoolkit.lucene.index.AbstractIndexer;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public abstract class AbstractCSWMetadataWriter extends AbstractMetadataWriter implements CSWMetadataWriter{

    /**
     * An indexer lucene to add object into the index.
     */
    protected final AbstractIndexer indexer;

     /**
     * Build a new metadata writer.
     *
     * @param MDReader an MDWeb database reader.
     */
    public AbstractCSWMetadataWriter(AbstractIndexer indexer) throws MetadataIoException {
        super();
        this.indexer        = indexer;
    }

    /**
     * Update an object in the metadata database.
     *
     * @param metadataID The identifier of the metadata to Replace.
     * @param properties A List of property-value to replace in the specified metadata.
     */
    @Override
    public abstract boolean updateMetadata(String metadataID, List<RecordPropertyType> properties) throws MetadataIoException;


    /**
     * Destoy all the resource and close connection.
     */
    @Override
    public void destroy() {
        if (indexer != null)
            indexer.destroy();
    }
}

