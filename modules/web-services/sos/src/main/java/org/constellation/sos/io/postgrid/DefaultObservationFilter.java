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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

// Constellation dependencies
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.sos.io.ObservationFilter;
import org.constellation.sos.io.ObservationResult;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.gml.xml.v311.EnvelopeEntry;
import org.geotoolkit.gml.xml.v311.ReferenceEntry;
import org.geotoolkit.gml.xml.v311.TimeInstantType;
import org.geotoolkit.gml.xml.v311.TimePeriodType;
import org.geotoolkit.observation.xml.v100.ProcessEntry;
import org.geotoolkit.sos.xml.v100.ObservationOfferingEntry;
import org.geotoolkit.sos.xml.v100.ResponseModeType;
import org.opengis.observation.Observation;
import static org.geotoolkit.sos.xml.v100.ResponseModeType.*;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import static org.constellation.sos.ws.Utils.*;
import static org.constellation.sos.ws.SOSConstants.*;

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
    protected static final Logger LOGGER = Logger.getLogger("org.constellation.sos");

    /**
     * The base for observation id.
     */
    protected String observationIdBase;

    /**
     * The base for observation id.
     */
    protected String observationTemplateIdBase;

    public DefaultObservationFilter(DefaultObservationFilter omFilter) {
        this.connection                = omFilter.connection;
        this.map                       = omFilter.map;
        this.observationIdBase         = omFilter.observationIdBase;
        this.observationTemplateIdBase = omFilter.observationTemplateIdBase;
    }

    /**
     *
     */
    public DefaultObservationFilter(String observationIdBase, String observationTemplateIdBase, Properties map, Automatic configuration) throws CstlServiceException {
        this.observationIdBase         = observationIdBase;
        this.observationTemplateIdBase = observationTemplateIdBase;
        this.map                       = map;
        
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
     * Initialize the query.
     */
    @Override
    public void initFilterObservation(ResponseModeType requestMode, QName resultModel) {
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
     * Initialize the query.
     */
    @Override
    public void initFilterGetResult(Observation template, QName resultModel) {
        ProcessEntry process = (ProcessEntry) template.getProcedure();
        
        if (resultModel.equals(MEASUREMENT_QNAME)) {
            sqlRequest = new StringBuilder("SELECT \"result\", \"sampling_time_begin\", \"sampling_time_end\" FROM \"observation\".\"measurements\" WHERE ");
        } else {
            sqlRequest = new StringBuilder("SELECT \"result\", \"sampling_time_begin\", \"sampling_time_end\" FROM \"observation\".\"observations\" WHERE ");
        }
        //we add to the request the property of the template
        sqlRequest.append("\"procedure\"='").append(process.getHref()).append("'");
    }

    /**
     * Add some procedure filter to the request.
     * if the list of procedure ID is empty it add all the offering procedure.
     *
     * @param procedures
     * @param off
     */
    @Override
    public void setProcedure(List<String> procedures, ObservationOfferingEntry off) {
        sqlRequest.append(" ( ");
        if (procedures.size() != 0) {

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
            for (ReferenceEntry proc : off.getProcedure()) {
                sqlRequest.append(" \"procedure\"='").append(proc.getHref()).append("' OR ");
            }
        }
        sqlRequest.delete(sqlRequest.length() - 3, sqlRequest.length());
        sqlRequest.append(") ");
    }

    /**
     * Add some phenomenon filter to the request.
     *
     * @param phenomenon
     * @param compositePhenomenon
     */
    @Override
    public void setObservedProperties(List<String> phenomenon, List<String> compositePhenomenon) {
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
     * Add some sampling point filter to the request.
     *
     * @param phenomenon
     * @param compositePhenomenon
     */
    @Override
    public void setFeatureOfInterest(List<String> fois) {
        sqlRequest.append(" AND (");
        for (String foi : fois) {
            sqlRequest.append("(\"feature_of_interest_point\"='").append(foi).append("' OR \"feature_of_interest\"='").append(foi).append("' OR \"feature_of_interest_curve\"='").append(foi).append("') OR");
        }
        sqlRequest.delete(sqlRequest.length() - 3, sqlRequest.length());
        sqlRequest.append(") ");
    }

    /**
     * Add a TM_Equals filter to the current request.
     *
     * @param time
     * @throws org.constellation.ws.CstlServiceException
     */
    @Override
    public void setTimeEquals(Object time) throws CstlServiceException {
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
     * Add a TM_Before filter to the current request.
     *
     * @param time
     * @throws org.constellation.ws.CstlServiceException
     */
    @Override
    public void setTimeBefore(Object time) throws CstlServiceException  {
        // for the operation before the temporal object must be an timeInstant
        if (time instanceof TimeInstantType) {
            final TimeInstantType ti = (TimeInstantType) time;
            final String position    = getTimeValue(ti.getTimePosition());
            sqlRequest.append("AND (");

            // the single and multpile observations whitch begin after the bound
            sqlRequest.append("(\"sampling_time_begin\"<='").append(position).append("'))");

        } else {
            throw new CstlServiceException("TM_Before operation require timeInstant!",
                    INVALID_PARAMETER_VALUE, EVENT_TIME);
        }
    }

    /**
     * Add a TM_After filter to the current request.
     *
     * @param time
     * @throws org.constellation.ws.CstlServiceException
     */
    @Override
    public void setTimeAfter(Object time) throws CstlServiceException {
        // for the operation after the temporal object must be an timeInstant
        if (time instanceof TimeInstantType) {
            final TimeInstantType ti = (TimeInstantType) time;
            final String position    = getTimeValue(ti.getTimePosition());
            sqlRequest.append("AND (");

            // the single and multpile observations whitch begin after the bound
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
     * Add a TM_During filter to the current request.
     *
     * @param time
     * @throws org.constellation.ws.CstlServiceException
     */
    @Override
    public void setTimeDuring(Object time) throws CstlServiceException {
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
            // the multiple observations whitch overlaps the first bound
            sqlRequest.append(" (\"sampling_time_begin\"<='").append(begin).append("' AND \"sampling_time_end\"<= '").append(end).append("' AND \"sampling_time_end\">='").append(begin).append("')");
            sqlRequest.append(" OR ");
            // the multiple observations whitch overlaps the second bound
            sqlRequest.append(" (\"sampling_time_begin\">='").append(begin).append("' AND \"sampling_time_end\">= '").append(end).append("' AND \"sampling_time_begin\"<='").append(end).append("')");
            sqlRequest.append(" OR ");
            // the multiple observations whitch overlaps the whole period
            sqlRequest.append(" (\"sampling_time_begin\"<='").append(begin).append("' AND \"sampling_time_end\">= '").append(end).append("'))");


        } else {
            throw new CstlServiceException("TM_During operation require TimePeriod!",
                    INVALID_PARAMETER_VALUE, EVENT_TIME);
        }
    }

    @Override
    public List<ObservationResult> filterResult() throws CstlServiceException {
        LOGGER.finer("request:" + sqlRequest.toString());
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
            LOGGER.severe("SQLException while executing the query: " + sqlRequest.toString());
            throw new CstlServiceException("the service has throw a SQL Exception:" + ex.getMessage(),
                                          NO_APPLICABLE_CODE);
        }

    }

    @Override
    public List<String> filterObservation() throws CstlServiceException {
        LOGGER.info("request:" + sqlRequest.toString());
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
            LOGGER.severe("SQLException while executing the query: " + sqlRequest.toString());
            throw new CstlServiceException("the service has throw a SQL Exception:" + ex.getMessage(),
                                          NO_APPLICABLE_CODE);
        }

    }
    
    @Override
    public String getInfos() {
        return "Constellation Postgrid O&M Filter 0.5";
    }

    @Override
    public boolean isBoundedObservation() {
        return false;
    }

    @Override
    public void setBoundingBox(EnvelopeEntry e) throws CstlServiceException {
        throw new CstlServiceException("SetBoundingBox is not supported by this ObservationFilter implementation.");
    }

    @Override
    public void refresh() {
        //do nothing
    }


}
