/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.seagis.xacml;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.security.acl.Group;
import java.util.Enumeration;
import net.seagis.xacml.api.PolicyDecisionPoint;
import net.seagis.xacml.api.RequestContext;
import net.seagis.xacml.api.ResponseContext;
import net.seagis.xacml.context.ActionType;
import net.seagis.xacml.context.AttributeType;
import net.seagis.xacml.context.EnvironmentType;
import net.seagis.xacml.context.RequestType;
import net.seagis.xacml.context.ResourceType;
import net.seagis.xacml.context.SubjectType;
import net.seagis.xacml.factory.RequestAttributeFactory;

/**
 *
 * @author Guilhem Legal
 */
public class PEP {
    
    private String issuer = "constellation.org";
    
    private PolicyDecisionPoint PDP;
    
    /**
     * Build a new Policy Enforcement Point.
     * 
     * @param PDP
     */
    public PEP(PolicyDecisionPoint PDP) {
        this.PDP = PDP;
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
    public RequestContext createXACMLRequest(String resourceURI, Principal principal, Group roleGroup, String action) throws URISyntaxException, IOException {
        RequestContext requestCtx = new CstlRequestContext();

        //Create a subject type
        SubjectType subject = createSubject(principal, roleGroup);

        //Create a resource type
        ResourceType resourceType = createResource(resourceURI);

        //Create an action type
        ActionType actionType = createAction(action);

        //Create an Environment Type (Optional)
        EnvironmentType environmentType = createTimeEnvironement();

        //Create a Request Type
        RequestType requestType = new RequestType();
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
    public RequestContext createXACMLRequest(URI resourceURI, Principal principal, Group roleGroup, String action) throws IOException {
        RequestContext requestCtx = new CstlRequestContext();

        //Create a subject type
        SubjectType subject = createSubject(principal, roleGroup);

        //Create a resource type
        ResourceType resourceType = createResource(resourceURI);

        //Create an action type
        ActionType actionType = createAction(action);

        //Create an Environment Type (Optional)
        EnvironmentType environmentType = createTimeEnvironement();

        //Create a Request Type
        RequestType requestType = new RequestType();
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
     * @param user      The authentified user.
     * @param roleGroup The user group.
     * 
     * @return a subject Type whitch is a part of XACML request.
     */
    protected SubjectType createSubject(Principal user, Group roleGroup) {
    
        //Create a subject type
        SubjectType subject = new SubjectType();
        subject.getAttribute().add(
                RequestAttributeFactory.createStringAttributeType(XACMLConstants.ATTRIBUTEID_SUBJECT_ID.key, 
                                                                  issuer, 
                                                                  user.getName()));
        
        Enumeration<Principal> roles = (Enumeration<Principal>) roleGroup.members();
        while (roles.hasMoreElements()) {
            Principal rolePrincipal = roles.nextElement();
            AttributeType attSubjectID = RequestAttributeFactory.createStringAttributeType(
                    XACMLConstants.ATTRIBUTEID_ROLE.key, issuer, rolePrincipal.getName());
            subject.getAttribute().add(attSubjectID);
        }
        return subject;
    }

    /**
     * Create a  part of XACML request about the requested resource.
     * 
     * @param URI the requested resource URI.
     * 
     * @return a resource Type whitch is a part of XACML request.
     */
    protected ResourceType createResource(String URI) throws URISyntaxException {
    
        //Create a resource type
        ResourceType resourceType = new ResourceType();
        resourceType.getAttribute().add(
                RequestAttributeFactory.createAnyURIAttributeType(XACMLConstants.ATTRIBUTEID_RESOURCE_ID.key, null, new URI(URI)));
        return resourceType;
    }
    
    /**
     * Create a  part of XACML request about the requested resource.
     * 
     * @param URI the requested resource URI.
     * 
     * @return a resource Type whitch is a part of XACML request.
     */
    protected ResourceType createResource(URI URI) {
    
        //Create a resource type
        ResourceType resourceType = new ResourceType();
        resourceType.getAttribute().add(
                RequestAttributeFactory.createAnyURIAttributeType(XACMLConstants.ATTRIBUTEID_RESOURCE_ID.key, null, URI));
        return resourceType;
    }
    
    /**
     * Create a  part of XACML request about the action to execute.
     * 
     * @param URI the requested resource URI.
     * 
     * @return a action Type whitch is a part of XACML request.
     */
    protected ActionType createAction(String action)  {
        
        //Create an action type
        ActionType actionType = new ActionType();
        actionType.getAttribute().add(
                RequestAttributeFactory.createStringAttributeType(XACMLConstants.ATTRIBUTEID_ACTION_ID.key, issuer, action));
        
        return actionType;
    
    }
    
    /**
     * Create a part of XACML request about the time.
     */
    protected EnvironmentType createTimeEnvironement() {
            
        //Create an Environment Type
        EnvironmentType environmentType = new EnvironmentType();
        environmentType.getAttribute().add(
                RequestAttributeFactory.createDateTimeAttributeType(XACMLConstants.ATTRIBUTEID_CURRENT_TIME.key, issuer));
        return environmentType;
        
    }
    
   /**
    * Get the response for a request from the PDP
    * @param pdp
    * @param request
    * @return
    * @throws Exception
    */
   public ResponseContext getResponse(RequestContext request) {
      return PDP.evaluate(request);
   }

   /**
    * Get the decision from the PDP
    * @param pdp
    * @param request RequestContext containing the request
    * @return
    * @throws Exception
    */
   public int getDecision(RequestContext request) {
      ResponseContext response = PDP.evaluate(request);
      return response.getDecision();
   }
}
