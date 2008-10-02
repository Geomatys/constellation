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
package org.constellation.xacml;

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

// Constellation dependencies
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.constellation.xacml.api.PolicyLocator;
import org.constellation.xacml.api.RequestContext;
import org.constellation.xacml.api.XACMLPolicy;
import org.constellation.xacml.factory.FactoryException;
import org.constellation.xacml.factory.PolicyAttributeFactory;
import org.constellation.xacml.factory.PolicyFactory;
import org.constellation.xacml.locators.JBossPolicyLocator;
import org.constellation.xacml.policy.ActionMatchType;
import org.constellation.xacml.policy.ActionType;
import org.constellation.xacml.policy.ActionsType;
import org.constellation.xacml.policy.ApplyType;
import org.constellation.xacml.policy.AttributeValueType;
import org.constellation.xacml.policy.ConditionType;
import org.constellation.xacml.policy.EffectType;
import org.constellation.xacml.policy.ExpressionType;
import org.constellation.xacml.policy.FunctionType;
import org.constellation.xacml.policy.ObjectFactory;
import org.constellation.xacml.policy.PolicySetType;
import org.constellation.xacml.policy.PolicyType;
import org.constellation.xacml.policy.ResourceMatchType;
import org.constellation.xacml.policy.ResourceType;
import org.constellation.xacml.policy.ResourcesType;
import org.constellation.xacml.policy.RuleType;
import org.constellation.xacml.policy.SubjectAttributeDesignatorType;
import org.constellation.xacml.policy.TargetType;

// Junit dependencies
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal
 */
public class XacmlTest {
    
    private Logger logger = Logger.getLogger("org.constellation.metadata");
   
    /**
     * enable the debug logging system 
     */
    private boolean debug = false;
    
    /**
     * A Policy Decision Point which received xacml request and decide to give or not the acces to the resource.
     */
    private CstlPDP PDP;
    
    /**
     * A Policy Enforcement Point whitch send xacml request to the PDP and retrieve the decision of the it.
     */
    private PEP pep;
    
    /**
     * A policy unmarshaller
     */
    private Unmarshaller unmarshaller;
    
    /**
     * A policy marshaller
     */
    private  Marshaller marshaller;
    
    private Principal user      = new PrincipalImpl("testuser");
    private Group grp_anomymous = new GroupImpl("anonymous");
    private Group grp_admin     = new GroupImpl("admin");
    private Group grp_developer = new GroupImpl("developer");
    private Group grp_imposter  = new GroupImpl("imposter");
    
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        
         JAXBContext jbcontext  = JAXBContext.newInstance("org.constellation.xacml.policy");
         unmarshaller           = jbcontext.createUnmarshaller();
         marshaller             = jbcontext.createMarshaller();
         marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

         //we construct an example policy
         PolicyType policyType1 = getExamplePolicy();
         
         //we build the services policies
         List<PolicyType> WservicePolicies = getWebServicePolicies("cswPolicy.xml", "sosPolicy.xml");
         WservicePolicies.add(policyType1);
                
         //we build a policySet 
         PolicySetType policySet     = buildSimplePolicySet(WservicePolicies);
         if (debug)
             marshaller.marshal(policySet, System.out);
         
         PDP = new CstlPDP(policySet);
         pep = new PEP(PDP);
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
        
        PDP = new CstlPDP();
        PolicyType examplePolicy = getExamplePolicy();
        
        if (debug)
            marshaller.marshal(examplePolicy, System.out);
        
        XACMLPolicy policy = PolicyFactory.createPolicy(examplePolicy);
        Set<XACMLPolicy> policies = new HashSet<XACMLPolicy>();
        policies.add(policy);
       
        //Pass a set of policies (and/or policy sets) to the PDP
        PDP.setPolicies(policies);
        
        //Add the basic locators also
        PolicyLocator policyLocator = new JBossPolicyLocator();
        policyLocator.setPolicies(policies);
        
        //Locators need to be given the policies
        Set<PolicyLocator> locators = new HashSet<PolicyLocator>();
        locators.add(policyLocator);
        PDP.setLocators(locators);
            
        pep = new PEP(PDP);

        assertNotNull(PDP);
        
        String requestURI   = "http://test/developer-guide.html";
        
