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
package org.constellation.wps.ws.soap;

// JDK dependencies

import org.constellation.ServiceDef;
import org.constellation.ServiceDef.Specification;
import org.constellation.wps.configuration.WPSConfigurer;
import org.constellation.wps.ws.WPSWorker;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.ServiceConfigurer;
import org.constellation.ws.soap.OGCWebService;
import org.geotoolkit.ows.xml.OWSExceptionCode;
import org.geotoolkit.wps.xml.v100.DescribeProcess;
import org.geotoolkit.wps.xml.v100.Execute;
import org.geotoolkit.wps.xml.v100.ExecuteResponse;
import org.geotoolkit.wps.xml.v100.GetCapabilities;
import org.geotoolkit.wps.xml.v100.ProcessDescriptions;
import org.geotoolkit.wps.xml.v100.WPSCapabilitiesType;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingType;
import java.util.logging.Level;

// JAX-WS dependencies
// Constellation dependencies
// Geotoolkit dependencies



/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@WebService(name = "WPSService")
@SOAPBinding(parameterStyle = ParameterStyle.BARE)
@BindingType(value="http://java.sun.com/xml/ns/jaxws/2003/05/soap/bindings/HTTP/")
@XmlSeeAlso({org.apache.sis.internal.jaxb.geometry.ObjectFactory.class})
public class WPSService extends OGCWebService<WPSWorker> {

    /**
     * Initialize the workers.
     */
    public WPSService() throws CstlServiceException {
       super(Specification.WPS);
       LOGGER.log(Level.INFO, "WPS SOAP service running ({0} instances)", getWorkerMapSize());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected WPSWorker createWorker(final String id) {
        return new WPSWorker(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class getWorkerClass() {
        return WPSWorker.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<? extends ServiceConfigurer> getConfigurerClass() {
        return WPSConfigurer.class;
    }

    /**
     * Web service operation describing the service and its capabilities.
     *
     * @param requestCapabilities
     * @throws WPSServiceException
     */
    @WebMethod(action="getCapabilities")
    @WebResult(name="Capabilities", targetNamespace="http://www.opengis.net/wps/1.0.0")
    public WPSCapabilitiesType getCapabilities(@WebParam(name = "GetCapabilities", targetNamespace="http://www.opengis.net/wps/1.0.0") GetCapabilities requestCapabilities) throws WPSServiceException  {
        try {
            LOGGER.info("received SOAP getCapabilities request");
            final WPSWorker worker = getCurrentWorker();
            worker.setServiceUrl(getServiceURL());

            return worker.getCapabilities(requestCapabilities);
        } catch (CstlServiceException ex) {
            throw new WPSServiceException(ex.getMessage(), ex.getExceptionCode().name(),
                                         ServiceDef.WPS_1_0_0.exceptionVersion.toString());
        }
    }

    /**
     * Web service operation which return an process description.
     *
     * @param requestDescProcess A document specifying the id of the process that we want the description.
     * @throws WPSServiceException
     */
    @WebMethod(action="describeProcess")
    @WebResult(name="ProcessDescriptions", targetNamespace="http://www.opengis.net/wps/1.0.0")
    public ProcessDescriptions describeProcess(@WebParam(name = "DescribeProcess", targetNamespace="http://www.opengis.net/wps/1.0.0") DescribeProcess requestDescProcess) throws WPSServiceException  {
        try {
            LOGGER.info("received SOAP DescribeProcess request");
            final WPSWorker worker = getCurrentWorker();
            worker.setServiceUrl(getServiceURL());
            return worker.describeProcess(requestDescProcess);
        } catch (CstlServiceException ex) {
            throw new WPSServiceException(ex.getMessage(), ex.getExceptionCode().name(),
                                         ServiceDef.WPS_1_0_0.exceptionVersion.toString());
        }
    }


    /**
     * Web service operation which execute a specific process.
     *
     * @param requestObservation a document specifying the parameter of the request.
     * @throws WPSServiceException
     */
    @WebMethod(action="Execute")
    @WebResult(name="ExecuteResponse", targetNamespace="http://www.opengis.net/wps/1.0.0")
    public ExecuteResponse Execute(@WebParam(name = "Execute", targetNamespace="http://www.opengis.net/wps/1.0.0") Execute requestExecute) throws WPSServiceException {
        try {
            LOGGER.info("received SOAP Execute request");
            final WPSWorker worker = getCurrentWorker();
            worker.setServiceUrl(getServiceURL());
            //if we receive a raw data output we throw an error
            if (requestExecute.getResponseForm() != null && requestExecute.getResponseForm().getRawDataOutput() != null) {
                throw new CstlServiceException("RawDataOutput is not allowed in SOAP protocol", OWSExceptionCode.INVALID_PARAMETER_VALUE, "responseForm");
            }
            return (ExecuteResponse) worker.execute(requestExecute);
        } catch (CstlServiceException ex) {
            throw new WPSServiceException(ex.getMessage(), ex.getExceptionCode().name(),
                                         ServiceDef.WPS_1_0_0.exceptionVersion.toString());
        }
    }

    @Override
    protected Object treatIncomingRequest(Object objectRequest, WPSWorker worker) throws CstlServiceException {
        throw new UnsupportedOperationException("TODO.");
    }

    @Override
    protected SOAPMessage processExceptionResponse(String message, String code, String locator) {
        throw new UnsupportedOperationException("TODO"); 
    }
}

