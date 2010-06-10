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

    protected static final String NOT_SPATIALLY_INDEXABLE = "unable to spatially index metadata: ";
    
    public AbstractCSWIndexer(String serviceID, File configDirectory) {
        super(serviceID, configDirectory);
    }

    public AbstractCSWIndexer(String serviceID, File configDirectory, Analyzer analyzer) {
        super(serviceID, configDirectory, analyzer);
    }

    /**
     * Spatially index the form extracting the BBOX values with the specified queryable set.
     *
     * @param doc The current Lucene document.
     * @param form The mdweb records to spatially index.
     * @param queryableSet A set of queryable Term.
     * @param ordinal
     *
     * @return true if the indexation succeed
     * @throws MD_IOException
     */
    protected boolean indexSpatialPart(Document doc, A form, Map<String, List<String>> queryableSet) throws IndexingException {

            final List<Double> minxs = extractPositions(form, queryableSet.get("WestBoundLongitude"));
            final List<Double> maxxs = extractPositions(form, queryableSet.get("EastBoundLongitude"));
            final List<Double> maxys = extractPositions(form, queryableSet.get("NorthBoundLatitude"));
            final List<Double> minys = extractPositions(form, queryableSet.get("SouthBoundLatitude"));

            if (minxs.size() == minys.size() && minys.size() == maxxs.size() && maxxs.size() == maxys.size()) {
                if (minxs.size() == 1) {
                    addBoundingBox(doc, minxs.get(0), maxxs.get(0), minys.get(0), maxys.get(0), SRID_4326);
                    return true;
                } else if (minxs.size() > 0) {
                    addMultipleBoundingBox(doc, minxs, maxxs, minys, maxys, SRID_4326);
                    return true;
                }
            } else {
                LOGGER.warning(NOT_SPATIALLY_INDEXABLE + getIdentifier(form) + "\n cause: missing coordinates."
                        + minxs.size() + " " + minys.size() + " " +  maxxs.size() + " " +  maxys.size());
            }
        return false;
    }

    protected abstract List<Double> extractPositions(A metadata, List<String> paths) throws IndexingException;

    protected abstract boolean indexISO19139(final Document doc, final A metadata, Map<String, List<String>> queryableSet, final StringBuilder anyText, boolean alreadySpatiallyIndexed) throws IndexingException;

    protected abstract boolean indexDublinCore(final Document doc, final A metadata, Map<String, List<String>> queryableSet, final StringBuilder anyText, boolean alreadySpatiallyIndexed) throws IndexingException;

    protected abstract boolean isISO19139(A meta);

    protected abstract boolean isDublinCore(A meta);

    protected abstract boolean isEbrim25(A meta);
    
    protected abstract boolean isEbrim30(A meta);

}
