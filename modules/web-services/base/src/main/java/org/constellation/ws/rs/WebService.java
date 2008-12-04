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

import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javax.servlet.ServletContext;

// jersey dependencies
import com.sun.jersey.api.core.HttpContext;
import java.io.StringWriter;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;

// JAXB xml binding dependencies
import javax.xml.bind.JAXBContext;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;

// Constellation dependencies
import org.constellation.ws.WebServiceException;
import org.constellation.ws.AbstractRequest;

import static org.constellation.ws.ExceptionCode.*;

/**
 * Main class for all web services.
 *
 * @author Guilhem Legal
 * @author Cédric Briançon
 */
public abstract class WebService {
    /**
     * Default logger for all web services.
     */
    protected static final Logger LOGGER = Logger.getLogger("org.constellation.ws.rs");

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
    protected Unmarshaller unmarshaller;

    /**
     * A JAXB marshaller used to transform the java object in XML String.
     */
    protected Marshaller marshaller;

    /**
     * The name of the service (WMS, WCS,...)
     */
    private final String service;

    /**
     * Specifies if the process is running on a Glassfish application server.
     */
    protected static Boolean isGlassfish = null;

     /**
     * the service URL.
     */
    private String serviceURL;

    /**
     * The http context containing the request parameter
     */
    @Context
    protected UriInfo context;

    /**
     * A servlet context used for access deployed file
     */
    @Context
    protected ServletContext servletContext;

    /**
     * The HTTP context used for get informations on the client which send the request.
     */
    @Context
    protected HttpContext httpContext;

    /**
     * Initialize the basic attribute of a web service.
     *
     * @param service The initials of the web service (CSW, WMS, WCS, SOS, ...)
     */
    public WebService(String service) {
        this.service = service;
        unmarshaller = null;
        serviceURL   = null;
    }
    
    /**
     * Initialize the JAXB context and build the unmarshaller/marshaller
     *
     * @param packagesName A list of package containing JAXB annoted classes.
     * @param rootNamespace The main namespace for all the document.
     */
    protected void setXMLContext(String packagesName, String rootNamespace) throws JAXBException {
        LOGGER.finer("SETTING XML CONTEXT: class " + this.getClass().getSimpleName() + '\n' +
                    " packages: " + packagesName);

        JAXBContext jbcontext = JAXBContext.newInstance(packagesName);
        unmarshaller = jbcontext.createUnmarshaller();
        marshaller = jbcontext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        setPrefixMapper(rootNamespace);
    }

