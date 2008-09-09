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

import java.sql.Time;
import java.util.Date;
import org.opengis.temporal.Position;
import org.opengis.temporal.TemporalPosition;
import org.opengis.util.InternationalString;

/**
 *
 * @author Guilhem Legal
 */
public class PositionImpl implements Position{

    private Date date;
    
    /**
     * An empty constructor used by JAXB
     */
    PositionImpl() {
        
    }
    
    public PositionImpl(Date date) {
        this.date = date;
    }
    
    public TemporalPosition anyOther() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Date getDate() {
        return date;
    }

    public Time getTime() {
        return new Time(date.getTime());
    }

    public InternationalString getDateTime() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
