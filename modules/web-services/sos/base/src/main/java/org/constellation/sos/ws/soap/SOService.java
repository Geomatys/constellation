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
import java.io.File;
import java.util.logging.Level;
import javax.xml.bind.annotation.XmlSeeAlso;

// JAX-WS dependencies
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.xml.ws.BindingType;
import javax.xml.ws.ResponseWrapper;

// Constellation dependencies
import org.constellation.ServiceDef;
import org.constellation.ServiceDef.Specification;
import org.constellation.ws.CstlServiceException;
import org.constellation.sos.ws.SOSworker;

// Geotoolkit dependencies
import org.constellation.ws.soap.OGCWebService;
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
import org.geotoolkit.gml.xml.v311.AbstractFeatureType;
import org.geotoolkit.gml.xml.v311.AbstractTimePrimitiveType;
import org.geotoolkit.observation.xml.v100.ObservationCollectionType;
import org.geotoolkit.sos.xml.v100.GetFeatureOfInterest;
import org.geotoolkit.sos.xml.v100.GetFeatureOfInterestTime;


/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@WebService(name = "SOService")
@SOAPBinding(parameterStyle = ParameterStyle.BARE)
@BindingType(value="http://java.sun.com/xml/ns/jaxws/2003/05/soap/bindings/HTTP/")
@XmlSeeAlso({org.geotoolkit.sml.xml.v100.ObjectFactory.class,
             org.geotoolkit.sml.xml.v101.ObjectFactory.class,
             org.geotoolkit.sampling.xml.v100.ObjectFactory.class,
             org.geotoolkit.internal.jaxb.geometry.ObjectFactory.class})
public class SOService extends OGCWebService<SOSworker> {
    
