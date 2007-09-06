/*
 * Sicade - Systemes integrés de connaissances pour l'aide à la décision en environnement
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

import net.sicade.catalog.Entry;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.observation.Measure;
import org.opengis.observation.sampling.SurveyProcedure;
import org.opengis.referencing.datum.Datum;
import org.opengis.temporal.TemporalObject;
import org.opengis.util.GenericName;

/**
 *Impl�mentation d'une entr�e repr�sentant une {@linkplain SurveyProcedure SurveyProcedure}.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class SurveyProcedureEntry extends Entry implements SurveyProcedure {
    
    /**
     */
    private String name;
    
    /**
     */
    private ResponsibleParty operator;
    
    /**
     */
    private Datum elevationDatum;
    
    private Process elevationMethod;
    
    private Measure elevationAccuracy;
    
    private Datum geodeticDatum;
    
    private Process positionMethod;
    
    private Measure positionAccuracy;
    
    private GenericName projection;
    
    private TemporalObject surveyTime;
    
    /** Creates a new instance of SurveyProcedureEntry */
    public SurveyProcedureEntry( final String name,
            final ResponsibleParty operator,
            final Datum elevationDatum,
            final Process elevationMethod,
            final Measure elevationAccuracy,
            final Datum geodeticDatum,
            final Process positionMethod,
            final Measure positionAccuracy,
            final GenericName projection,
            final TemporalObject surveyTime) 
    {
        super(name);
        this.operator = operator;
        this.elevationDatum = elevationDatum;
        this.elevationMethod = elevationMethod;
        this.elevationAccuracy = elevationAccuracy;
        this.geodeticDatum = geodeticDatum;
        this.positionMethod = positionMethod;
        this.positionAccuracy = positionAccuracy; 
        this.projection = projection;
        this.surveyTime = surveyTime;
                
    }
    
    public ResponsibleParty getOperator() {
        return operator;
    }
    
    public Datum getElevationDatum() {
        return elevationDatum;
    }
    
    public Process getElevationMethod() {
        return elevationMethod;
    }
    
    public Measure getElevationAccuracy() {
        return elevationAccuracy;
    }
    
    public Datum getGeodeticDatum() {
        return geodeticDatum;
    }
    
    public Process getPositionMethod() {
    return positionMethod;
    }
    
    public Measure getPositionAccuracy() {
        return positionAccuracy;
    }
    
    public GenericName getProjection() {
        return projection;
    }
    
    public TemporalObject getSurveyTime() {
        return surveyTime;
    }
    
     /**
     * Retourne le code numérique identifiant cette entrée.
     */
    @Override
    public int hashCode() {
        return name.hashCode();
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
            final SurveyProcedureEntry that = (SurveyProcedureEntry) object;
            return Utilities.equals(this.id,         that.id) &&
                   Utilities.equals(this.blockId,    that.blockId)   &&
                   Utilities.equals(this.definition, that.definition)   && 
                   Utilities.equals(this.fields,     that.fields) &&
                   Utilities.equals(this.fixed,      that.fixed);
        }
        return false;
    }
    
}
