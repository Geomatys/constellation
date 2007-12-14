package net.opengis.sld;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import net.opengis.wms.OperationType;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the net.opengis.sld package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _DescribeLayer_QNAME = new QName("http://www.opengis.net/sld", "DescribeLayer");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: net.opengis.sld
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OperationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/sld", name = "DescribeLayer", substitutionHeadNamespace = "http://www.opengis.net/wms", substitutionHeadName = "_ExtendedOperation")
    public JAXBElement<OperationType> createDescribeLayer(OperationType value) {
        return new JAXBElement<OperationType>(_DescribeLayer_QNAME, OperationType.class, null, value);
    }



}
