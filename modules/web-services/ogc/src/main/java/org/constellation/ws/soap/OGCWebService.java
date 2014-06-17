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
package org.constellation.ws.soap;

// J2SE dependencies
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

// Constellation dependencies
import org.constellation.ServiceDef.Specification;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.WSEngine;
import org.constellation.ws.WebServiceUtilities;
import org.constellation.ws.Worker;
import org.constellation.xml.PrefixMappingInvocationHandler;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

// Geotoolkit dependencies
import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.admin.ServiceBusiness;
import org.constellation.admin.SpringHelper;
import org.opengis.util.CodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXParseException;


// GeoAPI dependencies

/**
 * Abstract parent SOAP facade for all OGC web services in Constellation.
 * <p>
 * This class
 * </p>
 * <p>
 * The Open Geospatial Consortium (OGC) has defined a number of web services for
 * geospatial data such as:
 * <ul>
 *   <li><b>CSW</b> -- Catalog Service for the Web</li>
 *   <li><b>WCS</b> -- Web Coverage Service</li>
 *   <li><b>SOS</b> -- Sensor Observation Service</li>
 * </ul>
 * Many of these Web Services have been defined to work with SOAP based HTTP
 * message exchange; this class provides base functionality for those services.
 * </p>
 *
 * @version $Id$
 *
 * @author Guilhem Legal (Geomatys)
 * @since 0.7
 */
public abstract class OGCWebService<W extends Worker> implements Provider<SOAPMessage> {//Source>  {

    /**
     * use for debugging purpose
     */
    protected static final Logger LOGGER = Logging.getLogger("org.constellation.ws.soap");

    private final Specification specification;
    
    protected static final QName SENDER_CODE = new QName("http://www.w3.org/2003/05/soap-envelope", "Sender");
    protected static final QName RECEIVER_CODE = new QName("http://www.w3.org/2003/05/soap-envelope", "Receiver");
    /**
     * A pool of JAXB unmarshaller used to create Java objects from XML files.
     */
    private MarshallerPool marshallerPool;

    @Resource
    private volatile WebServiceContext context;

    @Inject
    private ServiceBusiness serviceBusiness;
    
    /**
     * Initialize the basic attributes of a web serviceType.
     *
     */
    public OGCWebService(final Specification spec) {
        LOGGER.log(Level.INFO, "Starting the SOAP {0} service facade.\n", spec.name());
        this.specification = spec;
        SpringHelper.injectDependencies(this);
        WSEngine.registerService(specification.name(), "SOAP", getWorkerClass(), getConfigurerClass());

        /*
         * build the map of Workers, by scanning the sub-directories of its
         * service directory.
         */
        if (!WSEngine.isSetService(specification.name())) {
            buildWorkerMap();
        } else {
            LOGGER.log(Level.INFO, "Workers already set for {0}", specification.name());
        }
    }
    
    /**
     * Initialize the JAXB context.
     */
    protected synchronized void setXMLContext(final MarshallerPool pool) {
        LOGGER.finer("SETTING XML CONTEXT: marshaller Pool version");
        marshallerPool = pool;
    }
    
    protected synchronized MarshallerPool getMarshallerPool() {
        return marshallerPool;
    }

    /**
     * Scan the configuration directory to instantiate Web service workers.
     */
    private void buildWorkerMap() {
        final Map<String, Worker> workersMap = new HashMap<>();
        for (String serviceID : serviceBusiness.getServiceIdentifiers(specification.name())) {
            final W newWorker = createWorker(serviceID);
            workersMap.put(serviceID, newWorker);
        }
        WSEngine.setServiceInstances(specification.name(), workersMap);
    }

    /**
     * Build a new instance of Web service worker with the specified configuration directory
     *
     * @param instanceDirectory The configuration directory of the instance.
     * @return
     */
    protected abstract W createWorker(final String identifier);

    /**
     * @return the worker binding class of the current service.
     */
    protected abstract Class getWorkerClass();

    /**
     * @return the {@link org.constellation.configuration.ServiceConfigurer} class implementation.
     */
    protected abstract Class getConfigurerClass();

