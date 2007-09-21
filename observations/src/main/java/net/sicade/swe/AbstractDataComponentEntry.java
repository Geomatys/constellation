
package net.sicade.swe;

import net.sicade.catalog.Entry;

/**
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public abstract class AbstractDataComponentEntry extends Entry implements AbstractDataComponent{
    
    /**
     * Constructeur utilisé par jaxb.
     */
    public AbstractDataComponentEntry() {}
    
    /**
     */
    public AbstractDataComponentEntry(String id) {
        super(id);
    }
    
}
