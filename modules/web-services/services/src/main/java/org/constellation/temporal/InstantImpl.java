/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2004, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */

package org.constellation.temporal;

import java.util.Collection;
import org.opengis.temporal.Instant;
import org.opengis.temporal.Period;
import org.opengis.temporal.Position;

/**
 *
 * @author Guilhem Legal
 */
public class InstantImpl extends TemporalGeometricPrimitiveImpl implements Instant {

    private Position position;
    
    /**
     * An empty constructor used by JAXB
     */
    InstantImpl() {
        
    }
    
    public InstantImpl(Position position) {
        this.position = position;
    }
    
    public Position getPosition() {
        return position;
    }

    public Collection<Period> getBegunBy() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<Period> getEndedBy() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

   

}