    /**
     * Initialize the JAXB context and build the unmarshaller/marshaller
     *
     * @param classesName A list of JAXB annoted classes.
     * @param rootNamespace The main namespace for all the document.
     */
    protected void setXMLContext(String rootNamespace, Class<?>... classes) throws JAXBException {
        LOGGER.finer("SETTING XML CONTEXT: classes version");

        JAXBContext jbcontext = JAXBContext.newInstance(classes);
        unmarshaller = jbcontext.createUnmarshaller();
        marshaller = jbcontext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        setPrefixMapper(rootNamespace);
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
     * @throw WebServiceException
     */
    protected String getParameter(final String parameterName, final boolean mandatory)
                                                            throws WebServiceException
    {
        final MultivaluedMap parameters = context.getQueryParameters();
        final Set<String> keySet = parameters.keySet();
        final Iterator<String> it = keySet.iterator();

        boolean notFound = true;
        String s = null;

        while (notFound && it.hasNext()) {
            s = it.next();
            if (parameterName.equalsIgnoreCase(s)) {
                notFound = false;
                break;
            }
        }
        if (notFound) {
            if (mandatory) {
                throw new WebServiceException("The parameter " + parameterName + " must be specified",
                        MISSING_PARAMETER_VALUE);
            }
            return null;
        } else {
            final String value = (String) ((LinkedList) parameters.get(s)).get(0);
            if ((value == null || value.equals("")) && mandatory) {
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
                throw new WebServiceException("The parameter " + parameterName + " should have a value",
                        INVALID_PARAMETER_VALUE);
            } else {
                return value;
            }
        }
    }

    /**
     * Extract all The parameters from the query and write it in the console.
     * It is a debug method.
     *
     */
    protected void writeParameters() throws WebServiceException {
        final MultivaluedMap parameters = context.getQueryParameters();
        if (!parameters.isEmpty())
            LOGGER.info(parameters.toString());
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
     * @throw WebServiceException
     */
    protected Object getComplexParameter(String parameterName, boolean mandatory) throws WebServiceException {

        try {
            MultivaluedMap parameters = context.getQueryParameters();
            LinkedList<String> list = (LinkedList) parameters.get(parameterName);
            if (list == null) {
                list = (LinkedList) parameters.get(parameterName.toLowerCase());
                if (list == null) {
                    if (!mandatory) {
                        return null;
                    } else {
                        throw new WebServiceException("The parameter " + parameterName + " must be specified",
                                       MISSING_PARAMETER_VALUE);

                    }
                }
            }
            StringReader sr = new StringReader(list.get(0));
            Object result = unmarshaller.unmarshal(sr);
            return result;
        } catch (JAXBException ex) {
             throw new WebServiceException("the xml object for parameter" + parameterName + " is not well formed:" + '\n' +
                            ex, INVALID_PARAMETER_VALUE);
        }
    }

   /**
     * Treat the incomming GET request.
     *
     * @return an image or xml response.
     * @throw JAXBException
     */
    @GET
    public Response doGET() throws JAXBException  {
        return treatIncomingRequest(null);
    }

    /**
     * Treat the incomming POST request encoded in kvp.
     * for each parameters in the request it fill the httpContext.
     *
     * @return an image or xml response.
     * @throw JAXBException
     */
    @POST
    @Consumes("application/x-www-form-urlencoded")
    public Response doPOSTKvp(String request) throws JAXBException  {
        final StringTokenizer tokens = new StringTokenizer(request, "&");
        String log = "";
        while (tokens.hasMoreTokens()) {
            final String token = tokens.nextToken().trim();
            String paramName  = token.substring(0, token.indexOf('='));
            String paramValue = token.substring(token.indexOf('=')+ 1);
            log += "put: " + paramName + "=" + paramValue + '\n';
            context.getQueryParameters().add(paramName, paramValue);
        }
        LOGGER.info("request POST kvp: " + request + '\n' + log);
        return treatIncomingRequest(null);
    }

    /**
     * Treat the incomming POST request encoded in xml.
     *
     * @return an image or xml response.
     * @throw JAXBException
     */
    @POST
    @Consumes("*/xml")
    public Response doPOSTXml(InputStream is) throws JAXBException  {
        LOGGER.info("request POST xml: ");
        if (unmarshaller != null) {
            Object request = null;
            try {
                request = unmarshaller.unmarshal(is);
            } catch (UnmarshalException e) {
                LOGGER.severe("UNMARSHALL EXCEPTION: " + e.getMessage());
                final StringWriter sw = new StringWriter();
                final Object obj = launchException("The XML request is not valid", INVALID_REQUEST.name(), null);
                marshaller.marshal(obj, sw);
                return Response.ok(sw, "text/xml").build();
            }

            if (request != null && request instanceof AbstractRequest) {
                AbstractRequest ar = (AbstractRequest) request;
                context.getQueryParameters().add("VERSION", ar.getVersion());
            }
            return treatIncomingRequest(request);
        } else {
            return Response.ok("This service is not running", "text/plain").build();
        }
    }

    /**
     * Treat the incomming POST request encoded in text plain.
     *
     * @return an xml exception report.
     * @throw JAXBException
     */
    @POST
    @Consumes("text/plain")
    public Response doPOSTPlain(InputStream is) throws JAXBException  {
        LOGGER.severe("request POST plain sending Exception");
        Object obj = launchException("This content type is not allowed try text/xml or application/x-www-form-urlencoded",
                                          INVALID_REQUEST.name(), null);
        return Response.ok(obj, "text/xml").build();
    }

   
    /**
     * Treat the incomming request and call the right function.
     *
     * @param objectRequest if the server receive a POST request in XML,
     *        this object contain the request. Else for a GET or a POST kvp
     *        request this param is {@code null}
     *
     * @return an image or xml response.
     * @throw JAXBException
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
    protected abstract Object launchException(String message, String codeName, String locator);

   /**
     * Return a file located in WEB-INF deployed directory.
     *
     * @param fileName The name of the file requested.
     * @return The specified file.
     */
    protected File getFile(String fileName) {
         File path;

         //we try to get the deployed "WEB-INF" directory
         String home = servletContext.getRealPath("WEB-INF");

         if (home == null || !(path = new File(home)).isDirectory()) {
            path = getSicadeDirectory();
         }
         if (fileName != null)
            return new File(path, fileName);
         else return path;
    }

    /**
     * Returns the context value for the key specified, or {@code null} if not found
     * in this context.
     *
     * @param key The key to search in the context.
     * @param context The context which to consider.
     */
    private static Object getContextProperty(final String key, final javax.naming.Context context) {
        Object value = null;
        try {
            value = context.lookup(key);
        } catch (NamingException n) {
            // Do nothing, the key is not found in the context and the value is still null.
        } finally {
            return value;
        }
    }

    /**
     * Get the value for a property defines in the JNDI context chosen.
     *
     * @param propGroup If you use Glassfish, you have to specify the name of the resource that
     *                  owns the property you wish to get. Otherwise you should specify {@code null}
     * @param propName  The name of the property to get.
     * @return The property value defines in the context, or {@code null} if no property of this name
     *         is defined in the resource given in parameter.
     * @throws NamingException if an error occurs while initializing the context, or if an empty value
     *                         for propGroup has been passed while using a Glassfish application server.
     */
    public static String getPropertyValue(final String propGroup, final String propName) throws NamingException {
        final InitialContext ctx = new InitialContext();
        if (isGlassfish == null) {
            isGlassfish = (System.getProperty("domain.name") != null) ? true : false;
        }
        if (isGlassfish) {
            if (propGroup == null) {
                throw new NamingException("The coverage property group is not specified.");
            }
            final Reference props = (Reference) getContextProperty(propGroup, ctx);
            if (props == null) {
                throw new NamingException("The coverage property group specified does not exist.");
            }
            final RefAddr permissionAddr = (RefAddr) props.get(propName);
            if (permissionAddr != null) {
                return (String) permissionAddr.getContent();
            }
            return null;
        } else {
            final javax.naming.Context envContext = (javax.naming.Context) ctx.lookup("java:/comp/env");
            return (String) getContextProperty(propName, envContext);
        }
    }

    /**
     * Return the ".sicade" directory.
     *
     * @return The ".sicade" directory containing .
     */
    public static File getSicadeDirectory() {
        File sicadeDirectory;
        String home = System.getProperty("user.home");

        if (System.getProperty("os.name", "").startsWith("Windows")) {
             sicadeDirectory = new File(home, WINDOWS_DIRECTORY);
        } else {
             sicadeDirectory = new File(home, UNIX_DIRECTORY);
        }
        return sicadeDirectory;
    }

    /**
     * Return the service url obtain by the first request made.
     *
     * @return the service url.
     */
    protected String getServiceURL() {
        if (serviceURL == null) {
            serviceURL = context.getBaseUri().toString();
        }
        return serviceURL;
    }

    /**
     * A utility method whitch replace the special character.
     *
     * @param s the string to clean.
     * @return a String without special character.
     */
    protected String cleanSpecialCharacter(String s) {
        if (s != null) {
            s = s.replace('é', 'e');
            s = s.replace('è', 'e');
            s = s.replace('à', 'a');
            s = s.replace('É', 'E');
        }
        return s;
    }

    /**
     * Transform an exception code into the OWS specification.
     * Example : MISSING_PARAMETER_VALUE become MissingParameterValue.
     *
     * @param code
     * @return
     */
    protected String transformCodeName(String code) {
        String result = "";
        final String prefix = code.charAt(0) + "";
        while (code.indexOf('_') != -1) {
            final String tmp = code.substring(0, code.indexOf('_')).toLowerCase();
            result += tmp.replace(tmp.charAt(0), prefix.charAt(0));
            code = code.substring(code.indexOf('_') + 1, code.length());
        }
        code = code.toLowerCase();
        result += code.replace(code.charAt(0), prefix.charAt(0));
        return result;
    }

    /**
     * Set the prefixMapper for the marshaller.
     * The root namespace specified will have no prefix.
     *
     * @param rootNamespace The main namespace of all the produced XML document (xmlns = rootNamespace)
     */
    protected void setPrefixMapper(String rootNamespace) throws PropertyException {
        NamespacePrefixMapperImpl prefixMapper = new NamespacePrefixMapperImpl(rootNamespace);
        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", prefixMapper);
    }
}
