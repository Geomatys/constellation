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
package org.constellation.ws.rs;

import static org.constellation.ws.ExceptionCode.INVALID_PARAMETER_VALUE;
import static org.constellation.ws.ExceptionCode.INVALID_REQUEST;
import static org.constellation.ws.ExceptionCode.MISSING_PARAMETER_VALUE;
import static org.constellation.ws.ExceptionCode.OPERATION_NOT_SUPPORTED;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.validation.Schema;

import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.api.PropertyConstants;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.engine.register.jooq.tables.pojos.Property;
import org.constellation.engine.register.repository.PropertyRepository;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.MimeType;
import org.constellation.ws.WebServiceUtilities;
import org.constellation.xml.PrefixMappingInvocationHandler;
import org.geotoolkit.util.StringUtilities;
import org.glassfish.jersey.internal.util.collection.StringKeyIgnoreCaseMultivaluedMap;

// Jersey dependencies
// JAXB dependencies
// Constellation dependencies
// Geotoolkit dependencies
// Apache SIS dependencies

/**
 * Abstract parent of all REST facade classes for Constellation web services.
 * <p>
 * This class begins the handling of all REST message exchange processing. In
 * the REST style of web service, message parameters either are passed directly
 * as arguments to the query, e.g.<br>
 *   {@code protocol://some.url/service?param=value&param2=othervalue }<br>
 * or are passed as raw messages in the body of an HTTP POST message, for
 * example as Key-Value Pairs (KVP) or as XML documents.
 * </p>
 * <p>
 * <i>Note:</i> This use of the term REST does not imply the services are
 * RESTful; we use the term to distinguish these classes from the other facade
 * classes in Constellation which use SOAP to exchange messages in HTTP POST
 * exchanges and JAXB to automatically unmarshall those messages into Java
 * objects.
 * </p>
 * <p>
 * All incoming requests are handled by one of the {@code doGET} or
 * {@code doPOST*} methods. These methods handle the incoming requests by
 * ensuring all KVP parameters are in the {@code uriContext} object and all
 * other information is in a serializable object of the right kind. The methods
 * then call the abstract {@code treatIncomingRequest(Object)} passing any
 * serializable object as the method parameter. Sub-classes then handle the
 * request calling the {@code uriContext} object or using the method parameter
 * as needed.
 * </p>
 * <p>
 * Two other abstract methods need to be implemented by extending classes. The
 * method {@code destroy()} will be called prior to the container shutting down
 * the service providing an opportunity to log that event. The method
 * {@code launchException(..)} forms part of the Constellation exception
 * handling design.
 * </p>
 * <p>
 * TODO: explain the design for exception handling.
 * </p>
 * <p>
 * Concrete extensions of this class should, in their constructor, call one of
 * the {@code setXMLContext(..)} methods to initialize the JAXB context and
 * populate the {@code marshaller} and {@code unmarshaller} fields.
 * </p>
 * <p>
 * Classes extending this one provide the REST facade to Constellation. Most of
 * the concrete extensions of this class in Constellation itself implement the
 * logic of {@code treatIncomingRequest(Object)} by calling a appropriate
 * method in a {@code Worker} object. Those same methods in the {@code Worker}
 * object are also called by the classes implementing the SOAP facade, enabling
 * the re-use of the logic.
 * </p>
 *
 * @author Guilhem Legal (Geomatys)
 * @author Cédric Briançon (Geomatys)
 * @author Adrian Custer (Geomatys)
 * @since 0.1
 */
public abstract class WebService {
    /**
     * The default debugging logger for all web services.
     */
    protected static final Logger LOGGER = Logging.getLogger(WebService.class);

    private static final String PROPERTIES_URL = ConfigDirectory.getServiceURL();

    /**
     * Automatically set by Jersey.
     *
     * Provides access to the URI used in the method call, for instance, to
     * obtain the Key-Value Pairs in the request. The field is injected, thanks
     * to the annotation, when a request arrives.
     */
    @Context
    private volatile UriInfo uriContext;

