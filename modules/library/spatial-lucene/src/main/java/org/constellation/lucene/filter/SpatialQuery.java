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
package org.constellation.lucene.filter;

import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Sort;
import org.geotools.util.Utilities;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author guilhem
 */
public class SpatialQuery {
    
    /**
     * The spatial filter added to the lucene query.
     */
    private Filter spatialFilter ;
    
    /**
     * The lucene query
     */
    private StringBuilder query;
    
    /**
     * Logical operator to apply between the spatial filter and the query
     * default operator is AND.
     */
    private int logicalOperator = SerialChainFilter.AND;
    
    /**
     * A list of sub-queries with have to be executed separely.
     */
    private List<SpatialQuery> subQueries;
    
    /**
     * An lucene Sort object allowing to sort the results
     */
    private Sort sort;
   
    
    /**
     * Build a new Query combinating a lucene query and a spatial filter.
     * 
     * @param geometry   A geometry object.
     * @param crsName    A corrdinate Reference System name
     * @param filterType A flag correspounding to the type of the spatial filter
     * 
     * @throws org.opengis.referencing.NoSuchAuthorityCodeException
     * @throws org.opengis.referencing.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     */
    public SpatialQuery(Object geometry, String crsName, int filterType) throws NoSuchAuthorityCodeException, FactoryException, TransformException {
        
        spatialFilter = new SpatialFilter(geometry, crsName, filterType);
        query         = new StringBuilder();
        subQueries    = new ArrayList<SpatialQuery>();
    }
    
    /**
     * Build a new Query combinating a lucene query and a distance filter .
     * 
     * @param geometry   A geometry object.
     * @param crsName    A corrdinate Reference System name
     * @param filterType A flag correspounding to the type of the spatial filter
     * @param distance   A distance expressed in the specified units.
     * @param units      The units of the specified distance. 
     * 
     * @throws org.opengis.referencing.NoSuchAuthorityCodeException
     * @throws org.opengis.referencing.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     */
    public SpatialQuery(Object geometry, String crsName, int filterType, double distance, String units) throws NoSuchAuthorityCodeException, FactoryException, TransformException {
        
        spatialFilter = new SpatialFilter(geometry, crsName, filterType, distance, units);
        query         = new StringBuilder();
        subQueries    = new ArrayList<SpatialQuery>();
    }
    
    /**
     * Build a new Query combinating a lucene query and a lucene filter.
     * 
     * @param query  A well-formed Lucene query. 
     * @param filter A lucene filter (spatial, serialChain, ...)
     * @pram  logicalOperator The logical operator to apply between the query and the spatialFilter.
     */
    public SpatialQuery(String query, Filter filter, int logicalOperator) {
        this.query           = new StringBuilder(query);
        spatialFilter        = filter;
        this.logicalOperator = logicalOperator;
        subQueries           = new ArrayList<SpatialQuery>();
    }
    
    /**
     * Build a new Simple Text Query.
     * 
     * @param query  A well-formed Lucene query. 
     */
    public SpatialQuery(String query) {
        this.query           = new StringBuilder(query);
        spatialFilter        = null;
        subQueries           = new ArrayList<SpatialQuery>();
    }
    
    
    
    /**
     * Return the spatial filter (it can be a SerialChainFilter) to add to the lucene query.
     */
    public Filter getSpatialFilter() {

        return spatialFilter;
    }

    /**
     * Return the lucene query associated with the filter. 
     */
    public String getQuery() {
        if (query == null || query.toString().equals("") || query.toString().equals(" "))
            return "metafile:doc";
        
        return query.toString();
    }
    
    /**
     * Return the logical operator to apply between the query and the filter. 
     */
    public int getLogicalOperator() {
        return logicalOperator;
    }
    
    /**
     * Return the sort Object joinded to this Query. 
     */
    public Sort getSort() {
        return sort;
    }

    /**
     * Add a sort Object to the query
     * 
     * @param sort
     */
    public void setSort(Sort sort) {
        this.sort = sort;
        for (SpatialQuery sub: getSubQueries()) {
            sub.setSort(sort);
        }
    }
    
    /**
     * Return the subQueries joined to this query. 
     */
    public List<SpatialQuery> getSubQueries() {
        return subQueries;
    }
    
    /**
     * Set the sub-queries list.
     * 
     * @param subQueries a list of spatial queries.
     */
    public void setSubQueries(List<SpatialQuery> subQueries) {
        this.subQueries = subQueries;
    }
    
    /**
     * Add a new spatial query to the list of sub-queries
     *
     * @param sq a spatial query.
     */
    public void addSubQuery(SpatialQuery sq) {
        subQueries.add(sq);
    }
    
    /**
     * Set the lucene query associated with the filter. 
     */
    public void setQuery(String query) {
        this.query = new StringBuilder(query);
    }
    
    /**
     * Append a piece of lucene query to the main query.
     * 
     * @param s a piece of lucene query.
     */
    public void appendToQuery(String s) {
        query.append(s);
    }
    
    /**
     * Return a String representation of the object. 
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("[SpatialQuery]:").append('\n');
        
        if (spatialFilter == null && !query.toString().equals("") && logicalOperator == SerialChainFilter.NOT) {
            s.append("query: NOT <").append(query).append(">").append('\n');
            
        } else if (!query.toString().equals("")) {
            s.append('\t').append("query: |").append(query.toString()).append('|').append('\n');
        }
            
        if (spatialFilter != null && !query.toString().equals("")) {
            s.append(SerialChainFilter.ValueOf(logicalOperator)).append('\n');
        }
        
        if (spatialFilter != null) {
            s.append('\t').append(spatialFilter).append('\n');
        }
        if (subQueries != null && subQueries.size() > 0) {
            s.append("subqueries:").append('\n');
            int i = 0;
            for (SpatialQuery sq: subQueries) {
                s.append("sub ").append(i).append(':').append(sq);
                i++;
            }
        }
        if (sort != null) {
            s.append("Sort: ").append(sort).append('\n');
        }
        return s.toString();
    }

    /**
     * Verify if this entry is identical to the specified object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof SpatialQuery) {
            final SpatialQuery that = (SpatialQuery) object;

            return (this.logicalOperator ==  that.logicalOperator)          &&
                   Utilities.equals(this.getQuery(), that.getQuery())       &&
                   Utilities.equals(this.sort, that.sort)                   &&
                   Utilities.equals(this.spatialFilter, that.spatialFilter) &&
                   Utilities.equals(this.subQueries, that.subQueries);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.spatialFilter != null ? this.spatialFilter.hashCode() : 0);
        hash = 97 * hash + (this.query != null ? getQuery().hashCode() : 0);
        hash = 97 * hash + this.logicalOperator;
        hash = 97 * hash + (this.subQueries != null ? this.subQueries.hashCode() : 0);
        hash = 97 * hash + (this.sort != null ? this.sort.hashCode() : 0);
        return hash;
    }
}
