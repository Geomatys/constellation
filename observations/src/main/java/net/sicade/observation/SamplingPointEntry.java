/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le D�veloppement
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
import org.opengis.observation.Observation;
import org.opengis.observation.sampling.SamplingFeatureRelation;
import org.opengis.observation.sampling.SamplingPoint;
import org.opengis.observation.sampling.SurveyProcedure;

/**
 * Description d'une station
 *
 * @author Guilhem Legal
 */
public class SamplingPointEntry extends SamplingFeatureEntry implements SamplingPoint{
   
    private Point position;
    
    /** Creates a new instance of SamplingPointEntry */
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
      * Construit une entr�e pour l'identifiant de station sp�cifi�.
      * adapt� au modele de BRGM.
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
    
    public Point getPosition(){
        return position;
    }
}