    /**
     * Automatically set by Jersey.
     *
     * Used to communicate with the Servlet container, for example, to obtain
     * the MIME type of a file, to dispatch requests or to write to a log file.
     * The field is injected, thanks to the annotation, when a request arrives.
     */
    @Context
    private volatile ServletContext servletContext;

    /**
     * Automatically set by Jersey.
     *
     * The HTTP context used to get information about the client which sent the
     * request. The field is injected, thanks to the annotation, when a request
     * arrives.
     */
    @Context
    protected volatile HttpHeaders httpHeaders;

    /**
     * Automatically set by Jersey.
     *
     * The HTTP Servlet request used to get information about the client which
     * sent the request. The field is injected, thanks to the annotation, when a
     * request arrives.
     */
    @Context
    private volatile HttpServletRequest httpServletRequest;

    /**
     * If this flag is set the method logParameters() will write the entire request in the logs
     * instead of the parameters map.
     *
     * @deprecated move to worker
     */
    @Deprecated
    private boolean fullRequestLog = false;

    /**
     * The POST  kvp request parameters (one by thread)
     */
    private final ThreadLocal<MultivaluedMap<String, String>> postKvpParameters = new ThreadLocal<MultivaluedMap<String, String>>(){

        @Override
        protected MultivaluedMap initialValue() {
            return new StringKeyIgnoreCaseMultivaluedMap();
        }
    };

    /**
     * A pool of JAXB unmarshaller used to create Java objects from XML files.
     */
    private MarshallerPool marshallerPool;

    /**
     * Provides access to the URI used in the method call, for instance, to
     * obtain the Key-Value Pairs in the request.
     *
     * @return
     */
    protected final UriInfo getUriContext(){
        return uriContext;
    }

    /**
     * Used to communicate with the servlet container, for example, to obtain
     * the MIME type of a file, to dispatch requests or to write to a log file.
     *
     * @return
     */
    protected final ServletContext getServletContext(){
        return servletContext;
    }

    /**
     * The HTTP servlet request used to get information about the client which
     * sent the request.
     *
     * @return
     */
    protected final HttpServletRequest getHttpServletRequest(){
        return httpServletRequest;
    }

    /**
     * Treat the incoming request and call the right function in the worker.
     * <p>
     * The parent class will have processed the request sufficiently to ensure
     * all the relevant information is either in the {@code uriContext} field or
     * in the {@code Object} passed in as a parameter. Here we proceed a step
     * further to ensure the request is encapsulated in a Java object which we
     * then pass to the worker when calling the appropriate method.
     * </p>
     *
     * @param  objectRequest  an object encapsulating the request or {@code null}
     *                          if the request parameters are all in the
     *                          {@code uriContext} field.
     * @return a Response, either an image or an XML document depending on the
     *           user's request.
     */
    public abstract Response treatIncomingRequest(Object objectRequest);

    /**
     * This method is called at undeploy time
     */
    public void destroy(){
    }

    /**
     * build an service Exception and marshall it into a StringWriter
     *
     * @param message
     * @param codeName
     * @param locator
     * @return
     */
    protected abstract Response launchException(String message, String codeName, String locator);

    /**
     * Provide the marshaller pool.
     * Live it's instantiation to implementations.
     */
    protected synchronized MarshallerPool getMarshallerPool() {
        return marshallerPool;
    }

    /**
     * Initialize the JAXB context.
     */
    protected synchronized void setXMLContext(final MarshallerPool pool) {
        LOGGER.finer("SETTING XML CONTEXT: marshaller Pool version");
        marshallerPool = pool;
    }

    /**
     * Treat the incoming GET request.
     *
     * @return an image or xml response.
     */
    @GET
    public Response doGET() {
        return treatIncomingRequest(null);
    }

