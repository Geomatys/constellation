/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 * (C) 2007, Geomatys
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

import java.util.Collection;
import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import net.sicade.catalog.Entry;
import net.sicade.metadata.CitationEntry;
import net.sicade.metadata.IdentifierEntry;
import net.sicade.metadata.InternationalStringEntry;
import net.sicade.metadata.ResultEntry;
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
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Element")
public class ElementEntry extends Entry implements Element {
    
    /**
     * Name of the test applied to the data.
     */
    private Collection<InternationalStringEntry> namesOfMeasure;

    /**
     * Code identifying a registered standard procedure, or {@code null} if none.
     */
    private IdentifierEntry measureIdentification;

    /**
     * Description of the measure being determined.
     */
    private InternationalStringEntry measureDescription;

    /**
     * Type of method used to evaluate quality of the dataset, or {@code null} if unspecified.
     
    JAXB issue private EvaluationMethodType evaluationMethodType;
     */
    
    /**
     * Description of the evaluation method.
     */
    private InternationalStringEntry evaluationMethodDescription;

    /**
     * Reference to the procedure information, or {@code null} if none.
     */
    private CitationEntry evaluationProcedure;

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
    private ResultEntry result;

    /**
     * Value (or set of values) obtained from applying a data quality measure or the out
     * come of evaluating the obtained value (or set of values) against a specified
     * acceptable conformance quality level.
     *
     * @since GeoAPI 2.1
     */
    private Collection<? extends ResultEntry> results;
    /**
     *
     */
    public ElementEntry() {
    }
    
    /**
     * Name of the test applied to the data.
     */
    @Override
    public Collection<InternationalString> getNamesOfMeasure(){ 
        throw new UnsupportedOperationException("Not supported yet.");
        //return  namesOfMeasure;
    }

    /**
     * Code identifying a registered standard procedure, or {@code null} if none.
     */
    @Override
    public Identifier getMeasureIdentification(){ 
        return measureIdentification;
    }

    /**
     * Description of the measure being determined.
     */
    @Override
    public InternationalString getMeasureDescription(){ 
        return measureDescription;
    }

    /**
     * Type of method used to evaluate quality of the dataset, or {@code null} if unspecified.
     */
    @Override
    public EvaluationMethodType getEvaluationMethodType(){ 
        throw new UnsupportedOperationException("Not supported yet.");
       //return evaluationMethodType;
    }

    /**
     * Description of the evaluation method.
     */
    @Override
    public InternationalString getEvaluationMethodDescription(){ 
        return evaluationMethodDescription;
    }

    /**
     * Reference to the procedure information, or {@code null} if none.
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
    public Collection<? extends Result> getResults(){ 
        return results;
    }
}
