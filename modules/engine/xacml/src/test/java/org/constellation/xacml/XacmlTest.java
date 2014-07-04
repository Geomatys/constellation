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

// J2SE dependencies

import org.constellation.xacml.api.PolicyLocator;
import org.constellation.xacml.api.RequestContext;
import org.constellation.xacml.api.XACMLPolicy;
import org.constellation.xacml.factory.FactoryException;
import org.constellation.xacml.factory.PolicyAttributeFactory;
import org.constellation.xacml.factory.PolicyFactory;
import org.constellation.xacml.locators.JBossPolicyLocator;
import org.geotoolkit.xacml.xml.XACMLMarshallerPool;
import org.geotoolkit.xacml.xml.policy.ActionMatchType;
import org.geotoolkit.xacml.xml.policy.ActionType;
import org.geotoolkit.xacml.xml.policy.ActionsType;
import org.geotoolkit.xacml.xml.policy.ApplyType;
import org.geotoolkit.xacml.xml.policy.AttributeValueType;
import org.geotoolkit.xacml.xml.policy.ConditionType;
import org.geotoolkit.xacml.xml.policy.EffectType;
import org.geotoolkit.xacml.xml.policy.ExpressionType;
import org.geotoolkit.xacml.xml.policy.FunctionType;
import org.geotoolkit.xacml.xml.policy.ObjectFactory;
import org.geotoolkit.xacml.xml.policy.PolicySetType;
import org.geotoolkit.xacml.xml.policy.PolicyType;
import org.geotoolkit.xacml.xml.policy.ResourceMatchType;
import org.geotoolkit.xacml.xml.policy.ResourceType;
import org.geotoolkit.xacml.xml.policy.ResourcesType;
import org.geotoolkit.xacml.xml.policy.RuleType;
import org.geotoolkit.xacml.xml.policy.SubjectAttributeDesignatorType;
import org.geotoolkit.xacml.xml.policy.TargetType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.net.URI;
import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// JAXB dependencies
// Constellation dependencies
// Junit dependencies

/**
 *
 * @author Guilhem Legal
 */
public class XacmlTest {
    
    private static final Logger LOGGER = Logger.getLogger("org.constellation.xacml");
   
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
        
         //we construct an example policy
         PolicyType policyType1 = getExamplePolicy();
         
         //we build the services policies
         List<PolicyType> WservicePolicies = getWebServicePolicies("cswPolicy.xml", "sosPolicy.xml");
         WservicePolicies.add(policyType1);
                
         //we build a policySet 
         PolicySetType policySet     = buildSimplePolicySet(WservicePolicies);
         if (debug) {
             Marshaller marshaller = XACMLMarshallerPool.getInstance().acquireMarshaller();
             marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
             marshaller.marshal(policySet, System.out);
             XACMLMarshallerPool.getInstance().recycle(marshaller);
         }
         
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

        LOGGER.finer('\n' + "-------- Object Model PDP Test --------" + '\n');
        
        PDP = new CstlPDP();
        PolicyType examplePolicy = getExamplePolicy();

