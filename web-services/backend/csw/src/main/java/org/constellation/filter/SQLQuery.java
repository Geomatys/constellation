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

package org.constellation.filter;

import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.search.Filter;

/**
 *
 * @author guilhem
 */
public class SQLQuery {
    
    private String query;
    
    private Filter spatialFilter;
    
    private List<SQLQuery> subQueries;
    
    public SQLQuery(String query) {
        this.query         = query;
        this.spatialFilter = null;
    }
    
    public SQLQuery(Filter spatialFilter) {
        this.query         = "";
        this.spatialFilter = spatialFilter;
    }
    
    public SQLQuery(String query, Filter spatialFilter) {
        this.query         = query;
        this.spatialFilter = spatialFilter;
    }

    public String getQuery() {
        return query;
    }
    
    public void addSelect(String select) {
        query = select + ' ' + query;
    }

    public Filter getSpatialFilter() {
        return spatialFilter;
    }

    public List<SQLQuery> getSubQueries() {
        if (subQueries == null)
            subQueries = new ArrayList<SQLQuery>();
        return subQueries;
    }

    public void setSubQueries(List<SQLQuery> subQueries) {
        this.subQueries = subQueries;
    }
    
    @Override
    public String toString() {
        StringBuilder s =new StringBuilder("[SQLquery]").append('\n');
        if (query != null && !query.equals(""))
            s.append("query= ").append(query).append('\n');
        if (spatialFilter != null) {
            s.append("spatialFilter").append(spatialFilter).append('\n');
        }
        if (subQueries != null && subQueries.size() != 0) {
            s.append("SubQueries:").append('\n');
            int i = 0;
            for (SQLQuery sq: subQueries) {
                s.append(i).append(": ").append(sq).append('\n'); 
                i++;
            }
        }
        return s.toString();
    }

}
