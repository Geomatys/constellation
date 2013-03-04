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

package org.constellation.sos.io.om2;


import java.util.Map;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import javax.xml.namespace.QName;

// Constellation dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.sos.factory.OMFactory;
import org.constellation.sos.io.ObservationFilter;
import org.constellation.sos.io.ObservationResult;
import org.constellation.ws.CstlServiceException;

import static org.constellation.sos.ws.Utils.*;
import static org.constellation.sos.ws.SOSConstants.*;

// Geotoolkit dependencies
import org.geotoolkit.gml.xml.Envelope;
import org.geotoolkit.sos.xml.ResponseModeType;
import org.geotoolkit.sos.xml.ObservationOffering;
import org.geotoolkit.util.logging.Logging;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

// GeoAPI dependencies
import org.opengis.temporal.Instant;
import org.opengis.temporal.Period;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class OM2ObservationFilter implements ObservationFilter {


    private StringBuilder sqlRequest;

    /**
     *
     */
    private final DataSource source;

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

    protected boolean template = false;
    
    protected boolean firstFilter = true;
    
    /**
     * Clone a new Observation Filter.
     * 
     * @param omFilter
     */
    public OM2ObservationFilter(final OM2ObservationFilter omFilter) {
        this.source                    = omFilter.source;
        this.observationIdBase         = omFilter.observationIdBase;
        this.observationTemplateIdBase = omFilter.observationTemplateIdBase;
        this.phenomenonIdBase          = omFilter.phenomenonIdBase;
        this.template                  = false;
    }

    
    public OM2ObservationFilter(final Automatic configuration, final Map<String, Object> properties) throws CstlServiceException {
        this.observationIdBase         = (String)     properties.get(OMFactory.OBSERVATION_ID_BASE);
        this.observationTemplateIdBase = (String)     properties.get(OMFactory.OBSERVATION_TEMPLATE_ID_BASE);
        this.phenomenonIdBase          = (String)     properties.get(OMFactory.PHENOMENON_ID_BASE);
        
        if (configuration == null) {
            throw new CstlServiceException("The configuration object is null", NO_APPLICABLE_CODE);
        }
        // we get the database informations
        final BDD db = configuration.getBdd();
        if (db == null) {
            throw new CstlServiceException("The configuration file does not contains a BDD object", NO_APPLICABLE_CODE);
        }
        try {
            this.source = db.getDataSource();
            // try if the connection is valid
            final Connection c = this.source.getConnection();
            c.close();
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
        sqlRequest = new StringBuilder("SELECT \"id\" , \"procedure\" FROM \"om\".\"observations\" WHERE ");
        if (ResponseModeType.RESULT_TEMPLATE.equals(requestMode)) {
            template = true;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initFilterGetResult(final String procedure, final QName resultModel) {
        sqlRequest = new StringBuilder("SELECT \"id\", \"time_begin\", \"time_end\" FROM \"om\".\"observations\" WHERE ");
        
        //we add to the request the property of the template
        sqlRequest.append("\"procedure\"='").append(procedure).append("'");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProcedure(final List<String> procedures, final List<ObservationOffering> offerings) {
        if (!procedures.isEmpty()) {
            sqlRequest.append(" ( ");
            for (String s : procedures) {
                if (s != null) {
                    sqlRequest.append(" \"procedure\"='").append(s).append("' OR ");
                }
            }
            sqlRequest.delete(sqlRequest.length() - 3, sqlRequest.length());
            sqlRequest.append(") ");
            firstFilter = false;
        } else if (!offerings.isEmpty()) {
            
            sqlRequest.append(" ( ");
            //if is not specified we use all the process of the offering
            for (ObservationOffering off : offerings) {
                for (String proc : off.getProcedures()) {
                    sqlRequest.append(" \"procedure\"='").append(proc).append("' OR ");
                }
            }
            sqlRequest.delete(sqlRequest.length() - 3, sqlRequest.length());
            sqlRequest.append(") ");
            firstFilter = false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setObservedProperties(final List<String> phenomenon) {
        if (!phenomenon.isEmpty()) {
            final StringBuilder sb = new StringBuilder();
            for (String p : phenomenon) {
                sb.append(" \"observed_property\"='").append(p).append("' OR ");

            }
            sb.delete(sb.length() - 3, sb.length());
            if (!firstFilter) {
                sqlRequest.append(" AND( ").append(sb).append(") ");
            } else {
                sqlRequest.append(sb);
                firstFilter = false;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFeatureOfInterest(final List<String> fois) {
        if (!fois.isEmpty()) {
            final StringBuilder sb = new StringBuilder();
            for (String foi : fois) {
                sb.append("(\"foi\"='").append(foi).append("') OR");
            }
            sb.delete(sb.length() - 3, sb.length());
            
            if (!firstFilter) {
                sqlRequest.append(" AND( ").append(sb).append(") ");
            } else {
                sqlRequest.append(sb);
                firstFilter = false;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeEquals(final Object time) throws CstlServiceException {
        if (time instanceof Period) {
            final Period tp    = (Period) time;
            final String begin = getTimeValue(tp.getBeginning().getPosition());
            final String end   = getTimeValue(tp.getEnding().getPosition());

            // we request directly a multiple observation or a period observation (one measure during a period)
            sqlRequest.append("AND (");
            sqlRequest.append(" \"time_begin\"='").append(begin).append("' AND ");
            sqlRequest.append(" \"time_end\"='").append(end).append("') ");

        // if the temporal object is a timeInstant
        } else if (time instanceof Instant) {
            final Instant ti      = (Instant) time;
            final String position = getTimeValue(ti.getPosition());
            sqlRequest.append("AND (");

            // case 1 a single observation
            sqlRequest.append("(\"time_begin\"='").append(position).append("' AND \"time_end\" IS NULL)");
            sqlRequest.append(" OR ");

            //case 2 multiple observations containing a matching value
            sqlRequest.append("(\"time_begin\"<='").append(position).append("' AND \"time_end\">='").append(position).append("'))");

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
        if (time instanceof Instant) {
            final Instant ti      = (Instant) time;
            final String position = getTimeValue(ti.getPosition());
            sqlRequest.append("AND (");

            // the single and multpile observations which begin after the bound
            sqlRequest.append("(\"time_begin\"<='").append(position).append("'))");

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
        if (time instanceof Instant) {
            final Instant ti      = (Instant) time;
            final String position = getTimeValue(ti.getPosition());
            sqlRequest.append("AND (");

            // the single and multpile observations which begin after the bound
            sqlRequest.append("(\"time_begin\">='").append(position).append("')");
            sqlRequest.append(" OR ");
            // the multiple observations overlapping the bound
            sqlRequest.append("(\"time_begin\"<='").append(position).append("' AND \"time_end\">='").append(position).append("'))");


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
        if (time instanceof Period) {
            final Period tp    = (Period) time;
            final String begin = getTimeValue(tp.getBeginning().getPosition());
            final String end   = getTimeValue(tp.getEnding().getPosition());
            sqlRequest.append("AND (");

            // the multiple observations included in the period
            sqlRequest.append(" (\"time_begin\">='").append(begin).append("' AND \"time_end\"<= '").append(end).append("')");
            sqlRequest.append(" OR ");
            // the single observations included in the period
            sqlRequest.append(" (\"time_begin\">='").append(begin).append("' AND \"time_begin\"<='").append(end).append("' AND \"time_end\" IS NULL)");
            sqlRequest.append(" OR ");
            // the multiple observations which overlaps the first bound
            sqlRequest.append(" (\"time_begin\"<='").append(begin).append("' AND \"time_end\"<= '").append(end).append("' AND \"time_end\">='").append(begin).append("')");
            sqlRequest.append(" OR ");
            // the multiple observations which overlaps the second bound
            sqlRequest.append(" (\"time_begin\">='").append(begin).append("' AND \"time_end\">= '").append(end).append("' AND \"time_begin\"<='").append(end).append("')");
            sqlRequest.append(" OR ");
            // the multiple observations which overlaps the whole period
            sqlRequest.append(" (\"time_begin\"<='").append(begin).append("' AND \"time_end\">= '").append(end).append("'))");


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
            final Connection c                    = source.getConnection();
            final Statement currentStatement      = c.createStatement();
            final ResultSet result                = currentStatement.executeQuery(sqlRequest.toString());
            while (result.next()) {
                results.add(new ObservationResult(result.getString(1),
                                                  result.getTimestamp(2),
                                                  result.getTimestamp(3)));
            }
            result.close();
            currentStatement.close();
            c.close();
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
    public Set<String> filterObservation() throws CstlServiceException {
        LOGGER.log(Level.FINER, "request:{0}", sqlRequest.toString());
        try {
            final Set<String> results        = new LinkedHashSet<String>();
            final Connection c               = source.getConnection();
            final Statement currentStatement = c.createStatement();
            final ResultSet result           = currentStatement.executeQuery(sqlRequest.toString());
            final List<String> procedures    = new ArrayList<String>();
            while (result.next()) {
                final String procedure = result.getString(2);
                if (!template || !procedures.contains(procedure)) {
                    results.add(result.getString(1));
                    procedures.add(procedure);
                }
            }
            result.close();
            currentStatement.close();
            c.close();
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
        return "Constellation O&M 2 Filter 0.9";
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
    public void setBoundingBox(final Envelope e) throws CstlServiceException {
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

    @Override
    public void setTimeLatest() throws CstlServiceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setTimeFirst() throws CstlServiceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setOfferings(final List<ObservationOffering> offerings) throws CstlServiceException {
        // not used in this implementations
    }
    
    @Override
    public boolean isDefaultTemplateTime() {
        return true;
    }
}
