
package net.sicade.sos;

import net.opengeospatial.sos.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;
import net.sicade.gml.AbstractFeatureEntry;
import net.sicade.gml.BoundingShapeEntry;
import net.sicade.gml.ReferenceEntry;
import net.sicade.observation.PhenomenonEntry;
import net.sicade.observation.ProcessEntry;
import net.sicade.observation.SamplingFeatureEntry;
import net.sicade.observation.TemporalObjectEntry;


/**
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ObservationOfferingType", propOrder = {
    "srsName",
    "intendedApplication",
    "eventTime",
    "procedure",
    "observedProperty",
    "featureOfInterest",
    "responseFormat",
    "resultModel",
    "responseMode"
})
public class ObservationOfferingEntry extends AbstractFeatureEntry {

    @XmlElementRef(name = "intendedApplication", namespace = "http://www.opengeospatial.net/sos/0", type = JAXBElement.class)
    protected List<JAXBElement<String>> intendedApplication;
    protected String srsName;
    @XmlElement(required = true)
    protected TemporalObjectEntry eventTime;
    @XmlElement(required = true)
    protected List<ProcessEntry> procedure = new ArrayList<ProcessEntry>();
    @XmlElement(required = true)
    protected List<PhenomenonEntry> observedProperty = new ArrayList<PhenomenonEntry>();
    @XmlElement(required = true)
    protected List<SamplingFeatureEntry> featureOfInterest = new ArrayList<SamplingFeatureEntry>();
    @XmlElement(required = true)
    protected String responseFormat;
    protected String resultModel;
    protected ResponseMode responseMode;

    
    /**
     *  Un constructeur vide utilise par JAXB
     */ 
    public ObservationOfferingEntry(){}
    
    /**
     *  Construit un nouvel offering.
     */ 
    public ObservationOfferingEntry(String id, String name, String description, ReferenceEntry descriptionReference,
            BoundingShapeEntry boundedBy, String srsName, TemporalObjectEntry eventTime, List<ProcessEntry> procedure,
            List<PhenomenonEntry> observedProperty, List<SamplingFeatureEntry> featureOfInterest,
            String responseFormat, String resultModel, ResponseMode responseMode) {
        
        super(id, name, description, descriptionReference, boundedBy);
        this.srsName = srsName;
        this.eventTime = eventTime;
        this.procedure = procedure;
        this.observedProperty = observedProperty;
        this.featureOfInterest = featureOfInterest;
        this.responseFormat = responseFormat;
        this.resultModel = resultModel;
        this.responseMode = responseMode;
    }
    
    /**
     * Return the value of the intendedApplication property.
     * 
     */
    public List<JAXBElement<String>> getIntendedApplication() {
        if (intendedApplication == null) {
            intendedApplication = new ArrayList<JAXBElement<String>>();
        }
        return this.intendedApplication;
    }

    /**
     * Return the value of the eventTime property.
     * 
     */
    public TemporalObjectEntry getEventTime() {
        return eventTime;
    }

    /**
     * Sets the value of the eventTime property.
     */
    public void setEventTime(TemporalObjectEntry value) {
        this.eventTime = value;
    }

    /**
     *  Return an unmodifiable list of the procedures
     */
    public List<ProcessEntry> getProcedure() {
        
        return Collections.unmodifiableList(procedure);
    }
    
    
    /**
     * Return an unmodifiable list of the observedProperty.
     */
    public List<PhenomenonEntry> getObservedProperty() {
        return Collections.unmodifiableList(observedProperty);
    }

    /**
     * Return an unmodifiable list of the featureOfInterest.
     * 
     */
    public List<SamplingFeatureEntry> getFeatureOfInterest() {
        return Collections.unmodifiableList(featureOfInterest);
    }

   
    /**
     * Return the value of the resultFormat property.
     * 
     */
    public String getResponseFormat() {
       return this.responseFormat;
    }

    /**
     * Return the value of the resultModel property.
     * 
     */
    public String getResultModel() {
        return this.resultModel;
    }

    /**
     * Return the value of the responseMode property.
     * 
     */
    public ResponseMode getResponseMode() {
        return this.responseMode;
    }
    
    /**
     * Return the value of srsName.
     */
    public String getSrsName() {
        return this.srsName;
    }

}
