/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2009, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package org.constellation.sos.io.lucene;

import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;

import org.constellation.generic.database.Automatic;
import org.constellation.sos.io.ObservationFilter;
import org.constellation.sos.io.ObservationResult;
import org.constellation.ws.CstlServiceException;
import static org.constellation.sos.ws.SOSConstants.*;
import static org.constellation.sos.ws.Utils.*;

import org.geotoolkit.gml.xml.v311.EnvelopeEntry;
import org.geotoolkit.gml.xml.v311.ReferenceEntry;
import org.geotoolkit.gml.xml.v311.TimeInstantType;
import org.geotoolkit.gml.xml.v311.TimePeriodType;
import org.geotoolkit.lucene.IndexingException;
import org.geotoolkit.lucene.SearchingException;
import org.geotoolkit.lucene.filter.SpatialQuery;
import org.geotoolkit.observation.xml.v100.ProcessEntry;
import org.geotoolkit.sos.xml.v100.ObservationOfferingEntry;
import org.geotoolkit.sos.xml.v100.ResponseModeType;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

import org.opengis.observation.Observation;
/**
 * TODO
 * 
 * @author Guilhem Legal (Geomatys)
 */
public class LuceneObservationFilter implements ObservationFilter {

    private Properties map;

    private StringBuilder luceneRequest;

    private LuceneObservationSearcher searcher;

    private static final String OR_OPERATOR = " OR ";

    public LuceneObservationFilter(LuceneObservationFilter omFilter) throws CstlServiceException {
        this.map      = omFilter.map;
        this.searcher = omFilter.searcher;
    }

