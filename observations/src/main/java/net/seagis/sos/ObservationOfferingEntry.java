
package net.seagis.sos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;
import net.seagis.gml32.AbstractFeatureEntry;
import net.seagis.gml32.BoundingShapeEntry;
import net.seagis.gml32.ReferenceEntry;
import net.seagis.observation.PhenomenonEntry;
import net.seagis.observation.ProcessEntry;
import net.seagis.observation.SamplingFeatureEntry;
import net.seagis.observation.TemporalObjectEntry;
import org.geotools.resources.Utilities;


/**
 * 
 * @author Guilhem Legal
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
    private List<JAXBElement<String>> intendedApplication;
    private String srsName;
    @XmlElement(required = true)
    private TemporalObjectEntry eventTime;
    @XmlElement(required = true)
    private List<ProcessEntry> procedure = new ArrayList<ProcessEntry>();
    @XmlElement(required = true)
    private List<PhenomenonEntry> observedProperty = new ArrayList<PhenomenonEntry>();
    @XmlElement(required = true)
    private List<SamplingFeatureEntry> featureOfInterest = new ArrayList<SamplingFeatureEntry>();
    @XmlElement(required = true)
    private String responseFormat;
    private String resultModel;
    private List<ResponseMode> responseMode;

    
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
            String responseFormat, String resultModel, List<ResponseMode> responseMode) {
        
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
    public List<ResponseMode> getResponseMode() {
        return this.responseMode;
    }
    
    /**
     * Return the value of srsName.
     */
    public String getSrsName() {
        return this.srsName;
    }
    
     /**
     * Verifie si cette entree est identique a l'objet specifie.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final ObservationOfferingEntry that = (ObservationOfferingEntry) object;
            boolean intendedApplicationEqual = true;
            if (this.intendedApplication != null && that.intendedApplication != null) {
                if (this.intendedApplication.size() == that.intendedApplication.size()) {
                    
                    for (int i = 0; i < this.intendedApplication.size(); i++) {
                        if (!Utilities.equals(this.intendedApplication.get(i).getValue(), that.intendedApplication.get(i).getValue())){
                            intendedApplicationEqual = false;
                        }
                    }
                
                } else {
                    intendedApplicationEqual = false;
                }
            }
            
            return Utilities.equals(this.eventTime,           that.eventTime)           &&
                   Utilities.equals(this.featureOfInterest,   that.featureOfInterest)   &&
                   intendedApplicationEqual                                             &&
                   Utilities.equals(this.observedProperty,    that.observedProperty)    &&
                   Utilities.equals(this.procedure,           that.procedure)           &&
                   Utilities.equals(this.responseFormat,      that.responseFormat)      &&
                   Utilities.equals(this.responseMode,        that.responseMode)        &&
                   Utilities.equals(this.resultModel,         that.resultModel)         &&
                   Utilities.equals(this.srsName,             that.srsName);
        } else System.out.println("SUPER NULLLLLLLLLLLLLLLLLLLLLLL");
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.intendedApplication != null ? this.intendedApplication.hashCode() : 0);
        hash = 67 * hash + (this.srsName != null ? this.srsName.hashCode() : 0);
        hash = 67 * hash + (this.eventTime != null ? this.eventTime.hashCode() : 0);
        hash = 67 * hash + (this.procedure != null ? this.procedure.hashCode() : 0);
        hash = 67 * hash + (this.observedProperty != null ? this.observedProperty.hashCode() : 0);
        hash = 67 * hash + (this.featureOfInterest != null ? this.featureOfInterest.hashCode() : 0);
        hash = 67 * hash + (this.responseFormat != null ? this.responseFormat.hashCode() : 0);
        hash = 67 * hash + (this.resultModel != null ? this.resultModel.hashCode() : 0);
        hash = 67 * hash + (this.responseMode != null ? this.responseMode.hashCode() : 0);
        return hash;
    }


}
