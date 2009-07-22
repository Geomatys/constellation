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

// Constellation dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.lucene.IndexingException;
import org.constellation.metadata.CSWworker;
import org.constellation.lucene.index.AbstractIndexSearcher;
import org.constellation.lucene.index.AbstractIndexer;
import org.constellation.metadata.index.generic.GenericIndexer;
import org.constellation.metadata.index.generic.GenericIndexSearcher;
import org.constellation.metadata.index.mdweb.MDWebIndexer;
import org.constellation.metadata.index.mdweb.MDWebIndexSearcher;
import org.constellation.metadata.io.FileMetadataReader;
import org.constellation.metadata.io.FileMetadataWriter;
import org.constellation.metadata.io.MDWebMetadataReader;
import org.constellation.metadata.io.MDWebMetadataWriter;
import org.constellation.metadata.io.MetadataReader;
import org.constellation.metadata.io.MetadataWriter;
import static org.constellation.generic.database.Automatic.*;
import org.constellation.ws.CstlServiceException;

/**
 * A default implementation of the CSW factory.
 * it provide various reader / writer and  lucene indexer / searcher.
 *
 * @author Guilhem Legal (Geomatys)
 */
public class DefaultCSWFactory extends AbstractCSWFactory {

    /*public DefaultCSWFactory() {
        super();
    }*/
    
    /**
     * Return a Metadata reader for the specified database type.
     * 
     * @throws org.constellation.ws.CstlServiceException
     */
    public MetadataReader getMetadataReader(Automatic configuration) throws CstlServiceException {
        int type = -1;
        if (configuration != null)
            type = configuration.getType();
        switch (type) {
            case MDWEB:
                return new MDWebMetadataReader(configuration);
            case FILESYSTEM:
                return new FileMetadataReader(configuration);
            default:
                throw new IllegalArgumentException("Unknow database type: " + type);
        }
    }
    
    /**
     * Return a Metadata Writer for the specified database type.
     * 
     * @param configuration
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    public MetadataWriter getMetadataWriter(Automatic configuration, AbstractIndexer indexer) throws CstlServiceException {
        int type = -1;
        if (configuration != null)
            type = configuration.getType();
        switch (type) {
            case MDWEB:
                return new MDWebMetadataWriter(configuration, indexer);
            case FILESYSTEM:
                return new FileMetadataWriter(configuration, indexer);
            default:
                throw new IllegalArgumentException("Unknow database type: " + type);
        }
    }
    
    /**
     * Return a profile (discovery or transactionnal) for the specified databaseType
     * @param dbType The type of the database.
     * 
     * @return DISCOVERY or TRANSACTIONAL 
     */
    public int getProfile(int dbType) {
        switch (dbType) {
            case MDWEB :
                return CSWworker.TRANSACTIONAL;
            case FILESYSTEM:
                return CSWworker.TRANSACTIONAL;
            default:
                throw new IllegalArgumentException("Unknow database type: " + dbType);
        }
    }
    
    /**
     * Return a Lucene index for the specified database type.
     * 
     * @param dbType The type of the database.
     * @param reader A metadataReader (unused for MDWeb database);
     * @param MDConnection A connecton to the database (used only for MDWeb database).
     * @param configDir
     * @return
     * @throws org.constellation.ws.CstlServiceException
     */
    public AbstractIndexer getIndexer(Automatic configuration, MetadataReader reader, String serviceID) throws IndexingException {
        int type = -1;
        if (configuration != null)
            type = configuration.getType();
        switch (type) {
            case MDWEB:
                return new MDWebIndexer(configuration, serviceID);
            case FILESYSTEM:
                return new GenericIndexer(reader, configuration, serviceID);
            default:
                throw new IllegalArgumentException("Unknow database type: " + type);
        }
    }
    
    @Override
    public AbstractIndexSearcher getIndexSearcher(int dbType, File configDirectory, String serviceID) throws IndexingException {
        switch (dbType) {
            case MDWEB:
                return new MDWebIndexSearcher(configDirectory, serviceID);
            case FILESYSTEM:
                return new GenericIndexSearcher(configDirectory, serviceID);
            default:
                throw new IllegalArgumentException("Unknow database type: " + dbType);
        }
    }
}
