
package net.seagis.swe;

import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 *
 * @version $Id:
 * @author Guilhem Legal
 */
@XmlRegistry
public class ObjectFactory {
    
    private final static QName _AbstractDataRecord_QNAME = new QName("http://www.opengis.net/swe/1.0", "AbstractDataRecord");
    private final static QName _SimpleDataRecord_QNAME = new QName("http://www.opengis.net/swe/1.0", "SimpleDataRecord");
    
    
    /**
     *
     */
    public ObjectFactory() {
    }
    
    /**
     * Create an instance of {@link Boolean }
     * 
     */
    public BooleanType createBooleanType() {
        return new BooleanType();
    }

    
    /**
     * Create an instance of {@link Time }
     * 
     */
    public TimeType createTimeType() {
        return new TimeType();
    }

    /**
     * Create an instance of {@link Quantity }
     * 
     */
    public QuantityType createQuantityType() {
        return new QuantityType();
    }
    
    /**
     * Create an instance of {@link UomPropertyType }
     * 
     */
    public UomPropertyType createUomPropertyType() {
        return new UomPropertyType();
    }

    /**
     * Create an instance of {@link AbstractDataComponentEntry }
     * 
     */
    public AbstractDataComponentEntry createAbstractDataComponentEntry() {
        return new AbstractDataComponentEntry();
    }
    
    /**
     * Create an instance of {@link AbstractDataComponentEntry }
     * 
     */
    public AbstractDataRecordEntry createAbstractDataRecordEntry() {
        return new AbstractDataRecordEntry();
    }
    
    /**
     * Create an instance of {@link AbstractEncodingEntry }
     * 
     */
    public AbstractEncodingEntry createAbstractEncodingEntry() {
        return new AbstractEncodingEntry();
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
     * Create an instance of {@link SimpleDataRecordEntry }
     * 
     */
    public SimpleDataRecordEntry createSimpleDataRecordEntry() {
        return new SimpleDataRecordEntry();
    }
    
     /**
     * Create an instance of {@link AnyScalarEntry }
     * 
     */
    public AnyScalarEntry createAnyScalarEntry() {
        return new AnyScalarEntry();
    }
    
    
     /**
      * 
      *  A REMETTRE lors de la suppression de dependance vers Sosall
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleDataRecordType }{@code >}}
     * 
     
    @XmlElementDecl(namespace = "http://www.opengis.net/swe/1.0", name = "SimpleDataRecord", substitutionHeadNamespace = "http://www.opengis.net/swe/1.0", substitutionHeadName = "AbstractDataRecord")
    public JAXBElement<SimpleDataRecordEntry> createSimpleDataRecord(SimpleDataRecordEntry value) {
        return new JAXBElement<SimpleDataRecordEntry>(_SimpleDataRecord_QNAME, SimpleDataRecordEntry.class, null, value);
    }
    
     /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractDataRecordType }{@code >}}
     * 
     
    @XmlElementDecl(namespace = "http://www.opengis.net/swe/1.0", name = "AbstractDataRecord")
    public JAXBElement<AbstractDataRecordEntry> createAbstractDataRecord(AbstractDataRecordEntry value) {
        return new JAXBElement<AbstractDataRecordEntry>(_AbstractDataRecord_QNAME, AbstractDataRecordEntry.class, null, value);
    }*/

}
