
package net.sicade.sos;

import net.opengeospatial.sos.*;
import java.util.ArrayList;
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
    @XmlElement(required = true)
    protected TemporalObjectEntry eventTime;
    @XmlElement(required = true)
    protected List<ProcessEntry> procedure;
    @XmlElement(required = true)
    protected List<PhenomenonEntry> observedProperty;
    @XmlElement(required = true)
    protected List<? extends SamplingFeatureEntry> featureOfInterest;
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
            BoundingShapeEntry boundedBy, TemporalObjectEntry eventTime, List<ProcessEntry> procedure,
            List<PhenomenonEntry> observedProperty, List<? extends SamplingFeatureEntry> featureOfInterest,
            String responseFormat, String resultModel, ResponseMode responseMode) {
        
        super(id, name, description, descriptionReference, boundedBy);
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
     * Return the value of the procedure property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the procedure property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProcedure().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ProcessPropertyType }
     * 
     * 
     */
    public List<ProcessEntry> getProcedure() {
        if (procedure == null) {
            procedure = new ArrayList<ProcessEntry>();
        }
        return this.procedure;
    }

    /**
     * Return the value of the observedProperty property.
     */
    public List<PhenomenonEntry> getObservedProperty() {
        if (observedProperty == null) {
            observedProperty = new ArrayList<PhenomenonEntry>();
        }
        return this.observedProperty;
    }

    /**
     * Return the value of the featureOfInterest property.
     * 
     */
    public List<? extends SamplingFeatureEntry> getFeatureOfInterest() {
        return featureOfInterest;
    }

    /**
     * Sets the value of the featureOfInterest property.
     * 
     */
    public void setFeatureOfInterest(List<? extends SamplingFeatureEntry> value) {
        this.featureOfInterest = value;
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

}
