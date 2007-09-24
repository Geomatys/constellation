
package net.sicade.swe;

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
     * Create an instance of {@link AnyResultEntry }
     * 
     */
    public AnyResultEntry createAnyResultEntry() {
        return new AnyResultEntry();
    }
    
    /**
     * Create an instance of {@link DataBlockDefinitionEntry }
     * 
     */
    public DataBlockDefinitionEntry createDataBlockDefinitionEntry() {
        return new DataBlockDefinitionEntry();
    }
    
    /**
     * Create an instance of {@link TextBlockEntry }
     * 
     */
    public TextBlockEntry createTextBlockEntry() {
        return new TextBlockEntry();
    }
    
     /**
     * Create an instance of {@link TextBlockEntry }
     * 
     */
    public SimpleDataRecordEntry createSimpleDataRecordEntry() {
        return new SimpleDataRecordEntry();
    }
    
    
    
    
  

}
