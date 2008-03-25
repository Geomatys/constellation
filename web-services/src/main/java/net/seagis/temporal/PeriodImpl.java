/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.seagis.temporal;

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
