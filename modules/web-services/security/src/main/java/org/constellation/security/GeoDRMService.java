/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
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
package org.constellation.security;

import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.spi.resource.Singleton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.annotation.PreDestroy;
import javax.ws.rs.Path;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.constellation.ws.ServiceType;
import org.constellation.ws.ServiceVersion;
import org.constellation.ws.CstlServiceException;
import org.constellation.ows.v110.ExceptionReport;
import org.constellation.util.Utils;
import org.constellation.ws.rs.OGCWebService;
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
import static org.constellation.ows.OWSExceptionCode.*;

/**
 * THIS CLASS IS NOT USED BUT ONLY HERE FOR REFERENCE.
 * 
 * @author guilhem
 */
@Path("pep")
@Singleton
public class GeoDRMService extends OGCWebService {

    /**
     * A Policy Decision Point allowing to secure the access to the resources.
     */
    private PolicyDecisionPoint PDP;

    /**
     * A Policy Enforcement Point allowing to secure the access to the resources.
     */
    private PEP pep;
    
    private String SERVICEURL = "http://demo.geomatys.fr/constellation/WS/wms";
    
    public GeoDRMService() {
        super("GeoDRM", new ServiceVersion(ServiceType.OTHER, "1.0.0"));
        try {
            initializePolicyDecisionPoint();
            setXMLContext("org.constellation.wms.v111:org.constellation.wms.v130:org.constellation.gml.v311:org.constellation.ws", "");
        } catch (JAXBException ex){
            LOGGER.severe("The GeoDRM service is not running."       + '\n' +
                          " cause  : Error creating XML context." + '\n' +
                          " error  : " + ex.getMessage()          + '\n' + 
                          " details: " + ex.toString());
        }
    } 
    
