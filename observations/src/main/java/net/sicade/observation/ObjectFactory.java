
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
     
    /**
     *
     */
    public ObjectFactory() {
    }
    
     /**
     * Create an instance of {@link ObservationEntry }
     * 
     */
    public ObservationEntry createObservationEntry() {
        return new ObservationEntry();
    }
    
    /**
     * Create an instance of {@link ObservationCollectionEntry }
     * 
     */
    public ObservationCollectionEntry createObservationCollectionEntry() {
        return new ObservationCollectionEntry();
    }
    
      /**
     * Create an instance of {@link MeasurementEntry }
     * 
     */
    public MeasurementEntry createMeasurementEntry() {
        return new MeasurementEntry();
    }
    
    /**
     * Create an instance of {@link SamplingPointEntry }
     * 
     */
    public SamplingPointEntry createSamplingPointEntry() {
        return new SamplingPointEntry();
    }
    
    /**
     * Create an instance of {@link CompositePhenomenonEntry }
     * 
     */
    public CompositePhenomenonEntry createCompositePhenomenonEntry() {
        return new CompositePhenomenonEntry();
    }
    
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ObservationEntry }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/om/1.0", name = "Observation", substitutionHeadNamespace = "http://www.opengis.net/gml/3.2", substitutionHeadName = "AbstractFeature")
    public JAXBElement<ObservationEntry> createObservation(ObservationEntry value) {
        return new JAXBElement<ObservationEntry>(_Observation_QNAME, ObservationEntry.class, null, value);
    }

}
