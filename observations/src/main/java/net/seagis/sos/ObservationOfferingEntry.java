/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */


package net.seagis.sos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;
import net.seagis.gml.AbstractFeatureEntry;
import net.seagis.gml.AbstractTimeGeometricPrimitiveType;
import net.seagis.gml.BoundingShapeEntry;
import net.seagis.gml.ReferenceEntry;
import net.seagis.swe.PhenomenonEntry;
import net.seagis.swe.PhenomenonPropertyType;
import net.seagis.swe.TimeGeometricPrimitivePropertyType;
import org.geotools.resources.Utilities;


/**
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ObservationOfferingType", propOrder = {
    "intendedApplication",
    "time",
    "procedure",
    "observedProperty",
    "featureOfInterest",
    "responseFormat",
    "resultModel",
    "responseMode"
})
public class ObservationOfferingEntry extends AbstractFeatureEntry {

    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    private List<String> intendedApplication;
    private TimeGeometricPrimitivePropertyType time;
    @XmlElement(required = true)
    private List<ReferenceEntry> procedure;
    @XmlElement(required = true)
    private List<PhenomenonPropertyType> observedProperty;
    @XmlElement(required = true)
    private List<ReferenceEntry> featureOfInterest;
    @XmlElement(required = true)
    private List<String> responseFormat;
    private List<QName> resultModel;
    private List<ResponseModeType> responseMode;

    /**
     *  An empty constructor used by jaxB
     */ 
    ObservationOfferingEntry(){}
    
    /**
     *  Build a new offering.
     */ 
    public ObservationOfferingEntry(String id, String name, String description, ReferenceEntry descriptionReference,
            BoundingShapeEntry boundedBy, AbstractTimeGeometricPrimitiveType time, List<ReferenceEntry> procedure,
            List<PhenomenonEntry> observedProperty, List<ReferenceEntry> featureOfInterest,
            List<String> responseFormat, List<QName> resultModel, List<ResponseModeType> responseMode) {
        
        super(id, name, description, descriptionReference, boundedBy);
        this.procedure         = procedure;
        this.observedProperty  = new ArrayList<PhenomenonPropertyType>();
        for (PhenomenonEntry p: observedProperty){
            this.observedProperty.add(new PhenomenonPropertyType(p));
        }
        this.featureOfInterest = featureOfInterest;
        this.responseFormat    = responseFormat;
        this.resultModel       = resultModel;
        this.responseMode      = responseMode;
        this.time              = new TimeGeometricPrimitivePropertyType(time);
    }
    
    /**
     * Return the value of the intendedApplication property.
     * 
     */
    public List<String> getIntendedApplication() {
        if (intendedApplication == null) {
            intendedApplication = new ArrayList<String>();
        }
        return Collections.unmodifiableList(intendedApplication);
    }

    /**
     * Return the value of the eventTime property.
     * 
     */
    public AbstractTimeGeometricPrimitiveType getTime() {
        if (time != null && time.getTimeGeometricPrimitive() != null)
            return time.getTimeGeometricPrimitive().getValue();
        else 
            return null;
    }

    /**
     * Sets the value of the eventTime property.
     */
    public void setTime(AbstractTimeGeometricPrimitiveType value) {
        if (time != null)
            this.time.setTimeGeometricPrimitive(value);
        else
            this.time = new TimeGeometricPrimitivePropertyType(value);
    }

    /**
     *  Return an unmodifiable list of the procedures
     */
    public List<ReferenceEntry> getProcedure() {
        if (procedure == null) {
            procedure = new ArrayList<ReferenceEntry>();
        }
        return Collections.unmodifiableList(procedure);
    }
    
    
    /**
     * Return an unmodifiable list of the observedProperty.
     */
    public List<PhenomenonEntry> getObservedProperty() {
        if (observedProperty == null){
           return Collections.unmodifiableList(new ArrayList<PhenomenonEntry>());

        } else {
            List<PhenomenonEntry> result = new ArrayList<PhenomenonEntry>();
            for (PhenomenonPropertyType pp:observedProperty){
                result.add(pp.getPhenomenon());
            }
            return Collections.unmodifiableList(result);
        }
    }

    /**
     * Return an unmodifiable list of the featureOfInterest.
     * 
     */
    public List<ReferenceEntry> getFeatureOfInterest() {
        if (featureOfInterest == null){
            featureOfInterest = new ArrayList<ReferenceEntry>();
        }
        return Collections.unmodifiableList(featureOfInterest);
    }

   
    /**
     * Return the value of the resultFormat property.
     * 
     */
    public List<String> getResponseFormat() {
        if (responseFormat == null){
            responseFormat = new ArrayList<String>();
        }
        return Collections.unmodifiableList(responseFormat);
    }

    /**
     * Return the value of the resultModel property.
     * 
     */
    public List<QName> getResultModel() {
        if (resultModel == null){
            resultModel = new ArrayList<QName>();
        }
        return Collections.unmodifiableList(resultModel);
    }

    /**
     * Return the value of the responseMode property.
     * 
     */
    public List<ResponseModeType> getResponseMode() {
       if (responseMode == null){
            responseMode = new ArrayList<ResponseModeType>();
        }
       return Collections.unmodifiableList(responseMode);
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
            
            return Utilities.equals(this.time,                that.time)                &&
                   Utilities.equals(this.featureOfInterest,   that.featureOfInterest)   &&
                   Utilities.equals(this.intendedApplication, that.intendedApplication) && 
                   Utilities.equals(this.observedProperty,    that.observedProperty)    &&
                   Utilities.equals(this.procedure,           that.procedure)           &&
                   Utilities.equals(this.responseFormat,      that.responseFormat)      &&
                   Utilities.equals(this.responseMode,        that.responseMode)        &&
                   Utilities.equals(this.resultModel,         that.resultModel);
        } else System.out.println("obs offering SUPER NOT EQUALSSSSS");
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.intendedApplication != null ? this.intendedApplication.hashCode() : 0);
        hash = 67 * hash + (this.time != null ? this.time.hashCode() : 0);
        hash = 67 * hash + (this.procedure != null ? this.procedure.hashCode() : 0);
        hash = 67 * hash + (this.observedProperty != null ? this.observedProperty.hashCode() : 0);
        hash = 67 * hash + (this.featureOfInterest != null ? this.featureOfInterest.hashCode() : 0);
        hash = 67 * hash + (this.responseFormat != null ? this.responseFormat.hashCode() : 0);
        hash = 67 * hash + (this.resultModel != null ? this.resultModel.hashCode() : 0);
        hash = 67 * hash + (this.responseMode != null ? this.responseMode.hashCode() : 0);
        return hash;
    }
    
    public String toString() {
        StringBuilder s = new StringBuilder("offering: " + this.getName()).append('\n');
        s.append("time=" + time ).append('\n');
        if (intendedApplication != null){
            s.append('\n').append("intendedApplication:").append('\n');
            for (String ss:intendedApplication){
                s.append(ss);
            }
        }
        if (responseFormat != null){
            s.append('\n').append("responseFormat:").append('\n');
            for (String ss:responseFormat){
                s.append(ss).append('\n');
            }
        }
        if (responseMode != null){
            s.append('\n').append("response mode:").append('\n');
            for (ResponseModeType ss:responseMode){
                s.append(ss.value()).append('\n');;
            }
        }
         if (resultModel != null){
            s.append('\n').append("result model:").append('\n');
            for (QName ss:resultModel){
                s.append(ss.toString()).append('\n');;
            }
        }
        if (featureOfInterest != null){
           s.append('\n').append("feature of interest:").append('\n');
           for (ReferenceEntry ref:featureOfInterest){
                s.append(ref.toString());
            } 
        }
        if (procedure != null){
           s.append('\n').append("procedure:").append('\n');
           for (ReferenceEntry ref:procedure){
                s.append(ref.toString());
            } 
        }
        if (observedProperty != null){
           s.append('\n').append("observedProperty:").append('\n');
           for (PhenomenonPropertyType phen:observedProperty){
                s.append(phen.getPhenomenon().toString());
            } 
        }
        return s.toString();
    }


}
