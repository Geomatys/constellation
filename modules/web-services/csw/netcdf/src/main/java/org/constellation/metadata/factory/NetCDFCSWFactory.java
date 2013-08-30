/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Institut de Recherche pour le Développement
 *    (C) 2007 - 2011, Geomatys
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
import java.util.List;
import java.util.Map;
import java.util.Arrays;

import org.constellation.filter.FilterParser;
import org.constellation.metadata.harvest.ByIDHarvester;
import org.constellation.metadata.harvest.FileSystemHarvester;
import org.constellation.metadata.harvest.DefaultCatalogueHarvester;
import org.constellation.configuration.DataSourceType;
import org.constellation.filter.LuceneFilterParser;
import org.constellation.filter.SQLFilterParser;
import org.constellation.generic.database.Automatic;
import org.constellation.metadata.harvest.CatalogueHarvester;
import org.constellation.metadata.index.generic.GenericIndexer;
import org.constellation.metadata.io.CSWMetadataReader;
import org.constellation.metadata.io.CSWMetadataWriter;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.metadata.io.MetadataReader;
import org.constellation.metadata.io.MetadataWriter;
import org.constellation.metadata.io.netcdf.NetCDFMetadataReader;
import org.geotoolkit.lucene.IndexingException;
import org.geotoolkit.lucene.index.LuceneIndexSearcher;
import org.geotoolkit.lucene.index.AbstractIndexer;

import static org.constellation.generic.database.Automatic.*;
import org.constellation.metadata.security.MetadataSecurityFilter;
import org.constellation.metadata.security.NoMetadataSecurityFilter;
import org.geotoolkit.index.tree.Tree;
import org.geotoolkit.lucene.tree.NamedEnvelope;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @since 0.8.4
 */
public class NetCDFCSWFactory implements AbstractCSWFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DataSourceType> availableType() {
        return Arrays.asList(DataSourceType.NETCDF);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean factoryMatchType(DataSourceType type) {
        return DataSourceType.NETCDF.equals(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CSWMetadataReader getMetadataReader(final Automatic configuration) throws MetadataIoException {
        return new NetCDFMetadataReader(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CSWMetadataWriter getMetadataWriter(final Automatic configuration, final AbstractIndexer indexer) throws MetadataIoException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractIndexer getIndexer(final Automatic configuration, final MetadataReader reader, final String serviceID, final Map<String, List<String>> additionalQueryable) throws IndexingException {
        return new GenericIndexer(reader, configuration.getConfigurationDirectory(), serviceID, additionalQueryable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LuceneIndexSearcher getIndexSearcher(final File configDir, final String serviceID, final Tree<NamedEnvelope> rTree) throws IndexingException {
        return new LuceneIndexSearcher(configDir, serviceID, null, true, rTree);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CatalogueHarvester getCatalogueHarvester(final Automatic configuration, final MetadataWriter writer) throws MetadataIoException {
        int type = -1;
        if (configuration != null) {
            type = configuration.getHarvestType();
        }
        switch (type) {
            case DEFAULT:
                return new DefaultCatalogueHarvester(writer);
            case FILESYSTEM:
                return new FileSystemHarvester(writer);
            case BYID:
                return new ByIDHarvester(writer, configuration.getIdentifierDirectory());
            default:
                throw new IllegalArgumentException("Unknow harvester type: " + type + " In Default CSW Factory.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FilterParser getLuceneFilterParser() {
        return new LuceneFilterParser();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FilterParser getSQLFilterParser() {
        return new SQLFilterParser();
    }

    @Override
    public MetadataSecurityFilter getSecurityFilter() {
        return new NoMetadataSecurityFilter();
    }
    
}
