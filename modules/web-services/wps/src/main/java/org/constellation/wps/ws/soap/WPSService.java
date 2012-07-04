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
package org.constellation.wps.ws.soap;

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

// Constellation dependencies
import org.constellation.ServiceDef;
import org.constellation.ServiceDef.Specification;
import org.constellation.ws.CstlServiceException;
import org.constellation.wps.ws.WPSWorker;

// Geotoolkit dependencies
import org.constellation.ws.soap.OGCWebService;
import org.geotoolkit.ows.xml.OWSExceptionCode;
import org.geotoolkit.wps.xml.v100.*;



/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@WebService(name = "WPSService")
@SOAPBinding(parameterStyle = ParameterStyle.BARE)
@BindingType(value="http://java.sun.com/xml/ns/jaxws/2003/05/soap/bindings/HTTP/")
@XmlSeeAlso({org.geotoolkit.internal.jaxb.geometry.ObjectFactory.class})
public class WPSService extends OGCWebService<WPSWorker> {

    /**
     * Initialize the workers.
     */
    public WPSService() throws CstlServiceException {
       super(Specification.WPS);
       LOGGER.log(Level.INFO, "WPS SOAP service running ({0} instances)\n", getWorkerMapSize());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected WPSWorker createWorker(final File instanceDirectory) {
        return new WPSWorker(instanceDirectory.getName(), instanceDirectory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class getWorkerClass() {
        return WPSWorker.class;
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

}

