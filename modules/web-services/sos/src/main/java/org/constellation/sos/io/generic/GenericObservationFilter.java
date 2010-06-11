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

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

// JAXB dependencies
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

// Constellation dependencies
import javax.xml.namespace.QName;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.generic.database.From;
import org.constellation.generic.filter.Query;
import org.constellation.generic.filter.Select;
import org.constellation.generic.database.Where;
import org.constellation.sos.io.ObservationFilter;
import org.constellation.sos.io.ObservationResult;
import org.geotoolkit.sos.xml.v100.ObservationOfferingEntry;
import org.geotoolkit.sos.xml.v100.ResponseModeType;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.gml.xml.v311.EnvelopeEntry;
import org.geotoolkit.gml.xml.v311.ReferenceEntry;
import org.geotoolkit.gml.xml.v311.TimeInstantType;
import org.geotoolkit.gml.xml.v311.TimePeriodType;
import org.geotoolkit.observation.xml.v100.ProcessEntry;
import org.opengis.observation.Observation;
import static org.geotoolkit.sos.xml.v100.ResponseModeType.*;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import static org.constellation.sos.ws.Utils.*;
import static org.constellation.sos.ws.SOSConstants.*;

/**
 *
 * @author Guilhem Legal
 */
public class GenericObservationFilter implements ObservationFilter {

    private final Query configurationQuery;

    private Query currentQuery;

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

    /**
     *
     */
    private final Connection connection;

    public GenericObservationFilter(GenericObservationFilter omFilter) {
        this.observationIdBase         = omFilter.observationIdBase;
        this.observationTemplateIdBase = omFilter.observationTemplateIdBase;
        this.map                       = omFilter.map;
        this.configurationQuery        = omFilter.configurationQuery;
        this.connection                = omFilter.connection;
    }

    public GenericObservationFilter(String observationIdBase, String observationTemplateIdBase, Properties map, Automatic configuration) throws CstlServiceException {
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
            final JAXBContext context = JAXBContext.newInstance("org.constellation.generic.filter");
            final Unmarshaller unmarshaller = context.createUnmarshaller();
            final File affinage = new File(configuration.getConfigurationDirectory(), "affinage.xml");
            if (affinage.exists()) {
                final Object object = unmarshaller.unmarshal(affinage);
                if (object instanceof Query)
                    this.configurationQuery = (Query) object;
                else
                    throw new CstlServiceException("Invalid content in affinage.xml", NO_APPLICABLE_CODE);
            } else {
                throw new CstlServiceException("Unable to find affinage.xml", NO_APPLICABLE_CODE);
            }
            this.connection = db.getConnection();
        } catch (JAXBException ex) {
            throw new CstlServiceException("JAXBException in genericObservationFilter constructor", NO_APPLICABLE_CODE);
        } catch (SQLException ex) {
            throw new CstlServiceException("SQLException while initializing the observation filter:" +'\n'+
                                           "cause:" + ex.getMessage(), NO_APPLICABLE_CODE);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void initFilterObservation(ResponseModeType requestMode, QName resultModel) {
        currentQuery        = new Query();
        final Select select = configurationQuery.getSelect("filterObservation");
        final From from     = configurationQuery.getFrom("observations");
        final Where where   = configurationQuery.getWhere("observationType");

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
    public void initFilterGetResult(Observation template, QName resultModel) {
        currentQuery        = new Query();
        final Select select = configurationQuery.getSelect("filterResult");
        final From from     = configurationQuery.getFrom("observations");
        final Where where   = configurationQuery.getWhere(PROCEDURE);

        final ProcessEntry process = (ProcessEntry) template.getProcedure();
        where.replaceVariable(PROCEDURE, process.getHref(), true);
        currentQuery.addSelect(select);
        currentQuery.addFrom(from);
        currentQuery.addWhere(where);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProcedure(List<String> procedures, ObservationOfferingEntry off) {
        if (procedures.size() != 0) {
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
            for (ReferenceEntry proc : off.getProcedure()) {
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
    public void setObservedProperties(List<String> phenomenon, List<String> compositePhenomenon) {
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
    public void setFeatureOfInterest(List<String> fois) {
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
    public void setTimeEquals(Object time) throws CstlServiceException {
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
    public void setTimeBefore(Object time) throws CstlServiceException  {
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
    public void setTimeAfter(Object time) throws CstlServiceException {
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
    public void setTimeDuring(Object time) throws CstlServiceException {
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
        LOGGER.info("request:" + request);
        try {
            final List<ObservationResult> results = new ArrayList<ObservationResult>();
            final Statement currentStatement      = connection.createStatement();
            final ResultSet result                = currentStatement.executeQuery(request);
            while (result.next()) {
                results.add(new ObservationResult(result.getString(1),
                                                  result.getTimestamp(2),
                                                  result.getTimestamp(3)));
            }
            result.close();
            currentStatement.close();
            return results;

        } catch (SQLException ex) {
            LOGGER.severe("SQLExcpetion while executing the query: " + request);
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
        LOGGER.info("request:" + request);
        try {
            final List<String> results       = new ArrayList<String>();
            final Statement currentStatement = connection.createStatement();
            final ResultSet result           = currentStatement.executeQuery(request);
            while (result.next()) {
                results.add(result.getString(1));
            }
            result.close();
            currentStatement.close();
            return results;
        } catch (SQLException ex) {
            LOGGER.severe("SQLException while executing the query: " + request);
            throw new CstlServiceException("the service has throw a SQL Exception:" + ex.getMessage(),
                                          NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInfos() {
        return "Constellation Generic O&M Filter 0.6";
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
    public void refresh() {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLoglevel(Level logLevel) {
         //do nothing
    }
}
