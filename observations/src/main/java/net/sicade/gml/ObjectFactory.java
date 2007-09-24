
package net.sicade.gml;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import net.sicade.gml.ReferenceEntry;

/**
 *
 * @version $Id:
 * @author Guilhem Legal
 */
@XmlRegistry
public class ObjectFactory {
    
    /**
     *
     */
    public ObjectFactory() {
    }
    
    /**
     * Create an instance of {@link ObservationEntry }
     * 
     */
    public ReferenceEntry createReferenceEntry() {
        return new ReferenceEntry();
    }
    
}