    /**
     * Treat the incoming POST request encoded in kvp.
     * for each parameters in the request it fill the httpContext.
     *
     * @param request
     * @return an image or xml response.
     */
    @POST
    @Consumes("application/x-www-form-urlencoded")
    public Response doPOSTKvp(final String request) {
        /**
         * decode string that can be encoded to utf8 url. ie : image%2Fpng will
         * be image/png
         */
        final String params = StringUtilities.decodeUTF8URL(request);
        final StringTokenizer tokens = new StringTokenizer(params, "&");
        final StringBuilder log = new StringBuilder("request POST kvp: ");
        log.append(params).append('\n');
        final MultivaluedMap kvpMap = new StringKeyIgnoreCaseMultivaluedMap();
        while (tokens.hasMoreTokens()) {
            final String token = tokens.nextToken().trim();
            final int equalsIndex = token.indexOf('=');
            final String paramName = token.substring(0, equalsIndex);
            final String paramValue = token.substring(equalsIndex + 1);
            // special case for XML request parameter
            if ("request".equalsIgnoreCase(paramName) && (paramValue.startsWith("<") || paramValue.startsWith("%3C"))) {
                final String xml = StringUtilities.decodeUTF8URL(paramValue);
                final InputStream in = new ByteArrayInputStream(xml.getBytes());
                return doPOSTXml(in);
            }
            log.append("put: ").append(paramName).append("=").append(paramValue).append('\n');
            kvpMap.add(paramName, paramValue);
        }
        postKvpParameters.set(kvpMap);
        try {
            LOGGER.info(log.toString());
            return treatIncomingRequest(null);
        } finally {
            postKvpParameters.set(new StringKeyIgnoreCaseMultivaluedMap());
        }
    }

    /**
     * Treat the incoming POST request encoded in xml.
     *
     * @return an image or xml response.
     * @throw JAXBException
     */
    @POST
    @Consumes("*/xml")
    public Response doPOSTXml(final InputStream is) {
        if (marshallerPool != null) {
            final Object request;
            final MarshallerPool pool;

            // we look for a configuration query
            final boolean requestValidationActivated;
            final List<Schema> schemas;
            final List<String> serviceId = getParameter("serviceId");
            if (serviceId != null && !serviceId.isEmpty()){
                requestValidationActivated = isRequestValidationActivated(serviceId.get(0));
                schemas                    = getRequestValidationSchema(serviceId.get(0));
                if ("admin".equals(serviceId.get(0))) {
                    pool = getConfigurationPool();
                } else {
                    pool = getMarshallerPool();
                }
            } else {
                schemas                    = null;
                pool                       = getMarshallerPool();
                requestValidationActivated = false;
            }

            final Map<String, String> prefixMapping = new LinkedHashMap<>();
            try {
                final Unmarshaller unmarshaller = pool.acquireUnmarshaller();
                if (requestValidationActivated) {
                    for (Schema schema : schemas) {
                        unmarshaller.setSchema(schema);
                    }
                    request = unmarshallRequestWithMapping(unmarshaller, is, prefixMapping);
                } else {
                    request = unmarshallRequest(unmarshaller, is);
                }
                pool.recycle(unmarshaller);
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

                return launchException("The XML request is not valid.\nCause:" + errorMsg, codeName, locator);
            } catch (CstlServiceException e) {

                return launchException(e.getMessage(), e.getExceptionCode().identifier(), e.getLocator());
            }

            /* parameters are now immutable
             *  if (request instanceof Versioned) {
                final Versioned ar = (Versioned) request;
                if (ar.getVersion() != null) {
                    getUriContext().getQueryParameters().add("VERSION", ar.getVersion().toString());
                }
            }*/
            return treatIncomingRequest(request);
        } else {
            return Response.ok("This service is not running", MimeType.TEXT_PLAIN).build();
        }
    }

    protected abstract boolean isRequestValidationActivated(final String workerID);

    protected abstract List<Schema> getRequestValidationSchema(final String workerID);

    /**
     * A method simply unmarshalling the request with the specified unmarshaller from the specified inputStream.
     * can be overriden by child class in case of specific extractionfrom the stream.
     *
     * @param unmarshaller A JAXB Unmarshaller correspounding to the service context.
     * @param is The request input stream.
     * @return
     * @throws JAXBException
     */
    protected Object unmarshallRequest(final Unmarshaller unmarshaller, final InputStream is) throws JAXBException, CstlServiceException {
        return unmarshaller.unmarshal(is);
    }

