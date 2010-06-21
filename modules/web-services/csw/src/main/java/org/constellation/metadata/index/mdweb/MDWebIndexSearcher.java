/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
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

package org.constellation.metadata.index.mdweb;

// J2SE dependencies
import java.io.File;

// Geotoolkit dependencies
import org.geotoolkit.lucene.IndexingException;
import org.geotoolkit.lucene.index.AbstractIndexSearcher;

/**
 * An index searcher for Lucene index connected to a MDWeb datasource.
 * 
 * @author Guilhem Legal (Geomatys)
 */
public class MDWebIndexSearcher extends AbstractIndexSearcher {

    /**
     * Build a new index searcher with the index located in the specified directory.
     * The index directory path must be :
     * <configDir path>/<serviceID>index-<some timestamp number>
     *
     * @param configDir A directory containing the lucene index directory.
     * @param serviceID The identifier of the index/service
     * @throws IndexingException
     */
    public MDWebIndexSearcher(File configDir, String serviceID) throws IndexingException {
        super(configDir, serviceID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIdentifierSearchField() {
        return "identifier_sort";
    }
}
