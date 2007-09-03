/* Created on 3 septembre 2007, 12:33 */

package net.sicade.observation;

import net.sicade.catalog.Entry;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.observation.Measure;
import org.opengis.observation.sampling.SurveyProcedure;
import org.opengis.referencing.datum.Datum;
import org.opengis.temporal.TemporalObject;
import org.opengis.util.GenericName;

/**
 *SurveyProcedureEntry.java
 *
 * @author Guilhem Legal
 */
public class SurveyProcedureEntry extends Entry implements SurveyProcedure {
    
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
    
}
