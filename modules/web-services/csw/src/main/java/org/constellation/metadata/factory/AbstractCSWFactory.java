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
import org.constellation.metadata.harvest.CatalogueHarvester;
import org.constellation.metadata.io.CSWMetadataReader;
import org.constellation.metadata.io.CSWMetadataWriter;
import org.constellation.metadata.io.MetadataIoException;

// Geotoolkit dependencies
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
     * Return a Metadata reader for the specified datasource type.
     *
     * @param configuration A configuration object containing all the information to use the datasource.
     *
     * @return a Metadata reader for the specified datasource type.
     * @throws MetadataIoException
     */
    public abstract CSWMetadataReader getMetadataReader(Automatic configuration) throws MetadataIoException;

    /**
     * Return a Metadata Writer for the specified datasource type.
     *
     * @param configuration A configuration object containing all the information to use the datasource.
     * @param indexer A metadata Indexer use by the metadata writer to add new metadata to the Lucene index.
     *
     * @return a Metadata Writer for the specified datasource type.
     * @throws org.constellation.metadata.io.MetadataIoException
     */
    public abstract CSWMetadataWriter getMetadataWriter(Automatic configuration, AbstractIndexer indexer) throws MetadataIoException;

    /**
     * Return a Lucene indexer for the specified datasource type.
     *
     * @param configuration A configuration object containing all the information to use the datasource.
     * @param reader A metadata reader to acces the datasource.
     * @param serviceID An identifier of the service/index.
     *
     * @return A Lucene indexer for the specified datasource type.
     * @throws IndexingException
     */
    public abstract AbstractIndexer getIndexer(Automatic configuration, CSWMetadataReader reader, String serviceID) throws IndexingException;

    /**
     * Return a Lucene index searcher for the specified datasource type.
     *
     * @param dbType The type of the datasource.
     * @param configDir The directory containing the lucene index.
     * @param serviceID  An identifier of the service/index.
     *
     * @return  a Lucene index searcher for the specified datasource type.
     * @throws IndexingException
     */
    public abstract AbstractIndexSearcher getIndexSearcher(int dbType, File configDir, String serviceID) throws IndexingException;

    /**
     * Return a catalogue harvester in the specified implementation type.
     *
     * @param configuration  A configuration object containing all the information about the choosen harvester implementation.
     * @param writer A metadata writer to store the harvested metadata into the datasource.
     *
     * @return A Catalogue harvester implementation.
     * @throws MetadataIoException
     */
    public abstract CatalogueHarvester getCatalogueHarvester(Automatic configuration, MetadataWriter writer) throws MetadataIoException;

}
