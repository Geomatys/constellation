/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
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

package org.constellation.sos.io;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.gml.v311.ReferenceEntry;
import org.constellation.gml.v311.TimeInstantType;
import org.constellation.gml.v311.TimePeriodType;
import org.constellation.sos.v100.ObservationOfferingEntry;
import org.constellation.sos.v100.ResponseModeType;
import org.constellation.ws.CstlServiceException;
import static org.constellation.sos.v100.ResponseModeType.*;
import static org.constellation.ows.OWSExceptionCode.*;

/**
 *
 * @author Guilhem Legal
 */
public class DefaultObservationFilter extends ObservationFilter {


    private StringBuilder SQLRequest;

    /**
     *
     */
    private final Connection connection;


    /**
     *
     */
    public DefaultObservationFilter(String observationIdBase, String observationTemplateIdBase, Properties map, Automatic configuration) throws CstlServiceException {
        super(observationIdBase, observationTemplateIdBase, map);
        if (configuration == null) {
            throw new CstlServiceException("The configuration object is null", NO_APPLICABLE_CODE);
        }
        // we get the database informations
        BDD db = configuration.getBdd();
        if (db == null) {
            throw new CstlServiceException("The configuration file does not contains a BDD object", NO_APPLICABLE_CODE);
        }
        try {
            this.connection = db.getConnection();
        } catch (SQLException ex) {
            throw new CstlServiceException("SQLException while initializing the observation filter:" +'\n'+
                                           "cause:" + ex.getMessage(), NO_APPLICABLE_CODE);
        }
    }


    /**
     * Initialize the query.
     */
    @Override
    public void initFilterObservation(ResponseModeType requestMode) {
        SQLRequest = new StringBuilder("SELECT name FROM observations WHERE name LIKE '%");
        if (requestMode == INLINE) {
            SQLRequest.append(observationIdBase).append("%' AND ");

        } else if (requestMode == RESULT_TEMPLATE) {
            SQLRequest.append(observationTemplateIdBase).append("%' AND ");
        }
    }

    /**
     * Initialize the query.
     */
    @Override
    public void initFilterGetResult(String procedure) {
        SQLRequest = new StringBuilder("SELECT result, sampling_time_begin, sampling_time_end FROM observations WHERE ");
        //we add to the request the property of the template
        SQLRequest.append("procedure='").append(procedure).append("'");
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
        SQLRequest.append(" ( ");
        if (procedures.size() != 0) {

            for (String s : procedures) {
                if (s != null) {
                    String dbId = map.getProperty(s);
                    if (dbId == null) {
                        dbId = s;
                    }
                    SQLRequest.append(" procedure='").append(dbId).append("' OR ");
                }
            }
        } else {
            //if is not specified we use all the process of the offering
            for (ReferenceEntry proc : off.getProcedure()) {
                SQLRequest.append(" procedure='").append(proc.getHref()).append("' OR ");
            }
        }
        SQLRequest.delete(SQLRequest.length() - 3, SQLRequest.length());
        SQLRequest.append(") ");
    }

    /**
     * Add some phenomenon filter to the request.
     *
     * @param phenomenon
     * @param compositePhenomenon
     */
    @Override
    public void setObservedProperties(List<String> phenomenon, List<String> compositePhenomenon) {
        SQLRequest.append(" AND( ");
        for (String p : phenomenon) {
            SQLRequest.append(" observed_property='").append(p).append("' OR ");

        }
        for (String p : compositePhenomenon) {
            SQLRequest.append(" observed_property_composite='").append(p).append("' OR ");
        }
        SQLRequest.delete(SQLRequest.length() - 3, SQLRequest.length());
        SQLRequest.append(") ");
    }

