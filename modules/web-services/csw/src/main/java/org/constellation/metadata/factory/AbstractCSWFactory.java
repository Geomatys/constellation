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

package org.constellation.metadata.factory;

import java.io.File;

// constellation dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.metadata.io.CSWMetadataReader;
import org.constellation.metadata.io.CSWMetadataWriter;
import org.constellation.metadata.io.MetadataIoException;

// Geotoolkit dependencies
import org.geotoolkit.factory.Factory;
import org.geotoolkit.lucene.IndexingException;
import org.geotoolkit.lucene.index.AbstractIndexSearcher;
import org.geotoolkit.lucene.index.AbstractIndexer;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public abstract class AbstractCSWFactory extends Factory {

    public AbstractCSWFactory() {
        super();
    }
    
    public abstract CSWMetadataReader getMetadataReader(Automatic configuration) throws MetadataIoException;

    public abstract CSWMetadataWriter getMetadataWriter(Automatic configuration, AbstractIndexer index) throws MetadataIoException;
    
    public abstract AbstractIndexer getIndexer(Automatic configuration, CSWMetadataReader reader, String serviceID) throws IndexingException;
    
    public abstract AbstractIndexSearcher getIndexSearcher(int dbType, File configDir, String serviceID) throws IndexingException;
}
