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
package org.constellation.ws.rs;

import java.io.InputStream;
import java.io.StringReader;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;

// jersey dependencies
import com.sun.jersey.api.core.HttpContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map.Entry;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;

// JAXB xml binding dependencies
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;

// Constellation dependencies
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.MimeType;

// Geotoolkit dependencies
import org.geotoolkit.util.Versioned;
import org.geotoolkit.util.logging.Logging;
import org.geotoolkit.xml.MarshallerPool;

import static org.constellation.ws.ExceptionCode.*;
import org.xml.sax.SAXException;


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

    /**
     * Automaticly set by Jersey.
     *
     * Provides access to the URI used in the method call, for instance, to
     * obtain the Key-Value Pairs in the request. The field is injected, thanks
     * to the annotation, when a request arrives.
     */
    @Context
    private volatile UriInfo uriContext;

    /**
     * Automaticly set by Jersey.
     * 
     * Used to communicate with the servlet container, for example, to obtain
     * the MIME type of a file, to dispatch requests or to write to a log file.
     * The field is injected, thanks to the annotation, when a request arrives.
     */
    @Context
    private volatile ServletContext servletContext;

    /**
     * Automaticly set by Jersey.
     * 
     * The HTTP context used to get information about the client which sent the
     * request. The field is injected, thanks to the annotation, when a request
     * arrives.
     */
    @Context
    private volatile HttpContext httpContext;

    /**
     * If this flag is set the method logParameters() will path the entire request in the logs
     * instead of the parameters map.
     */
    private boolean fullRequestLog = false;

    /**
     * If this flag is set to true a validator is added to the XML request unmarshaller.
     */
    private boolean requestValidationActivated = false;

    /**
     * if the flag requestValidationActivated is set to true this attribute muste contain the main xsd file.
     */
    private String mainXsdPath = null;

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
     * The HTTP context used to get information about the client which sent the
     * request.
     * 
     * @return
     */
    protected final HttpContext getHttpContext(){
        return httpContext;
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
    public abstract Response treatIncomingRequest(Object objectRequest) throws JAXBException;

    /**
     * This method is called at undeploy time
     */
    public abstract void destroy();

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
     * Live it's instanciation to implementations.
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
     * @throw JAXBException
     */
    @GET
    public Response doGET() throws JAXBException  {
        return treatIncomingRequest(null);
    }

    /**
     * Treat the incoming POST request encoded in kvp.
     * for each parameters in the request it fill the httpContext.
     *
     * @return an image or xml response.
     * @throw JAXBException
     */
    @POST
    @Consumes("application/x-www-form-urlencoded")
    public Response doPOSTKvp(String request) throws JAXBException  {
        final StringTokenizer tokens = new StringTokenizer(request, "&");
        final StringBuilder log = new StringBuilder();
        while (tokens.hasMoreTokens()) {
            final String token      = tokens.nextToken().trim();
            final int equalsIndex   = token.indexOf('=');
            final String paramName  = token.substring(0, equalsIndex);
            final String paramValue = token.substring(equalsIndex + 1);
            log.append("put: ").append(paramName).append("=").append(paramValue).append('\n');
            getUriContext().getQueryParameters().add(paramName, paramValue);
        }
        LOGGER.info("request POST kvp: " + request + '\n' + log.toString());
        return treatIncomingRequest(null);
    }

    /**
     * Treat the incoming POST request encoded in xml.
     *
     * @return an image or xml response.
     * @throw JAXBException
     */
    @POST
    @Consumes("*/xml")
    public Response doPOSTXml(InputStream is) throws JAXBException  {
        if (marshallerPool != null) {
            Object request = null;
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = marshallerPool.acquireUnmarshaller();
                if (requestValidationActivated) {
                    try {
                        final SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
                        final Schema schema    = sf.newSchema(new URL(mainXsdPath));
                        unmarshaller.setSchema(schema);
                    } catch (SAXException ex) {
                        LOGGER.severe("SAX exception while adding the Validator to the JAXB unmarshaller");
                    } catch (MalformedURLException ex) {
                        LOGGER.severe("MalformedURL exception while adding the Validator to the JAXB unmarshaller");
                    }
                }
                request = unmarshaller.unmarshal(is);
            } catch (UnmarshalException e) {
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

                return launchException("The XML request is not valid.\nCause:" + errorMsg, codeName, null);
            } finally {
                if (unmarshaller != null)  {
                    marshallerPool.release(unmarshaller);
                }
            }

            if (request instanceof Versioned) {
                final Versioned ar = (Versioned) request;
                if (ar.getVersion() != null) {
                    getUriContext().getQueryParameters().add("VERSION", ar.getVersion().toString());
                }
            }

            if (request != null) {
                if (request instanceof JAXBElement) {
                    request = ((JAXBElement) request).getValue();
                } 
                LOGGER.log(Level.FINER, "request type:{0}", request.getClass().getName());
            }
            return treatIncomingRequest(request);
        } else {
            return Response.ok("This service is not running", MimeType.TEXT_PLAIN).build();
        }
    }

    /**
     * Treat the incoming POST request encoded in text plain.
     *
     * @return an xml exception report.
     */
    @POST
    @Consumes("text/plain")
    public Response doPOSTPlain(InputStream is) {
        LOGGER.severe("request POST plain sending Exception");
        return launchException("The plain text content type is not allowed. Send " +
        		       "a message body with key=value pairs in the " +
        		       "application/x-www-form-urlencoded MIME type, or " +
        		       "an XML file using an application/xml or text/xml " +
        		       "MIME type.", INVALID_REQUEST.name(), null);
    }

    /**
     * Extracts the value, for a parameter specified, from a query.
     * If it is a mandatory one, and if it is {@code null}, it throws an exception.
     * Otherwise returns {@code null} in the case of an optional paramater not found.
     *
     * @param parameterName The name of the parameter.
     * @param mandatory true if this parameter is mandatory, false if its optional.
      *
     * @return the parameter, or {@code null} if not specified and not mandatory.
     * @throw CstlServiceException
     */
    protected String getParameter(final String parameterName, final boolean mandatory)
                                                           throws CstlServiceException {

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

        // we look also in Path parameters
        final MultivaluedMap<String,String> pathParameters = uriContext.getPathParameters();
        values = pathParameters.get(parameterName);

        if (values == null) {
            for (final Entry<String,List<String>> entry : parameters.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(parameterName)) {
                    values = entry.getValue();
                    break;
                }
            }
        }

        if (values == null) {
            if (mandatory) {
                throw new CstlServiceException("The parameter " + parameterName + " must be specified",
                        MISSING_PARAMETER_VALUE, parameterName.toLowerCase());
            }
            return null;
        } else {
            final String value = values.get(0);
            if ((value == null || value.isEmpty()) && mandatory) {
                /* For the STYLE/STYLES parameters, they are mandatory in the GetMap request.
                 * Nevertheless we do not know what to put in for raster, that's why for these
                 * parameters we will just return the value, even if it is empty.
                 *
                 * According to the WMS standard, if STYLES="" is set, then the default style
                 * should be applied.
                 *
                 * todo: fix the style parameter.
                 */
                if (parameterName.equalsIgnoreCase("STYLE") ||
                    parameterName.equalsIgnoreCase("STYLES")) {
                    return value;
                }
                throw new CstlServiceException("The parameter " + parameterName + " should have a value",
                        MISSING_PARAMETER_VALUE, parameterName.toLowerCase());
            } else {
                return value;
            }
        }
    }

    /**
     * Return a map of parameters put in the query.
     * @return
     */
    public MultivaluedMap<String,String> getParameters(){
        return uriContext.getQueryParameters();
    }

    /**
     * Extract all The parameters from the query and write it in the console.
     * It is a debug method.
     */
    protected void logParameters() throws CstlServiceException {        
        if (!fullRequestLog) {
            final MultivaluedMap<String,String> parameters = getUriContext().getQueryParameters();
            if (!parameters.isEmpty())
                LOGGER.info(parameters.toString());
        } else {
            if (getUriContext().getRequestUri() != null) {
                LOGGER.info(getUriContext().getRequestUri().toString());
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
    protected Object getComplexParameter(final String parameterName, final boolean mandatory)
                                                                  throws CstlServiceException
    {
        Unmarshaller unmarshaller = null;
        try {
            unmarshaller = marshallerPool.acquireUnmarshaller();
            final MultivaluedMap<String,String> parameters = getUriContext().getQueryParameters();
            List<String> list = parameters.get(parameterName);
            if (list == null) {
                list = parameters.get(parameterName.toLowerCase());
                if (list == null) {
                    if (!mandatory) {
                        return null;
                    } else {
                        throw new CstlServiceException("The parameter " + parameterName + " must be specified",
                                       MISSING_PARAMETER_VALUE);
                    }
                }
            }
            final StringReader sr = new StringReader(list.get(0));
            Object result = unmarshaller.unmarshal(sr);
            if (result instanceof JAXBElement) {
                result = ((JAXBElement)result).getValue();
            }
            return result;
        } catch (JAXBException ex) {
             throw new CstlServiceException("The xml object for parameter " + parameterName + " is not well formed:" + '\n' +
                            ex, INVALID_PARAMETER_VALUE);
        } finally {
            if (unmarshaller != null) {
                marshallerPool.release(unmarshaller);
            }
        }
    }

    /**
     * Return the service url obtain by the first request made.
     * something like : http://localhost:8080/constellation/WS
     *
     * @return the service url.
     */
    protected String getServiceURL() {
        return getUriContext().getBaseUri().toString();
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
    public void setFullRequestLog(boolean fullRequestLog) {
        this.fullRequestLog = fullRequestLog;
    }

    public void activateRequestValidation(String mainXsdPath) {
        this.mainXsdPath                = mainXsdPath;
        this.requestValidationActivated = true;
    }
}
