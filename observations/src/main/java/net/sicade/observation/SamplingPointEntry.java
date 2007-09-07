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
import net.sicade.swe.Point;
import org.geotools.resources.Utilities;
import org.opengis.observation.Observation;
import org.opengis.observation.sampling.SamplingFeatureRelation;
import org.opengis.observation.sampling.SamplingPoint;
import org.opengis.observation.sampling.SurveyProcedure;

/**
 * Description d'une station localisé.
 *
 * @author Guilhem Legal
 */
public class SamplingPointEntry extends SamplingFeatureEntry implements SamplingPoint{
   
    /**
     * La position de la station.
     */
    private Point position;
    
    /** 
     * Créé une nouvelle station localisé.
     */
    public SamplingPointEntry(final String            identifier,
                              final String            name,
                              final String            remarks,
                              final List<SamplingFeatureRelation> relatedSamplingFeature,
                              final List<Observation> relatedObservation,
                              final List<Object>      sampledFeature,
                              final SurveyProcedure   surveyDetail,
                              final Point location) 
    {
        super(identifier, name, remarks, relatedSamplingFeature, relatedObservation, sampledFeature, surveyDetail);
        this.position = position;
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
                              final Point location) 
    {
        super(identifier, name, remarks, sampledFeature);
        this.position = position;
    }
    
    /**
     * Retourne la position de la station.
     */
    public Point getPosition(){
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
