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
import net.seagis.xacml.api.RequestContext;
import net.seagis.xacml.context.ActionType;
import net.seagis.xacml.context.AttributeType;
import net.seagis.xacml.context.EnvironmentType;
import net.seagis.xacml.context.RequestType;
import net.seagis.xacml.context.ResourceType;
import net.seagis.xacml.context.SubjectType;
import net.seagis.xacml.factory.RequestAttributeFactory;
import net.seagis.xacml.factory.RequestResponseContextFactory;

/**
 *
 * @author guilhem
 */
public class PEP {
    
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
        RequestContext requestCtx = RequestResponseContextFactory.createRequestCtx();

        //Create a subject type
        SubjectType subject = new SubjectType();
        subject.getAttribute().add(
                RequestAttributeFactory.createStringAttributeType(XACMLConstants.ATTRIBUTEID_SUBJECT_ID.key, 
                                                                  "constellation.org", 
                                                                  principal.getName()));
        
        Enumeration<Principal> roles = (Enumeration<Principal>) roleGroup.members();
        while (roles.hasMoreElements()) {
            Principal rolePrincipal = roles.nextElement();
            AttributeType attSubjectID = RequestAttributeFactory.createStringAttributeType(
                    XACMLConstants.ATTRIBUTEID_ROLE.key, "constellation.org", rolePrincipal.getName());
            subject.getAttribute().add(attSubjectID);
        }

        //Create a resource type
        ResourceType resourceType = new ResourceType();
        resourceType.getAttribute().add(
                RequestAttributeFactory.createAnyURIAttributeType(XACMLConstants.ATTRIBUTEID_RESOURCE_ID.key, null, 
                                                                  new URI(resourceURI)));

        //Create an action type
        ActionType actionType = new ActionType();
        actionType.getAttribute().add(
                RequestAttributeFactory.createStringAttributeType(XACMLConstants.ATTRIBUTEID_ACTION_ID.key, "constellation.org", action));

        //Create an Environment Type (Optional)
        EnvironmentType environmentType = new EnvironmentType();
        environmentType.getAttribute().add(
                RequestAttributeFactory.createDateTimeAttributeType(XACMLConstants.ATTRIBUTEID_CURRENT_TIME.key, null));

        //Create a Request Type
        RequestType requestType = new RequestType();
        requestType.getSubject().add(subject);
        requestType.getResource().add(resourceType);
        requestType.setAction(actionType);
        requestType.setEnvironment(environmentType);

        requestCtx.setRequest(requestType);

        return requestCtx;
    }

}
