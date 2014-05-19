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
package org.constellation.xacml;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.security.acl.Group;
import java.util.Enumeration;
import org.constellation.xacml.api.PolicyDecisionPoint;
import org.constellation.xacml.api.RequestContext;
import org.constellation.xacml.api.ResponseContext;
import org.geotoolkit.xacml.xml.context.ActionType;
import org.geotoolkit.xacml.xml.context.AttributeType;
import org.geotoolkit.xacml.xml.context.EnvironmentType;
import org.geotoolkit.xacml.xml.context.RequestType;
import org.geotoolkit.xacml.xml.context.ResourceType;
import org.geotoolkit.xacml.xml.context.SubjectType;
import org.constellation.xacml.factory.RequestAttributeFactory;

/**
 * The Policy Enforcement Point.
 *
 * @author Guilhem Legal
 */
public class PEP {
    
    private final static String ISSUER = "constellation.org";
    
    private final PolicyDecisionPoint pdp;
    
    /**
     * Build a new Policy Enforcement Point.
     * 
     * @param PDP
     */
    public PEP(final PolicyDecisionPoint pdp) {
        this.pdp = pdp;
    }
    
    /**
     * Create A XACML request for the request resource containing the userName (principal), the role group and the action.
     * 
     * @param resourceURI The request resource URI.
     * @param principal   The user.
     * @param roleGroup   The user group.
     * 
     * @return An XACML request.
     * @throws java.lang.Exception
     */
    public RequestContext createXACMLRequest(final String resourceURI, final Principal principal, final Group roleGroup, final String action) throws URISyntaxException, IOException {
        final RequestContext requestCtx = new CstlRequestContext();

        //Create a subject type
        final SubjectType subject = createSubject(principal, roleGroup);

        //Create a resource type
        final ResourceType resourceType = createResource(resourceURI);

        //Create an action type
        final ActionType actionType = createAction(action);

        //Create an Environment Type (Optional)
        final EnvironmentType environmentType = createTimeEnvironement();

        //Create a Request Type
        final RequestType requestType = new RequestType();
        requestType.getSubject().add(subject);
        requestType.getResource().add(resourceType);
        requestType.setAction(actionType);
        requestType.setEnvironment(environmentType);

        requestCtx.setRequest(requestType);

        return requestCtx;
    }
    
    /**
     * Create A XACML request for the request resource containing the userName (principal), the role group and the action.
     * 
     * @param resourceURI The request resource URI.
     * @param principal   The user.
     * @param roleGroup   The user group.
     * 
     * @return An XACML request.
     * @throws java.lang.Exception
     */
    public RequestContext createXACMLRequest(final URI resourceURI, final Principal principal, final Group roleGroup, final String action) throws IOException {
        final RequestContext requestCtx = new CstlRequestContext();

        //Create a subject type
        final SubjectType subject = createSubject(principal, roleGroup);

        //Create a resource type
        final ResourceType resourceType = createResource(resourceURI);

        //Create an action type
        final ActionType actionType = createAction(action);

        //Create an Environment Type (Optional)
        final EnvironmentType environmentType = createTimeEnvironement();

        //Create a Request Type
        final RequestType requestType = new RequestType();
        requestType.getSubject().add(subject);
        requestType.getResource().add(resourceType);
        requestType.setAction(actionType);
        requestType.setEnvironment(environmentType);

        requestCtx.setRequest(requestType);

        return requestCtx;
    }
    
    /**
     * Create a part of XACML request about the user and group.
     * 
     * @param user      The authenticated user.
     * @param roleGroup The user group.
     * 
     * @return a subject Type which is a part of XACML request.
     */
    protected SubjectType createSubject(final Principal user, final Group roleGroup) {
    
        //Create a subject type
        final SubjectType subject = new SubjectType();
        subject.getAttribute().add(
                RequestAttributeFactory.createStringAttributeType(XACMLConstants.ATTRIBUTEID_SUBJECT_SUBJECTID.key, 
                                                                  ISSUER,
                                                                  user.getName()));
        
        final Enumeration<Principal> roles = (Enumeration<Principal>) roleGroup.members();
        while (roles.hasMoreElements()) {
            final Principal rolePrincipal = roles.nextElement();
            final AttributeType attSubjectID = RequestAttributeFactory.createStringAttributeType(
                    XACMLConstants.ATTRIBUTEID_SUBJECT_ROLE.key, ISSUER, rolePrincipal.getName());
            subject.getAttribute().add(attSubjectID);
        }
        return subject;
    }

    /**
     * Create a  part of XACML request about the requested resource.
     * 
     * @param URI the requested resource URI.
     * 
     * @return a resource Type which is a part of XACML request.
     */
    protected ResourceType createResource(final String uri) throws URISyntaxException {
    
        //Create a resource type
        final ResourceType resourceType = new ResourceType();
        resourceType.getAttribute().add(
                RequestAttributeFactory.createAnyURIAttributeType(XACMLConstants.ATTRIBUTEID_RESOURCE_RESOURCEID.key, null, new URI(uri)));
        return resourceType;
    }
    
    /**
     * Create a  part of XACML request about the requested resource.
     * 
     * @param URI the requested resource URI.
     * 
     * @return a resource Type which is a part of XACML request.
     */
    protected ResourceType createResource(final URI uri) {
    
        //Create a resource type
        final ResourceType resourceType = new ResourceType();
        resourceType.getAttribute().add(
                RequestAttributeFactory.createAnyURIAttributeType(XACMLConstants.ATTRIBUTEID_RESOURCE_RESOURCEID.key, null, uri));
        return resourceType;
    }
    
    /**
     * Create a  part of XACML request about the action to execute.
     * 
     * @param URI the requested resource URI.
     * 
     * @return a action Type which is a part of XACML request.
     */
    protected ActionType createAction(final String action)  {
        
        //Create an action type
        final ActionType actionType = new ActionType();
        actionType.getAttribute().add(
                RequestAttributeFactory.createStringAttributeType(XACMLConstants.ATTRIBUTEID_ACTION_ACTIONID.key, ISSUER, action));
        
        return actionType;
    
    }
    
    /**
     * Create a part of XACML request about the time.
     */
    protected EnvironmentType createTimeEnvironement() {
            
        //Create an Environment Type
        final EnvironmentType environmentType = new EnvironmentType();
        environmentType.getAttribute().add(
                RequestAttributeFactory.createDateTimeAttributeType(XACMLConstants.ATTRIBUTEID_ENVIRONMENT_CURRENTTIME.key, ISSUER));
        return environmentType;
        
    }
    
   /**
    * Get the response for a request from the PDP
    * @param pdp
    * @param request
    * @return
    * @throws Exception
    */
   public ResponseContext getResponse(final RequestContext request) {
      return pdp.evaluate(request);
   }

   /**
    * Get the decision from the PDP
    * @param pdp
    * @param request RequestContext containing the request
    * @return
    * @throws Exception
    */
   public int getDecision(final RequestContext request) {
      final ResponseContext response = pdp.evaluate(request);
      return response.getDecision();
   }
}
