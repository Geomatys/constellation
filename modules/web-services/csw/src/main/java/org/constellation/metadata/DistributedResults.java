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

package org.constellation.metadata;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class DistributedResults {
        
    /**
     * The number of records matched on all distributed servers.
     */
    public int nbMatched;

    /**
     * The merged list of records.
     */
    public List<Object> additionalResults;

    /**
     * Build an empty distributed results with 0 records matching and 0 additional results.
     */
    public DistributedResults() {
        this.nbMatched         = 0;
        this.additionalResults = new ArrayList<Object>();
    }

    /**
     * Build a distributed results with the specified number of records matching and additional results.
     */
    public DistributedResults(int nbMatched, List<Object> additionalResults) {
        this.nbMatched         = nbMatched;
        this.additionalResults = additionalResults;
    }

}
