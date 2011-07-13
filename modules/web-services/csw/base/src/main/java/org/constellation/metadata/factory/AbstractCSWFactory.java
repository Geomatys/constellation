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
import java.util.List;
import java.util.Map;
import org.constellation.generic.database.Automatic;
import org.constellation.metadata.harvest.CatalogueHarvester;
import org.constellation.metadata.io.CSWMetadataReader;
import org.constellation.metadata.io.CSWMetadataWriter;
import org.constellation.metadata.io.MetadataIoException;

// Geotoolkit dependencies
import org.constellation.metadata.io.MetadataReader;
import org.constellation.metadata.io.MetadataWriter;
import org.geotoolkit.factory.Factory;
import org.geotoolkit.lucene.IndexingException;
import org.geotoolkit.lucene.index.AbstractIndexSearcher;
import org.geotoolkit.lucene.index.AbstractIndexer;

/**
 * Factory used to load various implementation of metadata reader/writer, and Lucene indexer/searcher.
 * 
 * @author Guilhem Legal (Geomatys)
 */
public abstract class AbstractCSWFactory extends Factory {

    /**
     * Return a Metadata reader for the specified dataSource type.
     *
     * @param configuration A configuration object containing all the information to use the dataSource.
     *
     * @return a Metadata reader for the specified dataSource type.
     * @throws MetadataIoException
     */
    public abstract CSWMetadataReader getMetadataReader(final Automatic configuration) throws MetadataIoException;

    /**
     * Return a Metadata Writer for the specified dataSource type.
     *
     * @param configuration A configuration object containing all the information to use the dataSource.
     * @param indexer A metadata Indexer use by the metadata writer to add new metadata to the Lucene index.
     *
     * @return a Metadata Writer for the specified dataSource type.
     * @throws org.constellation.metadata.io.MetadataIoException
     */
    public abstract CSWMetadataWriter getMetadataWriter(final Automatic configuration, final AbstractIndexer indexer) throws MetadataIoException;

    /**
     * Return a Lucene indexer for the specified dataSource type.
     *
     * @param configuration A configuration object containing all the information to use the dataSource.
     * @param reader A metadata reader to access the dataSource.
     * @param serviceID An identifier of the service/index.
     * @param additionalQueryable A map of additional queryable element.
     *
     * @return A Lucene indexer for the specified dataSource type.
     * @throws IndexingException
     */
    public abstract AbstractIndexer getIndexer(final Automatic configuration, final MetadataReader reader,
            final String serviceID, final Map<String, List<String>> additionalQueryable) throws IndexingException;

    /**
     * Return a Lucene index searcher for the specified dataSource type.
     *
     * @param dbType The type of the dataSource.
     * @param configDir The directory containing the lucene index.
     * @param serviceID  An identifier of the service/index.
     *
     * @return  a Lucene index searcher for the specified dataSource type.
     * @throws IndexingException
     */
    public abstract AbstractIndexSearcher getIndexSearcher(final int dbType, final File configDir, final String serviceID) throws IndexingException;

    /**
     * Return a catalog harvester in the specified implementation type.
     *
     * @param configuration  A configuration object containing all the information about the chosen harvester implementation.
     * @param writer A metadata writer to store the harvested metadata into the dataSource.
     *
     * @return A Catalog harvester implementation.
     * @throws MetadataIoException
     */
    public abstract CatalogueHarvester getCatalogueHarvester(final Automatic configuration, final MetadataWriter writer) throws MetadataIoException;

}
