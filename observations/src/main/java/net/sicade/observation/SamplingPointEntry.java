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
package net.sicade.observation;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;
import net.opengis.gml.PointType;
import org.geotools.resources.Utilities;
import org.opengis.observation.sampling.SamplingPoint;

/**
 * Description d'une station localisé.
 *
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SamplingPoint", namespace="http://www.opengis.net/sa/1.0",
propOrder = {"position"})

public class SamplingPointEntry extends SamplingFeatureEntry implements SamplingPoint{
    
    /**
     * La position de la station.
     */
    @XmlElement
    private PointType position;
    
    /**
     * Constructeur utilisé par JAXB.
     */
    public SamplingPointEntry(){};
            
    /** 
     * Créé une nouvelle station localisé.
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
      * Construit une entrée pour l'identifiant de station spécifié.
      * adapté au modele de BRGM.
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
     * Retourne la position de la station.
     */
    public PointType getPosition(){
        return position;
    }
    
    /**
     * Vérifie que cette station est identique à l'objet spécifié
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final SamplingPointEntry that = (SamplingPointEntry) object;
            return  Utilities.equals(this.position, that.position);
        }
        return false;
    }

  
}
