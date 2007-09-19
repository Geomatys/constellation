
package net.sicade.observation;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 *
 * @version $Id:
 * @author Guilhem Legal
 */
@XmlRegistry
public class ObjectFactory {
    
    private final static QName _Observation_QNAME = new QName("http://www.opengis.net/om/1.0", "Observation");
    private final static QName _ProcessPropertyTypeProcess_QNAME = new QName("http://www.opengis.net/om/1.0", "Process");
    /**
     *
     */
    public ObjectFactory() {
    }
    
     /**
     * Create an instance of {@link ObservationType }
     * 
     */
    public ObservationEntry createObservationType() {
        return new ObservationEntry();
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ObservationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/om/1.0", name = "Observation", substitutionHeadNamespace = "http://www.opengis.net/gml/3.2", substitutionHeadName = "AbstractFeature")
    public JAXBElement<ObservationEntry> createObservation(ObservationEntry value) {
        return new JAXBElement<ObservationEntry>(_Observation_QNAME, ObservationEntry.class, null, value);
    }
    
}
