/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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

package org.constellation.sos.io.postgrid;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import javax.xml.namespace.QName;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.logging.Logging;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.sos.factory.OMFactory;
import org.geotoolkit.observation.ObservationFilter;
import org.geotoolkit.observation.ObservationResult;
import static org.constellation.sos.ws.SOSConstants.*;

// Geotoolkit dependencies
import static org.constellation.sos.ws.SOSUtils.*;
import org.geotoolkit.gml.xml.Envelope;
import org.geotoolkit.observation.ObservationStoreException;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

// GeoAPI dependencies
import org.geotoolkit.sos.xml.ObservationOffering;
import org.geotoolkit.sos.xml.ResponseModeType;
import static org.geotoolkit.sos.xml.ResponseModeType.*;
import org.opengis.temporal.Instant;
import org.opengis.temporal.Period;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class DefaultObservationFilter implements ObservationFilter {


    private StringBuilder sqlRequest;

    /**
     *
     */
    private final Connection connection;

     /**
     * use for debugging purpose
     */
    protected static final Logger LOGGER = Logging.getLogger("org.constellation.sos");

    /**
     * The base for observation id.
     */
    protected final String observationIdBase;

    /**
     * The base for observation id.
     */
    protected final String observationTemplateIdBase;
    
    protected final String phenomenonIdBase;

    /**
     * Clone a new Observation Filter.
     * 
     * @param omFilter
     */
    public DefaultObservationFilter(final DefaultObservationFilter omFilter) {
        this.connection                = omFilter.connection;
        this.observationIdBase         = omFilter.observationIdBase;
        this.observationTemplateIdBase = omFilter.observationTemplateIdBase;
        this.phenomenonIdBase          = omFilter.phenomenonIdBase;
    }

    
    public DefaultObservationFilter(final Automatic configuration, final Map<String, Object> properties) throws DataStoreException {
        this.observationIdBase         = (String)     properties.get(OMFactory.OBSERVATION_ID_BASE);
        this.observationTemplateIdBase = (String)     properties.get(OMFactory.OBSERVATION_TEMPLATE_ID_BASE);
        this.phenomenonIdBase          = (String)     properties.get(OMFactory.PHENOMENON_ID_BASE);
        
        if (configuration == null) {
            throw new DataStoreException("The configuration object is null");
        }
        // we get the database informations
        final BDD db = configuration.getBdd();
        if (db == null) {
            throw new DataStoreException("The configuration file does not contains a BDD object");
        }
        try {
            Connection candidate = DatabasePool.getDatabaseConnection(db);
            if (candidate == null) {
                final DataSource ds = db.getPooledDataSource();
                candidate = ds.getConnection();
            }
            this.connection = candidate;
        } catch (SQLException ex) {
            throw new DataStoreException("SQLException while initializing the observation filter:" +'\n'+
                                           "cause:" + ex.getMessage());
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void initFilterObservation(final ResponseModeType requestMode, final QName resultModel) {
        if (resultModel.equals(MEASUREMENT_QNAME)) {
            sqlRequest = new StringBuilder("SELECT \"name\" FROM \"observation\".\"measurements\" WHERE \"name\" LIKE '%");
        } else {
            sqlRequest = new StringBuilder("SELECT \"name\" FROM \"observation\".\"observations\" WHERE \"name\" LIKE '%");
        }
        if (requestMode == INLINE) {
            sqlRequest.append(observationIdBase).append("%' ");

        } else if (requestMode == RESULT_TEMPLATE) {
            sqlRequest.append(observationTemplateIdBase).append("%' ");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initFilterGetResult(final String procedure, final QName resultModel) {
        if (resultModel.equals(MEASUREMENT_QNAME)) {
            sqlRequest = new StringBuilder("SELECT \"result\", \"sampling_time_begin\", \"sampling_time_end\" FROM \"observation\".\"measurements\" WHERE ");
        } else {
            sqlRequest = new StringBuilder("SELECT \"result\", \"sampling_time_begin\", \"sampling_time_end\" FROM \"observation\".\"observations\" WHERE ");
        }
        //we add to the request the property of the template
        sqlRequest.append("\"procedure\"='").append(procedure).append("'");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initFilterGetFeatureOfInterest() throws DataStoreException {
        // do nothing not implemented
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setProcedure(final List<String> procedures, final List<ObservationOffering> offerings) {
        if (!procedures.isEmpty()) {
            sqlRequest.append("AND ( ");
            for (String s : procedures) {
                if (s != null) {
                    sqlRequest.append(" \"procedure\"='").append(s).append("' OR ");
                }
            }
            sqlRequest.delete(sqlRequest.length() - 3, sqlRequest.length());
            sqlRequest.append(") ");
        } else if (!offerings.isEmpty()) {
            
            sqlRequest.append("AND ( ");
            //if is not specified we use all the process of the offering
            for (ObservationOffering off : offerings) {
                for (String proc : off.getProcedures()) {
                    sqlRequest.append(" \"procedure\"='").append(proc).append("' OR ");
                }
            }
            sqlRequest.delete(sqlRequest.length() - 3, sqlRequest.length());
            sqlRequest.append(") ");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setObservedProperties(final List<String> phenomenon) {
        if (!phenomenon.isEmpty()) {
            sqlRequest.append(" AND( ");
            for (String p : phenomenon) {
                p = p.replace(phenomenonIdBase, "");
                sqlRequest.append(" \"observed_property\"='").append(p).append("' OR \"observed_property_composite\"='").append(p).append("' OR ");

            }
            sqlRequest.delete(sqlRequest.length() - 3, sqlRequest.length());
            sqlRequest.append(") ");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFeatureOfInterest(final List<String> fois) {
        if (!fois.isEmpty()) {
            sqlRequest.append(" AND (");
            for (String foi : fois) {
                sqlRequest.append("(\"feature_of_interest_point\"='").append(foi).append("' OR \"feature_of_interest\"='").append(foi).append("' OR \"feature_of_interest_curve\"='").append(foi).append("') OR");
            }
            sqlRequest.delete(sqlRequest.length() - 3, sqlRequest.length());
            sqlRequest.append(") ");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeEquals(final Object time) throws DataStoreException {
        if (time instanceof Period) {
            final Period tp    = (Period) time;
            final String begin = getTimeValue(tp.getBeginning().getPosition());
            final String end   = getTimeValue(tp.getEnding().getPosition());

            // we request directly a multiple observation or a period observation (one measure during a period)
            sqlRequest.append("AND (");
            sqlRequest.append(" \"sampling_time_begin\"='").append(begin).append("' AND ");
            sqlRequest.append(" \"sampling_time_end\"='").append(end).append("') ");

        // if the temporal object is a timeInstant
        } else if (time instanceof Instant) {
            final Instant ti      = (Instant) time;
            final String position = getTimeValue(ti.getPosition());
            sqlRequest.append("AND (");

            // case 1 a single observation
            sqlRequest.append("(\"sampling_time_begin\"='").append(position).append("' AND \"sampling_time_end\" IS NULL)");
            sqlRequest.append(" OR ");

            //case 2 multiple observations containing a matching value
            sqlRequest.append("(\"sampling_time_begin\"<='").append(position).append("' AND \"sampling_time_end\">='").append(position).append("'))");

        } else {
            throw new ObservationStoreException("TM_Equals operation require timeInstant or TimePeriod!",
                    INVALID_PARAMETER_VALUE, EVENT_TIME);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeBefore(final Object time) throws DataStoreException  {
        // for the operation before the temporal object must be an timeInstant
        if (time instanceof Instant) {
            final Instant ti      = (Instant) time;
            final String position = getTimeValue(ti.getPosition());
            sqlRequest.append("AND (");

            // the single and multpile observations which begin after the bound
            sqlRequest.append("(\"sampling_time_begin\"<='").append(position).append("'))");

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
            final Instant ti      = (Instant) time;
            final String position = getTimeValue(ti.getPosition());
            sqlRequest.append("AND (");

            // the single and multpile observations which begin after the bound
            sqlRequest.append("(\"sampling_time_begin\">='").append(position).append("')");
            sqlRequest.append(" OR ");
            // the multiple observations overlapping the bound
            sqlRequest.append("(\"sampling_time_begin\"<='").append(position).append("' AND \"sampling_time_end\">='").append(position).append("'))");


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
            final Period tp    = (Period) time;
            final String begin = getTimeValue(tp.getBeginning().getPosition());
            final String end   = getTimeValue(tp.getEnding().getPosition());
            sqlRequest.append("AND (");

            // the multiple observations included in the period
            sqlRequest.append(" (\"sampling_time_begin\">='").append(begin).append("' AND \"sampling_time_end\"<= '").append(end).append("')");
            sqlRequest.append(" OR ");
            // the single observations included in the period
            sqlRequest.append(" (\"sampling_time_begin\">='").append(begin).append("' AND \"sampling_time_begin\"<='").append(end).append("' AND \"sampling_time_end\" IS NULL)");
            sqlRequest.append(" OR ");
            // the multiple observations which overlaps the first bound
            sqlRequest.append(" (\"sampling_time_begin\"<='").append(begin).append("' AND \"sampling_time_end\"<= '").append(end).append("' AND \"sampling_time_end\">='").append(begin).append("')");
            sqlRequest.append(" OR ");
            // the multiple observations which overlaps the second bound
            sqlRequest.append(" (\"sampling_time_begin\">='").append(begin).append("' AND \"sampling_time_end\">= '").append(end).append("' AND \"sampling_time_begin\"<='").append(end).append("')");
            sqlRequest.append(" OR ");
            // the multiple observations which overlaps the whole period
            sqlRequest.append(" (\"sampling_time_begin\"<='").append(begin).append("' AND \"sampling_time_end\">= '").append(end).append("'))");


        } else {
            throw new ObservationStoreException("TM_During operation require TimePeriod!",
                    INVALID_PARAMETER_VALUE, EVENT_TIME);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setResultEquals(final String propertyName, final String value) throws DataStoreException{
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
    public List<ObservationResult> filterResult() throws DataStoreException {
        LOGGER.log(Level.FINER, "request:{0}", sqlRequest.toString());
        try {
            final List<ObservationResult> results = new ArrayList<>();
            final Statement currentStatement      = connection.createStatement();
            final ResultSet result                = currentStatement.executeQuery(sqlRequest.toString());
            while (result.next()) {
                results.add(new ObservationResult(result.getString(1),
                                                  result.getTimestamp(2),
                                                  result.getTimestamp(3)));
            }
            result.close();
            currentStatement.close();
            return results;

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "SQLException while executing the query: {0}", sqlRequest.toString());
            throw new DataStoreException("the service has throw a SQL Exception:" + ex.getMessage());
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> filterObservation() throws DataStoreException {
        LOGGER.log(Level.FINER, "request:{0}", sqlRequest.toString());
        try {
            final Set<String> results       = new LinkedHashSet<>();
            final Statement currentStatement = connection.createStatement();
            final ResultSet result           = currentStatement.executeQuery(sqlRequest.toString());
            while (result.next()) {
                results.add(result.getString(1));
            }
            result.close();
            currentStatement.close();
            return results;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "SQLException while executing the query: {0}", sqlRequest.toString());
            throw new DataStoreException("the service has throw a SQL Exception:" + ex.getMessage());
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInfos() {
        return "Constellation Postgrid O&M Filter 0.9";
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
    public void setBoundingBox(final Envelope e) throws DataStoreException {
        throw new DataStoreException("SetBoundingBox is not supported by this ObservationFilter implementation.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refresh() {
        //do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLoglevel(final Level logLevel) {
         //do nothing
    }

    @Override
    public void setTimeLatest() throws DataStoreException {
        throw new DataStoreException("setTimeLatest is not supported by this ObservationFilter implementation.");
    }

    @Override
    public void setTimeFirst() throws DataStoreException {
        throw new DataStoreException("setTimeFirst is not supported by this ObservationFilter implementation.");
    }

    @Override
    public void setOfferings(final List<ObservationOffering> offerings) throws DataStoreException {
        // not used in this implementations
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
        //do nothing
    }
}
