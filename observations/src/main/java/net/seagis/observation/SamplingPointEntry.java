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
package net.seagis.observation;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import net.seagis.gml.PointType;
import org.geotools.resources.Utilities;
import org.opengis.observation.sampling.SamplingPoint;

/**
 * Description of a station localised.
 *
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SamplingPoint", namespace="http://www.opengis.net/sa/1.0",
propOrder = {"position"})

public class SamplingPointEntry extends SamplingFeatureEntry implements SamplingPoint{
    
    /**
     * the station position.
     */
    @XmlElement(required = true)
    private PointType position;
    
    /**
     * Constructor used by JAXB.
     */
    public SamplingPointEntry(){};
            
    /** 
     * Build a new station localised.
     */
    public SamplingPointEntry(final String            identifier,
                              final String            name,
                              final String            remarks,
                              final List<SamplingFeatureRelationEntry > relatedSamplingFeature,
                              final List<ObservationEntry > relatedObservation,
                              final List<Object>      sampledFeature,
                              final SurveyProcedureEntry   surveyDetail,
                              final PointType location) 
    {
        super(identifier, name, remarks, relatedSamplingFeature, relatedObservation, sampledFeature, surveyDetail);
        this.position = location;
    }
    
     /** 
      * Build an entry to the identifier of the spécified station .
      * adapted for the BRGM model.
      * 
      */
    public SamplingPointEntry(final String            identifier,
                              final String            name,
                              final String            remarks,
                              final String            sampledFeature,
                              final PointType         location) 
    {
        super(identifier, name, remarks, sampledFeature);
        this.position = location;
    }
    
    /**
     * Return the station position.
     */
    public PointType getPosition(){
        return position;
    }
    
    /**
     * Verify that this entry is identical to the specified object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof SamplingFeatureEntry && super.equals(object)) {
            final SamplingPointEntry that = (SamplingPointEntry) object;
        
            return  Utilities.equals(this.position, that.position);
        } else {
            System.out.println("samplingFeature.equals=false");
        }
        return false;
    }

    @Override
    public int hashCode() {
       
        return getId().hashCode();
    }
    
    /**
     * Return a String representing the station.
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(super.toString());
        s.append('\n').append("Position: ").append(position) ;
        return s.toString();
    }

  
}