    /**
     * extract the service URL (before serviceName/serviceID?)
     * @return
     */
    protected String getServiceURL() {
        final HttpServletRequest request =   (HttpServletRequest) context.getMessageContext().get(MessageContext.SERVLET_REQUEST);
        String url = "";
        if (request != null) {
            url = request.getRequestURL().toString();
            url = url.substring(0, url.lastIndexOf('/'));
            url = url.substring(0, url.lastIndexOf('/') + 1);
        } else {
            LOGGER.warning("uable to find the service URL");
        }
        return url;
    }

    /**
     * Extract the instance ID from the URL.
     *
     * @return
     */
    private String extractWorkerID() {
        final String pathInfo            = (String) context.getMessageContext().get(MessageContext.PATH_INFO);
        final HttpServletRequest request = (HttpServletRequest) context.getMessageContext().get(MessageContext.SERVLET_REQUEST);
        if (request != null) {
            final String url = request.getRequestURL().toString();
            return url.substring(url.lastIndexOf('/') + 1);
        } else if (pathInfo != null) {
            return pathInfo.substring(pathInfo.lastIndexOf('/') + 1);
        } else {
            LOGGER.severe("Unable to extract the servletRequest");
            return null;
        }
    }
    
    /**
     * This method is used in adition to a 
     * @SchemaValidation(handler = ValidationHandler.class) annotations on a JAX-WS service class
     * 
     * @throws CstlServiceException 
     */
    protected void verifyValidation() throws CstlServiceException {
        final SAXParseException e = (SAXParseException) context.getMessageContext().get(ValidationHandler.ERROR);
        if (e != null) {
            String errorMsg = e.getMessage();
            if (errorMsg == null) {
                if (e.getCause() != null && e.getCause().getMessage() != null) {
                    errorMsg = e.getCause().getMessage();
                }
            }
            final CodeList codeName;
            if (errorMsg != null && errorMsg.startsWith("unexpected element")) {
                codeName = OPERATION_NOT_SUPPORTED;
            } else {
                codeName = INVALID_REQUEST;
            }
            final String locator = WebServiceUtilities.getValidationLocator(errorMsg, WebServiceUtilities.DUMMY_MAPPING);

            throw new CstlServiceException("The XML request is not valid.\nCause:" + errorMsg, codeName, locator);
        }
    }

    /**
     * Return the current worker specified by the URL.
     *
     * @return
     * @throws CstlServiceException
     */
    protected W getCurrentWorker() throws CstlServiceException {
        final String serviceID = extractWorkerID();
        if (serviceID == null || !WSEngine.serviceInstanceExist(specification.name(), serviceID)) {
            LOGGER.log(Level.WARNING, "Received request on undefined instance identifier:{0}", serviceID);
            final Set<String> instanceNames = WSEngine.getInstanceNames(specification.name());
            final String msg;
            if (serviceID == null) {
                msg = "You must specify an instance id.\n available instance:" + instanceNames;
            } else {
                msg = "Undefined instance id.\n available instance:" + instanceNames;
            }
            throw new CstlServiceException(msg);
            // TODO return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            return (W) WSEngine.getInstance(specification.name(), serviceID);
        }
    }

    /**
     * Return the number of instance if the web-service
     */
    protected int getWorkerMapSize() {
        return WSEngine.getInstanceSize(specification.name());
    }

    @PreDestroy
    public void destroy() {
        LOGGER.log(Level.INFO, "Shutting down the SOAP {0} service facade.", specification.name());
        WSEngine.destroyInstances(specification.name());
    }