    protected Object unmarshallRequestWithMapping(final Unmarshaller unmarshaller, final InputStream is, final Map<String, String> prefixMapping) throws JAXBException {
        try {
            final XMLEventReader rootEventReader    = XMLInputFactory.newInstance().createXMLEventReader(is);
            final XMLEventReader eventReader        = (XMLEventReader) Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class[]{XMLEventReader.class}, new PrefixMappingInvocationHandler(rootEventReader, prefixMapping));

            return unmarshaller.unmarshal(eventReader);
        } catch (XMLStreamException ex) {
            throw new JAXBException(ex);
        }
    }

    /**
     * Treat the incoming POST request encoded in text plain.
     *
     * @return an xml exception report.
     */
    @POST
    @Consumes("text/plain")
    public Response doPOSTPlain(final InputStream is) {
        LOGGER.warning("request POST plain sending Exception");
        return launchException("The plain text content type is not allowed. Send " +
        		       "a message body with key=value pairs in the " +
        		       "application/x-www-form-urlencoded MIME type, or " +
        		       "an XML file using an application/xml or text/xml " +
        		       "MIME type.", INVALID_REQUEST.name(), null);
    }


    /**
     * Extracts the value, for a parameter specified, from a query.
     *
     * @param parameterName The name of the parameter.
     *
     * @return the parameter, or {@code null} if not specified.
     */
    private List<String> getParameter(final String parameterName) {
        final MultivaluedMap<String,String> parameters = uriContext.getQueryParameters();
        List<String> values = parameters.get(parameterName);

        //maybe the parameterName is case sensitive.
        if (values == null) {
            for(final Entry<String, List<String>> key : parameters.entrySet()){
                if(key.getKey().equalsIgnoreCase(parameterName)){
                    values = key.getValue();
                    break;
                }
            }
        }

        // look in the POST kvp parameter
        if (values == null) {
            values = postKvpParameters.get().get(parameterName);
        }

        // look in Path parameters
        if (values == null) {
            final MultivaluedMap<String,String> pathParameters = uriContext.getPathParameters();
            values = pathParameters.get(parameterName);
        }
        return values;
    }

    /**
     * Extracts the value, for a parameter specified, from a query.
     * If it is a mandatory one, and if it is {@code null}, it throws an exception.
     * Otherwise returns {@code null} in the case of an optional parameter not found.
     * The parameter is then parsed as boolean.
     *
     * @param parameterName The name of the parameter.
     * @param mandatory true if this parameter is mandatory, false if its optional.
      *
     * @return the parameter, or {@code null} if not specified and not mandatory.
     * @throw CstlServiceException
     */
    protected boolean getBooleanParameter(final String parameterName, final boolean mandatory) throws CstlServiceException {
        return Boolean.parseBoolean(getParameter(parameterName, mandatory));
    }

    /**
     * Extracts the value, for a parameter specified, from a query.
     * If it is a mandatory one, and if it is {@code null}, it throws an exception.
     * Otherwise returns {@code null} in the case of an optional parameter not found.
     *
     * @param parameterName The name of the parameter.
     * @param mandatory true if this parameter is mandatory, false if its optional.
      *
     * @return the parameter, or {@code null} if not specified and not mandatory.
     * @throw CstlServiceException
     */
    protected String getParameter(final String parameterName, final boolean mandatory) throws CstlServiceException {

        final List<String> values = getParameter(parameterName);
        if (values == null) {
            if (mandatory) {
                throw new CstlServiceException("The parameter " + parameterName + " must be specified",
                        MISSING_PARAMETER_VALUE, parameterName.toLowerCase());
            }
            return null;
        } else {
            final String value = values.get(0);
            if (value == null && mandatory) {
                throw new CstlServiceException("The parameter " + parameterName + " should have a value",
                        MISSING_PARAMETER_VALUE, parameterName.toLowerCase());
            } else {
                return value;
            }
        }
    }

    protected String getSafeParameter(final String parameterName) {
        final List<String> values = getParameter(parameterName);
        if (values == null) {
            return null;
        } else {
            return values.get(0);
        }
    }

    /**
     * Return a map of parameters put in the query.
     * @return
     */
    public MultivaluedMap<String, String> getParameters() {
        final MultivaluedMap<String, String> results = new StringKeyIgnoreCaseMultivaluedMap();

        // GET parameters
        for (final Entry<String, List<String>> entry : uriContext.getQueryParameters().entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                results.add(entry.getKey(), entry.getValue().get(0));
            }
        }

        // POST kvp parameters
        for (final Entry<String, List<String>> entry : postKvpParameters.get().entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                results.add(entry.getKey(), entry.getValue().get(0));
            }
        }
        return results;
    }

    /**
     * Extract all The parameters from the query and write it in the console.
     * It is a debug method.
     */
    protected void logParameters() {
        if (!fullRequestLog) {
            final MultivaluedMap<String,String> parameters = getUriContext().getQueryParameters();
            if (!parameters.isEmpty()) {
                // we don't write POST request with VERSION parameters automatically put
                if (parameters.size() != 1 || !parameters.containsKey("VERSION")) {
                    LOGGER.info(parameters.toString());
                }
            }
        } else {
            if (getUriContext().getRequestUri() != null) {
                LOGGER.info(getUriContext().getRequestUri().toString());
            }
        }
    }

    protected void logPostParameters(final Object request) {
        if (request != null) {
            final MarshallerPool pool = getMarshallerPool();
            try {
                final Marshaller m = pool.acquireMarshaller();
                m.marshal(request, System.out);
                pool.recycle(m);
            } catch (JAXBException ex) {
                LOGGER.log(Level.WARNING, "Error while marshalling the request", ex);
            }
        }
    }

    /**
     * Extract The complex parameter encoded in XML from the query.
     * If the parameter is mandatory and if it is null it throw an exception.
     * else it return null.
     *
     * @param parameterName The name of the parameter.
     * @param mandatory true if this parameter is mandatory, false if its optional.
     *
     * @return the parameter or null if not specified
     * @throw CstlServiceException
     */
    protected Object getComplexParameter(final String parameterName, final boolean mandatory) throws CstlServiceException {
        try {
            final String value = getParameter(parameterName, mandatory);
            final StringReader sr = new StringReader(value);
            final Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();
            Object result = unmarshaller.unmarshal(sr);
            marshallerPool.recycle(unmarshaller);
            if (result instanceof JAXBElement) {
                result = ((JAXBElement)result).getValue();
            }
            return result;
        } catch (JAXBException ex) {
             throw new CstlServiceException("The xml object for parameter " + parameterName + " is not well formed:" + '\n' +
                            ex, INVALID_PARAMETER_VALUE);
        }
    }

    /**
     * Return the service URL obtain by the first request made.
     * something like : http://localhost:8080/constellation/WS/
     *
     * @return the service uURL.
     */
    
    @Inject
    private PropertyRepository propertyRepository;
    
    protected String getServiceURL() {
        String result;
        Property service;
        if (propertyRepository != null) {
            service = propertyRepository.findOne(PropertyConstants.SERVICES_URL_KEY);
        } else {
            service = null;
        }
        if (service != null && (result = service.getValue()) != null && !result.isEmpty()) {
        } else if (PROPERTIES_URL != null && !PROPERTIES_URL.isEmpty()) {
            result = PROPERTIES_URL;
        /*
         * TODO : Reactivate the following line ? If Constellation is redirected, there's no way the last condition can
         * give us the right URI, but does the referer really provide WS URL ?
         */
//        } else if (httpHeaders != null && (result = httpHeaders.getHeaderString(com.google.common.net.HttpHeaders.REFERER)) != null && !result.isEmpty()) {
        } else {
            result = getUriContext().getBaseUri().toString();
        }
        return result;
    }

    /**
     * @return the fullRequestLog
     */
    public boolean isFullRequestLog() {
        return fullRequestLog;
    }

    /**
     * @param fullRequestLog the fullRequestLog to set
     */
    public void setFullRequestLog(final boolean fullRequestLog) {
        this.fullRequestLog = fullRequestLog;
    }

    /**
     * Return the Marshaller pool for configuration request
     * @return
     */
    protected abstract MarshallerPool getConfigurationPool();

}
