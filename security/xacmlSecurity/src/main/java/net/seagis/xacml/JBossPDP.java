/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package net.seagis.xacml;

import com.sun.xacml.PDPConfig;
import com.sun.xacml.ctx.RequestCtx;
import com.sun.xacml.ctx.ResponseCtx;
import com.sun.xacml.finder.AttributeFinder;
import com.sun.xacml.finder.AttributeFinderModule;
import com.sun.xacml.finder.PolicyFinderModule;
import com.sun.xacml.finder.impl.CurrentEnvModule;
import com.sun.xacml.finder.impl.SelectorModule;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamReader;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import net.seagis.xacml.bridge.JBossPolicyFinder;
import net.seagis.xacml.factory.FactoryException;
import net.seagis.xacml.factory.PolicyFactory;
import net.seagis.xacml.factory.RequestResponseContextFactory;
import net.seagis.xacml.api.PolicyDecisionPoint;
import net.seagis.xacml.api.PolicyLocator;
import net.seagis.xacml.api.RequestContext;
import net.seagis.xacml.api.ResponseContext;
import net.seagis.xacml.api.XACMLPolicy;
import net.seagis.xacml.jaxb.LocatorType;
import net.seagis.xacml.jaxb.LocatorsType;
import net.seagis.xacml.jaxb.PDP;
import net.seagis.xacml.jaxb.PoliciesType;
import net.seagis.xacml.jaxb.PolicySetType;
import net.seagis.xacml.jaxb.PolicyType;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 *  PDP for JBoss XACML
 *  @author Anil.Saldhana@redhat.com
 *  @since  Jul 6, 2007
 *  @version $Revision$
 */
public class JBossPDP implements PolicyDecisionPoint {
    
    private Set<PolicyLocator> locators = new HashSet<PolicyLocator>();
    
    private Set<XACMLPolicy> policies = new HashSet<XACMLPolicy>();
    
    private final JBossPolicyFinder policyFinder = new JBossPolicyFinder();

    private Logger logger = Logger.getLogger("net.seagis.xacml");
    
    /**
     * CTR
     */
    public JBossPDP() {}

    /**
     * Create a PDP
     * @param configFile Inputstream for the JBossXACML Config File
     */
    public JBossPDP(final InputStream configFile) throws XACMLException {
        final JAXBElement<?> jxb;
        try {
            final Unmarshaller unmarshaller = createValidatingUnMarshaller();
            jxb = (JAXBElement<?>) unmarshaller.unmarshal(configFile);
        } catch (JAXBException j) {
            throw new RuntimeException(j);
        } catch (FileNotFoundException j) {
            throw new RuntimeException(j);
        }
        bootstrap((PDP) jxb.getValue());
    }

    /**
     * Create a PDP
     * @param configFile InputSource for the JBossXACML Config File
     */
    public JBossPDP(final InputSource configFile) throws XACMLException {
        final JAXBElement<?> jxb;
        try {
            final Unmarshaller unmarshaller = createValidatingUnMarshaller();
            jxb = (JAXBElement<?>) unmarshaller.unmarshal(configFile);
        } catch (JAXBException j) {
            throw new RuntimeException(j);
        } catch (FileNotFoundException j) {
            throw new RuntimeException(j);
        }
        bootstrap((PDP) jxb.getValue());
    }

    /**
     * Create a PDP
     * @param configFile Parsed Node for the JBossXACML Config File
     */
    public JBossPDP(final Node configFile) throws XACMLException {
        final JAXBElement<?> jxb;
        try {
            final Unmarshaller unmarshaller = createValidatingUnMarshaller();
            jxb = (JAXBElement<?>) unmarshaller.unmarshal(configFile);
        } catch (JAXBException j) {
            throw new RuntimeException(j);
        } catch (FileNotFoundException j) {
            throw new RuntimeException(j);
        }
        bootstrap((PDP) jxb.getValue());
    }

    /**
     * Create a PDP
     * @param configFile XMLStreamReader for the JBossXACML Config File
     */
    public JBossPDP(final XMLStreamReader configFile) throws XACMLException {
        final JAXBElement<?> jxb;
        try {
            final Unmarshaller unmarshaller = createValidatingUnMarshaller();
            jxb = (JAXBElement<?>) unmarshaller.unmarshal(configFile);
        } catch (JAXBException j) {
            throw new RuntimeException(j);
        } catch (FileNotFoundException j) {
            throw new RuntimeException(j);
        }
        bootstrap((PDP) jxb.getValue());
    }

    /**
     * Create a PDP
     * @param configFileURL URL of the JBossXACML Config File
     */
    public JBossPDP(final URL configFileURL) throws XACMLException {
        final JAXBElement<?> jxb;
        try {
            final Unmarshaller unmarshaller = createValidatingUnMarshaller();
            jxb = (JAXBElement<?>) unmarshaller.unmarshal(configFileURL.openStream());
        } catch (JAXBException j) {
            throw new RuntimeException(j);
        } catch (IOException io) {
            throw new RuntimeException(io);
        }
        bootstrap((PDP) jxb.getValue());
    }

