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

import java.awt.geom.Line2D;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.GeneralEnvelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author guilhem
 */
public class SpatialQuery {
    
    /**
     * The geometry object witch witch we want to filter.
     */
    private Object geometry ;
    
    /**
     * The spatial operator to apply.
     */
    private int filterType;

    /**
     * The distance use in the Distance filter like DWITHIN or BEYOND
     */
    private Double distance;
    
    /**
     * The distance units  use in the Distance filter like DWITHIN or BEYOND
     */
    private String units;
    
    private String crsName;
    
    /**
     * 
     * @param geometry
     * @param crsName
     * @param filterType
     * @throws org.opengis.referencing.NoSuchAuthorityCodeException
     * @throws org.opengis.referencing.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     */
    public SpatialQuery(Object geometry, String crsName, int filterType) throws NoSuchAuthorityCodeException, FactoryException, TransformException {
        if (!(geometry instanceof GeneralEnvelope) && !(geometry instanceof Line2D) && !(geometry instanceof GeneralDirectPosition)) {
            throw new IllegalArgumentException("illegal geometry type. supported ones are: GeneralEnvelope, Line2D, GeneralDirectPosition");
        }
        this.filterType = filterType;
        this.distance   = null;
        this.units      = null;
        this.geometry   = geometry;
        this.crsName    = crsName;
    }
    
    public SpatialQuery(Object geometry, String crsName, int filterType, double distance, String units) throws NoSuchAuthorityCodeException, FactoryException, TransformException {
        if (!(geometry instanceof GeneralEnvelope) && !(geometry instanceof Line2D) && !(geometry instanceof GeneralDirectPosition)) {
            throw new IllegalArgumentException("illegal geometry type. supported ones are: GeneralEnvelope, Line2D, GeneralDirectPosition");
        }
        this.filterType = filterType;
        this.distance   = distance;
        this.units      = units;
        this.geometry   = geometry;
        this.crsName    = crsName;
    }
    
    
    
    /**
     * 
     */
    public Filter getFilter() throws NoSuchAuthorityCodeException, FactoryException {

        if (distance == null)
            return new SpatialFilter(geometry, crsName, filterType);
        else
            return new SpatialFilter(geometry, crsName, filterType, distance, units);
    }

    public Query getQuery() throws NoSuchAuthorityCodeException, FactoryException {
        return new ConstantScoreQuery(getFilter());
    }

}
