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

// JAX-WS dependencies
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.xml.ws.BindingType;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.soap.Detail;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;

// Constellation dependencies
import org.constellation.ServiceDef.Specification;
import org.constellation.ws.CstlServiceException;
import org.constellation.sos.ws.SOSworker;

// Geotoolkit dependencies
import org.constellation.ws.soap.OGCWebService;
import org.geotoolkit.ows.xml.OWSExceptionCode;
import org.geotoolkit.ows.xml.v110.ExceptionType;
import org.geotoolkit.ows.xml.v110.ObjectFactory;
import org.geotoolkit.sos.xml.GetCapabilities;
import org.geotoolkit.sos.xml.GetObservation;
import org.geotoolkit.sos.xml.GetResult;
import org.geotoolkit.sos.xml.InsertObservation;
import org.geotoolkit.sos.xml.GetObservationById;
import org.geotoolkit.sos.xml.GetResultTemplate;
import org.geotoolkit.sos.xml.InsertResult;
import org.geotoolkit.sos.xml.InsertResultTemplate;
import org.geotoolkit.sos.xml.SOSMarshallerPool;
import org.geotoolkit.sos.xml.GetFeatureOfInterest;
import org.geotoolkit.sos.xml.v100.GetFeatureOfInterestTime;
import org.geotoolkit.swes.xml.DescribeSensor;
import org.geotoolkit.swes.xml.DeleteSensor;
import org.geotoolkit.swes.xml.InsertSensor;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@WebServiceProvider(serviceName = "SOServiceService", 
                    portName = "SOServicePort", 
                    targetNamespace = "http://soap.ws.sos.constellation.org/", 
                    wsdlLocation = "WEB-INF/wsdl/sos.wsdl")
@ServiceMode(value=Service.Mode.MESSAGE)
@SOAPBinding(parameterStyle = ParameterStyle.BARE)
@BindingType(value="http://java.sun.com/xml/ns/jaxws/2003/05/soap/bindings/HTTP/")
public class SOService extends OGCWebService<SOSworker> {

    /**
     * Initialize the workers.
     */
    public SOService() throws CstlServiceException {
       super(Specification.SOS);
       setXMLContext(SOSMarshallerPool.getInstance());
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
     * {@inheritDoc}
     */
    @Override
    protected Class getWorkerClass() {
        return SOSworker.class;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object treatIncomingRequest(final Object request, final SOSworker worker) throws CstlServiceException {
        
             if (request instanceof GetObservation) {
                final GetObservation go = (GetObservation) request;
                return worker.getObservation(go);
             }
             
             if (request instanceof GetObservationById) {
                final GetObservationById ds   = (GetObservationById)request;
                return worker.getObservationById(ds);
             }

             if (request instanceof DescribeSensor) {
                final DescribeSensor ds       = (DescribeSensor)request;
                return worker.describeSensor(ds);
             }

             if (request instanceof GetFeatureOfInterest) {
                final GetFeatureOfInterest gf     = (GetFeatureOfInterest)request;
                return worker.getFeatureOfInterest(gf);
             }

             if (request instanceof InsertObservation) {
                final InsertObservation is = (InsertObservation)request;
                return worker.insertObservation(is);
             }

             if (request instanceof GetResult) {
                final GetResult gr = (GetResult)request;
                return worker.getResult(gr);
             }

             if (request instanceof InsertSensor) {
                final InsertSensor rs = (InsertSensor)request;
                return worker.registerSensor(rs);
             }
             
             if (request instanceof DeleteSensor) {
                final DeleteSensor rs = (DeleteSensor)request;
                return worker.deleteSensor(rs);
             }
             
             if (request instanceof InsertResult) {
                final InsertResult rs = (InsertResult)request;
                return worker.insertResult(rs);
             }
             
             if (request instanceof InsertResultTemplate) {
                final InsertResultTemplate rs = (InsertResultTemplate)request;
                return worker.insertResultTemplate(rs);
             }
             
             if (request instanceof GetResultTemplate) {
                final GetResultTemplate rs = (GetResultTemplate)request;
                return worker.getResultTemplate(rs);
             }

             if (request instanceof GetFeatureOfInterestTime) {
                final GetFeatureOfInterestTime gft = (GetFeatureOfInterestTime)request;
                return worker.getFeatureOfInterestTime(gft);
             }

             if (request instanceof GetCapabilities) {
                final GetCapabilities gc = (GetCapabilities)request;
                return worker.getCapabilities(gc);
             }

             throw new CstlServiceException("The operation " + request + " is not supported by the service",
                     OWSExceptionCode.INVALID_PARAMETER_VALUE, "request");
    }
    
    @Override
    protected SOAPMessage processExceptionResponse(final String message, final String code, final String locator) {
        try {
            final ObjectFactory owsFactory = new ObjectFactory();
            final ExceptionType exceptionType = new ExceptionType(message, code, locator);
            
            final MessageFactory factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
            final SOAPMessage response = factory.createMessage();
            final Detail detail = response.getSOAPBody().addFault(SENDER_CODE, message).addDetail();
            
            final Marshaller m = getMarshallerPool().acquireMarshaller();
            m.marshal(owsFactory.createException(exceptionType), detail);
            getMarshallerPool().recycle(m);
                    
            //detail.appendChild(n);
            return response;
        } catch (JAXBException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            throw new RuntimeException(ex.getMessage());
        } catch (SOAPException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            throw new RuntimeException(ex.getMessage());
        }
    }
}

