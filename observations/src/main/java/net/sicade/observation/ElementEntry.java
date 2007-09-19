
package net.sicade.observation;

import java.util.Collection;
import java.util.Date;
import net.sicade.catalog.Entry;
import org.opengis.annotation.UML;
import org.opengis.metadata.Identifier;
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.quality.Element;
import org.opengis.metadata.quality.EvaluationMethodType;
import org.opengis.metadata.quality.Result;
import org.opengis.util.InternationalString;

/**
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class ElementEntry extends Entry implements Element {
    
    /**
     * Name of the test applied to the data.
     */
    private Collection<InternationalString> namesOfMeasure;

    /**
     * Code identifying a registered standard procedure, or {@code null} if none.
     */
    private Identifier measureIdentification;

    /**
     * Description of the measure being determined.
     */
    private InternationalString measureDescription;

    /**
     * Type of method used to evaluate quality of the dataset, or {@code null} if unspecified.
     */
    private EvaluationMethodType evaluationMethodType;

    /**
     * Description of the evaluation method.
     */
    private InternationalString evaluationMethodDescription;

    /**
     * Reference to the procedure information, or {@code null} if none.
     */
    private Citation evaluationProcedure;

    /**
     * Date that the metadata was created.
     * The array length is 1 for a single date, or 2 for a range.
     * Returns {@code null} if this information is not available.
     *
     * @deprecated Replaced by {@link #getDates}.
     */
    private Date[] date;
    
    /**
     * Date or range of dates on which a data quality measure was applied.
     * The collection size is 1 for a single date, or 2 for a range. Returns
     * an empty collection if this information is not available.
     *
     * @since GeoAPI 2.1
     */
    private Collection<Date> dates;

    /**
     * Value (or set of values) obtained from applying a data quality measure or the out
     * come of evaluating the obtained value (or set of values) against a specified
     * acceptable conformance quality level.
     *
     * @deprecated Replaced by {@link #getResults}.
     */
    private Result result;

    /**
     * Value (or set of values) obtained from applying a data quality measure or the out
     * come of evaluating the obtained value (or set of values) against a specified
     * acceptable conformance quality level.
     *
     * @since GeoAPI 2.1
     */
    private Collection<? extends Result> results;
    /**
     *
     */
    public ElementEntry() {
    }
    
    /**
     * Name of the test applied to the data.
     */
    public Collection<InternationalString> getNamesOfMeasure(){ 
        return namesOfMeasure;
    }

    /**
     * Code identifying a registered standard procedure, or {@code null} if none.
     */
    public Identifier getMeasureIdentification(){ 
        return measureIdentification;
    }

    /**
     * Description of the measure being determined.
     */
    public InternationalString getMeasureDescription(){ 
        return measureDescription;
    }

    /**
     * Type of method used to evaluate quality of the dataset, or {@code null} if unspecified.
     */
    public EvaluationMethodType getEvaluationMethodType(){ 
        return evaluationMethodType;
    }

    /**
     * Description of the evaluation method.
     */
    public InternationalString getEvaluationMethodDescription(){ 
        return evaluationMethodDescription;
    }

    /**
     * Reference to the procedure information, or {@code null} if none.
     */
    public Citation getEvaluationProcedure(){ 
        return evaluationProcedure;
    }

    /**
     * Date that the metadata was created.
     * The array length is 1 for a single date, or 2 for a range.
     * Returns {@code null} if this information is not available.
     *
     * @deprecated Replaced by {@link #getDates}.
     */
    public Date[] getDate(){ 
        return date;
    }
    
    /**
     * Date or range of dates on which a data quality measure was applied.
     * The collection size is 1 for a single date, or 2 for a range. Returns
     * an empty collection if this information is not available.
     *
     * @since GeoAPI 2.1
     */
    public Collection<Date> getDates(){ 
        return dates;
    }

    /**
     * Value (or set of values) obtained from applying a data quality measure or the out
     * come of evaluating the obtained value (or set of values) against a specified
     * acceptable conformance quality level.
     *
     * @deprecated Replaced by {@link #getResults}.
     */
    public Result getResult(){ 
        return result;
    }

    /**
     * Value (or set of values) obtained from applying a data quality measure or the out
     * come of evaluating the obtained value (or set of values) against a specified
     * acceptable conformance quality level.
     *
     * @since GeoAPI 2.1
     */
    public Collection<? extends Result> getResults(){ 
        return results;
    }
}
