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

package org.constellation.sos.io.generic;

import javax.xml.namespace.QName;
import java.util.Map;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

// Constellation dependencies
import org.constellation.sos.factory.OMFactory;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.From;
import org.constellation.generic.database.FilterQuery;
import org.constellation.generic.database.FilterSelect;
import org.constellation.generic.database.Where;
import org.constellation.sos.io.ObservationResult;
import org.geotoolkit.sos.xml.v100.ObservationOfferingType;
import org.geotoolkit.sos.xml.v100.ResponseModeType;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.gml.xml.v311.EnvelopeType;
import org.geotoolkit.gml.xml.v311.ReferenceType;
import org.geotoolkit.gml.xml.v311.TimeInstantType;
import org.geotoolkit.gml.xml.v311.TimePeriodType;
import org.geotoolkit.observation.xml.v100.ProcessType;
import org.opengis.observation.Observation;
import static org.geotoolkit.sos.xml.v100.ResponseModeType.*;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import static org.constellation.sos.ws.Utils.*;
import static org.constellation.sos.ws.SOSConstants.*;

/**
 *
 * @author Guilhem Legal
 */
public class GenericObservationFilter extends AbstractGenericObservationFilter {

    /**
     * The properties file allowing to store the id mapping between physical and database ID.
     */
    protected Properties map;


    /**
     * Clone a  Generic Observation Filter for CSTL O&M datasource.
     * @param omFilter
     */
    public GenericObservationFilter(final GenericObservationFilter omFilter) {
        super(omFilter);
        this.map                       = omFilter.map;
    }

    /**
     * Build a new Generic Observation Filter for CSTL O&M datasource.
     *
     * @param observationIdBase
     * @param observationTemplateIdBase
     * @param map
     * @param configuration
     * @throws CstlServiceException
     */
    public GenericObservationFilter(final Automatic configuration, final Map<String, Object> properties) throws CstlServiceException {
        super(configuration, properties);
        this.map = (Properties) properties.get(OMFactory.IDENTIFIER_MAPPING);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void initFilterObservation(final ResponseModeType requestMode, final QName resultModel) {
        currentQuery              = new FilterQuery();
        final FilterSelect select = configurationQuery.getSelect("filterObservation");
        final From from           = configurationQuery.getFrom("observations");
        final Where where         = configurationQuery.getWhere("observationType");

        if (requestMode == INLINE) {
            where.replaceVariable("observationIdBase", observationIdBase, false);
        } else if (requestMode == RESULT_TEMPLATE) {
            where.replaceVariable("observationIdBase", observationTemplateIdBase, false);
        }
        currentQuery.addSelect(select);
        currentQuery.addFrom(from);
        currentQuery.addWhere(where);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initFilterGetResult(final Observation template, final QName resultModel) {
        currentQuery              = new FilterQuery();
        final FilterSelect select = configurationQuery.getSelect("filterResult");
        final From from           = configurationQuery.getFrom("observations");
        final Where where         = configurationQuery.getWhere(PROCEDURE);

        final ProcessType process = (ProcessType) template.getProcedure();
        where.replaceVariable(PROCEDURE, process.getHref(), true);
        currentQuery.addSelect(select);
        currentQuery.addFrom(from);
        currentQuery.addWhere(where);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProcedure(final List<String> procedures, final ObservationOfferingType off) {
        if (!procedures.isEmpty()) {
            for (String s : procedures) {
                if (s != null) {
                    String dbId = map.getProperty(s);
                    if (dbId == null) {
                        dbId = s;
                    }
                    final Where where = configurationQuery.getWhere(PROCEDURE);
                    where.replaceVariable(PROCEDURE, dbId, true);
                    currentQuery.addWhere(where);
                }
            }
        } else {
            //if is not specified we use all the process of the offering
            for (ReferenceType proc : off.getProcedure()) {
                 final Where where = configurationQuery.getWhere(PROCEDURE);
                 where.replaceVariable(PROCEDURE, proc.getHref(), true);
                 currentQuery.addWhere(where);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setObservedProperties(final List<String> phenomenon, final List<String> compositePhenomenon) {
        for (String p : phenomenon) {
            final Where where = configurationQuery.getWhere("simplePhenomenon");
            where.replaceVariable("phenomenon", p, true);
            currentQuery.addWhere(where);
        }
        for (String p : compositePhenomenon) {
            final Where where = configurationQuery.getWhere("compositePhenomenon");
            where.replaceVariable("phenomenon", p, true);
            currentQuery.addWhere(where);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFeatureOfInterest(final List<String> fois) {
        for (String foi : fois) {
            final Where where = configurationQuery.getWhere("foi");
            where.replaceVariable("foi", foi, true);
            currentQuery.addWhere(where);
        }
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

            final Where where       = configurationQuery.getWhere("tequalsTP");
            where.replaceVariable("begin", begin, true);
            where.replaceVariable("end", end, true);
            currentQuery.addWhere(where);

        // if the temporal object is a timeInstant
        } else if (time instanceof TimeInstantType) {
            final TimeInstantType ti = (TimeInstantType) time;
            final String position = getTimeValue(ti.getTimePosition());

            final Where where = configurationQuery.getWhere("tequalsTI");
            where.replaceVariable("position", position, true);
            currentQuery.addWhere(where);

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
            final String position = getTimeValue(ti.getTimePosition());
            
            final Where where = configurationQuery.getWhere("tbefore");
            where.replaceVariable("time", position, true);
            currentQuery.addWhere(where);

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
            
            final Where where        = configurationQuery.getWhere("tafter");
            where.replaceVariable("time", position, true);
            currentQuery.addWhere(where);

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

            final Where where = configurationQuery.getWhere("tduring");
            where.replaceVariable("begin", begin, true);
            where.replaceVariable("end", end, true);
            currentQuery.addWhere(where);

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
        final String request = currentQuery.buildSQLQuery();
        LOGGER.log(Level.INFO, "request:{0}", request);
        try {
            final List<ObservationResult> results = new ArrayList<ObservationResult>();
            final Connection connection           = acquireConnection();
            final Statement currentStatement      = connection.createStatement();
            final ResultSet result                = currentStatement.executeQuery(request);
            while (result.next()) {
                results.add(new ObservationResult(result.getString(1),
                                                  result.getTimestamp(2),
                                                  result.getTimestamp(3)));
            }
            result.close();
            currentStatement.close();
            connection.close();
            return results;

        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "SQLException while executing the query: {0}", request);
            throw new CstlServiceException("the service has throw a SQL Exception:" + ex.getMessage() + '\n' + "while executing the request:" + request,
                                          NO_APPLICABLE_CODE);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> filterObservation() throws CstlServiceException {
        final String request = currentQuery.buildSQLQuery();
        LOGGER.log(Level.INFO, "request:{0}", request);
        try {
            final List<String> results       = new ArrayList<String>();
            final Connection connection      = acquireConnection();
            final Statement currentStatement = connection.createStatement();
            final ResultSet result           = currentStatement.executeQuery(request);
            while (result.next()) {
                results.add(result.getString(1));
            }
            result.close();
            currentStatement.close();
            connection.close();
            return results;
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "SQLException while executing the query: {0}", request);
            throw new CstlServiceException("the service has throw a SQL Exception:" + ex.getMessage(),
                                          NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInfos() {
        return "Constellation Generic O&M Filter 0.7";
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
    public void setBoundingBox(EnvelopeType e) throws CstlServiceException {
        throw new CstlServiceException("SetBoundingBox is not supported by this ObservationFilter implementation.");
    }

}