    /**
     * Initialize the policy Decision Point and load all the correspounding policy file.
     */
    private void initializePolicyDecisionPoint() {
        //we create a new PDP
        PDP = new CstlPDP();

        //load the correspounding policy file
        final String url = "org/constellation/xacml/wmsPolicy.xml";
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
            LOGGER.severe("JAXB exception while unmarshalling policyFile wmsPolicy.xml");
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
                LOGGER.info("cookie authent present");
                if (authent.getValue().equals("admin:admin")) {
                    Principal user = new PrincipalImpl("admin");
                    identifiedGrp  = new GroupImpl("admin");
                    identifiedGrp.addMember(user);
                }
            } else {
                LOGGER.info("no cookie authent found");
            }
            if (identifiedGrp == null) {
                identifiedGrp = anonymousGrp;
            }
            return identifiedGrp;
        } else {
            LOGGER.severe("httpcontext null");
        }
        return anonymousGrp;
    }

    
    /**
     * Decide if the user who send the request has access to this resource.
     */
    private Response allowRequest(Object objectRequest) throws JAXBException {
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
            String action;
            List<String> actions = new ArrayList<String>();
            
            if (request.equalsIgnoreCase("getMap")) {
                action = getParameter("LAYERS", true);
                if (action.equals("") && objectRequest != null) {
                    action = objectRequest.getClass().getSimpleName();
                    action = action.replace("Type", "");
                } else {
                    final StringTokenizer tokens = new StringTokenizer(action, ",;");
                    while (tokens.hasMoreTokens()) {
                        actions.add(tokens.nextToken());
                    }
                }
            } else {
                action = request;
            }
            
            //we define the selected URI
            String requestedURI = uriContext.getRequestUri().toString();
            if (objectRequest == null) {
                objectRequest = requestedURI.substring(requestedURI.indexOf('?'));
            }


            LOGGER.info("Request base URI=" + requestedURI + " user =" + userGrp.getName() + " action = " + action);
            
            boolean allowed;
            if ( actions.size() <= 1 ) {
                
                RequestContext decisionRequest = pep.createXACMLRequest(SERVICEURL, user, userGrp, action);
                int decision = pep.getDecision(decisionRequest);
                allowed = decision == XACMLConstants.DECISION_PERMIT;
                
            } else {
                
                allowed = true;
                for (String act : actions) {
                    RequestContext decisionRequest = pep.createXACMLRequest(SERVICEURL, user, userGrp, act);
                    if (pep.getDecision(decisionRequest) == XACMLConstants.DECISION_DENY)
                        allowed = false;
                }
            }
            
            if (allowed) {
                LOGGER.info("request allowed");
                return sendRequest(objectRequest);
            } else {
                final StringWriter sw = new StringWriter();
                final Object obj = launchException("You are not authorized to execute this request. " +
                        "Please identify yourself first.", "NO_APPLICABLE_CODE", null);
                marshaller.marshal(obj, sw);
                return Response.ok(sw.toString(), "text/xml").build();
            } 
        } catch (CstlServiceException ex) {
            StringWriter sw = new StringWriter();
            final String code = Utils.transformCodeName(ex.getExceptionCode().name());
            final ExceptionReport report = new ExceptionReport(ex.getMessage(), code, ex.getLocator(), getActingVersion());
            marshaller.marshal(report, sw);
            return Response.ok(sw.toString(), "text/xml").build();
        }  catch (IOException ex) {
            StringWriter sw = new StringWriter();
            Object obj = launchException("The service has throw an IO exception",  "NO_APPLICABLE_CODE", null);
            marshaller.marshal(obj, sw);
            return Response.ok(sw.toString(), "text/xml").build();
        } catch (URISyntaxException ex) {
            StringWriter sw = new StringWriter();
            Object obj = launchException("The service has throw an URI syntax exception",  "NO_APPLICABLE_CODE", null);
            marshaller.marshal(obj, sw);
            return Response.ok(sw.toString(), "text/xml").build();
        }
    }
    
    
    @Override
    public Response treatIncomingRequest(Object objectRequest) throws JAXBException {
        return allowRequest(objectRequest);
    }

    private Response sendRequest(Object objectRequest) throws CstlServiceException, MalformedURLException {
        Object response = null;
        String contentType = "";
        try {
             URLConnection conec;
             
            // for a POST request
            if (!(objectRequest instanceof String)) {
                URL source  = new URL(SERVICEURL);
                conec       = source.openConnection();
                
                conec.setDoOutput(true);
                conec.setRequestProperty("Content-Type","text/xml");
                OutputStreamWriter wr = new OutputStreamWriter(conec.getOutputStream());
                StringWriter sw = new StringWriter();
                try {
                    
                    marshaller.marshal(objectRequest, sw);
                } catch (JAXBException ex) {
                    throw new CstlServiceException("Unable to marshall the request: " + ex.getMessage(),
                                                  NO_APPLICABLE_CODE, getActingVersion());
                }
                String XMLRequest = sw.toString();
            
                // in the special case 1 we need to remove ogc prefix inside  the >Filter
                LOGGER.info("sended:" + XMLRequest);
                wr.write(XMLRequest);
                wr.flush();
            
            // for a GET request
            } else {
                 
                URL source  = new URL(SERVICEURL + objectRequest);
                LOGGER.info("url sended:" + SERVICEURL + objectRequest);
                conec       = source.openConnection(); 
            }
        
            // we get the response document
            InputStream in = conec.getInputStream();
            contentType = conec.getContentType();
            
            if (contentType.contains("xml") || contentType.contains("text")) {
                byte[] buffer = new byte[1024];
                int size;
                StringWriter out = new StringWriter();
                while ((size = in.read(buffer, 0, 1024)) > 0) {
                    out.write(new String(buffer, 0, size));
                }
                
                //we convert the brut String value into UTF-8 encoding
                String brutString = out.toString();

                //we need to replace % character by "percent because they are reserved char for url encoding
                brutString = brutString.replaceAll("%", "percent");
                String decodedString = java.net.URLDecoder.decode(brutString, "UTF-8");
                
                try {
                    response = unmarshaller.unmarshal(new StringReader(decodedString));
                    if (response != null && response instanceof JAXBElement) {
                        response = ((JAXBElement) response).getValue();
                    }
                } catch (JAXBException ex) {
                    LOGGER.severe("The distant service does not respond correctly: unable to unmarshall response document." + '\n' +
                                 "cause: " + ex.getMessage());
                }
            } else {
                File temp = File.createTempFile("keeperWriter", "tmp");
                temp.deleteOnExit();
                FileOutputStream byteArrayOut = new FileOutputStream(temp);
                int c;
                while ((c = in.read()) != -1) {
                    byteArrayOut.write(c);
                }
                byteArrayOut.close();
                response = temp;
            }
            
            LOGGER.info("ResponseType : " + response.getClass().getName() + '\n' + 
                        "MIME Type    : " + contentType);
            
        } catch (IOException ex) {
            LOGGER.severe("The Distant service have made an error");
            return null;
        }
        return Response.ok(response, contentType).build();
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

    @PreDestroy
    public void destroy() {
        LOGGER.info("destroying GeoDRM service");
    }
}
