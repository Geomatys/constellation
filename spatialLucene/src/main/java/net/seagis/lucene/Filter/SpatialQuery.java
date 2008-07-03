/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 * (C) 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package net.seagis.lucene.Filter;

import org.apache.lucene.search.Filter;
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
        
        this.spatialFilter = new SpatialFilter(geometry, crsName, filterType, distance, units);
        query         = new StringBuilder();
    }
    
    public SpatialQuery(String query, Filter filter) {
        this.query    = new StringBuilder(query);
        spatialFilter = filter;
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
        return query.toString();
    }
    
    /**
     * set the lucene query associated with the filter. 
     */
    public void setQuery(String query) {
        this.query = new StringBuilder(query);
    }
    
    /**
     * append a piece of lucene query to the main query.
     * 
     * @param s a piece of lucene query.
     */
    public void appendToQuery(String s) {
        query.append(s);
    }

}
