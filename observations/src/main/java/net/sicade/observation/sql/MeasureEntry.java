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
    
    /**
     * L'unite de la mesure
     */
    private String uom;
    
    /**
     * La valeur de la mesure
     */
    private float value;
    
    /** Creates a new instance of MeasureEntry */
    public MeasureEntry(final String name,
                        final String uom,
                        final float value)
    {
        super(name);
        this.uom   = uom;
        this.value = value;        
    }
    
    /**
     * {@inheritDoc}
     *
     * @todo Implementer le retour des unites.
     */
    public String getUom() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public float getValue() {
        return value;
    }
    
}
