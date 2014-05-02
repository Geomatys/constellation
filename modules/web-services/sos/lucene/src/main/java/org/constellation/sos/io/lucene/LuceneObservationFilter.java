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

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.sis.storage.DataStoreException;
import org.constellation.generic.database.Automatic;
import org.geotoolkit.observation.ObservationFilter;
import org.geotoolkit.observation.ObservationResult;
import static org.constellation.sos.ws.SOSConstants.*;
import static org.constellation.sos.ws.SOSUtils.*;

import org.constellation.ws.CstlServiceException;
import org.geotoolkit.gml.xml.Envelope;
import org.geotoolkit.lucene.IndexingException;
import org.geotoolkit.lucene.SearchingException;
import org.geotoolkit.lucene.filter.SpatialQuery;
import org.geotoolkit.observation.ObservationStoreException;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

import org.geotoolkit.sos.xml.ObservationOffering;
import org.geotoolkit.sos.xml.ResponseModeType;
import org.opengis.temporal.Instant;
import org.opengis.temporal.Period;
/**
 * TODO
 * 
 * @author Guilhem Legal (Geomatys)
 */
public class LuceneObservationFilter implements ObservationFilter {

    private static final Logger LOGGER =Logger.getLogger("org.constellation.sos.io.lucene");
    
    private StringBuilder luceneRequest;

    private LuceneObservationSearcher searcher;

    private static final String OR_OPERATOR = " OR ";

    public LuceneObservationFilter(final LuceneObservationFilter omFilter) throws DataStoreException {
        this.searcher = omFilter.searcher;
    }