    /**
     * Add some sampling point filter to the request.
     *
     * @param phenomenon
     * @param compositePhenomenon
     */
    @Override
    public void setFeatureOfInterest(List<String> fois) {
        SQLRequest.append(" AND (");
        for (String foi : fois) {
            SQLRequest.append("feature_of_interest_point='").append(foi).append("' OR");
        }
        SQLRequest.delete(SQLRequest.length() - 3, SQLRequest.length());
        SQLRequest.append(") ");
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
            TimePeriodType tp = (TimePeriodType) time;
            String begin = getTimeValue(tp.getBeginPosition());
            String end = getTimeValue(tp.getEndPosition());

            // we request directly a multiple observation or a period observation (one measure during a period)
            SQLRequest.append("AND (");
            SQLRequest.append(" sampling_time_begin='").append(begin).append("' AND ");
            SQLRequest.append(" sampling_time_end='").append(end).append("') ");

        // if the temporal object is a timeInstant
        } else if (time instanceof TimeInstantType) {
            TimeInstantType ti = (TimeInstantType) time;
            String position = getTimeValue(ti.getTimePosition());
            SQLRequest.append("AND (");

            // case 1 a single observation
            SQLRequest.append("(sampling_time_begin='").append(position).append("' AND sampling_time_end=NULL)");
            SQLRequest.append(" OR ");

            //case 2 multiple observations containing a matching value
            SQLRequest.append("(sampling_time_begin<='").append(position).append("' AND sampling_time_end>='").append(position).append("'))");

        } else {
            throw new CstlServiceException("TM_Equals operation require timeInstant or TimePeriod!",
                    INVALID_PARAMETER_VALUE, "eventTime");
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
            TimeInstantType ti = (TimeInstantType) time;
            String position = getTimeValue(ti.getTimePosition());
            SQLRequest.append("AND (");

            // the single and multpile observations whitch begin after the bound
            SQLRequest.append("(sampling_time_begin<='").append(position).append("'))");

        } else {
            throw new CstlServiceException("TM_Before operation require timeInstant!",
                    INVALID_PARAMETER_VALUE, "eventTime");
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
            TimeInstantType ti = (TimeInstantType) time;
            String position = getTimeValue(ti.getTimePosition());
            SQLRequest.append("AND (");

            // the single and multpile observations whitch begin after the bound
            SQLRequest.append("(sampling_time_begin>='").append(position).append("')");
            SQLRequest.append(" OR ");
            // the multiple observations overlapping the bound
            SQLRequest.append("(sampling_time_begin<='").append(position).append("' AND sampling_time_end>='").append(position).append("'))");


        } else {
            throw new CstlServiceException("TM_After operation require timeInstant!",
                    INVALID_PARAMETER_VALUE, "eventTime");
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
            TimePeriodType tp = (TimePeriodType) time;
            String begin = getTimeValue(tp.getBeginPosition());
            String end = getTimeValue(tp.getEndPosition());
            SQLRequest.append("AND (");

            // the multiple observations included in the period
            SQLRequest.append(" (sampling_time_begin>='").append(begin).append("' AND sampling_time_end<= '").append(end).append("')");
            SQLRequest.append(" OR ");
            // the single observations included in the period
            SQLRequest.append(" (sampling_time_begin>='").append(begin).append("' AND sampling_time_begin<='").append(end).append("' AND sampling_time_end IS NULL)");
            SQLRequest.append(" OR ");
            // the multiple observations whitch overlaps the first bound
            SQLRequest.append(" (sampling_time_begin<='").append(begin).append("' AND sampling_time_end<= '").append(end).append("' AND sampling_time_end>='").append(begin).append("')");
            SQLRequest.append(" OR ");
            // the multiple observations whitch overlaps the second bound
            SQLRequest.append(" (sampling_time_begin>='").append(begin).append("' AND sampling_time_end>= '").append(end).append("' AND sampling_time_begin<='").append(end).append("')");
            SQLRequest.append(" OR ");
            // the multiple observations whitch overlaps the whole period
            SQLRequest.append(" (sampling_time_begin<='").append(begin).append("' AND sampling_time_end>= '").append(end).append("'))");


        } else {
            throw new CstlServiceException("TM_During operation require TimePeriod!",
                    INVALID_PARAMETER_VALUE, "eventTime");
        }
    }

    @Override
    public List<ObservationFilter.ObservationResult> filterResult() throws CstlServiceException {
        logger.info("request:" + SQLRequest.toString());
        try {
            List<ObservationFilter.ObservationResult> results = new ArrayList<ObservationFilter.ObservationResult>();
            Statement currentStatement = connection.createStatement();
            ResultSet result = currentStatement.executeQuery(SQLRequest.toString());
            while (result.next()) {
                results.add(new ObservationFilter.ObservationResult(result.getString(1),
                                                                    result.getTimestamp(2),
                                                                    result.getTimestamp(3)));
            }
            result.close();
            currentStatement.close();
            return results;

        } catch (SQLException ex) {
            logger.severe("SQLExcpetion while executing the query: " + SQLRequest.toString());
            throw new CstlServiceException("the service has throw a SQL Exception:" + ex.getMessage(),
                                          NO_APPLICABLE_CODE);
        }

    }

    @Override
    public List<String> filterObservation() throws CstlServiceException {
        logger.info("request:" + SQLRequest.toString());
        try {
            List<String> results = new ArrayList<String>();
            Statement currentStatement = connection.createStatement();
            ResultSet result = currentStatement.executeQuery(SQLRequest.toString());
            while (result.next()) {
                results.add(result.getString(1));
            }
            result.close();
            currentStatement.close();
            return results;
        } catch (SQLException ex) {
            logger.severe("SQLException while executing the query: " + SQLRequest.toString());
            throw new CstlServiceException("the service has throw a SQL Exception:" + ex.getMessage(),
                                          NO_APPLICABLE_CODE);
        }

    }
}
