/* Created on 3 septembre 2007, 12:35 */

package net.sicade.observation;

import java.sql.Date;
import org.opengis.temporal.TemporalObject;

/**
 * a temporal object  from ISO 19108
 *
 * @author Guilhem Legal
 */
public class TemporalObjectEntry implements TemporalObject{
    
    private Date time;
    
    public TemporalObjectEntry(Date time) {
        this.time = time;
    }
    
}