    public LuceneObservationFilter(final Automatic configuration, final Map<String, Object> properties) throws DataStoreException {
        try {
            this.searcher = new LuceneObservationSearcher(configuration.getConfigurationDirectory(), "");
        } catch (IndexingException ex) {
            throw new DataStoreException("IndexingException in LuceneObservationFilter constructor", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initFilterObservation(final ResponseModeType requestMode, final QName resultModel) {
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
    public void initFilterGetResult(final String procedure, final QName resultModel) {
        if (resultModel.equals(MEASUREMENT_QNAME)) {
            luceneRequest = new StringBuilder("type:measurement AND template:FALSE AND procedure:\"" + procedure + "\" ");
        } else {
            luceneRequest = new StringBuilder("type:observation AND template:FALSE AND procedure:\"" + procedure + "\" ");
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void initFilterGetFeatureOfInterest() throws DataStoreException {
        // do nothing no implementes
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProcedure(final List<String> procedures, final List<ObservationOffering> offerings) {
        luceneRequest.append(" ( ");
        boolean add = false;
        if (!procedures.isEmpty()) {

            for (String s : procedures) {
                if (s != null) {
                    luceneRequest.append(" procedure:\"").append(s).append("\" OR ");
                    add = true;
                }
            }
        } else {
            //if is not specified we use all the process of the offering
            for (ObservationOffering off : offerings) {
                for (String proc : off.getProcedures()) {
                    luceneRequest.append(" procedure:\"").append(proc).append("\" OR ");
                    add = true;
                }
            }
        }
        luceneRequest.delete(luceneRequest.length() - 3, luceneRequest.length());
        if (add) {
            luceneRequest.append(") ");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setObservedProperties(final List<String> phenomenon) {
        luceneRequest.append(" AND( ");
        for (String p : phenomenon) {
            luceneRequest.append(" observed_property:\"").append(p).append('"').append(OR_OPERATOR);

        }
        luceneRequest.delete(luceneRequest.length() - 3, luceneRequest.length());
        luceneRequest.append(") ");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFeatureOfInterest(final List<String> fois) {
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
    public void setTimeEquals(final Object time) throws DataStoreException {
        if (time instanceof Period) {
            final Period tp = (Period) time;
            final String begin      = getLuceneTimeValue(tp.getBeginning().getPosition());
            final String end        = getLuceneTimeValue(tp.getEnding().getPosition());

            // we request directly a multiple observation or a period observation (one measure during a period)
            luceneRequest.append("AND (");
            luceneRequest.append(" sampling_time_begin:").append(begin).append(" AND ");
            luceneRequest.append(" sampling_time_end:").append(end).append(") ");

        // if the temporal object is a timeInstant
        } else if (time instanceof Instant) {
            final Instant ti = (Instant) time;
            final String position    = getLuceneTimeValue(ti.getPosition());
            luceneRequest.append("AND (");

            // case 1 a single observation
            luceneRequest.append("(sampling_time_begin:'").append(position).append("' AND sampling_time_end:NULL)");
            luceneRequest.append(OR_OPERATOR);

            //case 2 multiple observations containing a matching value
            luceneRequest.append("(sampling_time_begin: [19700000 ").append(position).append("] ").append(" AND sampling_time_end: [").append(position).append(" 30000000]))");

        } else {
            throw new ObservationStoreException("TM_Equals operation require timeInstant or TimePeriod!",
                    INVALID_PARAMETER_VALUE, EVENT_TIME);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeBefore(final Object time) throws DataStoreException {
        // for the operation before the temporal object must be an timeInstant
        if (time instanceof Instant) {
            final Instant ti = (Instant) time;
            final String position    = getLuceneTimeValue(ti.getPosition());
            luceneRequest.append("AND (");

            // the single and multpile observations which begin after the bound
            luceneRequest.append("(sampling_time_begin: [19700000000000 ").append(position).append("]))");

        } else {
            throw new ObservationStoreException("TM_Before operation require timeInstant!",
                    INVALID_PARAMETER_VALUE, EVENT_TIME);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeAfter(final Object time) throws DataStoreException {
        // for the operation after the temporal object must be an timeInstant
        if (time instanceof Instant) {
            final Instant ti = (Instant) time;
            final String position    = getLuceneTimeValue(ti.getPosition());
            luceneRequest.append("AND (");

            // the single and multpile observations which begin after the bound
            luceneRequest.append("(sampling_time_begin:[").append(position).append(" 30000000])");
            luceneRequest.append(OR_OPERATOR);
            // the multiple observations overlapping the bound
            luceneRequest.append("(sampling_time_begin: [19700000 ").append(position).append("] AND sampling_time_end:[").append(position).append(" 30000000]))");


        } else {
            throw new ObservationStoreException("TM_After operation require timeInstant!",
                    INVALID_PARAMETER_VALUE, EVENT_TIME);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeDuring(final Object time) throws DataStoreException {
        if (time instanceof Period) {
            final Period tp = (Period) time;
            final String begin      = getLuceneTimeValue(tp.getBeginning().getPosition());
            final String end        = getLuceneTimeValue(tp.getEnding().getPosition());
            luceneRequest.append("AND (");

            // the multiple observations included in the period
            luceneRequest.append(" (sampling_time_begin:[").append(begin).append(" 30000000] AND sampling_time_end:[19700000 ").append(end).append("])");
            luceneRequest.append(OR_OPERATOR);
            // the single observations included in the period
            luceneRequest.append(" (sampling_time_begin:[").append(begin).append(" 30000000] AND sampling_time_begin:[19700000 ").append(end).append("] AND sampling_time_end IS NULL)");
            luceneRequest.append(OR_OPERATOR);
            // the multiple observations which overlaps the first bound
            luceneRequest.append(" (sampling_time_begin:[19700000 ").append(begin).append("] AND sampling_time_end:[19700000 ").append(end).append("] AND sampling_time_end:[").append(begin).append(" 30000000])");
            luceneRequest.append(OR_OPERATOR);
            // the multiple observations which overlaps the second bound
            luceneRequest.append(" (sampling_time_begin:[").append(begin).append(" 30000000] AND sampling_time_end:[").append(end).append(" 30000000] AND sampling_time_begin:[19700000 ").append(end).append("])");
            luceneRequest.append(OR_OPERATOR);
            // the multiple observations which overlaps the whole period
            luceneRequest.append(" (sampling_time_begin:[19700000 ").append(begin).append("] AND sampling_time_end:[").append(end).append(" 30000000]))");


        } else {
            throw new ObservationStoreException("TM_During operation require TimePeriod!",
                    INVALID_PARAMETER_VALUE, EVENT_TIME);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOfferings(final List<ObservationOffering> offerings) throws DataStoreException {
        // not used in this implementations
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<ObservationResult> filterResult() throws DataStoreException {
        try {
            final SpatialQuery query = new SpatialQuery(luceneRequest.toString());
            final SortField sf       = new SortField("sampling_time_begin", SortField.Type.STRING, false);
            query.setSort(new Sort(sf));
            return searcher.doResultSearch(query);
        } catch(SearchingException ex) {
            throw new DataStoreException("Search exception while filtering the observation", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> filterObservation() throws DataStoreException {
        try {
            return searcher.doSearch(new SpatialQuery(luceneRequest.toString()));
        } catch(SearchingException ex) {
            throw new DataStoreException("Search exception while filtering the observation", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInfos() {
        return "Constellation Lucene O&M Filter 0.9";
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
    public void setBoundingBox(Envelope e) throws DataStoreException {
        throw new DataStoreException("SetBoundingBox is not supported by this ObservationFilter implementation.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setResultEquals(String propertyName, String value) throws DataStoreException {
        throw new DataStoreException("setResultEquals is not supported by this ObservationFilter implementation.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> supportedQueryableResultProperties() {
        return new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refresh() throws DataStoreException {
        try {
            searcher.refresh();
        } catch (IndexingException ex) {
            throw new DataStoreException("Indexing Exception while refreshing the lucene index", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLoglevel(final Level logLevel) {
        if (searcher != null) {
            searcher.setLogLevel(logLevel);
        }
    }

    @Override
    public void setTimeLatest() throws DataStoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setTimeFirst() throws DataStoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isDefaultTemplateTime() {
        return true;
    }

    @Override
    public Set<String> filterFeatureOfInterest() throws DataStoreException {
        throw new DataStoreException("filterFeatureOfInterest is not supported by this ObservationFilter implementation.");
    }

    @Override
    public void destroy() {
        if (searcher != null) {
            searcher.destroy();
        }
    }
}
