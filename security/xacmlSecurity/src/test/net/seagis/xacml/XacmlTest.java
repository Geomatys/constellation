/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 * (C) 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package net.seagis.xacml;

// J2SE dependencies
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.security.Principal;
import java.security.acl.Group;

// JAXB dependencies
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;

// seagis dependencies
import javax.xml.bind.Marshaller;
import net.seagis.xacml.api.PolicyLocator;
import net.seagis.xacml.api.RequestContext;
import net.seagis.xacml.api.XACMLPolicy;
import net.seagis.xacml.factory.PolicyAttributeFactory;
import net.seagis.xacml.factory.PolicyFactory;
import net.seagis.xacml.locators.JBossPolicyLocator;
import net.seagis.xacml.policy.ActionMatchType;
import net.seagis.xacml.policy.ActionType;
import net.seagis.xacml.policy.ActionsType;
import net.seagis.xacml.policy.ApplyType;
import net.seagis.xacml.policy.AttributeValueType;
import net.seagis.xacml.policy.ConditionType;
import net.seagis.xacml.policy.EffectType;
import net.seagis.xacml.policy.ExpressionType;
import net.seagis.xacml.policy.FunctionType;
import net.seagis.xacml.policy.ObjectFactory;
import net.seagis.xacml.policy.ResourceMatchType;
import net.seagis.xacml.policy.ResourceType;
import net.seagis.xacml.policy.ResourcesType;
import net.seagis.xacml.policy.RuleType;
import net.seagis.xacml.policy.SubjectAttributeDesignatorType;
import net.seagis.xacml.policy.TargetType;

// Junit dependencies
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal
 */
public class XacmlTest {
    
    private Logger logger = Logger.getLogger("net.seagis.metadata");
   
    /**
     * enable the debug logging system 
     */
    private boolean debug = true;
    
    /**
     * A Policy Decision Point which received xacml request and decide to give or not the acces to the resource.
     */
    private JBossPDP PDP;
    
    /**
     * A example Policy.
     */
    private net.seagis.xacml.policy.PolicyType policyType1;
    
    
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        
         policyType1 = constructExamplePolicy();
         PDP = new JBossPDP();
        
         XACMLPolicy policy = PolicyFactory.createPolicy(policyType1);

         Set<XACMLPolicy> policies = new HashSet<XACMLPolicy>();
         policies.add(policy);
         PDP.setPolicies(policies);
        
         //Add the basic locators also
         PolicyLocator policyLocator = new JBossPolicyLocator();
         policyLocator.setPolicies(policies);
        
