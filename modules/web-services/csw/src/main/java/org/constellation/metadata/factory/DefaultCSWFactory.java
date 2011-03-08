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
import org.constellation.metadata.index.generic.GenericIndexer;
import org.constellation.metadata.index.mdweb.MDWebIndexer;
import org.constellation.metadata.io.CSWMetadataReader;
import org.constellation.metadata.io.CSWMetadataWriter;
import org.constellation.metadata.io.filesystem.FileMetadataReader;
import org.constellation.metadata.io.filesystem.FileMetadataWriter;
import org.constellation.metadata.io.mdweb.MDWebCSWMetadataReader;
import org.constellation.metadata.io.mdweb.MDWebCSWMetadataWriter;
import org.constellation.metadata.io.MetadataIoException;
import static org.constellation.generic.database.Automatic.*;
import org.constellation.metadata.harvest.ByIDHarvester;
import org.constellation.metadata.harvest.CatalogueHarvester;
import org.constellation.metadata.harvest.DefaultCatalogueHarvester;
import org.constellation.metadata.harvest.FileSystemHarvester;
import org.constellation.metadata.io.MetadataWriter;
import org.geotoolkit.lucene.IndexingException;
import org.geotoolkit.lucene.index.AbstractIndexSearcher;
import org.geotoolkit.lucene.index.AbstractIndexer;

/**
 * A default implementation of the CSW factory.
 * it provide various reader / writer and  lucene indexer / searcher.
 *
 * @author Guilhem Legal (Geomatys)
 */
public class DefaultCSWFactory extends AbstractCSWFactory {

    private static final String UNKNOW_DATABASE_TYPE = "Unknow database type: ";

    private static final String DEFAULT_FACTORY = " In Default CSW Factory.";

    /**
     * {@inheritDoc}
     */
    @Override
    public CSWMetadataReader getMetadataReader(Automatic configuration) throws MetadataIoException {
        int type = -1;
        if (configuration != null)
            type = configuration.getType();
        switch (type) {
            case MDWEB:
                return new MDWebCSWMetadataReader(configuration);
            case FILESYSTEM:
                return new FileMetadataReader(configuration);
            default:
                throw new IllegalArgumentException(UNKNOW_DATABASE_TYPE + type + DEFAULT_FACTORY);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public CSWMetadataWriter getMetadataWriter(Automatic configuration, AbstractIndexer indexer) throws MetadataIoException {
        int type = -1;
        if (configuration != null)
            type = configuration.getType();
        switch (type) {
            case MDWEB:
                return new MDWebCSWMetadataWriter(configuration, indexer);
            case FILESYSTEM:
                return new FileMetadataWriter(configuration, indexer);
            default:
                throw new IllegalArgumentException(UNKNOW_DATABASE_TYPE + type + DEFAULT_FACTORY);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractIndexer getIndexer(Automatic configuration, CSWMetadataReader reader, String serviceID) throws IndexingException {
        int type = -1;
        if (configuration != null)
            type = configuration.getType();
        switch (type) {
            case MDWEB:
                return new MDWebIndexer(configuration, serviceID);
            case FILESYSTEM:
                return new GenericIndexer(reader, configuration, serviceID);
            default:
                throw new IllegalArgumentException(UNKNOW_DATABASE_TYPE + type + DEFAULT_FACTORY);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractIndexSearcher getIndexSearcher(int dbType, File configDirectory, String serviceID) throws IndexingException {
        switch (dbType) {
            case MDWEB:
                return new AbstractIndexSearcher(configDirectory, serviceID);
            case FILESYSTEM:
                return new AbstractIndexSearcher(configDirectory, serviceID);
            default:
                throw new IllegalArgumentException(UNKNOW_DATABASE_TYPE + dbType + DEFAULT_FACTORY);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CatalogueHarvester getCatalogueHarvester(Automatic configuration, MetadataWriter writer) throws MetadataIoException {
        int type = -1;
        if (configuration != null)
            type = configuration.getHarvestType();
        switch (type) {
            case DEFAULT:
                return new DefaultCatalogueHarvester(writer);
            case FILESYSTEM:
                return new FileSystemHarvester(writer);
            case BYID:
                return new ByIDHarvester(writer, configuration.getIdentifierDirectory());
            default:
                throw new IllegalArgumentException("Unknow harvester type: " + type + DEFAULT_FACTORY);
        }
    }
}
