
package net.seagis.observation;

import java.sql.Timestamp;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import net.seagis.catalog.Entry;
import org.geotools.resources.Utilities;
import org.opengis.temporal.TemporalObject;

/**
 * un objet de type Date
 *
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="TemporalObject")
public class TemporalObjectEntry implements TemporalObject{
    
    /**
     * The begin date of a duration or a single TimeInstant (store in a string for jaxB issue).
     */
    @XmlElement(required = true)
    private String beginTime;
    /**
     * The end date of a duration or {@code null} (store in a string for jaxB issue).
     */
    private String endTime;
    
    /**
     * Constructeur vide utilisé par JAXB.
     */
    public TemporalObjectEntry(){}
    
    /** 
     * Create a new Temporal Object. 
     * It can be a TimeInstant Object if only the beginTime is define and endTime is null.
     * else it is record like a TimePeriod Object.
     *
     * @param beginTime The instant time or the begin date of the period.
     * @param endTime   if not {@code null} the object became a time period.
     */
    public TemporalObjectEntry(Timestamp beginTime, Timestamp endTime) {
        if (beginTime != null)
            this.beginTime = beginTime.toString();
        else
            this.beginTime = null;
        if (endTime != null)
            this.endTime = endTime.toString();
        else
            this.endTime = null;
    }

    public Timestamp getBeginTime() {
        Timestamp t = null;
        if(beginTime != null)
            t = Timestamp.valueOf(beginTime);
                    
        return t;
    }

    public Timestamp getEndTime() {
        Timestamp t = null;
        if(endTime != null)
            t = Timestamp.valueOf(endTime);
                    
        return t;
    }
    
     public void setBeginTime(Timestamp beginTime) {
        this.beginTime = beginTime.toString();
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime.toString();
    }
    
   
    
    /**
     * Vérifie que cette station est identique à l'objet spécifié
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        final TemporalObjectEntry that = (TemporalObjectEntry) object;
        return Utilities.equals(this.beginTime, that.beginTime) &&
               Utilities.equals(this.endTime,   that.endTime);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.beginTime != null ? this.beginTime.hashCode() : 0);
        hash = 89 * hash + (this.endTime != null ? this.endTime.hashCode() : 0);
        return hash;
    }
    
    /**
     * Retourne une chaine de charactere representant l'objet.
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("BeginTime=").append(beginTime).append('\n').append("EndTime=").append(endTime).append('\n');
        return s.toString();
    }
   
    
}
