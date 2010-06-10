/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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

package org.constellation.metadata.index;

import java.io.File;
import java.util.List;
import java.util.Map;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.geotoolkit.lucene.IndexingException;
import org.geotoolkit.lucene.index.AbstractIndexer;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public abstract class AbstractCSWIndexer<A> extends AbstractIndexer<A>{

    public AbstractCSWIndexer(String serviceID, File configDirectory) {
        super(serviceID, configDirectory);
    }

    public AbstractCSWIndexer(String serviceID, File configDirectory, Analyzer analyzer) {
        super(serviceID, configDirectory, analyzer);
    }

    protected abstract boolean indexISO19139(final Document doc, final A metadata, Map<String, List<String>> queryableSet, final StringBuilder anyText, boolean alreadySpatiallyIndexed) throws IndexingException;

    protected abstract boolean indexDublinCore(final Document doc, final A metadata, Map<String, List<String>> queryableSet, final StringBuilder anyText, boolean alreadySpatiallyIndexed) throws IndexingException;

    protected abstract boolean isISO19139(A meta);

    protected abstract boolean isDublinCore(A meta);

    protected abstract boolean isEbrim25(A meta);
    
    protected abstract boolean isEbrim30(A meta);

}
