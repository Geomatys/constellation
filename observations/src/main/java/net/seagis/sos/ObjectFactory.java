package net.seagis.sos;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

/**
 *
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the net.opengeospatial.sos package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 *
 * @author legal
 */
public class ObjectFactory {
    
    private final static QName _ObservationOfferingTypeIntendedApplication_QNAME = new QName("http://www.opengeospatial.net/sos/0", "intendedApplication");

    /**
     * Create an instance of {@link ObservationOfferingType }
     * 
     */
    public ObservationOfferingEntry createObservationOfferingType() {
        return new ObservationOfferingEntry();
    }
    
     /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengeospatial.net/sos/0", name = "intendedApplication", scope = ObservationOfferingEntry.class)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    public JAXBElement<String> createObservationOfferingTypeIntendedApplication(String value) {
        return new JAXBElement<String>(_ObservationOfferingTypeIntendedApplication_QNAME, String.class, ObservationOfferingEntry.class, value);
    }

}
