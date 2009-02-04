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

// JAXB dependencies
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

// constellation dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.lucene.IndexingException;
import org.constellation.lucene.index.AbstractIndexSearcher;
import org.constellation.lucene.index.AbstractIndexer;
import org.constellation.metadata.io.MetadataReader;
import org.constellation.metadata.io.MetadataWriter;
import org.constellation.ws.CstlServiceException;

// Geotools dependencies
import org.geotools.factory.AbstractFactory;

/**
 *
 * @author Guilhem Legal
 */
public abstract class AbstractCSWFactory extends AbstractFactory {

    public AbstractCSWFactory(int priority) {
        super(priority);
    }
    
    public abstract MetadataReader getMetadataReader(Automatic configuration, File dataDir, Unmarshaller unmarshaller, File configDir) throws CstlServiceException;

    public abstract MetadataWriter getMetadataWriter(Automatic configuration, AbstractIndexer index, Marshaller marshaller, File dataDirectory) throws CstlServiceException;
    
    public abstract int getProfile(int dbType);
    
    public abstract AbstractIndexer getIndexer(Automatic configuration, MetadataReader reader, File configDir, String serviceID) throws IndexingException;
    
    public abstract AbstractIndexSearcher getIndexSearcher(int dbType, File configDir, String serviceID) throws IndexingException;
}
