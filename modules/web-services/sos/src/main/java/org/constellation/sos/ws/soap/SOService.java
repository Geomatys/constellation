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
package org.constellation.sos.ws.soap;

// JDK dependencies
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlSeeAlso;

// JAX-WS dependencies
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;

// Constellation dependencies
import org.constellation.ServiceDef;
import org.constellation.ws.CstlServiceException;
import org.constellation.sos.ws.SOSworker;

// Geotoolkit dependencies
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.sos.xml.v100.Capabilities;
import org.geotoolkit.sos.xml.v100.DescribeSensor;
import org.geotoolkit.sos.xml.v100.GetCapabilities;
import org.geotoolkit.sos.xml.v100.GetObservation;
import org.geotoolkit.sos.xml.v100.GetResult;
import org.geotoolkit.sos.xml.v100.GetResultResponse;
import org.geotoolkit.sos.xml.v100.InsertObservation;
import org.geotoolkit.sos.xml.v100.InsertObservationResponse;
import org.geotoolkit.sos.xml.v100.RegisterSensor;
import org.geotoolkit.sos.xml.v100.RegisterSensorResponse;
import org.geotoolkit.gml.xml.v311.AbstractFeatureEntry;
import org.geotoolkit.gml.xml.v311.AbstractTimePrimitiveType;
import org.geotoolkit.observation.xml.v100.ObservationCollectionEntry;
import org.geotoolkit.sos.xml.v100.GetFeatureOfInterest;
import org.geotoolkit.sos.xml.v100.GetFeatureOfInterestTime;


/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@WebService(name = "SOService")
@SOAPBinding(parameterStyle = ParameterStyle.BARE)
@XmlSeeAlso({org.geotoolkit.sml.xml.v100.ObjectFactory.class,
             org.geotoolkit.sml.xml.v101.ObjectFactory.class,
             org.geotoolkit.sampling.xml.v100.ObjectFactory.class,
             org.geotoolkit.internal.jaxb.geometry.ObjectFactory.class})
public class SOService {
    
    /**
     * use for debugging purpose
     */
    private static final Logger LOGGER = Logger.getLogger("org.constellation.sos");
    
    /**
     * a service worker
     */
    private SOSworker worker;
    
    /**
     * Initialize the database connection.
     */
    public SOService() throws CstlServiceException {
       worker = new SOSworker("", null);

       //TODO find real url
       worker.setServiceUrl("http://localhost:8080/SOServer/SOService");
    }
    
    /**
     * Web service operation describing the service and its capabilities.
     * 
     * @param requestCapabilities A document specifying the section you would obtain like :
     *      ServiceIdentification, ServiceProvider, Contents, operationMetadata.
     * @throws SOServiceException
     */
    @WebMethod(action="getCapabilities")
    public Capabilities getCapabilities(@WebParam(name = "GetCapabilities") GetCapabilities requestCapabilities) throws SOServiceException  {
        try {
            LOGGER.info("received SOAP getCapabilities request");
            return worker.getCapabilities(requestCapabilities);
        } catch (CstlServiceException ex) {
            throw new SOServiceException(ex.getMessage(), ex.getExceptionCode().name(),
                                         ServiceDef.SOS_1_0_0.exceptionVersion.toString());
        }
    }
    
    /**
     * Web service operation whitch return an sml description of the specified sensor.
     * 
     * @param requestDescSensor A document specifying the id of the sensor that we want the description.
     * @throws SOServiceException
     */
    @WebMethod(action="describeSensor")
    public AbstractSensorML describeSensor(@WebParam(name = "DescribeSensor") DescribeSensor requestDescSensor) throws SOServiceException  {
        try {
            LOGGER.info("received SOAP DescribeSensor request");
            return worker.describeSensor(requestDescSensor);
        } catch (CstlServiceException ex) {
            throw new SOServiceException(ex.getMessage(), ex.getExceptionCode().name(),
                                         ServiceDef.SOS_1_0_0.exceptionVersion.toString());
        }
    }
    
    
    /**
     * Web service operation which respond a collection of observation satisfying 
     * the restriction specified in the query.
     * 
     * @param requestObservation a document specifying the parameter of the request.
     * @throws SOServiceException
     */
    @WebMethod(action="getObservation")
    public ObservationCollectionEntry getObservation(@WebParam(name = "GetObservation") GetObservation requestObservation) throws SOServiceException {
        try {
            LOGGER.info("received SOAP getObservation request");
            return (ObservationCollectionEntry) worker.getObservation(requestObservation);
        } catch (CstlServiceException ex) {
            throw new SOServiceException(ex.getMessage(), ex.getExceptionCode().name(),
                                         ServiceDef.SOS_1_0_0.exceptionVersion.toString());
        }
    }

