/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.constellation.metadata.factory;

import org.constellation.configuration.DataSourceType;
import org.constellation.filter.FilterParser;
import org.constellation.generic.database.Automatic;
import org.constellation.metadata.harvest.CatalogueHarvester;
import org.constellation.metadata.io.CSWMetadataReader;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.metadata.io.MetadataReader;
import org.constellation.metadata.io.MetadataWriter;
import org.constellation.metadata.security.MetadataSecurityFilter;
import org.geotoolkit.lucene.IndexingException;
import org.geotoolkit.lucene.index.AbstractIndexer;
import org.geotoolkit.lucene.index.LuceneIndexSearcher;

import java.io.File;
import java.util.List;
import java.util.Map;

// constellation dependencies
// Geotoolkit dependencies

/**
 * Factory used to load various implementation of metadata reader/writer, and Lucene indexer/searcher.
 * 
 * @author Guilhem Legal (Geomatys)
 */
public interface AbstractCSWFactory {

    /**
     * Return true if the factory can return an implementation for the specified type.
     */
    boolean factoryMatchType(final DataSourceType type);
    
    /**
     * Return a list of supported datasource type
     */
    List<DataSourceType> availableType();
    
    /**
     * Return a Metadata reader for the specified dataSource type.
     *
     * @param configuration A configuration object containing all the information to use the dataSource.
     * @param serviceID
     *
     * @return a Metadata reader for the specified dataSource type.
     * @throws MetadataIoException
     */
    CSWMetadataReader getMetadataReader(final Automatic configuration, final String serviceID) throws MetadataIoException;

    /**
     * Return a Metadata Writer for the specified dataSource type.
     *
     * @param configuration A configuration object containing all the information to use the dataSource.
     * @param indexer A metadata Indexer use by the metadata writer to add new metadata to the Lucene index.
     *
     * @return a Metadata Writer for the specified dataSource type.
     * @throws org.constellation.metadata.io.MetadataIoException
     */
    MetadataWriter getMetadataWriter(final Automatic configuration, final AbstractIndexer indexer, final String serviceID) throws MetadataIoException;

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
    AbstractIndexer getIndexer(final Automatic configuration, final MetadataReader reader, final String serviceID, 
            final Map<String, List<String>> additionalQueryable) throws IndexingException;

    /**
     * Return a Lucene index searcher for the specified dataSource type.
     *
     * @param configDir The directory containing the lucene index.
     * @param serviceID  An identifier of the service/index.
     *
     * @return  a Lucene index searcher for the specified dataSource type.
     * @throws IndexingException
     */
    LuceneIndexSearcher getIndexSearcher(final File configDir, final String serviceID) throws IndexingException;

    /**
     * Return a catalog harvester in the specified implementation type.
     *
     * @param configuration  A configuration object containing all the information about the chosen harvester implementation.
     * @param writer A metadata writer to store the harvested metadata into the dataSource.
     *
     * @return A Catalog harvester implementation.
     * @throws MetadataIoException
     */
    CatalogueHarvester getCatalogueHarvester(final Automatic configuration, final MetadataWriter writer) throws MetadataIoException;

    FilterParser getLuceneFilterParser();
    
    FilterParser getSQLFilterParser();
    
    MetadataSecurityFilter getSecurityFilter();
    
    Map<String, List<String>> getBriefFieldMap();
}
