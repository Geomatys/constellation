/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.sos.ws.soap;

// JDK dependencies
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
import org.constellation.configuration.ServiceConfigurer;
import org.constellation.sos.configuration.SOSConfigurer;
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
    protected SOSworker createWorker(final String id) {
        return new SOSworker(id);
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
    protected Class<? extends ServiceConfigurer> getConfigurerClass() {
        return SOSConfigurer.class;
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
        } catch (JAXBException | SOAPException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            throw new RuntimeException(ex.getMessage());
        }
    }
}

