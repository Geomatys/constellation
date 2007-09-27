
package net.sicade.swe;

/**
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public abstract class AbstractDataRecordEntry extends AbstractDataComponentEntry {
    
    /**
     * constructeur utilisé par jaxB.
     */
    public AbstractDataRecordEntry() {}
            
    /**
     * super-constructeur appellé par les sous-classes.
     */
    public AbstractDataRecordEntry(final String id, final String definition, boolean fixed) {
        super(id, definition, fixed);
    }
    
}
