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
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

// Constellation dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.generic.filter.From;
import org.constellation.generic.filter.Query;
import org.constellation.generic.filter.Select;
import org.constellation.generic.filter.Where;
import org.constellation.gml.v311.EnvelopeEntry;
import org.constellation.gml.v311.ReferenceEntry;
import org.constellation.gml.v311.TimeInstantType;
import org.constellation.gml.v311.TimePeriodType;
import org.constellation.sos.io.ObservationFilter;
import org.constellation.sos.io.ObservationResult;
import org.constellation.sos.v100.ObservationOfferingEntry;
import org.constellation.sos.v100.ResponseModeType;
import org.constellation.ws.CstlServiceException;
import static org.constellation.sos.v100.ResponseModeType.*;
import static org.constellation.ows.OWSExceptionCode.*;
import static org.constellation.sos.ws.Utils.*;

/**
 *
 * @author Guilhem Legal
 */
public class GenericObservationFilter implements ObservationFilter {

    public final Query configurationQuery;

    public Query currentQuery;

    /**
     * The properties file allowing to store the id mapping between physical and database ID.
     */
    protected Properties map;

    /**
     * use for debugging purpose
     */
    protected Logger logger = Logger.getLogger("org.constellation.sos");

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

    public GenericObservationFilter(String observationIdBase, String observationTemplateIdBase, Properties map, Automatic configuration) throws CstlServiceException {
        this.observationIdBase         = observationIdBase;
        this.observationTemplateIdBase = observationTemplateIdBase;
        this.map                       = map;
        
        if (configuration == null) {
            throw new CstlServiceException("The configuration object is null", NO_APPLICABLE_CODE);
        }
        // we get the database informations
        BDD db = configuration.getBdd();
        if (db == null) {
            throw new CstlServiceException("The configuration file does not contains a BDD object", NO_APPLICABLE_CODE);
        }
        try {
            JAXBContext context = JAXBContext.newInstance("org.constellation.generic.filter");
            Unmarshaller unmarshaller = context.createUnmarshaller();
            File affinage = new File(configuration.getConfigurationDirectory(), "affinage.xml");
            if (affinage.exists()) {
                Object object = unmarshaller.unmarshal(affinage);
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
     * Initialize the query.
     */
    @Override
    public void initFilterObservation(ResponseModeType requestMode) {
        currentQuery  = new Query();
        Select select = configurationQuery.getSelect("filterObservation");
        From from     = configurationQuery.getFrom("observations");
        Where where   = configurationQuery.getWhere("observationType");

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
     * Initialize the query.
     */
    @Override
    public void initFilterGetResult(String procedure) {
        currentQuery  = new Query();
        Select select = configurationQuery.getSelect("filterResult");
        From from     = configurationQuery.getFrom("observations");
        Where where   = configurationQuery.getWhere("procedure");
        where.replaceVariable("procedure", procedure, true);
        currentQuery.addSelect(select);
        currentQuery.addFrom(from);
        currentQuery.addWhere(where);
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
        if (procedures.size() != 0) {
            for (String s : procedures) {
                if (s != null) {
                    String dbId = map.getProperty(s);
                    if (dbId == null) {
                        dbId = s;
                    }
                    Where where = configurationQuery.getWhere("procedure");
                    where.replaceVariable("procedure", dbId, true);
                    currentQuery.addWhere(where);
                }
            }
        } else {
            //if is not specified we use all the process of the offering
            for (ReferenceEntry proc : off.getProcedure()) {
                 Where where = configurationQuery.getWhere("procedure");
                 where.replaceVariable("procedure", proc.getHref(), true);
                 currentQuery.addWhere(where);
            }
        }
    }

    /**
     * Add some phenomenon filter to the request.
     *
     * @param phenomenon
     * @param compositePhenomenon
     */
    @Override
    public void setObservedProperties(List<String> phenomenon, List<String> compositePhenomenon) {
        for (String p : phenomenon) {
            Where where = configurationQuery.getWhere("simplePhenomenon");
            where.replaceVariable("phenomenon", p, true);
            currentQuery.addWhere(where);
        }
        for (String p : compositePhenomenon) {
            Where where = configurationQuery.getWhere("compositePhenomenon");
            where.replaceVariable("phenomenon", p, true);
            currentQuery.addWhere(where);
        }
    }

    /**
     * Add some sampling point filter to the request.
     *
     * @param phenomenon
     * @param compositePhenomenon
     */
    @Override
    public void setFeatureOfInterest(List<String> fois) {
        for (String foi : fois) {
            Where where = configurationQuery.getWhere("foi");
            where.replaceVariable("foi", foi, true);
            currentQuery.addWhere(where);
        }
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

            Where where = configurationQuery.getWhere("tequalsTP");
            where.replaceVariable("begin", begin, true);
            where.replaceVariable("end", end, true);
            currentQuery.addWhere(where);

        // if the temporal object is a timeInstant
        } else if (time instanceof TimeInstantType) {
            TimeInstantType ti = (TimeInstantType) time;
            String position = getTimeValue(ti.getTimePosition());

            Where where = configurationQuery.getWhere("tequalsTI");
            where.replaceVariable("position", position, true);
            currentQuery.addWhere(where);

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
            
            Where where = configurationQuery.getWhere("tbefore");
            where.replaceVariable("time", position, true);
            currentQuery.addWhere(where);

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
            
            Where where = configurationQuery.getWhere("tafter");
            where.replaceVariable("time", position, true);
            currentQuery.addWhere(where);

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

            Where where = configurationQuery.getWhere("tduring");
            where.replaceVariable("begin", begin, true);
            where.replaceVariable("end", end, true);
            currentQuery.addWhere(where);

        } else {
            throw new CstlServiceException("TM_During operation require TimePeriod!",
                    INVALID_PARAMETER_VALUE, "eventTime");
        }
    }

    @Override
    public List<ObservationResult> filterResult() throws CstlServiceException {
        String request = currentQuery.buildSQLQuery();
        logger.info("request:" + request);
        try {
            List<ObservationResult> results = new ArrayList<ObservationResult>();
            Statement currentStatement = connection.createStatement();
            ResultSet result = currentStatement.executeQuery(request);
            while (result.next()) {
                results.add(new ObservationResult(result.getString(1),
                                                  result.getTimestamp(2),
                                                  result.getTimestamp(3)));
            }
            result.close();
            currentStatement.close();
            return results;

        } catch (SQLException ex) {
            logger.severe("SQLExcpetion while executing the query: " + request);
            throw new CstlServiceException("the service has throw a SQL Exception:" + ex.getMessage(),
                                          NO_APPLICABLE_CODE);
        }

    }

    @Override
    public List<String> filterObservation() throws CstlServiceException {
        String request = currentQuery.buildSQLQuery();
        logger.info("request:" + request);
        try {
            List<String> results = new ArrayList<String>();
            Statement currentStatement = connection.createStatement();
            ResultSet result = currentStatement.executeQuery(request);
            while (result.next()) {
                results.add(result.getString(1));
            }
            result.close();
            currentStatement.close();
            return results;
        } catch (SQLException ex) {
            logger.severe("SQLException while executing the query: " + request);
            throw new CstlServiceException("the service has throw a SQL Exception:" + ex.getMessage(),
                                          NO_APPLICABLE_CODE);
        }
    }

    public String getInfos() {
        return "Constellation Generic O&M Filter 0.3";
    }

    public boolean isBoundedObservation() {
        return false;
    }

    public void setBoundingBox(EnvelopeEntry e) throws CstlServiceException {
        throw new CstlServiceException("SetBoundingBox is not supported by this ObservationFilter implementation.");
    }
}
