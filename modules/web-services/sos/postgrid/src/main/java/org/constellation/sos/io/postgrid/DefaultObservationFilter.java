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


import java.util.Map;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;

// Constellation dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.sos.factory.AbstractOMSOSFactory;
import org.constellation.sos.io.ObservationFilter;
import org.constellation.sos.io.ObservationResult;
import org.constellation.ws.CstlServiceException;

import static org.constellation.sos.ws.Utils.*;
import static org.constellation.sos.ws.SOSConstants.*;

// Geotoolkit dependencies
import org.geotoolkit.gml.xml.v311.EnvelopeType;
import org.geotoolkit.gml.xml.v311.ReferenceType;
import org.geotoolkit.gml.xml.v311.TimeInstantType;
import org.geotoolkit.gml.xml.v311.TimePeriodType;
import org.geotoolkit.observation.xml.v100.ProcessType;
import org.geotoolkit.sos.xml.v100.ObservationOfferingType;
import org.geotoolkit.sos.xml.v100.ResponseModeType;
import org.geotoolkit.util.logging.Logging;
import static org.geotoolkit.sos.xml.v100.ResponseModeType.*;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

// GeoAPI dependencies
import org.opengis.observation.Observation;

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
     * The properties file allowing to store the id mapping between physical and database ID.
     */
    protected Properties map;

     /**
     * use for debugging purpose
     */
    protected static final Logger LOGGER = Logging.getLogger("org.constellation.sos");

    /**
     * The base for observation id.
     */
    protected String observationIdBase;

    /**
     * The base for observation id.
     */
    protected String observationTemplateIdBase;

    /**
     * Clone a new Observation Filter.
     * 
     * @param omFilter
     */
    public DefaultObservationFilter(final DefaultObservationFilter omFilter) {
        this.connection                = omFilter.connection;
        this.map                       = omFilter.map;
        this.observationIdBase         = omFilter.observationIdBase;
        this.observationTemplateIdBase = omFilter.observationTemplateIdBase;
    }

    
    public DefaultObservationFilter(final Automatic configuration, final Map<String, Object> properties) throws CstlServiceException {
        this.observationIdBase         = (String)     properties.get(AbstractOMSOSFactory.OBSERVATION_ID_BASE);
        this.observationTemplateIdBase = (String)     properties.get(AbstractOMSOSFactory.OBSERVATION_TEMPLATE_ID_BASE);
        this.map                       = (Properties) properties.get(AbstractOMSOSFactory.IDENTIFIER_MAPPING);
        
        if (configuration == null) {
            throw new CstlServiceException("The configuration object is null", NO_APPLICABLE_CODE);
        }
        // we get the database informations
        final BDD db = configuration.getBdd();
        if (db == null) {
            throw new CstlServiceException("The configuration file does not contains a BDD object", NO_APPLICABLE_CODE);
        }
        try {
            this.connection = DatabasePool.getDatabaseConnection(db);
            if (this.connection == null) {
                db.getConnection();
            }
        } catch (SQLException ex) {
            throw new CstlServiceException("SQLException while initializing the observation filter:" +'\n'+
                                           "cause:" + ex.getMessage(), NO_APPLICABLE_CODE);
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
            sqlRequest.append(observationIdBase).append("%' AND ");

        } else if (requestMode == RESULT_TEMPLATE) {
            sqlRequest.append(observationTemplateIdBase).append("%' AND ");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initFilterGetResult(final Observation template, final QName resultModel) {
        final ProcessType process = (ProcessType) template.getProcedure();
        
        if (resultModel.equals(MEASUREMENT_QNAME)) {
            sqlRequest = new StringBuilder("SELECT \"result\", \"sampling_time_begin\", \"sampling_time_end\" FROM \"observation\".\"measurements\" WHERE ");
        } else {
            sqlRequest = new StringBuilder("SELECT \"result\", \"sampling_time_begin\", \"sampling_time_end\" FROM \"observation\".\"observations\" WHERE ");
        }
        //we add to the request the property of the template
        sqlRequest.append("\"procedure\"='").append(process.getHref()).append("'");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProcedure(final List<String> procedures, final ObservationOfferingType off) {
        sqlRequest.append(" ( ");
        if (!procedures.isEmpty()) {

            for (String s : procedures) {
                if (s != null) {
                    String dbId = map.getProperty(s);
                    if (dbId == null) {
                        dbId = s;
                    }
                    sqlRequest.append(" \"procedure\"='").append(dbId).append("' OR ");
                }
            }
        } else {
            //if is not specified we use all the process of the offering
            for (ReferenceType proc : off.getProcedure()) {
                sqlRequest.append(" \"procedure\"='").append(proc.getHref()).append("' OR ");
            }
        }
        sqlRequest.delete(sqlRequest.length() - 3, sqlRequest.length());
        sqlRequest.append(") ");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setObservedProperties(final List<String> phenomenon, final List<String> compositePhenomenon) {
        sqlRequest.append(" AND( ");
        for (String p : phenomenon) {
            sqlRequest.append(" \"observed_property\"='").append(p).append("' OR ");

        }
        for (String p : compositePhenomenon) {
            sqlRequest.append(" \"observed_property_composite\"='").append(p).append("' OR ");
        }
        sqlRequest.delete(sqlRequest.length() - 3, sqlRequest.length());
        sqlRequest.append(") ");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFeatureOfInterest(final List<String> fois) {
        sqlRequest.append(" AND (");
        for (String foi : fois) {
            sqlRequest.append("(\"feature_of_interest_point\"='").append(foi).append("' OR \"feature_of_interest\"='").append(foi).append("' OR \"feature_of_interest_curve\"='").append(foi).append("') OR");
        }
        sqlRequest.delete(sqlRequest.length() - 3, sqlRequest.length());
        sqlRequest.append(") ");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeEquals(final Object time) throws CstlServiceException {
        if (time instanceof TimePeriodType) {
            final TimePeriodType tp = (TimePeriodType) time;
            final String begin      = getTimeValue(tp.getBeginPosition());
            final String end        = getTimeValue(tp.getEndPosition());

            // we request directly a multiple observation or a period observation (one measure during a period)
            sqlRequest.append("AND (");
            sqlRequest.append(" \"sampling_time_begin\"='").append(begin).append("' AND ");
            sqlRequest.append(" \"sampling_time_end\"='").append(end).append("') ");

        // if the temporal object is a timeInstant
        } else if (time instanceof TimeInstantType) {
            final TimeInstantType ti = (TimeInstantType) time;
            final String position    = getTimeValue(ti.getTimePosition());
            sqlRequest.append("AND (");

            // case 1 a single observation
            sqlRequest.append("(\"sampling_time_begin\"='").append(position).append("' AND \"sampling_time_end\" IS NULL)");
            sqlRequest.append(" OR ");

            //case 2 multiple observations containing a matching value
            sqlRequest.append("(\"sampling_time_begin\"<='").append(position).append("' AND \"sampling_time_end\">='").append(position).append("'))");

        } else {
            throw new CstlServiceException("TM_Equals operation require timeInstant or TimePeriod!",
                    INVALID_PARAMETER_VALUE, EVENT_TIME);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeBefore(final Object time) throws CstlServiceException  {
        // for the operation before the temporal object must be an timeInstant
        if (time instanceof TimeInstantType) {
            final TimeInstantType ti = (TimeInstantType) time;
            final String position    = getTimeValue(ti.getTimePosition());
            sqlRequest.append("AND (");

            // the single and multpile observations which begin after the bound
            sqlRequest.append("(\"sampling_time_begin\"<='").append(position).append("'))");

        } else {
            throw new CstlServiceException("TM_Before operation require timeInstant!",
                    INVALID_PARAMETER_VALUE, EVENT_TIME);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeAfter(final Object time) throws CstlServiceException {
        // for the operation after the temporal object must be an timeInstant
        if (time instanceof TimeInstantType) {
            final TimeInstantType ti = (TimeInstantType) time;
            final String position    = getTimeValue(ti.getTimePosition());
            sqlRequest.append("AND (");

            // the single and multpile observations which begin after the bound
            sqlRequest.append("(\"sampling_time_begin\">='").append(position).append("')");
            sqlRequest.append(" OR ");
            // the multiple observations overlapping the bound
            sqlRequest.append("(\"sampling_time_begin\"<='").append(position).append("' AND \"sampling_time_end\">='").append(position).append("'))");


        } else {
            throw new CstlServiceException("TM_After operation require timeInstant!",
                    INVALID_PARAMETER_VALUE, EVENT_TIME);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeDuring(final Object time) throws CstlServiceException {
        if (time instanceof TimePeriodType) {
            final TimePeriodType tp = (TimePeriodType) time;
            final String begin      = getTimeValue(tp.getBeginPosition());
            final String end        = getTimeValue(tp.getEndPosition());
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
            throw new CstlServiceException("TM_During operation require TimePeriod!",
                    INVALID_PARAMETER_VALUE, EVENT_TIME);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setResultEquals(final String propertyName, final String value) throws CstlServiceException{
        throw new CstlServiceException("setResultEquals is not supported by this ObservationFilter implementation.");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> supportedQueryableResultProperties() {
        return new ArrayList<String>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ObservationResult> filterResult() throws CstlServiceException {
        LOGGER.log(Level.FINER, "request:{0}", sqlRequest.toString());
        try {
            final List<ObservationResult> results = new ArrayList<ObservationResult>();
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
            throw new CstlServiceException("the service has throw a SQL Exception:" + ex.getMessage(),
                                          NO_APPLICABLE_CODE);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> filterObservation() throws CstlServiceException {
        LOGGER.log(Level.FINER, "request:{0}", sqlRequest.toString());
        try {
            final List<String> results       = new ArrayList<String>();
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
            throw new CstlServiceException("the service has throw a SQL Exception:" + ex.getMessage(),
                                          NO_APPLICABLE_CODE);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInfos() {
        return "Constellation Postgrid O&M Filter 0.7";
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
    public void setBoundingBox(final EnvelopeType e) throws CstlServiceException {
        throw new CstlServiceException("SetBoundingBox is not supported by this ObservationFilter implementation.");
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
}