    /**
     * Web service operation which respond a collection of featureOfInterest
     * the restriction specified in the query.
     *
     * @param requestfeatureOfInterest a document specifying the parameter of the request.
     * @throws SOServiceException
     */
    @WebMethod(action="getFeatureOfInterest")
    public AbstractFeatureEntry getFeatureOfInterest(@WebParam(name = "GetFeatureOfInterest") GetFeatureOfInterest requestfeatureOfInterest) throws SOServiceException {
        try {
            LOGGER.info("received SOAP getfeatureOfInterest request");
            return worker.getFeatureOfInterest(requestfeatureOfInterest);
        } catch (CstlServiceException ex) {
            throw new SOServiceException(ex.getMessage(), ex.getExceptionCode().name(),
                                         ServiceDef.SOS_1_0_0.exceptionVersion.toString());
        }
    }

    /**
     * Web service operation which respond a primitive time object
     * the restriction specified in the query.
     *
     * @param requestfeatureOfInterestTime a document specifying the parameter of the request.
     * @throws SOServiceException
     */
    @WebMethod(action="getFeatureOfInterestTime")
    public AbstractTimePrimitiveType getFeatureOfInterestTime(@WebParam(name = "GetFeatureOfInterestTime") GetFeatureOfInterestTime requestfeatureOfInterestTime) throws SOServiceException {
        try {
            LOGGER.info("received SOAP getfeatureOfInterest request");
            return worker.getFeatureOfInterestTime(requestfeatureOfInterestTime);
        } catch (CstlServiceException ex) {
            throw new SOServiceException(ex.getMessage(), ex.getExceptionCode().name(),
                                         ServiceDef.SOS_1_0_0.exceptionVersion.toString());
        }
    }
    
    /**
     * Web service operation
     *
     * @throws SOServiceException
     */
    @WebMethod(action="getResult")
    public GetResultResponse getResult(@WebParam(name = "GetResult") GetResult requestResult) throws SOServiceException {
        try {
            LOGGER.info("received SOAP getResult request");
            return worker.getResult(requestResult);
        } catch (CstlServiceException ex) {
            throw new SOServiceException(ex.getMessage(), ex.getExceptionCode().name(),
                                         ServiceDef.SOS_1_0_0.exceptionVersion.toString());
        }
    }
    
    /**
     * Web service operation whitch register a Sensor in the SensorML database, 
     * and initialize its observation by adding an observation template in the O&M database.
     *
     * @param requestRegSensor A request containing a SensorML File describing a Sensor,
     *                         and an observation template for this sensor.
     * @throws SOServiceException
     */
    @WebMethod(action="registerSensor")
    public RegisterSensorResponse registerSensor(@WebParam(name = "RegisterSensor") RegisterSensor requestRegSensor) throws SOServiceException {
        try {
            LOGGER.info("received SOAP registerSensor request");
            return worker.registerSensor(requestRegSensor);
        } catch (CstlServiceException ex) {
            throw new SOServiceException(ex.getMessage(), ex.getExceptionCode().name(),
                                         ServiceDef.SOS_1_0_0.exceptionVersion.toString());
        }
    }
    
    /**
     * Web service operation whitch insert a new Observation for the specified sensor
     * in the O&M database.
     * 
     * @param requestInsObs an InsertObservation request containing an O&M object and a Sensor id.
     * @throws SOServiceException
     */
    @WebMethod(action="InsertObservation")
    public InsertObservationResponse insertObservation(@WebParam(name = "InsertObservation") InsertObservation requestInsObs) throws SOServiceException {
        try {
            LOGGER.info("received SOAP insertObservation request");
            return worker.insertObservation(requestInsObs);
        } catch (CstlServiceException ex) {
            throw new SOServiceException(ex.getMessage(), ex.getExceptionCode().name(),
                                         ServiceDef.SOS_1_0_0.exceptionVersion.toString());
        }
    }
    
}