        if (debug) {
             Marshaller marshaller = XACMLMarshallerPool.getInstance().acquireMarshaller();
             marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
             marshaller.marshal(examplePolicy, System.out);
             XACMLMarshallerPool.getInstance().recycle(marshaller);
         }
        
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
            LOGGER.finer("Positive Web Binding request: role='developer' action='read'");
            request.marshall(System.out);
            LOGGER.finer("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "read");
        if (debug) {
            LOGGER.finer("Positive Web Binding request: role='adminr' action='write'");
            request.marshall(System.out);
            LOGGER.finer("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "write");
        if (debug) {
            LOGGER.finer("Positive Web Binding request: role='adminr' action='write'");
            request.marshall(System.out);
            LOGGER.finer("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check DENY condition
        request = pep.createXACMLRequest(requestURI, user, grp_imposter, "read");
        
        if (debug) {
            LOGGER.finer("Negative Web Binding request: role= 'imposter' action='read' ");
            request.marshall(System.out);
            LOGGER.finer("");
        }
        assertEquals(XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
        //Check DENY condition
        request = pep.createXACMLRequest(requestURI, user, grp_developer, "write");
        
        if (debug) {
            LOGGER.finer("Negative Web Binding request: role= 'developer' action='write' ");
            request.marshall(System.out);
            LOGGER.finer("");
        }
        assertEquals("Access Disallowed?", XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
        LOGGER.finer('\n' + "-------- Fin Object Model PDP Test --------" + '\n');
    }
    
    /**
     * Test sending a request to the PDP with an user of the group developer.
     * The response must be positive
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void testPositiveWebBinding() throws Exception {
        
        LOGGER.finer('\n' + "-------- Positive Web Binding Test --------" + '\n');
        assertNotNull(PDP);

        String requestURI   = "http://test/developer-guide.html";
        
        //Check PERMIT condition
        RequestContext request = pep.createXACMLRequest(requestURI, user, grp_developer, "read");
        if (debug) {
            LOGGER.finer("Positive Web Binding request: role='developer' action='read'");
            request.marshall(System.out);
            LOGGER.finer("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "read");
        if (debug) {
            LOGGER.finer("Positive Web Binding request: role='adminr' action='write'");
            request.marshall(System.out);
            LOGGER.finer("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "write");
        if (debug) {
            LOGGER.finer("Positive Web Binding request: role='adminr' action='write'");
            request.marshall(System.out);
            LOGGER.finer("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        LOGGER.finer('\n' + "-------- Fin Positive Web Binding Test --------" + '\n');
    }
    
    /**
     * Test sending a request to the PDP with an user of the group imposter.
     * The response must be negative.
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void testNegativeAccessWebBinding() throws Exception {
        
        LOGGER.finer('\n' + "-------- Negative Web Binding Test --------" + '\n');
        assertNotNull(PDP);
        
        String requestURI = "http://test/developer-guide.html";
        
        //Check DENY condition
        RequestContext request = pep.createXACMLRequest(requestURI, user, grp_imposter, "read");
        
        if (debug) {
            LOGGER.finer("Negative Web Binding request: role= 'imposter' action='read' ");
            request.marshall(System.out);
            LOGGER.finer("");
        }
        assertEquals(XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
        //Check DENY condition
        request = pep.createXACMLRequest(requestURI, user, grp_developer, "write");
        
        if (debug) {
            LOGGER.finer("Negative Web Binding request: role= 'developer' action='write' ");
            request.marshall(System.out);
            LOGGER.finer("");
        }
        assertEquals("Access Disallowed?", XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
        LOGGER.finer('\n' + "-------- Fin Negative Web Binding Test --------" + '\n');
    }


    /**
     * Test sending CSW request to the PDP.
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void testCSWPolicy() throws Exception {
        
        LOGGER.finer('\n' + "-------- CSW Policy Test --------" + '\n');
        
        assertNotNull(PDP);

        String requestURI   = "http://test.geomatys.fr/constellation/WS/csw";
        
        //Check PERMIT condition
        RequestContext request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "getcapabilities");
        if (debug) {
            LOGGER.finer("csw request: role='anonymous' action='getCapabilities'");
            request.marshall(System.out);
            LOGGER.finer("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "getrecords");
        if (debug) {
            LOGGER.finer("csw request: role='anonymous' action='getRecords'");
            request.marshall(System.out);
            LOGGER.finer("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check DENY condition
        request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "transaction");
        if (debug) {
            LOGGER.finer("csw request: role='anonymous' action='transaction'");
            request.marshall(System.out);
            LOGGER.finer("");
        }
        assertEquals( XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "transaction");
        if (debug) {
            LOGGER.finer("csw request: role='admin' action='transaction'");
            request.marshall(System.out);
            LOGGER.finer("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "harvest");
        if (debug) {
            LOGGER.finer("csw request: role='admin' action='harvest'");
            request.marshall(System.out);
            LOGGER.finer("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
         //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "getcapabilities");
        if (debug) {
            LOGGER.finer("csw request: role='admin' action='harvest'");
            request.marshall(System.out);
            LOGGER.finer("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check DENY condition
        request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "whatever");
        if (debug) {
            LOGGER.finer("csw request: role='anonymous' action='whatever'");
            request.marshall(System.out);
            LOGGER.finer("");
        }
        assertEquals( XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
         //Check DENY condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "whatever");
        if (debug) {
            LOGGER.finer("csw request: role='admin' action='whatever'");
            request.marshall(System.out);
            LOGGER.finer("");
        }
        assertEquals( XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
        LOGGER.finer('\n' + "-------- Fin CSW Policy Test --------" + '\n');
    }
    
    /**
     * Test sending CSW request to the PDP.
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void testCSWPolicy2() throws Exception {
        
        LOGGER.finer('\n' + "-------- CSW Policy Test 2--------" + '\n');
        
        initializePolicyDecisionPoint();
                
        assertNotNull(PDP);

        String requestURI   = "http://test.geomatys.fr/constellation/WS/csw";
        
        //Check PERMIT condition
        RequestContext request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "getcapabilities");
        if (debug) {
            LOGGER.finer("csw request: role='anonymous' action='getCapabilities'");
            request.marshall(System.out);
            LOGGER.finer("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "getrecords");
        if (debug) {
            LOGGER.finer("csw request: role='anonymous' action='getRecords'");
            request.marshall(System.out);
            LOGGER.finer("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check DENY condition
        request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "transaction");
        if (debug) {
            LOGGER.finer("csw request: role='anonymous' action='transaction'");
            request.marshall(System.out);
            LOGGER.finer("");
        }
        assertEquals( XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "transaction");
        if (debug) {
            LOGGER.finer("csw request: role='admin' action='transaction'");
            request.marshall(System.out);
            LOGGER.finer("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "harvest");
        if (debug) {
            LOGGER.finer("csw request: role='admin' action='harvest'");
            request.marshall(System.out);
            LOGGER.finer("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check DENY condition
        request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "whatever");
        if (debug) {
            LOGGER.finer("csw request: role='anonymous' action='whatever'");
            request.marshall(System.out);
            LOGGER.finer("");
        }
        assertEquals( XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
         //Check DENY condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "whatever");
        if (debug) {
            LOGGER.finer("csw request: role='admin' action='whatever'");
            request.marshall(System.out);
            LOGGER.finer("");
        }
        assertEquals( XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
        LOGGER.finer('\n' + "-------- Fin CSW Policy Test --------" + '\n');
    }
    
    /**
     * Initialize the policy Decision Point and load all the correspounding policy file.
     */
    private void initializePolicyDecisionPoint() throws FactoryException {
        
        //we create a new PDP
        PDP = new CstlPDP();

        //load the correspounding policy file
        String url = "org/constellation/xacml/" + "csw" + "Policy.xml";
        InputStream is = SecurityActions.getResourceAsStream(url);
        if (is == null) {
            LOGGER.severe("unable to find the resource: " + url);
            return;
        }
        Object p = null;
        try {
            final Unmarshaller unmarshaller = XACMLMarshallerPool.getInstance().acquireUnmarshaller();
            p = unmarshaller.unmarshal(is);
            XACMLMarshallerPool.getInstance().recycle(unmarshaller);
        } catch (JAXBException e) {
            LOGGER.severe("JAXB exception while unmarshalling policyFile " + "csw" + "Policy.xml");
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
            LOGGER.severe("Factory exception while initializing Policy Decision Point: " + e.getMessage());
        }
        LOGGER.finer("PDP succesfully initialized");
    }
    
    /**
     * Test sending SOS request to the PDP.
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void testSOSPolicy() throws Exception {
        
        LOGGER.finer('\n' + "-------- SOS Policy Test --------" + '\n');
        
        assertNotNull(PDP);
                               
        String requestURI   = "http://test.geomatys.fr/constellation/WS/sos";
        
        //Check PERMIT condition
        RequestContext request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "getcapabilities");
        if (debug) {
            LOGGER.finer("sos request: role='anonymous' action='getCapabilities'");
            request.marshall(System.out);
            LOGGER.finer("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "describesensor");
        if (debug) {
            LOGGER.finer("sos request: role='anonymous' action='describesensor'");
            request.marshall(System.out);
            LOGGER.finer("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "getobservation");
        if (debug) {
            LOGGER.finer("sos request: role='anonymous' action='getobservation'");
            request.marshall(System.out);
            LOGGER.finer("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "getobservation");
        if (debug) {
            LOGGER.finer("sos request: role='anonymous' action='getobservation'");
            request.marshall(System.out);
            LOGGER.finer("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        
        //Check DENY condition
        request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "registersensor");
        if (debug) {
            LOGGER.finer("sos request: role='anonymous' action='registersensor'");
            request.marshall(System.out);
            LOGGER.finer("");
        }
        assertEquals( XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "registersensor");
        if (debug) {
            LOGGER.finer("sos request: role='admin' action='registersensor'");
            request.marshall(System.out);
            LOGGER.finer("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "insertobservation");
        if (debug) {
            LOGGER.finer("sos request: role='admin' action='insertObservation'");
            request.marshall(System.out);
            LOGGER.finer("");
        }
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check DENY condition
        request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "insertobservation");
        if (debug) {
            LOGGER.finer("sos request: role='anonymous' action='insertObservation'");
            request.marshall(System.out);
            LOGGER.finer("");
        }
        assertEquals( XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
        //Check DENY condition
        request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "whatever");
        if (debug) {
            LOGGER.finer("sos request: role='anonymous' action='whatever'");
            request.marshall(System.out);
            LOGGER.finer("");
        }
        assertEquals( XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
         //Check DENY condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "whatever");
        if (debug) {
            LOGGER.finer("sos request: role='admin' action='whatever'");
            request.marshall(System.out);
            LOGGER.finer("");
        }
        assertEquals( XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
        LOGGER.finer('\n' + "-------- Fin SOS Policy Test --------" + '\n');
    }
    
    
    /**
     * Test sending WMS/WCS request to the PDP.
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void testWMSPolicy() throws Exception {
        
        LOGGER.finer('\n' + "-------- WMS Policy Test --------" + '\n');
        
         //we get the coverage policySet
         List<PolicyType> WservicePolicies = getWebServicePolicies("wmsPolicy.xml");
         PolicySetType coveragePolicySet = buildSimplePolicySet(WservicePolicies);
         
         //we build a policySet 
         PolicySetType policySet = buildComplexPolicySet(WservicePolicies, coveragePolicySet);
                  
         if (debug) {
             Marshaller marshaller = XACMLMarshallerPool.getInstance().acquireMarshaller();
             marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
             marshaller.marshal(policySet, System.out);
             XACMLMarshallerPool.getInstance().recycle(marshaller);
         }

         
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
        
        
        LOGGER.finer('\n' + "-------- Fin WMS Policy Test --------" + '\n');
    }
    
    
    /**
     * Test sending WMS/WCS request to the PDP.
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void testCoveragePolicy() throws Exception {
        
        LOGGER.finer('\n' + "-------- Coverage Policy Test --------" + '\n');
        
         //we get the coverage policySet
         List<PolicyType> WservicePolicies = getWebServicePolicies("wmsPolicy.xml", "wcsPolicy.xml");
         PolicySetType coveragePolicySet = buildSimplePolicySet(WservicePolicies);
         
         //we build a policySet 
         PolicySetType policySet = buildComplexPolicySet(WservicePolicies, coveragePolicySet);
                  
         
         if (debug) {
             Marshaller marshaller = XACMLMarshallerPool.getInstance().acquireMarshaller();
             marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
             marshaller.marshal(policySet, System.out);
             XACMLMarshallerPool.getInstance().recycle(marshaller);
         }
         
         PDP = new CstlPDP(policySet);
         pep = new PEP(PDP);
        

        /**
         * wms TEST
         */
        String requestURI   = "http://demo.geomatys.fr/constellation/WS/wms";
        
        //Check PERMIT condition
        RequestContext request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "BlueMarble");
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "GetCapabilities");
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check DENY condition
        request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "Caraibes");
        assertEquals( XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
        //Check DENY condition
        request = pep.createXACMLRequest(requestURI, user, grp_anomymous, "wathever");
        assertEquals( XACMLConstants.DECISION_DENY, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "Caraibes");
        assertEquals( XACMLConstants.DECISION_PERMIT, pep.getDecision(request));
        
        //Check PERMIT condition
        request = pep.createXACMLRequest(requestURI, user, grp_admin, "BlueMarble");
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
        
        LOGGER.finer('\n' + "-------- Fin Coverage Policy Test --------" + '\n');
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
    private org.geotoolkit.xacml.xml.policy.PolicyType getExamplePolicy() throws Exception {
        
        ObjectFactory objectFactory = new ObjectFactory();
        String PERMIT_OVERRIDES = "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:permit-overrides";
        org.geotoolkit.xacml.xml.policy.PolicyType policyType = new org.geotoolkit.xacml.xml.policy.PolicyType();
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
                XACMLConstants.ATTRIBUTEID_RESOURCE_RESOURCEID.key,
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
                PolicyAttributeFactory.createAttributeDesignatorType(XACMLConstants.ATTRIBUTEID_ACTION_ACTIONID.key,
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
        SubjectAttributeDesignatorType sadt = PolicyAttributeFactory.createSubjectAttributeDesignatorType(XACMLConstants.ATTRIBUTEID_SUBJECT_ROLE.key,
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
                PolicyAttributeFactory.createAttributeDesignatorType(XACMLConstants.ATTRIBUTEID_ACTION_ACTIONID.key,
                                                                     XMLSchemaConstants.DATATYPE_STRING.key,
                                                                     null, true));
        
        permitRuleActionType1.getActionMatch().add(amct);
        
        ActionType permitRuleActionType2 = new ActionType();
        amct                             = new ActionMatchType();

        //here the rule is apply when the action on ressource is equal to "read"
        amct.setMatchId("urn:oasis:names:tc:xacml:1.0:function:string-equal");
        amct.setAttributeValue(PolicyAttributeFactory.createStringAttributeType("write"));
        amct.setActionAttributeDesignator(
                PolicyAttributeFactory.createAttributeDesignatorType(XACMLConstants.ATTRIBUTEID_ACTION_ACTIONID.key,
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
        sadt = PolicyAttributeFactory.createSubjectAttributeDesignatorType(XACMLConstants.ATTRIBUTEID_SUBJECT_ROLE.key,
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

        Unmarshaller unmarshaller = XACMLMarshallerPool.getInstance().acquireUnmarshaller();

        for (String policyName: policyNames) {
            InputStream is             = SecurityActions.getResourceAsStream("org/constellation/xacml/" + policyName);
            JAXBElement<PolicyType> jb = (JAXBElement<PolicyType>) unmarshaller.unmarshal(is);  
            policies.add(jb.getValue());
        }
        XACMLMarshallerPool.getInstance().recycle(unmarshaller);
        
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
        org.geotoolkit.xacml.xml.policy.ObjectFactory factory = new org.geotoolkit.xacml.xml.policy.ObjectFactory();
        
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
        org.geotoolkit.xacml.xml.policy.ObjectFactory factory = new org.geotoolkit.xacml.xml.policy.ObjectFactory();
        
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
