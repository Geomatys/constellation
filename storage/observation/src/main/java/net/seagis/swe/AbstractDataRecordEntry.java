
package net.seagis.swe;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @version $Id:
 * @author Guilhem Legal
 */
@XmlSeeAlso({SimpleDataRecordEntry.class})
@XmlType(name="AbstractDataRecord")
public class AbstractDataRecordEntry extends AbstractDataComponentEntry {
    
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
