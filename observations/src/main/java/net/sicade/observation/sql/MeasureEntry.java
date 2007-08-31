/* Created on 31 aout 2007, 12:02 */

package net.sicade.observation.sql;

// Sicade dependencies
import net.sicade.catalog.Entry;

// OpenGis dependencies
import org.opengis.observation.Measure;

/**
 *MeasureEntry.java
 *
 * @author Guilhem Legal
 * @author Mehdi Sidhoum
 */
public class MeasureEntry extends Entry implements Measure{
    
    /** Creates a new instance of MeasureEntry */
    public MeasureEntry() {
    }
    
    /**
     * {@inheritDoc}
     *
     * @todo Implementer le retour des unites.
     */
    public Unit getUom() {
        return null;
    }
    
}
