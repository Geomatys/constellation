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
package org.constellation.temporal;

import java.util.Date;
import org.opengis.temporal.Instant;
import org.opengis.temporal.Period;


/**
 *
 * @author legal
 */
public class PeriodImpl extends TemporalGeometricPrimitiveImpl implements Period {

    private Instant begining;
    
    private Instant ending;
    
    public PeriodImpl() {
        
    }
    
    public PeriodImpl(Instant begining, Instant ending) {
        this.begining = begining;
        this.ending   = ending;
    }
    
    public Instant getBeginning() {
        return begining;
    }
    
    public void setBegining(Instant begining) {
        this.begining = begining;
    }
    
    /**
     * temporary patch fixing the error of MDWeb
     * 
     */
    public void setBegining(Date date) {
        this.begining = new InstantImpl(new PositionImpl(date));
    }

    public Instant getEnding() {
        return ending;
    }
    
    public void setEnding(Instant ending) {
        this.ending = ending;
    }
    
    /**
     * temporary patch fixing the error of MDWeb
     * 
     */
    public void setEnding(Date date) {
        this.begining = new InstantImpl(new PositionImpl(date));
    }
}