    @Override
    public SOAPMessage invoke(final SOAPMessage requestMsg) {
        final Map<String, String> prefixMapping = new LinkedHashMap<>();
        try {

            final W worker = getCurrentWorker();
            worker.setServiceUrl(getServiceURL());
            
            /*
             * Unmarshal Request
             */
            final Unmarshaller ummarshaller = marshallerPool.acquireUnmarshaller();
            final Node requestNode = requestMsg.getSOAPBody().extractContentAsDocument();
            Object request;
            if (worker.isRequestValidationActivated()) {
                final List<Schema> schemas = worker.getRequestValidationSchema();
                for (Schema schema : schemas) {
                    ummarshaller.setSchema(schema);
                }
                request = unmarshallRequestWithMapping(ummarshaller, requestNode, prefixMapping);
            } else {
                request = unmarshallRequest(ummarshaller, requestNode);
            }
            
            if (request instanceof JAXBElement) {
                request = ((JAXBElement) request).getValue();
            }
            marshallerPool.recycle(ummarshaller);
            
            final Object result = treatIncomingRequest(request, worker);
            
            final Marshaller m = marshallerPool.acquireMarshaller();
            final DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
            final Document resultNode             = dfactory.newDocumentBuilder().newDocument();
            m.marshal(result, resultNode);
            marshallerPool.recycle(m);
            
            final MessageFactory factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
            final SOAPMessage response = factory.createMessage();
            response.getSOAPBody().addDocument(resultNode);

            return response;
        } catch (CstlServiceException e) {
            return processExceptionResponse(e.getMessage(), e.getExceptionCode().name(), e.getLocator());
        } catch (JAXBException e) {
            String errorMsg = e.getMessage();
            if (errorMsg == null) {
                if (e.getCause() != null && e.getCause().getMessage() != null) {
                    errorMsg = e.getCause().getMessage();
                } else if (e.getLinkedException() != null && e.getLinkedException().getMessage() != null) {
                    errorMsg = e.getLinkedException().getMessage();
                }
            }
            final String codeName;
            if (errorMsg != null && errorMsg.startsWith("unexpected element")) {
                codeName = OPERATION_NOT_SUPPORTED.name();
            } else {
                codeName = INVALID_REQUEST.name();
            }
            final String locator = WebServiceUtilities.getValidationLocator(errorMsg, prefixMapping);

            return processExceptionResponse("The XML request is not valid.\nCause:" + errorMsg, codeName, locator);
        } catch (SOAPException e) {
            return processExceptionResponse(e.getMessage(), NO_APPLICABLE_CODE.name(), null);
        }  catch (ParserConfigurationException e) {
            return processExceptionResponse(e.getMessage(), NO_APPLICABLE_CODE.name(), null);
        }
    }
    
    /**
     * A method simply unmarshalling the request with the specified unmarshaller from the specified inputStream.
     * can be overriden by child class in case of specific extractionfrom the stream.
     *
     * @param unmarshaller A JAXB Unmarshaller correspounding to the service context.
     * @param is The request input stream.
     * @return
     * @throws JAXBException
     */
    protected Object unmarshallRequest(final Unmarshaller unmarshaller, final Node is) throws JAXBException, CstlServiceException {
        return unmarshaller.unmarshal(is);
    }
    
    protected Object unmarshallRequestWithMapping(final Unmarshaller unmarshaller, final Node is, final Map<String, String> prefixMapping) throws JAXBException {
        try {
            final DOMSource source = new DOMSource(is);
            final XMLEventReader rootEventReader    = XMLInputFactory.newInstance().createXMLEventReader(source);
            final XMLEventReader eventReader        = (XMLEventReader) Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class[]{XMLEventReader.class}, new PrefixMappingInvocationHandler(rootEventReader, prefixMapping));

            return unmarshaller.unmarshal(eventReader);
        } catch (XMLStreamException ex) {
            throw new JAXBException(ex);
        }
    }
    
    protected abstract SOAPMessage processExceptionResponse(final String message, final String code, final String locator);
    
    
    /**
     * Treat the incoming request and call the right function.
     *
     * @param objectRequest if the server receive a POST request in XML,
     *        this object contain the request. Else for a GET or a POST kvp
     *        request this parameter is {@code null}
     *
     * @param worker the selected worker on which apply the request.
     *
     * @return an xml response.
     */
    protected abstract Object treatIncomingRequest(final Object objectRequest,final  W worker) throws CstlServiceException;
}
