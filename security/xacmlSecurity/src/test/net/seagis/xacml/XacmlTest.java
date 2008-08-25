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
import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.security.Principal;
import java.security.acl.Group;

// JAXB dependencies
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;

// seagis dependencies
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
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
import net.seagis.xacml.policy.PolicySetType;
import net.seagis.xacml.policy.PolicyType;
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
    private boolean debug = false;
    
    /**
     * A Policy Decision Point which received xacml request and decide to give or not the acces to the resource.
     */
    private JBossPDP PDP;
    
    /**
     * A example Policy.
     */
    private net.seagis.xacml.policy.PolicyType policyType1;
    
    /**
     * The CSW Policy.
     */
    private net.seagis.xacml.policy.PolicyType CSWpolicy;
    
    /**
     * A policy unmarshaller
     */
    private Unmarshaller unmarshaller;
    
    /**
     * A policy marshaller
     */
    private  Marshaller marshaller;
    
    
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        
         JAXBContext jbcontext  = JAXBContext.newInstance("net.seagis.xacml.policy");
         unmarshaller           = jbcontext.createUnmarshaller();
         marshaller             = jbcontext.createMarshaller();
         marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
         PDP = new JBossPDP();

         net.seagis.xacml.policy.ObjectFactory factory = new net.seagis.xacml.policy.ObjectFactory(); 
                 
         //we construct an example policy
         policyType1                 = getExamplePolicy();
         XACMLPolicy policy1         = PolicyFactory.createPolicy(policyType1);
         JAXBElement<PolicyType> jb1 = factory.createPolicy(policyType1);
         
         //we build the csw policy
         CSWpolicy                   = getCSWPolicy();
         XACMLPolicy policy2         = PolicyFactory.createPolicy(CSWpolicy);
         JAXBElement<PolicyType> jb2 = factory.createPolicy(CSWpolicy);

         //we build a policySet 
         PolicySetType policySet = new PolicySetType();
         policySet.setDescription("a container of service policies");
         policySet.setPolicySetId("constellation-policyset");
         policySet.setPolicyCombiningAlgId("urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:deny-overrides");
         policySet.setVersion("2.0");
         
         //we build the target of the policySet
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
         rmt.setAttributeValue(PolicyAttributeFactory.createAnyURIAttributeType(new URI("http://someurl")));
         resourceType.getResourceMatch().add(rmt);
         resourcesType.getResource().add(resourceType);
         targetType.setResources(resourcesType);
         policySet.setTarget(targetType);
         
         //we add the policies to the policy set
         policySet.getPolicySetOrPolicyOrPolicySetIdReference().add(jb1);
         policySet.getPolicySetOrPolicyOrPolicySetIdReference().add(jb2);
         
         if (debug)
             marshaller.marshal(policySet, System.out);
         
         
         XACMLPolicy policySet1 = PolicyFactory.createPolicySet(policySet);
         List<XACMLPolicy> poli = new ArrayList<XACMLPolicy>();
         poli.add(policy1);
         poli.add(policy2);
         policySet1.setEnclosingPolicies(poli);
         
         // we add the policies to the PDP
         Set<XACMLPolicy> policies = new HashSet<XACMLPolicy>();
         policies.add(policySet1);
         PDP.setPolicies(policies);
         
        
         //Add the basic locators also
         PolicyLocator policyLocator = new JBossPolicyLocator(policies);
        
         //Locators need to be given the policies
         Set<PolicyLocator> locators = new HashSet<PolicyLocator>();
         locators.add(policyLocator);
         PDP.setLocators(locators);
    }

    @After
    public void tearDown() throws Exception {
    }
    
    /**
     * Test the build of a PDP with a object model. 
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void ObjectModelPDPTest() throws Exception {

        logger.info('\n' + "-------- Object Model PDP Test --------" + '\n');
        
        PDP = new JBossPDP();
        
        JAXBContext jbcontext = JAXBContext.newInstance("net.seagis.xacml.policy");
        Marshaller marshaller    = jbcontext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        if (debug)
            marshaller.marshal(policyType1, System.out);
        
        XACMLPolicy policy = PolicyFactory.createPolicy(policyType1);
        Set<XACMLPolicy> policies = new HashSet<XACMLPolicy>();
        policies.add(policy);
       
        //Pass a set of policies (and/or policy sets) to the PDP
        PDP.setPolicies(policies);

        assertNotNull(PDP);
        
        logger.info('\n' + "-------- Fin Object Model PDP Test --------" + '\n');
    }
    
    /**
     * Test sending a request to the PDP with an user of the group developer.
     * The response must be positive
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void testPositiveWebBinding() throws Exception {
        
        logger.info('\n' + "-------- Positive Web Binding Test --------" + '\n');
        assertNotNull(PDP);
        PEP pep = new PEP(PDP);
        
        //we create an user
        Principal p = new PrincipalImpl("testuser");
        
        //we create Role Group
        Group grp_developer = new GroupImpl("developer");
        Group grp_admin     = new GroupImpl("admin");
        String requestURI   = "http://test/developer-guide.html";
        
        //Check PERMIT condition
        RequestContext request = pep.createXACMLRequest(requestURI, p, grp_developer, "read");
        if (debug) {
            logger.info("Positive Web Binding request: role='developer' action='read'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals("Access Allowed?", XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, p, grp_admin, "read");
        if (debug) {
            logger.info("Positive Web Binding request: role='adminr' action='write'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals("Access Allowed?", XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, p, grp_admin, "write");
        if (debug) {
            logger.info("Positive Web Binding request: role='adminr' action='write'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals("Access Allowed?", XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        logger.info('\n' + "-------- Fin Positive Web Binding Test --------" + '\n');
    }
    
    /**
     * Test sending a request to the PDP with an user of the group imposter.
     * The response must be negative.
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void testNegativeAccessWebBinding() throws Exception {
        
        logger.info('\n' + "-------- Negative Web Binding Test --------" + '\n');
        assertNotNull(PDP);
        PEP pep = new PEP(PDP);
        
        //we create an user
        Principal p = new PrincipalImpl("testuser");
        
        //we create Role Group
        Group grp_imposter  = new GroupImpl("imposter");
        Group grp_developer = new GroupImpl("developer");
        
        String requestURI = "http://test/developer-guide.html";
        
        //Check DENY condition
        RequestContext request = pep.createXACMLRequest(requestURI, p, grp_imposter, "read");
        
        if (debug) {
            logger.info("Negative Web Binding request: role= 'imposter' action='read' ");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals(XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
        //Check DENY condition
        request = pep.createXACMLRequest(requestURI, p, grp_developer, "write");
        
        if (debug) {
            logger.info("Negative Web Binding request: role= 'developer' action='write' ");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals("Access Disallowed?", XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
        logger.info('\n' + "-------- Fin Negative Web Binding Test --------" + '\n');
    }


    /**
     * Test sending a request to the PDP with an user of the group imposter.
     * The response must be negative.
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void testCSWPolicy() throws Exception {
        
        logger.info('\n' + "-------- CSW Policy Test --------" + '\n');
        
        assertNotNull(PDP);
        PEP pep = new PEP(PDP);
        
        //we create an user
        Principal p = new PrincipalImpl("testuser");
        
        //we create Role Group
        Group grp_anomymous = new GroupImpl("anonymous");
        Group grp_admin     = new GroupImpl("admin");
        String requestURI   = "http://test.geomatys.fr/csw";
        
        //Check PERMIT condition
        RequestContext request = pep.createXACMLRequest(requestURI, p, grp_anomymous, "getcapabilities");
        if (debug) {
            logger.info("csw request: role='anonymous' action='getCapabilities'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals("Access Allowed?", XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, p, grp_anomymous, "getrecords");
        if (debug) {
            logger.info("csw request: role='anonymous' action='getRecords'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals("Access Allowed?", XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check DENY condition
        request = pep.createXACMLRequest(requestURI, p, grp_anomymous, "transaction");
        if (debug) {
            logger.info("csw GetCapabilities request: role='anonymous' action='transaction'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals("Access Allowed?", XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, p, grp_admin, "transaction");
        if (debug) {
            logger.info("csw GetCapabilities request: role='anonymous' action='transaction'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals("Access Allowed?", XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        logger.info('\n' + "-------- Fin CSW Policy Test --------" + '\n');
    }
    
    /**
     * Build an example Policy.
     * 
     * This policy file basically provides "read" access to the url when the subject has a role of "developer".
     * It also provides "read/write" access to the url when the subject has a role of "admin".
     * 
     * All other requests are denied because of the explicit rule at the bottom of the policy file,
     * without which the PDP would have returned a decision of NotAPPLICABLE.
     * 
     * @return
     * @throws java.lang.Exception
     */
    private net.seagis.xacml.policy.PolicyType getExamplePolicy() throws Exception {
        
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
    
    private net.seagis.xacml.policy.PolicyType getCSWPolicy() throws JAXBException {
        String url     = "net/seagis/xacml/cswPolicy.xml";
        InputStream is = SecurityActions.getResourceAsStream(url);
        JAXBElement<PolicyType> jb2 = (JAXBElement<PolicyType>) unmarshaller.unmarshal(is);  
        return jb2.getValue(); 
    }
    
    /**
     * An temporary implementations of java.security.acl.group
     */
    private class GroupImpl implements Group {

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
    
    private class PrincipalImpl implements Principal {

        private String name;
        
        public PrincipalImpl(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
        
    }
}