         //Locators need to be given the policies
         Set<PolicyLocator> locators = new HashSet<PolicyLocator>();
         locators.add(policyLocator);
         PDP.setLocators(locators);
    }

    @After
    public void tearDown() throws Exception {
    }
    
    /**
     * Test the build of a PDP with a configuration file. 
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void configFilePDPTest() throws Exception {
        
        File configFile = new File("PDPConfig.xml");
        InputStream is = new FileInputStream(configFile);
        PDP = new JBossPDP(is);
        
        assertNotNull(PDP);
    }
    
     /**
     * Test the build of a PDP with a object model. 
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void ObjectModelPDPTest() throws Exception {

        PDP = new JBossPDP();
        
        JAXBContext jbcontext = JAXBContext.newInstance("net.seagis.xacml.policy");
        Marshaller marshaller    = jbcontext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(policyType1, System.out);
        
        XACMLPolicy policy = PolicyFactory.createPolicy(policyType1);
        Set<XACMLPolicy> policies = new HashSet<XACMLPolicy>();
        policies.add(policy);
       
        //Pass a set of policies (and/or policy sets) to the PDP
        PDP.setPolicies(policies);

        assertNotNull(PDP);
    }
    
    /**
     * Test sending a request to the PDP with an user of the group developer.
     * The response must be positive
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void testPositiveWebBinding() throws Exception {
        
        assertNotNull("JBossPDP is != null", PDP);
        PEP pep = new PEP();
        
        //we create an user
        Principal p = new Principal() {

            public String getName() {
                return "tesuser";
            }
        };
        
        //we create Role Group
        Group grp_developer = XACMLTestUtil.getRoleGroup("developer");
        Group grp_admin     = XACMLTestUtil.getRoleGroup("admin");
        String requestURI   = "http://test/developer-guide.html";
        
        //Check PERMIT condition
        RequestContext request = pep.createXACMLRequest(requestURI, p, grp_developer, "read");
        if (debug) {
            System.out.println("Positive Web Binding request: role='developer' action='read'");
            request.marshall(System.out);
            System.out.println("");
        }
        assertEquals("Access Allowed?", XACMLConstants.DECISION_PERMIT, XACMLTestUtil.getDecision(PDP, request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, p, grp_admin, "read");
        if (debug) {
            System.out.println("Positive Web Binding request: role='adminr' action='write'");
            request.marshall(System.out);
            System.out.println("");
        }
        assertEquals("Access Allowed?", XACMLConstants.DECISION_PERMIT, XACMLTestUtil.getDecision(PDP, request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, p, grp_admin, "write");
        if (debug) {
            System.out.println("Positive Web Binding request: role='adminr' action='write'");
            request.marshall(System.out);
            System.out.println("");
        }
        assertEquals("Access Allowed?", XACMLConstants.DECISION_PERMIT, XACMLTestUtil.getDecision(PDP, request));
    }
    
    /**
     * Test sending a request to the PDP with an user of the group imposter.
     * The response must be negative.
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void testNegativeAccessWebBinding() throws Exception {
        
        assertNotNull("JBossPDP is != null", PDP);
        PEP pep = new PEP();
        
        //we create an user
        Principal p = new Principal() {

            public String getName() {
                return "testuser";
            }
        };
        
        //we create Role Group
        Group grp_imposter  = XACMLTestUtil.getRoleGroup("imposter");
        Group grp_developer = XACMLTestUtil.getRoleGroup("developer");
        
        String requestURI = "http://test/developer-guide.html";
        
        //Check DENY condition
        RequestContext request = pep.createXACMLRequest(requestURI, p, grp_imposter, "read");
        
        if (debug) {
            System.out.println("Negative Web Binding request: role= 'imposter' action='read' ");
            request.marshall(System.out);
            System.out.println("");
        }
        assertEquals("Access Disallowed?", XACMLConstants.DECISION_DENY,
                      XACMLTestUtil.getDecision(PDP, request));
        
        //Check DENY condition
        request = pep.createXACMLRequest(requestURI, p, grp_developer, "write");
        
        if (debug) {
            System.out.println("Negative Web Binding request: role= 'developer' action='write' ");
            request.marshall(System.out);
            System.out.println("");
        }
        assertEquals("Access Disallowed?", XACMLConstants.DECISION_DENY,
                      XACMLTestUtil.getDecision(PDP, request));
    }


    
    /**
     * Build an example Policy.
     * 
     * This policy file basically provides access to the url when the subject has a role of "developer".
     * All other requests are denied because of the explicit rule at the bottom of the policy file,
     * without which the PDP would have returned a decision of NotAPPLICABLE.
     * 
     * @return
     * @throws java.lang.Exception
     */
    private net.seagis.xacml.policy.PolicyType constructExamplePolicy() throws Exception {
        
        ObjectFactory objectFactory = new ObjectFactory();
        String PERMIT_OVERRIDES = "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:permit-overrides";
        net.seagis.xacml.policy.PolicyType policyType = new net.seagis.xacml.policy.PolicyType();
        policyType.setPolicyId("ExamplePolicy");
        policyType.setVersion("2.0");
        policyType.setRuleCombiningAlgId(PERMIT_OVERRIDES);
        
        /**
         * we Create the target resource here its : http://test/developer-guide.html
         */ 
        TargetType targetType       = new TargetType();
        ResourcesType resourcesType = new ResourcesType();
        ResourceType resourceType   = new ResourceType();
        ResourceMatchType rmt       = new ResourceMatchType();
        
        //this policy is applicable when the URI of the requested ressource equals the specified URI
        rmt.setMatchId(XACMLConstants.FUNCTION_ANYURI_EQUAL.key);
        
        // description of the attribute here the resource ID of type anyURI
        rmt.setResourceAttributeDesignator(PolicyAttributeFactory.createAttributeDesignatorType(
                XACMLConstants.ATTRIBUTEID_RESOURCE_ID.key,
                XMLSchemaConstants.DATATYPE_ANYURI.key,
                null,
                true));
        
        // the value of  the attribute
        rmt.setAttributeValue(PolicyAttributeFactory.createAnyURIAttributeType(new URI("http://test/developer-guide.html")));
        resourceType.getResourceMatch().add(rmt);
        resourcesType.getResource().add(resourceType);
        targetType.setResources(resourcesType);
        policyType.setTarget(targetType);

        /**
         * Create a Rule allowing the access of the ressource when the subject has a role of "developper" for a READ action
         */ 
        RuleType permitRule = new RuleType();
        permitRule.setRuleId("ReadRule");
        
        // if this rule is applicable the effect is to "PERMIT" the access
        permitRule.setEffect(EffectType.PERMIT);
        
        ActionsType permitRuleActionsType = new ActionsType();
        ActionType permitRuleActionType = new ActionType();
        ActionMatchType amct = new ActionMatchType();

        //here the rule is apply when the action on ressource is equal to "read"
        amct.setMatchId("urn:oasis:names:tc:xacml:1.0:function:string-equal");
        amct.setAttributeValue(PolicyAttributeFactory.createStringAttributeType("read"));
        amct.setActionAttributeDesignator(
                PolicyAttributeFactory.createAttributeDesignatorType(XACMLConstants.ATTRIBUTEID_ACTION_ID.key,
                                                                     XMLSchemaConstants.DATATYPE_STRING.key,
                                                                     null, true));
        
        permitRuleActionType.getActionMatch().add(amct);
        TargetType permitRuleTargetType = new TargetType();
        permitRuleActionsType.getAction().add(permitRuleActionType);
        permitRuleTargetType.setActions(permitRuleActionsType);
        permitRule.setTarget(permitRuleTargetType);
        
        // now we create the condition to fill to permit the access
        ConditionType permitRuleConditionType = new ConditionType();
        FunctionType functionType = new FunctionType();
        functionType.setFunctionId(XACMLConstants.FUNCTION_STRING_EQUAL.key);
        JAXBElement<ExpressionType> jaxbElementFunctionType = objectFactory.createExpression(functionType);
        permitRuleConditionType.setExpression(jaxbElementFunctionType);
        ApplyType permitRuleApplyType = new ApplyType();
        
        // the condition is: "developer" is in role id 
        permitRuleApplyType.setFunctionId(XACMLConstants.FUNCTION_STRING_IS_IN.key);
        SubjectAttributeDesignatorType sadt = PolicyAttributeFactory.createSubjectAttributeDesignatorType(XACMLConstants.ATTRIBUTEID_ROLE.key,
                                                                                                          XMLSchemaConstants.DATATYPE_STRING.key,
                                                                                                          null, true, null);
        
        JAXBElement<SubjectAttributeDesignatorType> sadtElement = objectFactory.createSubjectAttributeDesignator(sadt);
        AttributeValueType avt = PolicyAttributeFactory.createStringAttributeType("developer");
        
        JAXBElement<AttributeValueType> jaxbAVT = objectFactory.createAttributeValue(avt);
        permitRuleApplyType.getExpression().add(jaxbAVT);
        permitRuleApplyType.getExpression().add(sadtElement);
        permitRuleConditionType.setExpression(objectFactory.createApply(permitRuleApplyType));
        permitRule.setCondition(permitRuleConditionType);
        policyType.getCombinerParametersOrRuleCombinerParametersOrVariableDefinition().add(permitRule);
        
        /**
         * Create a Rule allowing the access of the ressource when the subject has a role of "admin" for a READ or WRITE action
         */ 
        RuleType permitRule2 = new RuleType();
        permitRule2.setRuleId("ReadWriteRule");
        
        // if this rule is applicable the effect is to "PERMIT" the access
        permitRule2.setEffect(EffectType.PERMIT);
        
        permitRuleActionsType            = new ActionsType();
        ActionType permitRuleActionType1 = new ActionType();
        amct                             = new ActionMatchType();

        //here the rule is apply when the action on ressource is equal to "read"
        amct.setMatchId("urn:oasis:names:tc:xacml:1.0:function:string-equal");
        amct.setAttributeValue(PolicyAttributeFactory.createStringAttributeType("read"));
        amct.setActionAttributeDesignator(
                PolicyAttributeFactory.createAttributeDesignatorType(XACMLConstants.ATTRIBUTEID_ACTION_ID.key,
                                                                     XMLSchemaConstants.DATATYPE_STRING.key,
                                                                     null, true));
        
        permitRuleActionType1.getActionMatch().add(amct);
        
        ActionType permitRuleActionType2 = new ActionType();
        amct                             = new ActionMatchType();

        //here the rule is apply when the action on ressource is equal to "read"
        amct.setMatchId("urn:oasis:names:tc:xacml:1.0:function:string-equal");
        amct.setAttributeValue(PolicyAttributeFactory.createStringAttributeType("write"));
        amct.setActionAttributeDesignator(
                PolicyAttributeFactory.createAttributeDesignatorType(XACMLConstants.ATTRIBUTEID_ACTION_ID.key,
                                                                     XMLSchemaConstants.DATATYPE_STRING.key,
                                                                     null, true));
        
        permitRuleActionType2.getActionMatch().add(amct);
        
        permitRuleTargetType = new TargetType();
        permitRuleActionsType.getAction().add(permitRuleActionType1);
        permitRuleActionsType.getAction().add(permitRuleActionType2);
        permitRuleTargetType.setActions(permitRuleActionsType);
        permitRule2.setTarget(permitRuleTargetType);
        
        // now we create the condition to fill to permit the access
        permitRuleConditionType = new ConditionType();
        functionType            = new FunctionType();
        functionType.setFunctionId(XACMLConstants.FUNCTION_STRING_EQUAL.key);
        jaxbElementFunctionType = objectFactory.createExpression(functionType);
        permitRuleConditionType.setExpression(jaxbElementFunctionType);
        permitRuleApplyType     = new ApplyType();
        
        // the condition is: "admin" is in role id 
        permitRuleApplyType.setFunctionId(XACMLConstants.FUNCTION_STRING_IS_IN.key);
        sadt = PolicyAttributeFactory.createSubjectAttributeDesignatorType(XACMLConstants.ATTRIBUTEID_ROLE.key,
                                                                           XMLSchemaConstants.DATATYPE_STRING.key,
                                                                           null, true, null);
        
        sadtElement = objectFactory.createSubjectAttributeDesignator(sadt);
        avt = PolicyAttributeFactory.createStringAttributeType("admin");
        
        jaxbAVT = objectFactory.createAttributeValue(avt);
        permitRuleApplyType.getExpression().add(jaxbAVT);
        permitRuleApplyType.getExpression().add(sadtElement);
        permitRuleConditionType.setExpression(objectFactory.createApply(permitRuleApplyType));
        permitRule2.setCondition(permitRuleConditionType);
        policyType.getCombinerParametersOrRuleCombinerParametersOrVariableDefinition().add(permitRule2);
        
        
        //Create a Deny Rule wich is applied if the precedent rule doens't match. it refuse always the access.
        RuleType denyRule = new RuleType();
        denyRule.setRuleId("DenyRule");
        denyRule.setEffect(EffectType.DENY);
        policyType.getCombinerParametersOrRuleCombinerParametersOrVariableDefinition().add(denyRule);
        
        //we return the result
        return policyType;
   }


}