    /**
     * @see PolicyDecisionPoint#evaluate(RequestContext)
     */
    public ResponseContext evaluate(final RequestContext request) {
        final HashSet<PolicyFinderModule> policyModules = new HashSet<PolicyFinderModule>();
        //Go through the Locators
        for (PolicyLocator locator : locators) {
            final List finderModulesList = (List) locator.get(XACMLConstants.POLICY_FINDER_MODULE);
            if (finderModulesList == null) {
                throw new IllegalStateException("Locator " + locator.getClass().getName() + " has no policy finder modules");
            }
            policyModules.addAll(finderModulesList);
        }
        policyFinder.setModules(policyModules);

        final AttributeFinder attributeFinder = new AttributeFinder();
        final List<AttributeFinderModule> attributeModules = new ArrayList<AttributeFinderModule>();
        attributeModules.add(new CurrentEnvModule());
        attributeModules.add(new SelectorModule());
        attributeFinder.setModules(attributeModules);

        final com.sun.xacml.PDP pdp = new com.sun.xacml.PDP(new PDPConfig(
                attributeFinder, policyFinder, null));
        final RequestCtx req = (RequestCtx) request.get(XACMLConstants.REQUEST_CTX);
        if (req == null) {
            throw new IllegalStateException("Request Context does not contain a request");
        }
        final ResponseCtx resp = pdp.evaluate(req);
        final ResponseContext response = RequestResponseContextFactory.createResponseContext();
        response.set(XACMLConstants.RESPONSE_CTX, resp);
        return response;
    }

    /**
     * @see PolicyDecisionPoint#setLocators(Set)
     */
    public void setLocators(final Set<PolicyLocator> locators) {
        this.locators = locators;
    }

    /**
     * @see PolicyDecisionPoint#setPolicies(Set)
     */
    public void setPolicies(final Set<XACMLPolicy> policies) {
        this.policies = policies;
    }

    private List<XACMLPolicy> addPolicySets(final List<PolicySetType> policySets,
            final boolean topLevel) throws FactoryException
    {
        final List<XACMLPolicy> list = new ArrayList<XACMLPolicy>();

        for (PolicySetType pst : policySets) {
            final String loc = pst.getLocation();
            final XACMLPolicy policySet;
            try {
                policySet = PolicyFactory.createPolicySet(getInputStream(loc), policyFinder);
            } catch (IOException ex) {
                throw new FactoryException(ex);
            }
            list.add(policySet);

            final List<XACMLPolicy> policyList = addPolicies(pst.getPolicy());
            policySet.setEnclosingPolicies(policyList);

            final List<PolicySetType> pset = pst.getPolicySet();
            if (pset != null) {
                policySet.getEnclosingPolicies().addAll(addPolicySets(pset, false));
            }
            if (topLevel) {
                policies.add(policySet);
            }
        }

        return list;
    }

    private List<XACMLPolicy> addPolicies(final List<PolicyType> policies) throws FactoryException {
        final List<XACMLPolicy> policyList = new ArrayList<XACMLPolicy>();
        for (PolicyType pt : policies) {
            try {
                policyList.add(PolicyFactory.createPolicy(getInputStream(pt.getLocation())));
            } catch (IOException ex) {
                throw new FactoryException(ex);
            }
        }
        return policyList;
    }

    private void bootstrap(final PDP pdp) throws XACMLException {
        final PoliciesType policiesType = pdp.getPolicies();
        final List<PolicySetType> policySet = policiesType.getPolicySet();

        addPolicySets(policySet, true);

        //Take care of additional policies
        final List<XACMLPolicy> policyList = addPolicies(policiesType.getPolicy());
        policies.addAll(policyList);

        //Take care of the locators
        final LocatorsType locatorsType = pdp.getLocators();
        final List<LocatorType> locs = locatorsType.getLocator();
        for (LocatorType lt : locs) {
            final Class<?> candidate;
            try {
                candidate = loadClass(lt.getName());
            } catch (ClassNotFoundException cnfe) {
                logger.severe("class not found:" + lt.getName());
                throw new PDPException(cnfe);
            }
            final PolicyLocator pl;
            try {
                pl = (PolicyLocator) candidate.newInstance();
            } catch (InstantiationException inst) {
                throw new PDPException(inst);
            } catch (IllegalAccessException ill) {
                throw new PDPException(ill);
            }
            pl.setPolicies(policies);
            locators.add(pl);
        }
    }

    /**
     * Create a Unmarshaller with validator.
     * 
     * @throws javax.xml.bind.JAXBException
     */
    private static Unmarshaller createValidatingUnMarshaller() throws JAXBException, FileNotFoundException {
        final JAXBContext context = JAXBContext.newInstance("net.seagis.xacml.jaxb");
        final Unmarshaller unmarshaller = context.createUnmarshaller();
        //Validate against schema
        final ClassLoader tcl = SecurityActions.getContextClassLoader();
        final URL schemaURL = tcl.getResource("net/seagis/xacml/jbossxacml-2.0.xsd");
        final SchemaFactory scFact = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        if (schemaURL == null) {
            throw new FileNotFoundException("Unable to find the resource file jbossxacml-2.0.xsd.");
        }
        final Schema schema;
        try {
            schema = scFact.newSchema(schemaURL);
        } catch (SAXException sax) {
            throw new JAXBException(sax);
        }
        unmarshaller.setSchema(schema);
        return unmarshaller;
    }

    private InputStream getInputStream(final String loc) throws IOException {
        InputStream is;
        try {
             final URL url = new URL(loc);
            is = url.openStream();
        } catch (IOException io) {
            final ClassLoader tcl = SecurityActions.getContextClassLoader();
            is = tcl.getResourceAsStream(loc);
        }
        if (is == null) {
            throw new IOException("Null Inputstream for " + loc);
        }
        return is;
    }

    private Class<?> loadClass(String name) throws ClassNotFoundException {
        final ClassLoader tcl = SecurityActions.getContextClassLoader();
        return tcl.loadClass(name);
    }
}