        //Check PERMIT condition
        RequestContext request = pep.createXACMLRequest(requestURI, user, grp_developer, "read");
        if (debug) {
            logger.info("Positive Web Binding request: role='developer' action='read'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "read");
        if (debug) {
            logger.info("Positive Web Binding request: role='adminr' action='write'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "write");
        if (debug) {
            logger.info("Positive Web Binding request: role='adminr' action='write'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check DENY condition
        request = pep.createXACMLRequest(requestURI, user, grp_imposter, "read");
        
        if (debug) {
            logger.info("Negative Web Binding request: role= 'imposter' action='read' ");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals(XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
        //Check DENY condition
        request = pep.createXACMLRequest(requestURI, user, grp_developer, "write");
        
        if (debug) {
            logger.info("Negative Web Binding request: role= 'developer' action='write' ");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals("Access Disallowed?", XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
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

        String requestURI   = "http://test/developer-guide.html";
        
        //Check PERMIT condition
        RequestContext request = pep.createXACMLRequest(requestURI, user, grp_developer, "read");
        if (debug) {
            logger.info("Positive Web Binding request: role='developer' action='read'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "read");
        if (debug) {
            logger.info("Positive Web Binding request: role='adminr' action='write'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "write");
        if (debug) {
            logger.info("Positive Web Binding request: role='adminr' action='write'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
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
        
        String requestURI = "http://test/developer-guide.html";
        
        //Check DENY condition
        RequestContext request = pep.createXACMLRequest(requestURI, user, grp_imposter, "read");
        
        if (debug) {
            logger.info("Negative Web Binding request: role= 'imposter' action='read' ");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals(XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
        //Check DENY condition
        request = pep.createXACMLRequest(requestURI, user, grp_developer, "write");
        
        if (debug) {
            logger.info("Negative Web Binding request: role= 'developer' action='write' ");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals("Access Disallowed?", XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
        logger.info('\n' + "-------- Fin Negative Web Binding Test --------" + '\n');
    }


    /**
     * Test sending CSW request to the PDP.
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void testCSWPolicy() throws Exception {
        
        logger.info('\n' + "-------- CSW Policy Test --------" + '\n');
        
        assertNotNull(PDP);

        String requestURI   = "http://test.geomatys.fr/constellation/WS/csw";
        
        //Check PERMIT condition
        RequestContext request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "getcapabilities");
        if (debug) {
            logger.info("csw request: role='anonymous' action='getCapabilities'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "getrecords");
        if (debug) {
            logger.info("csw request: role='anonymous' action='getRecords'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check DENY condition
        request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "transaction");
        if (debug) {
            logger.info("csw request: role='anonymous' action='transaction'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals( XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "transaction");
        if (debug) {
            logger.info("csw request: role='admin' action='transaction'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "harvest");
        if (debug) {
            logger.info("csw request: role='admin' action='harvest'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
         //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "getcapabilities");
        if (debug) {
            logger.info("csw request: role='admin' action='harvest'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check DENY condition
        request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "whatever");
        if (debug) {
            logger.info("csw request: role='anonymous' action='whatever'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals( XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
         //Check DENY condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "whatever");
        if (debug) {
            logger.info("csw request: role='admin' action='whatever'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals( XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
        logger.info('\n' + "-------- Fin CSW Policy Test --------" + '\n');
    }
    
    /**
     * Test sending CSW request to the PDP.
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void testCSWPolicy2() throws Exception {
        
        logger.info('\n' + "-------- CSW Policy Test 2--------" + '\n');
        
        initializePolicyDecisionPoint();
                
        assertNotNull(PDP);

        String requestURI   = "http://test.geomatys.fr/constellation/WS/csw";
        
        //Check PERMIT condition
        RequestContext request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "getcapabilities");
        if (debug) {
            logger.info("csw request: role='anonymous' action='getCapabilities'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "getrecords");
        if (debug) {
            logger.info("csw request: role='anonymous' action='getRecords'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check DENY condition
        request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "transaction");
        if (debug) {
            logger.info("csw request: role='anonymous' action='transaction'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals( XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "transaction");
        if (debug) {
            logger.info("csw request: role='admin' action='transaction'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "harvest");
        if (debug) {
            logger.info("csw request: role='admin' action='harvest'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check DENY condition
        request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "whatever");
        if (debug) {
            logger.info("csw request: role='anonymous' action='whatever'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals( XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
         //Check DENY condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "whatever");
        if (debug) {
            logger.info("csw request: role='admin' action='whatever'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals( XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
        logger.info('\n' + "-------- Fin CSW Policy Test --------" + '\n');
    }
    
    /**
     * Initialize the policy Decision Point and load all the correspounding policy file.
     */
    private void initializePolicyDecisionPoint() {
        
        //we create a new PDP
        PDP = new CstlPDP();

        //load the correspounding policy file
        String url = "org/constellation/xacml/" + "csw" + "Policy.xml";
        InputStream is = SecurityActions.getResourceAsStream(url);
        if (is == null) {
            logger.severe("unable to find the resource: " + url);
            return;
        }
        Object p = null;
        try {
            JAXBContext jbcontext = JAXBContext.newInstance("org.constellation.xacml.policy");
            Unmarshaller policyUnmarshaller = jbcontext.createUnmarshaller();
            p = policyUnmarshaller.unmarshal(is);
        } catch (JAXBException e) {
            logger.severe("JAXB exception while unmarshalling policyFile " + "csw" + "Policy.xml");
        }
        
        if (p instanceof JAXBElement) {
            p = ((JAXBElement)p).getValue();
        } 
        
        if (p == null) {
            logger.severe("the unmarshalled service policy is null.");
            return;
        } else if (!(p instanceof PolicyType)) {
            logger.severe("unknow unmarshalled type for service policy file:" + p.getClass());
            return;
        }
        PolicyType servicePolicy  = (PolicyType) p;
        
        try {
            XACMLPolicy policy = PolicyFactory.createPolicy(servicePolicy);
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
            
            pep = new PEP(PDP);
            
        } catch (FactoryException e) {
            logger.severe("Factory exception while initializing Policy Decision Point: " + e.getMessage());
        }
        logger.info("PDP succesfully initialized");
    }
    
    /**
     * Test sending SOS request to the PDP.
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void testSOSPolicy() throws Exception {
        
        logger.info('\n' + "-------- SOS Policy Test --------" + '\n');
        
        assertNotNull(PDP);
                               
        String requestURI   = "http://test.geomatys.fr/constellation/WS/sos";
        
        //Check PERMIT condition
        RequestContext request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "getcapabilities");
        if (debug) {
            logger.info("sos request: role='anonymous' action='getCapabilities'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "describesensor");
        if (debug) {
            logger.info("sos request: role='anonymous' action='describesensor'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "getobservation");
        if (debug) {
            logger.info("sos request: role='anonymous' action='getobservation'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "getobservation");
        if (debug) {
            logger.info("sos request: role='anonymous' action='getobservation'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        
        //Check DENY condition
        request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "registersensor");
        if (debug) {
            logger.info("sos request: role='anonymous' action='registersensor'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals( XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "registersensor");
        if (debug) {
            logger.info("sos request: role='admin' action='registersensor'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "insertobservation");
        if (debug) {
            logger.info("sos request: role='admin' action='insertObservation'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check DENY condition
        request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "insertobservation");
        if (debug) {
            logger.info("sos request: role='anonymous' action='insertObservation'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals( XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
        //Check DENY condition
        request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "whatever");
        if (debug) {
            logger.info("sos request: role='anonymous' action='whatever'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals( XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
         //Check DENY condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "whatever");
        if (debug) {
            logger.info("sos request: role='admin' action='whatever'");
            request.marshall(System.out);
            logger.info("");
        }
        assertEquals( XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
        logger.info('\n' + "-------- Fin SOS Policy Test --------" + '\n');
    }
    
    
    /**
     * Test sending WMS/WCS request to the PDP.
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void testWMSPolicy() throws Exception {
        
        logger.info('\n' + "-------- WMS Policy Test --------" + '\n');
        
         //we get the coverage policySet
         List<PolicyType> WservicePolicies = getWebServicePolicies("wmsPolicy.xml");
         PolicySetType coveragePolicySet = buildSimplePolicySet(WservicePolicies);
         
         //we build a policySet 
         PolicySetType policySet = buildComplexPolicySet(WservicePolicies, coveragePolicySet);
                  
         
         if (debug)
             marshaller.marshal(policySet, System.out);
         
         PDP = new CstlPDP(policySet);
         pep = new PEP(PDP);
        

        /**
         * wms TEST
         */
        String requestURI   = "http://demo.geomatys.fr/constellation/WS/wms";
        
        //Check PERMIT condition
        RequestContext request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "BlueMarble");
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check DENY condition
        request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "Caraibes");
        assertEquals( XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "BlueMarble");
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "Caraibes");
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        
        logger.info('\n' + "-------- Fin WMS Policy Test --------" + '\n');
    }
    
    
    /**
     * Test sending WMS/WCS request to the PDP.
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void testCoveragePolicy() throws Exception {
        
        logger.info('\n' + "-------- Coverage Policy Test --------" + '\n');
        
         //we get the coverage policySet
         List<PolicyType> WservicePolicies = getWebServicePolicies("wmsPolicy.xml", "wcsPolicy.xml");
         PolicySetType coveragePolicySet = buildSimplePolicySet(WservicePolicies);
         
         //we build a policySet 
         PolicySetType policySet = buildComplexPolicySet(WservicePolicies, coveragePolicySet);
                  
         
         if (debug)
             marshaller.marshal(policySet, System.out);
         
         PDP = new CstlPDP(policySet);
         pep = new PEP(PDP);
        

        /**
         * wms TEST
         */
        String requestURI   = "http://demo.geomatys.fr/constellation/WS/wms";
        
        //Check PERMIT condition
        RequestContext request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "layer3");
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "layer4");
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check DENY condition
        request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "layer2");
        assertEquals( XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
        //Check DENY condition
        request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "layer1");
        assertEquals( XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "layer2");
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "layer1");
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        
        /**
         * wcs TEST
         */
        requestURI   = "http://test.geomatys.fr/constellation/WS/wcs";
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "layer1");
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "layer2");
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check DENY condition
        request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "layer3");
        assertEquals( XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
        //Check DENY condition
        request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "layer4");
        assertEquals( XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "layer3");
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "layer4");
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        logger.info('\n' + "-------- Fin Coverage Policy Test --------" + '\n');
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
    private org.constellation.xacml.policy.PolicyType getExamplePolicy() throws Exception {
        
        ObjectFactory objectFactory = new ObjectFactory();
        String PERMIT_OVERRIDES = "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:permit-overrides";
        org.constellation.xacml.policy.PolicyType policyType = new org.constellation.xacml.policy.PolicyType();
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
    
    /**
     * Build The CSW Policy from a resource file.
     * 
     * @return
     * @throws javax.xml.bind.JAXBException
     */
    private List<PolicyType> getWebServicePolicies(String... policyNames) throws JAXBException {
        List<PolicyType> policies = new ArrayList<PolicyType>();
        
        for (String policyName: policyNames) {
            InputStream is             = SecurityActions.getResourceAsStream("org/constellation/xacml/" + policyName);
            JAXBElement<PolicyType> jb = (JAXBElement<PolicyType>) unmarshaller.unmarshal(is);  
            policies.add(jb.getValue());
        }
        
        
        return policies;
    }
    
    /**
     * Build a policy Set with containing the specified policies.
     * 
     * @param policies
     * @return
     * @throws java.lang.Exception
     */
    private PolicySetType buildSimplePolicySet(List<PolicyType> policies) throws Exception {
        
        PolicySetType policySet = new PolicySetType();
        policySet.setDescription("a container of service policies");
        policySet.setPolicySetId("constellation-policyset");
        policySet.setPolicyCombiningAlgId("urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:deny-overrides");
        policySet.setVersion("2.0");
         
        //we build the target of the policySet (no target)
        policySet.setTarget(new TargetType());
         
        //we add the policies to the policy set
        org.constellation.xacml.policy.ObjectFactory factory = new org.constellation.xacml.policy.ObjectFactory();
        
        for (PolicyType p : policies) {
            JAXBElement<PolicyType> jb = factory.createPolicy(p);
            policySet.getPolicySetOrPolicyOrPolicySetIdReference().add(jb);
        }
            
        return policySet;
    }
    
    /**
     * Build a policy Set with containing the specified policies and another policySet.
     * 
     * @param policies
     * @return
     * @throws java.lang.Exception
     */
    private PolicySetType buildComplexPolicySet(List<PolicyType> policies, PolicySetType policySet2) throws Exception {
        
        PolicySetType policySet = new PolicySetType();
        policySet.setDescription("a container of service policies");
        policySet.setPolicySetId("constellation-policyset");
        policySet.setPolicyCombiningAlgId("urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:deny-overrides");
        policySet.setVersion("2.0");
         
        //we build the target of the policySet (no target)
        policySet.setTarget(new TargetType());
         
        //we add the policies to the policy set
        org.constellation.xacml.policy.ObjectFactory factory = new org.constellation.xacml.policy.ObjectFactory();
        
        for (PolicyType p : policies) {
            JAXBElement<PolicyType> jb = factory.createPolicy(p);
            policySet.getPolicySetOrPolicyOrPolicySetIdReference().add(jb);
        }
        
        policySet.getPolicySetOrPolicyOrPolicySetIdReference().add(factory.createPolicySet(policySet2));
            
        return policySet;
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
