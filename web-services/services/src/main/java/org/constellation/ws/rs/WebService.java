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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.servlet.ServletContext;

// jersey dependencies
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.HttpRequestContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;

// JAXB xml binding dependencies
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;

// Constellation dependencies
import org.constellation.coverage.web.Service;
import org.constellation.coverage.web.ServiceVersion;
import org.constellation.ows.v110.OWSWebServiceException;
import org.constellation.coverage.web.WMSWebServiceException;
import org.constellation.coverage.web.WebServiceException;
import org.constellation.coverage.wms.WMSExceptionCode;
import org.constellation.ows.AbstractDCP;
import org.constellation.ows.AbstractOnlineResourceType;
import org.constellation.ows.AbstractOperation;
import org.constellation.ows.AbstractRequest;
import org.constellation.ows.OWSExceptionCode;
import org.constellation.xacml.CstlPDP;
import org.constellation.xacml.PEP;
import org.constellation.xacml.SecurityActions;
import org.constellation.xacml.XACMLConstants;
import org.constellation.xacml.api.PolicyDecisionPoint;
import org.constellation.xacml.api.PolicyLocator;
import org.constellation.xacml.api.RequestContext;
import org.constellation.xacml.api.XACMLPolicy;
import org.constellation.xacml.factory.FactoryException;
import org.constellation.xacml.factory.PolicyFactory;
import org.constellation.xacml.locators.JBossPolicyLocator;
import org.constellation.xacml.policy.PolicyType;
import org.geotools.util.Version;


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
     * The supported versions supportd by this web service.
     */
    private final List<ServiceVersion> versions = new ArrayList<ServiceVersion>();

    /**
     * The current version used (since the last request)
     */
    private ServiceVersion currentVersion;

     /**
     * The version of the SLD profile for the WMS web service. fixed a 1.1.0 for now.
     */
    private final ServiceVersion sldVersion = new ServiceVersion(Service.WMS, "1.1.0");

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
     * A map containing the Capabilities Object already load from file.
     */
    private Map<String,Object> capabilities = new HashMap<String,Object>();

     /**
     * the service URL (used in getCapabilities document).
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
     * The last update sequence
     */
    private long lastUpdateSequence;

    /**
     * A Policy Decision Point allowing to secure the access to the resources.
     */
    private PolicyDecisionPoint PDP;

    /**
     * A Policy Enforcement Point allowing to secure the access to the resources.
     */
    private PEP pep;

    /**
     * Initialize the basic attribute of a web service.
     *
     * @param service The initials of the web service (CSW, WMS, WCS, SOS, ...)
     * @param versions A list of the supported version of this service.
     */
    public WebService(String service, ServiceVersion... versions) {
        this.service = service;

        for (final ServiceVersion element : versions) {
            this.versions.add(element);
        }
        if (this.versions.size() == 0)
             throw new IllegalArgumentException("A web service must have at least one version");
        else
            this.currentVersion = this.versions.get(0);
        unmarshaller = null;
        serviceURL   = null;
        ImageIO.scanForPlugins();
        initializePolicyDecisionPoint();
    }

    /**
     * Initialize the policy Decision Point and load all the correspounding policy file.
     */
    private void initializePolicyDecisionPoint() {
        //we create a new PDP
        PDP = new CstlPDP();

        //load the correspounding policy file
        final String url = "org/constellation/xacml/" + service.toLowerCase() + "Policy.xml";
        final InputStream is = SecurityActions.getResourceAsStream(url);
        if (is == null) {
            LOGGER.severe("unable to find the resource: " + url);
            return;
        }
        Object p = null;
        try {
            JAXBContext jbcontext = JAXBContext.newInstance("org.constellation.xacml.policy");
            Unmarshaller policyUnmarshaller = jbcontext.createUnmarshaller();
            p = policyUnmarshaller.unmarshal(is);
        } catch (JAXBException e) {
            LOGGER.severe("JAXB exception while unmarshalling policyFile " + service.toLowerCase() + "Policy.xml");
        }

        if (p instanceof JAXBElement) {
            p = ((JAXBElement)p).getValue();
        }

        if (p == null) {
            LOGGER.severe("the unmarshalled service policy is null.");
            return;
        } else if (!(p instanceof PolicyType)) {
            LOGGER.severe("unknow unmarshalled type for service policy file:" + p.getClass());
            return;
        }
        final PolicyType servicePolicy  = (PolicyType) p;

        try {
            final XACMLPolicy policy = PolicyFactory.createPolicy(servicePolicy);
            final Set<XACMLPolicy> policies = new HashSet<XACMLPolicy>();
            policies.add(policy);
            PDP.setPolicies(policies);

            //Add the basic locators also
            final PolicyLocator policyLocator = new JBossPolicyLocator();
            policyLocator.setPolicies(policies);

            //Locators need to be given the policies
            final Set<PolicyLocator> locators = new HashSet<PolicyLocator>();
            locators.add(policyLocator);
            PDP.setLocators(locators);

            pep = new PEP(PDP);

        } catch (FactoryException e) {
            LOGGER.severe("Factory exception while initializing Policy Decision Point: " + e.getMessage());
        }
        LOGGER.info("PDP succesfully initialized");
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
    protected void setXMLContext(String rootNamespace, Class... classes) throws JAXBException {
        LOGGER.finer("SETTING XML CONTEXT: classes version");

        JAXBContext jbcontext = JAXBContext.newInstance(classes);
        unmarshaller = jbcontext.createUnmarshaller();
        marshaller = jbcontext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        setPrefixMapper(rootNamespace);
    }

    /**
     *
     * Extract The parameter named parameterName from the query.
     * If the parameter is mandatory and if it is null it throw an exception.
     * else it return null.
     *
     * @param parameterName The name of the parameter.
     * @param mandatory true if this parameter is mandatory, false if its optional.
      *
     * @return the parameter or null if not specified and not mandatory.
     * @throw WebServiceException
     */
    protected String getParameter(String parameterName, boolean mandatory) throws WebServiceException {
        final MultivaluedMap parameters = context.getQueryParameters();
        final Set<String> keySet = parameters.keySet();
        final Iterator<String> it = keySet.iterator();

        boolean notFound = true;
        String s = null;

        while (notFound && it.hasNext()) {
            s = it.next();
            if (parameterName.equalsIgnoreCase(s)) {
                notFound = false;
            }
        }
        if (notFound) {
            if (mandatory) {
                throwException("The parameter " + parameterName + " must be specified",
                               "MISSING_PARAMETER_VALUE", parameterName);
                //never reach;
                throw new AssertionError();
            } else {
                return null;
            }
        } else {
             return (String) ((LinkedList) parameters.get(s)).get(0);
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
     * Verify the base parameter or each request.
     *
     * @param sld case 0: no sld.
     *            case 1: VERSION parameter for WMS version and SLD_VERSION for sld version.
     *            case 2: VERSION parameter for sld version.
     *
     * @throws org.constellation.coverage.web.WebServiceException
     */
    protected void verifyBaseParameter(int sld) throws WebServiceException {
        if (sld == 2) {
            if (!getParameter("VERSION", true).equals(sldVersion.toString())) {
                throwException("The parameter VERSION=" + sldVersion + " must be specified",
                               "MISSING_PARAMETER_VALUE", "version");
            } else {
                return;
            }
        }
        // if the version is not accepted we send an exception
        String inputVersion = getParameter("VERSION", true);
        if (getVersionFromNumber(inputVersion) == null) {

            String message = "The parameter ";
            for (ServiceVersion vers : versions) {
                message += "VERSION=" + vers.toString() + " OR ";
            }
            message = message.substring(0, message.length()-3);
            message += " must be specified";
            throwException(message, "VERSION_NEGOTIATION_FAILED", null);

        } else {
            setCurrentVersion(inputVersion);
        }
        if (sld == 1) {
            if (!getParameter("SLD_VERSION", true).equals(sldVersion.toString())) {
                throwException("The parameter SLD_VERSION=" + sldVersion + " must be specified",
                               "VERSION_NEGOTIATION_FAILED", null);
            }
        }
    }

    /**
     * Verify if the version is supported by the service.
     * if the version is not accepted we send an exception
     */
    protected void isSupportedVersion(String versionNumber) throws WebServiceException {

        if (getVersionFromNumber(versionNumber) == null) {

            String message = "The parameter ";
            for (ServiceVersion vers : versions) {
                message += "VERSION=" + vers.toString() + " OR ";
            }
            message = message.substring(0, message.length()-3);
            message += " must be specified";
            throwException(message, "VERSION_NEGOTIATION_FAILED", null);

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
                        throwException("The parameter " + parameterName + " must be specified",
                                       "MISSING_PARAMETER_VALUE", parameterName);
                        //never reach
                        return null;
                    }
                }
            }
            StringReader sr = new StringReader(list.get(0));
            Object result = unmarshaller.unmarshal(sr);
            return result;
        } catch (JAXBException ex) {
             throwException("the xml object for parameter" + parameterName + " is not well formed:" + '\n' +
                            ex, "INVALID_PARAMETER_VALUE", null);
             //never reach
             return null;
        }
    }

    /**
     * Return the current version of the Web Service.
     *
     * @deprecated
     */
    protected ServiceVersion getCurrentVersion() {
        return this.currentVersion;
    }

    /**
     * Return the current version of the Web Service.
     *
     * @deprecated
     */
    protected void setCurrentVersion(String versionNumber) {
        currentVersion = getVersionFromNumber(versionNumber);
    }

    /**
     * Return the SLD version.
     */
    protected ServiceVersion getSldVersion() {
        return this.sldVersion;
    }

    /**
     * Treat the incomming GET request.
     *
     * @return an image or xml response.
     * @throw JAXBException
     */
    @GET
    public Response doGET() throws JAXBException  {
        return allowRequest(null);
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
        return allowRequest(null);
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
                StringWriter sw = launchException("The XML request is not valid", "INVALID_REQUEST");

                return Response.ok(sw.toString(), "text/xml").build();
            }

            if (request != null && request instanceof AbstractRequest) {
                AbstractRequest ar = (AbstractRequest) request;
                context.getQueryParameters().add("VERSION", ar.getVersion());
            }
            return allowRequest(request);
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
        StringWriter sw = launchException("This content type is not allowed try text/xml or application/x-www-form-urlencoded",
                                          "INVALID_REQUEST");
        return Response.ok(sw.toString(), "text/xml").build();
    }

    /**
     * Decide if the user who send the request has access to this resource.
     */
    private Response allowRequest(Object objectRequest) throws JAXBException {
        if (pep == null || PDP == null) {
            return treatIncomingRequest(objectRequest);
        }
        
        try {
            //we look for an authentified user
            Group userGrp = getAuthentifiedUser();
            Principal user = userGrp.members().nextElement();

            if (objectRequest instanceof JAXBElement) {
                objectRequest = ((JAXBElement) objectRequest).getValue();
            }

            // if the request is not an xml request we fill the request parameter.
            String request = "";
            if (objectRequest == null) {
                request = (String) getParameter("REQUEST", true);
            }

            //we define the action
            String action = request;
            if (action.equals("") && objectRequest != null) {
                action = objectRequest.getClass().getSimpleName();
                action = action.replace("Type", "");
            }
            action = action.toLowerCase();

            //we define the selected URI
            String requestedURI = context.getBaseUri().toString() + service.toLowerCase();


            LOGGER.finer("Request base URI=" + requestedURI + " user =" + userGrp.getName() + " action = " + action);
            RequestContext decisionRequest = pep.createXACMLRequest(requestedURI, user, userGrp, action);
            int decision = pep.getDecision(decisionRequest);

            if (decision == XACMLConstants.DECISION_PERMIT) {
                LOGGER.finer("request allowed");
                return treatIncomingRequest(objectRequest);
            } else if (decision == XACMLConstants.DECISION_DENY) {
                StringWriter sw = launchException("You are not authorized to execute this request. " +
                                                  "Please identify yourself first.", "NO_APPLICABLE_CODE");
                return Response.ok(sw.toString(), "text/xml").build();
            } else {
                LOGGER.severe("Unable to take a decision for the request, we let pass");
                return treatIncomingRequest(objectRequest);
            }
        } catch (WebServiceException ex) {
            StringWriter sw = new StringWriter();
            marshaller.marshal(ex.getExceptionReport(), sw);
            return Response.ok(sw.toString(), "text/xml").build();
        }  catch (IOException ex) {
            StringWriter sw = launchException("The service has throw an IO exception",  "NO_APPLICABLE_CODE");
            return Response.ok(sw.toString(), "text/xml").build();
        } catch (URISyntaxException ex) {
            StringWriter sw = launchException("The service has throw an URI syntax exception",  "NO_APPLICABLE_CODE");
            return Response.ok(sw.toString(), "text/xml").build();
        }
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
     * build an service Exception and marshall it into a StringWriter
     *
     * @param message
     * @param codeName
     * @return
     */
    protected StringWriter launchException(String message, String codeName) throws JAXBException {
        StringWriter sw = new StringWriter();

        if (getCurrentVersion().isOWS()) {
            OWSExceptionCode code = OWSExceptionCode.valueOf(codeName);
            OWSWebServiceException wse = new OWSWebServiceException(message,
                                                                    code,
                                                                    null,
                                                                    getCurrentVersion());
            marshaller.marshal(wse.getExceptionReport(), sw);
        } else {
            WMSExceptionCode code = WMSExceptionCode.valueOf(codeName);
            WMSWebServiceException wse = new WMSWebServiceException(message,
                                                                    code,
                                                                    getCurrentVersion());
            marshaller.marshal(wse.getExceptionReport(), sw);
        }
        return sw;
    }

    /**
     * Temporary test method until we put in place an authentification process.
     */
    private Group getAuthentifiedUser() {
        //for now we consider an user and is group as the same.
        Principal anonymous    = new PrincipalImpl("anonymous");
        Group     anonymousGrp = new GroupImpl("anonymous");
        anonymousGrp.addMember(anonymous);

        if (httpContext != null && httpContext.getRequest() != null) {
            HttpRequestContext httpRequest = httpContext.getRequest();
            Cookie authent = httpRequest.getCookies().get("authent");

            Group identifiedGrp = null;
            if (authent != null) {
                if (authent.getValue().equals("admin:admin")) {
                    Principal user = new PrincipalImpl("admin");
                    identifiedGrp  = new GroupImpl("admin");
                    identifiedGrp.addMember(user);
                }
            }
            if (identifiedGrp == null) {
                identifiedGrp = anonymousGrp;
            }
            return identifiedGrp;
        }
        return anonymousGrp;
    }

    /**
     * Returns the file where to read the capabilities document for each service.
     * If no such file is found, then this method returns {@code null}.
     *
     * @return The capabilities Object, or {@code null} if none.
     *
     * @deprecated
     */
    public Object getCapabilitiesObject() throws JAXBException, FileNotFoundException, IOException {
        return getCapabilitiesObject(getCurrentVersion());
    }

    /**
     * Returns the file where to read the capabilities document for each service.
     * If no such file is found, then this method returns {@code null}.
     *
     * @return The capabilities Object, or {@code null} if none.
     */
    public Object getCapabilitiesObject(final Version version) throws JAXBException, FileNotFoundException, IOException {
       String fileName = this.service + "Capabilities" + version.toString() + ".xml";
       File changeFile = getFile("change.properties");
       Properties p = new Properties();

       // if the flag file is present we load the properties
       if (changeFile != null && changeFile.exists()) {
           FileInputStream in    = new FileInputStream(changeFile);
           p.load(in);
           in.close();
       } else {
           p.put("update", "false");
       }

       //we recup the capabilities file and unmarshall it
       if (fileName == null) {
           return null;

       } else {

           //we look if we have already put it in cache
           Object response = capabilities.get(fileName);
           boolean update  = p.getProperty("update").equals("true");

           if (response == null || update) {
               if (update)
                    LOGGER.info("updating metadata");

               File f = getFile(fileName);
               response = unmarshaller.unmarshal(f);
               capabilities.put(fileName, response);
               this.setLastUpdateSequence(System.currentTimeMillis());
               p.put("update", "false");

               // if the flag file is present we store the properties
               if (changeFile != null && changeFile.exists()) {
                   FileOutputStream out = new FileOutputStream(changeFile);
                   p.store(out, "updated from WebService");
                   out.close();
               }
           }

           return response;
        }
    }

    /**
     * Return a file located in WEB-INF deployed directory.
     *
     * @param fileName The name of the file requested.
     * @return The specified file.
     */
    public File getFile(String fileName) {
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
    public File getSicadeDirectory() {
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
     * Set the prefixMapper for the marshaller.
     * The root namespace specified will have no prefix.
     *
     * @param rootNamespace The main namespace of all the produced XML document (xmlns = rootNamespace)
     */
    protected void setPrefixMapper(String rootNamespace) throws PropertyException {
        NamespacePrefixMapperImpl prefixMapper = new NamespacePrefixMapperImpl(rootNamespace);
        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", prefixMapper);
    }

    /**
     *  Throw a WebserviceException.
     *  If the service and version applies to OWS specification it throw an OWSException.
     */
    protected void throwException(final String message, String code, String locator) throws WebServiceException {
        if (getCurrentVersion().isOWS()) {
            code = transformCodeName(code);
            throw new OWSWebServiceException(message, OWSExceptionCode.valueOf(code), locator, getCurrentVersion());
        } else {
            throw new WMSWebServiceException(message, WMSExceptionCode.valueOf(code), getCurrentVersion());
        }

    }

    /**
     * Transform an exception code into the OWS specification.
     * Example : MISSING_PARAMETER_VALUE become MissingParameterValue.
     *
     * @param code
     * @return
     */
    private String transformCodeName(String code) {
        String result = "";
        final String prefix = code.charAt(0) + "";
        while (code.indexOf('_') != -1) {
            final String tmp = code.substring(0, code.indexOf('_')).toLowerCase();
            result += tmp.replace(tmp.charAt(0), prefix.charAt(0));
            code = code.substring(code.indexOf('_') + 1, code.length());
            //System.out.println(code);
        }
        code = code.toLowerCase();
        result += code.replace(code.charAt(0), prefix.charAt(0));
        return result;
    }

    /**
     * Return a Version Object from the version number.
     * if the version number is not correct return the default version.
     *
     * @param number the version number.
     * @return
     */
    protected ServiceVersion getVersionFromNumber(String number) {
        for (ServiceVersion v : versions) {
            if (v.toString().equals(number)){
                return v;
            }
        }
        return null;
    }

    /**
     * If the requested version number is not available we choose the best version to return.
     *
     * @param A service version number.
     */
    protected ServiceVersion getBestVersion(String number) {
        for (ServiceVersion v : versions) {
            if (v.toString().equals(number)){
                return v;
            }
        }
        ServiceVersion wrongVersion = new ServiceVersion(null, number);
        if (wrongVersion.compareTo(versions.get(0)) < 0) {
            return this.versions.get(0);
        } else {
            if (wrongVersion.compareTo(versions.get(versions.size() - 1)) > 0) {
                return versions.get(versions.size() - 1);
            }
        }
        return versions.get(0);
    }

    /**
     * Update all the url in a OWS capabilities document.
     *
     * @param operations A list of OWS operation.
     * @param url The url of the web application.
     * @param service the initials of the web service (WMS, SOS, WCS, CSW, ...). This string correspound to the resource name in lower case.
     */
    public static void updateOWSURL(List<? extends AbstractOperation> operations, String url, String service) {
        for (AbstractOperation op:operations) {
            for (AbstractDCP dcp: op.getDCP()) {
                for (AbstractOnlineResourceType method:dcp.getHTTP().getGetOrPost())
                    method.setHref(url + service.toLowerCase() + "?");
            }
       }
    }

    /**
     * return the last time that the capabilities have been updated (not yet really used)
     */
    public long getLastUpdateSequence() {
        return lastUpdateSequence;
    }

    /**
     * set the last time that the capabilities have been updated (not yet really used)
     *
     * @param lastUpdateSequence A Date.
     */
    public void setLastUpdateSequence(long lastUpdateSequence) {
        this.lastUpdateSequence = lastUpdateSequence;
    }

    /**
     * An temporary implementations of java.security.principal
     */
    public class PrincipalImpl implements Principal {

        private String name;

        public PrincipalImpl(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

    }

     /**
     * An temporary implementations of java.security.acl.group
     */
    public class GroupImpl implements Group {

        private Vector<Principal> vect = new Vector<Principal>();
        private String roleName;

        public GroupImpl(String roleName) {
            this.roleName = roleName;
        }

        public boolean addMember(final Principal principal) {
            return vect.add(principal);
        }

        public boolean isMember(Principal principal) {
            return vect.contains(principal);
        }

        public Enumeration<? extends Principal> members() {
            vect.add(new Principal() {

                public String getName() {
                    return roleName;
                }
            });
            return vect.elements();
        }

        public boolean removeMember(Principal principal) {
            return vect.remove(principal);
        }

        public String getName() {
            return roleName;
        }
    }
}
