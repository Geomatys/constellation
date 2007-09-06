
package net.sicade.observation;

import java.sql.Date;
import net.sicade.catalog.Entry;
import org.opengis.temporal.TemporalObject;

/**
 * un objet de type Date
 *
 * @author Guilhem Legal
 */
public class TemporalObjectEntry extends Entry implements TemporalObject{
    
    /**
     * The begin date of a duration or a single TimeInstant.
     */
    private Date beginTime;
    
    /**
     * The end date of a duration or {@code null}.
     */
    private Date endTime;
    
    /** 
     * Create a new Temporal Object. 
     * It can be a TimeInstant Object if only the beginTime is define and endTime is null.
     * else it is record like a TimePeriod Object.
     *
     * @param beginTime The instant time or the begin date of the period.
     * @param endTime   if not {@code null} the object became a time period.
     */
    public TemporalObjectEntry(Date beginTime, Date endTime) {
        super(null);
        this.beginTime = beginTime;
        this.endTime   = endTime;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

   
    
}