    public LuceneObservationFilter(String observationIdBase, String observationTemplateIdBase, Properties map, Automatic configuration) throws CstlServiceException {
        this.map                       = map;
        try {
            this.searcher = new LuceneObservationSearcher(configuration.getConfigurationDirectory(), "");
        } catch (IndexingException ex) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "IndexingException in LuceneObservationFilter constructor", ex);
            throw new CstlServiceException("IndexingException in LuceneObservationFilter constructor", ex, NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initFilterObservation(ResponseModeType requestMode, QName resultModel) {
        if (resultModel.equals(MEASUREMENT_QNAME)) {
            luceneRequest = new StringBuilder("type:measurement ");
        } else {
            luceneRequest = new StringBuilder("type:observation ");
        }

        if (ResponseModeType.RESULT_TEMPLATE.equals(requestMode)) {
            luceneRequest.append("template:TRUE ");
        } else {
            luceneRequest.append("template:FALSE ");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initFilterGetResult(Observation template, QName resultModel) {
        final ProcessEntry process = (ProcessEntry) template.getProcedure();
        if (resultModel.equals(MEASUREMENT_QNAME)) {
            luceneRequest = new StringBuilder("type:measurement AND procedure:\"" + process.getHref() + "\" ");
        } else {
            luceneRequest = new StringBuilder("type:observation AND procedure:\"" + process.getHref() + "\" ");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProcedure(List<String> procedures, ObservationOfferingEntry off) {
        luceneRequest.append(" ( ");
        if (procedures.size() != 0) {

            for (String s : procedures) {
                if (s != null) {
                    String dbId = map.getProperty(s);
                    if (dbId == null) {
                        dbId = s;
                    }
                    luceneRequest.append(" procedure:\"").append(dbId).append("\" OR ");
                }
            }
        } else {
            //if is not specified we use all the process of the offering
            for (ReferenceEntry proc : off.getProcedure()) {
                luceneRequest.append(" procedure:\"").append(proc.getHref()).append("\" OR ");
            }
        }
        luceneRequest.delete(luceneRequest.length() - 3, luceneRequest.length());
        luceneRequest.append(") ");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setObservedProperties(List<String> phenomenon, List<String> compositePhenomenon) {
        luceneRequest.append(" AND( ");
        for (String p : phenomenon) {
            luceneRequest.append(" observed_property:\"").append(p).append('"').append(OR_OPERATOR);

        }
        for (String p : compositePhenomenon) {
            luceneRequest.append(" observed_property:\"").append(p).append('"').append(OR_OPERATOR);
        }
        luceneRequest.delete(luceneRequest.length() - 3, luceneRequest.length());
        luceneRequest.append(") ");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFeatureOfInterest(List<String> fois) {
        luceneRequest.append(" AND (");
        for (String foi : fois) {
            luceneRequest.append("feature_of_interest:").append(foi).append(" OR ");
        }
        luceneRequest.delete(luceneRequest.length() - 3, luceneRequest.length());
        luceneRequest.append(") ");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeEquals(Object time) throws CstlServiceException {
        if (time instanceof TimePeriodType) {
            final TimePeriodType tp = (TimePeriodType) time;
            final String begin      = getLuceneTimeValue(tp.getBeginPosition());
            final String end        = getLuceneTimeValue(tp.getEndPosition());

            // we request directly a multiple observation or a period observation (one measure during a period)
            luceneRequest.append("AND (");
            luceneRequest.append(" sampling_time_begin:").append(begin).append(" AND ");
            luceneRequest.append(" sampling_time_end:").append(end).append(") ");

        // if the temporal object is a timeInstant
        } else if (time instanceof TimeInstantType) {
            final TimeInstantType ti = (TimeInstantType) time;
            final String position    = getLuceneTimeValue(ti.getTimePosition());
            luceneRequest.append("AND (");

            // case 1 a single observation
            luceneRequest.append("(sampling_time_begin:'").append(position).append("' AND sampling_time_end:NULL)");
            luceneRequest.append(OR_OPERATOR);

            //case 2 multiple observations containing a matching value
            luceneRequest.append("(sampling_time_begin: [19700000 ").append(position).append("] ").append(" AND sampling_time_end: [").append(position).append(" 30000000]))");

        } else {
            throw new CstlServiceException("TM_Equals operation require timeInstant or TimePeriod!",
                    INVALID_PARAMETER_VALUE, EVENT_TIME);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeBefore(Object time) throws CstlServiceException {
        // for the operation before the temporal object must be an timeInstant
        if (time instanceof TimeInstantType) {
            final TimeInstantType ti = (TimeInstantType) time;
            final String position    = getLuceneTimeValue(ti.getTimePosition());
            luceneRequest.append("AND (");

            // the single and multpile observations whitch begin after the bound
            luceneRequest.append("(sampling_time_begin: [19700000000000 ").append(position).append("]))");

        } else {
            throw new CstlServiceException("TM_Before operation require timeInstant!",
                    INVALID_PARAMETER_VALUE, EVENT_TIME);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeAfter(Object time) throws CstlServiceException {
        // for the operation after the temporal object must be an timeInstant
        if (time instanceof TimeInstantType) {
            final TimeInstantType ti = (TimeInstantType) time;
            final String position    = getLuceneTimeValue(ti.getTimePosition());
            luceneRequest.append("AND (");

            // the single and multpile observations whitch begin after the bound
            luceneRequest.append("(sampling_time_begin:[").append(position).append(" 30000000])");
            luceneRequest.append(OR_OPERATOR);
            // the multiple observations overlapping the bound
            luceneRequest.append("(sampling_time_begin: [19700000 ").append(position).append("] AND sampling_time_end:[").append(position).append(" 30000000]))");


        } else {
            throw new CstlServiceException("TM_After operation require timeInstant!",
                    INVALID_PARAMETER_VALUE, EVENT_TIME);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeDuring(Object time) throws CstlServiceException {
        if (time instanceof TimePeriodType) {
            final TimePeriodType tp = (TimePeriodType) time;
            final String begin      = getLuceneTimeValue(tp.getBeginPosition());
            final String end        = getLuceneTimeValue(tp.getEndPosition());
            luceneRequest.append("AND (");

            // the multiple observations included in the period
            luceneRequest.append(" (sampling_time_begin:[").append(begin).append(" 30000000] AND sampling_time_end:[19700000 ").append(end).append("])");
            luceneRequest.append(OR_OPERATOR);
            // the single observations included in the period
            luceneRequest.append(" (sampling_time_begin:[").append(begin).append(" 30000000] AND sampling_time_begin:[19700000 ").append(end).append("] AND sampling_time_end IS NULL)");
            luceneRequest.append(OR_OPERATOR);
            // the multiple observations whitch overlaps the first bound
            luceneRequest.append(" (sampling_time_begin:[19700000 ").append(begin).append("] AND sampling_time_end:[19700000 ").append(end).append("] AND sampling_time_end:[").append(begin).append(" 30000000])");
            luceneRequest.append(OR_OPERATOR);
            // the multiple observations whitch overlaps the second bound
            luceneRequest.append(" (sampling_time_begin:[").append(begin).append(" 30000000] AND sampling_time_end:[").append(end).append(" 30000000] AND sampling_time_begin:[19700000 ").append(end).append("])");
            luceneRequest.append(OR_OPERATOR);
            // the multiple observations whitch overlaps the whole period
            luceneRequest.append(" (sampling_time_begin:[19700000 ").append(begin).append("] AND sampling_time_end:[").append(end).append(" 30000000]))");


        } else {
            throw new CstlServiceException("TM_During operation require TimePeriod!",
                    INVALID_PARAMETER_VALUE, EVENT_TIME);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ObservationResult> filterResult() throws CstlServiceException {
        try {
            final SpatialQuery query = new SpatialQuery(luceneRequest.toString());
            final SortField sf       = new SortField("sampling_time_begin", SortField.STRING, false);
            query.setSort(new Sort(sf));
            return searcher.doResultSearch(query);
        } catch(SearchingException ex) {
            throw new CstlServiceException("Search exception while filtering the observation", ex, NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> filterObservation() throws CstlServiceException {
        try {
            return searcher.doSearch(new SpatialQuery(luceneRequest.toString()));
        } catch(SearchingException ex) {
            throw new CstlServiceException("Search exception while filtering the observation", ex, NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInfos() {
        return "Constellation Lucene O&M Filter 0.6";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBoundedObservation() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBoundingBox(EnvelopeEntry e) throws CstlServiceException {
        throw new CstlServiceException("SetBoundingBox is not supported by this ObservationFilter implementation.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refresh() throws CstlServiceException {
        try {
            searcher.refresh();
        } catch (IndexingException ex) {
            throw new CstlServiceException("Indexing Exception while refreshing the lucene index", ex, NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLoglevel(Level logLevel) {
        if (searcher != null) {
            searcher.setLogLevel(logLevel);
        }
    }
}
