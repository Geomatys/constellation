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

package org.constellation.metadata.index;

import org.apache.lucene.search.IndexSearcher;

/**
 *
 * @author Guilhem legal (Geomatys)
 */
public class AbstractIndexSearcher {

    /**
     * This is the index searcher of Lucene.
     */
    private IndexSearcher searcher;


    /**
     * Returns the IndexSearcher of this index.
     */
    public IndexSearcher getSearcher() {
        return searcher;
    }

    /**
     * The IndexSearcher setter of this index.
     *
     * @param searcher an IndexSearcher object.
     */
    public void setSearcher(IndexSearcher searcher) {
        this.searcher = searcher;
    }

}
