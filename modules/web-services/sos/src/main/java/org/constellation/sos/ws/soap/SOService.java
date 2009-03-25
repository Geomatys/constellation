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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

// JAX-WS dependencies
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;

// JAXB dependencies
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

// Constellation dependencies
import org.constellation.ServiceDef;
import org.constellation.ws.CstlServiceException;
import org.constellation.observation.ObservationCollectionEntry;
import org.constellation.sml.AbstractSensorML;
import org.constellation.sos.v100.Capabilities;
import org.constellation.sos.v100.DescribeSensor;
import org.constellation.sos.v100.GetCapabilities;
import org.constellation.sos.v100.GetObservation;
import org.constellation.sos.v100.GetResult;
import org.constellation.sos.v100.GetResultResponse;
import org.constellation.sos.v100.InsertObservation;
import org.constellation.sos.v100.InsertObservationResponse;
import org.constellation.sos.v100.RegisterSensor;
import org.constellation.sos.v100.RegisterSensorResponse;
import org.constellation.sos.ws.SOSworker;


/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@WebService(name = "SOService")
@SOAPBinding(parameterStyle = ParameterStyle.BARE)
public class SOService {
    
    /**
     * use for debugging purpose
     */
    Logger logger = Logger.getLogger("org.constellation.sos");
    
    /**
     * A map containing the Capabilities Object already load from file.
     */
    private Map<String,Object> capabilities = new HashMap<String,Object>();
    
    /**
     * a service worker
     */
    private SOSworker worker;
    
    /**
     * The user directory where to store the configuration file on Unix platforms.
     */
    private static final String UNIX_DIRECTORY = ".sicade";

    /**
     * The user directory where to store the configuration file on Windows platforms.
     */
    private static final String WINDOWS_DIRECTORY = "Application Data\\Sicade";
    
    /**
     * A JAXB unmarshaller used to create java object from XML file.
     */
    private Unmarshaller unmarshaller;
    
    /**
     * Initialize the database connection.
     */
    public SOService() throws JAXBException, CstlServiceException {
       worker = new SOSworker(null);
       JAXBContext jbcontext = JAXBContext.newInstance("org.constellation.sos.v100:org.constellation.observation");
       unmarshaller = jbcontext.createUnmarshaller();
       //TODO find real url
       worker.setServiceURL("http://localhost:8080/SOServer/SOService");
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
            logger.info("received SOAP getCapabilities request");
            worker.setSkeletonCapabilities((Capabilities)getCapabilitiesObject());
             
            return worker.getCapabilities(requestCapabilities);
        } catch (CstlServiceException ex) {
            throw new SOServiceException(ex.getMessage(), ex.getExceptionCode().name(),
                                         ServiceDef.SOS_1_0_0.exceptionVersion.toString());
        } catch (JAXBException ex) {
            throw new SOServiceException(ex.getMessage(), ex.getErrorCode(), null);
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
            logger.info("received SOAP DescribeSensor request");
            return worker.describeSensor(requestDescSensor);
        } catch (CstlServiceException ex) {
            throw new SOServiceException(ex.getMessage(), ex.getExceptionCode().name(),
                                         ServiceDef.SOS_1_0_0.exceptionVersion.toString());
        }
    }
    
    
    /**
     * Web service operation whitch respond a collection of observation satisfying 
     * the restriction specified in the query.
     * 
     * @param requestObservation a document specifying the parameter of the request.
     * @throws SOServiceException
     */
    @WebMethod(action="getObservation")
    public ObservationCollectionEntry getObservation(@WebParam(name = "GetObservation") GetObservation requestObservation) throws SOServiceException {
        try {
            logger.info("received SOAP getObservation request");
            return (ObservationCollectionEntry) worker.getObservation(requestObservation);
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
            logger.info("received SOAP getResult request");
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
            logger.info("received SOAP registerSensor request");
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
            logger.info("received SOAP insertObservation request");
            return worker.insertObservation(requestInsObs);
        } catch (CstlServiceException ex) {
            throw new SOServiceException(ex.getMessage(), ex.getExceptionCode().name(),
                                         ServiceDef.SOS_1_0_0.exceptionVersion.toString());
        }
    }
    
    /**
     * Returns the file where to read the capabilities document for each service.
     * If no such file is found, then this method returns {@code null}.
     *
     * @param  version the version of the service.
     * @return The capabilities Object, or {@code null} if none.
     * @throws JAXBException
     */
    public Object getCapabilitiesObject() throws JAXBException {
       String fileName = "SOSCapabilities1.0.0.xml";
       
       if (fileName == null) {
           return null;
       } else {
           Object response = capabilities.get(fileName);
           if (response == null) {
           
               String home;
                    
               String env = System.getenv("CATALINA_HOME");
                // we get the configuration file
               File path = new File(env + "/sos_configuration/");     
               //we delete the /WS
               if (!path.isDirectory()) {
                    home = System.getProperty("user.home");
                    if (System.getProperty("os.name", "").startsWith("Windows")) {
                        path = new File(home, WINDOWS_DIRECTORY);
                    } else {
                        path = new File(home, UNIX_DIRECTORY);
                    }
                } 
            
               File f = new File(path, fileName);
               logger.info(f.toString());
               response = unmarshaller.unmarshal(f);
               capabilities.put(fileName, response);
           }
           
           return response;
        }
    }
}

