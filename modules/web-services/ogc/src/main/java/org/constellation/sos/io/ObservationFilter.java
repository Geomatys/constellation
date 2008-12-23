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

import java.sql.Timestamp;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.constellation.gml.v311.TimePositionType;
import org.constellation.sos.ObservationOfferingEntry;
import org.constellation.sos.ResponseModeType;
import org.constellation.ws.WebServiceException;
import static org.constellation.ows.OWSExceptionCode.*;

/**
 *
 * @author Guilhem Legal
 */
public abstract class ObservationFilter {

    /**
     * use for debugging purpose
     */
    protected Logger logger = Logger.getLogger("org.constellation.sos.io");

    /**
     * The base for observation id.
     */
    protected String observationIdBase;

    /**
     * The base for observation id.
     */
    protected String observationTemplateIdBase;

    /**
     * The properties file allowing to store the id mapping between physical and database ID.
     */
    protected Properties map;


    /**
     *
     */
    public ObservationFilter(String observationIdBase, String observationTemplateIdBase, Properties map) {
        this.observationIdBase = observationIdBase;
        this.observationTemplateIdBase = observationTemplateIdBase;
        this.map = map;
    }

    /**
     * Initialize the query.
     */
    public abstract void initFilterObservation(ResponseModeType requestMode);

    /**
     * Initialize the query.
     */
    public abstract void initFilterGetResult(String procedure);

    /**
     * Add some procedure filter to the request.
     * if the list of procedure ID is empty it add all the offering procedure.
     *
     * @param procedures
     * @param off
     */
    public abstract void setProcedure(List<String> procedures, ObservationOfferingEntry off);

    /**
     * Add some phenomenon filter to the request.
     *
     * @param phenomenon
     * @param compositePhenomenon
     */
    public abstract void setObservedProperties(List<String> phenomenon, List<String> compositePhenomenon);

    /**
     * Add some sampling point filter to the request.
     *
     * @param phenomenon
     * @param compositePhenomenon
     */
    public abstract void setFeatureOfInterest(List<String> fois);

    /**
     * Add a TM_Equals filter to the current request.
     * 
     * @param time
     * @throws org.constellation.ws.WebServiceException
     */
    public abstract void setTimeEquals(Object time) throws WebServiceException;

    /**
     * Add a TM_Before filter to the current request.
     *
     * @param time
     * @throws org.constellation.ws.WebServiceException
     */
    public abstract void setTimeBefore(Object time) throws WebServiceException;

    /**
     * Add a TM_After filter to the current request.
     *
     * @param time
     * @throws org.constellation.ws.WebServiceException
     */
    public abstract void setTimeAfter(Object time) throws WebServiceException;

    /**
     * Add a TM_During filter to the current request.
     *
     * @param time
     * @throws org.constellation.ws.WebServiceException
     */
    public abstract void setTimeDuring(Object time) throws WebServiceException;

    /**
     * Execute the current query and return a list of observation result.
     * 
     * @return
     * @throws org.constellation.ws.WebServiceException
     */
    public abstract List<ObservationResult> filterResult() throws WebServiceException;

    /**
     * Execute the current query and return a list of observation ID.
     * @return
     * @throws org.constellation.ws.WebServiceException
     */
    public abstract List<String> filterObservation() throws WebServiceException;
    
    /**
     * return a SQL formatted timestamp
     *
     * @param time a GML time position object.
     */
    protected String getTimeValue(TimePositionType time) throws WebServiceException {
        if (time != null && time.getValue() != null) {
            String value = time.getValue();
            value = value.replace("T", " ");

            //we delete the data after the second
            if (value.indexOf('.') != -1) {
                value = value.substring(0, value.indexOf('.'));
            }
             try {
                 //here t is not used but it allow to verify the syntax of the timestamp
                 Timestamp t = Timestamp.valueOf(value);
                 return t.toString();

             } catch(IllegalArgumentException e) {
                throw new WebServiceException("Unable to parse the value: " + value + '\n' +
                                                 "Bad format of timestamp: accepted format yyyy-mm-jjThh:mm:ss.msmsms.",
                                                 INVALID_PARAMETER_VALUE, "eventTime");
             }
          } else {
            String locator;
            if (time == null)
                locator = "Timeposition";
            else
                locator = "TimePosition value";
            throw new  WebServiceException("bad format of time, " + locator + " mustn't be null",
                                              MISSING_PARAMETER_VALUE, "eventTime");
          }
    }

    public class ObservationResult {

        public String resultID;

        public Timestamp beginTime;

        public Timestamp endTime;

        public ObservationResult(String resultID, Timestamp beginTime, Timestamp endTime) {
            this.beginTime = beginTime;
            this.endTime   = endTime;
            this.resultID  = resultID;
        }
    }
}