    /**
     * Initialize the workers.
     */
    public SOService() throws CstlServiceException {
       super(Specification.SOS);
       LOGGER.log(Level.INFO, "SOS SOAP service running ({0} instances)\n", getWorkerMapSize());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SOSworker createWorker(final File instanceDirectory) {
        return new SOSworker(instanceDirectory.getName(), instanceDirectory);
    }
    
    /**
     * Web service operation describing the service and its capabilities.
     * 
     * @param requestCapabilities A document specifying the section you would obtain like :
     *      ServiceIdentification, ServiceProvider, Contents, operationMetadata.
     * @throws SOServiceException
     */
    @WebMethod(action="getCapabilities")
    @WebResult(name="Capabilities", targetNamespace="http://www.opengis.net/sos/1.0")
    public Capabilities getCapabilities(@WebParam(name = "GetCapabilities", targetNamespace="http://www.opengis.net/sos/1.0") GetCapabilities requestCapabilities) throws SOServiceException  {
        try {
            LOGGER.info("received SOAP getCapabilities request");
            final SOSworker worker = getCurrentWorker();
            worker.setServiceUrl(getServiceURL());
            
            return worker.getCapabilities(requestCapabilities);
        } catch (CstlServiceException ex) {
            throw new SOServiceException(ex.getMessage(), ex.getExceptionCode().name(),
                                         ServiceDef.SOS_1_0_0.exceptionVersion.toString());
        }
    }
    
    /**
     * Web service operation which return an sml description of the specified sensor.
     * 
     * @param requestDescSensor A document specifying the id of the sensor that we want the description.
     * @throws SOServiceException
     */
    @WebMethod(action="describeSensor")
    public AbstractSensorML describeSensor(@WebParam(name = "DescribeSensor", targetNamespace="http://www.opengis.net/sos/1.0") DescribeSensor requestDescSensor) throws SOServiceException  {
        try {
            LOGGER.info("received SOAP DescribeSensor request");
            final SOSworker worker = getCurrentWorker();
            worker.setServiceUrl(getServiceURL());
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
    @WebResult(name="ObservationCollection", targetNamespace="http://www.opengis.net/om/1.0")
    public ObservationCollectionType getObservation(@WebParam(name = "GetObservation", targetNamespace="http://www.opengis.net/sos/1.0") GetObservation requestObservation) throws SOServiceException {
        try {
            LOGGER.info("received SOAP getObservation request");
            final SOSworker worker = getCurrentWorker();
            worker.setServiceUrl(getServiceURL());
            return (ObservationCollectionType) worker.getObservation(requestObservation);
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
    public AbstractFeatureType getFeatureOfInterest(@WebParam(name = "GetFeatureOfInterest", targetNamespace="http://www.opengis.net/sos/1.0") GetFeatureOfInterest requestfeatureOfInterest) throws SOServiceException {
        try {
            LOGGER.info("received SOAP getfeatureOfInterest request");
            final SOSworker worker = getCurrentWorker();
            worker.setServiceUrl(getServiceURL());
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
    public AbstractTimePrimitiveType getFeatureOfInterestTime(@WebParam(name = "GetFeatureOfInterestTime", targetNamespace="http://www.opengis.net/sos/1.0") GetFeatureOfInterestTime requestfeatureOfInterestTime) throws SOServiceException {
        try {
            LOGGER.info("received SOAP getfeatureOfInterest request");
            final SOSworker worker = getCurrentWorker();
            worker.setServiceUrl(getServiceURL());
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
    @WebResult(name="GetResultResponse", targetNamespace="http://www.opengis.net/sos/1.0")
    public GetResultResponse getResult(@WebParam(name = "GetResult", targetNamespace="http://www.opengis.net/sos/1.0") GetResult requestResult) throws SOServiceException {
        try {
            LOGGER.info("received SOAP getResult request");
            final SOSworker worker = getCurrentWorker();
            worker.setServiceUrl(getServiceURL());
            return worker.getResult(requestResult);
        } catch (CstlServiceException ex) {
            throw new SOServiceException(ex.getMessage(), ex.getExceptionCode().name(),
                                         ServiceDef.SOS_1_0_0.exceptionVersion.toString());
        }
    }
    
    /**
     * Web service operation which register a Sensor in the SensorML database,
     * and initialize its observation by adding an observation template in the O&M database.
     *
     * @param requestRegSensor A request containing a SensorML File describing a Sensor,
     *                         and an observation template for this sensor.
     * @throws SOServiceException
     */
    @WebMethod(action="registerSensor")
    @WebResult(name="RegisterSensorResponse", targetNamespace="http://www.opengis.net/sos/1.0")
    public RegisterSensorResponse registerSensor(@WebParam(name = "RegisterSensor", targetNamespace="http://www.opengis.net/sos/1.0") RegisterSensor requestRegSensor) throws SOServiceException {
        try {
            LOGGER.info("received SOAP registerSensor request");
            final SOSworker worker = getCurrentWorker();
            worker.setServiceUrl(getServiceURL());
            return worker.registerSensor(requestRegSensor);
        } catch (CstlServiceException ex) {
            throw new SOServiceException(ex.getMessage(), ex.getExceptionCode().name(),
                                         ServiceDef.SOS_1_0_0.exceptionVersion.toString());
        }
    }
    
    /**
     * Web service operation which insert a new Observation for the specified sensor
     * in the O&M database.
     * 
     * @param requestInsObs an InsertObservation request containing an O&M object and a Sensor id.
     * @throws SOServiceException
     */
    @WebMethod(action="InsertObservation")
    @WebResult(name="InsertObservationResponse", targetNamespace="http://www.opengis.net/sos/1.0")
    public InsertObservationResponse insertObservation(@WebParam(name = "InsertObservation", targetNamespace="http://www.opengis.net/sos/1.0") InsertObservation requestInsObs) throws SOServiceException {
        try {
            LOGGER.info("received SOAP insertObservation request");
            final SOSworker worker = getCurrentWorker();
            worker.setServiceUrl(getServiceURL());
            return worker.insertObservation(requestInsObs);
        } catch (CstlServiceException ex) {
            throw new SOServiceException(ex.getMessage(), ex.getExceptionCode().name(),
                                         ServiceDef.SOS_1_0_0.exceptionVersion.toString());
        }
    }
    
}

