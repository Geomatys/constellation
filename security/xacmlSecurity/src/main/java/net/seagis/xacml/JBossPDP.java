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
import com.sun.xacml.finder.PolicyFinder;
import com.sun.xacml.finder.PolicyFinderModule;
import com.sun.xacml.finder.impl.CurrentEnvModule;
import com.sun.xacml.finder.impl.SelectorModule;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import net.seagis.xacml.factory.FactoryException;
import net.seagis.xacml.factory.PolicyFactory;
import net.seagis.xacml.factory.RequestResponseContextFactory;
import net.seagis.xacml.api.PolicyDecisionPoint;
import net.seagis.xacml.api.PolicyLocator;
import net.seagis.xacml.api.RequestContext;
import net.seagis.xacml.api.ResponseContext;
import net.seagis.xacml.api.XACMLPolicy;
import net.seagis.xacml.locators.JBossPolicyLocator;
import net.seagis.xacml.policy.PolicySetType;
import net.seagis.xacml.policy.PolicyType;



/**
 *  PDP for JBoss XACML
 *  @author Anil.Saldhana@redhat.com
 *  @since  Jul 6, 2007
 *  @version $Revision$
 */
public class JBossPDP implements PolicyDecisionPoint {
    
    private Set<PolicyLocator> locators = new HashSet<PolicyLocator>();
    
    private Set<XACMLPolicy> policies = new HashSet<XACMLPolicy>();
    
    private final PolicyFinder policyFinder = new PolicyFinder();

    private Logger logger = Logger.getLogger("net.seagis.xacml");
    
    /**
     * Build a new empty PDP.
     */
    public JBossPDP() {}
    
    /**
     * Build a PDP with the following policySet.
     */
    public JBossPDP(PolicySetType policySet) throws FactoryException {
        
        XACMLPolicy policySet1 = PolicyFactory.createPolicySet(policySet);
        
        List<XACMLPolicy> enclosing = new ArrayList<XACMLPolicy>();
        for (PolicyType p: policySet.getPoliciesChild()) {
            XACMLPolicy policy = PolicyFactory.createPolicy(p);
            enclosing.add(policy);
        }
        policySet1.setEnclosingPolicies(enclosing);
        
        // we add the policies to the PDP
        policies.add(policySet1);
        
        locators.add(new JBossPolicyLocator(policies));
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

        final com.sun.xacml.PDP pdp = new com.sun.xacml.PDP(new PDPConfig(attributeFinder, policyFinder, null));
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

    /**private List<XACMLPolicy> addPolicySets(final List<PolicySetType> policySets, final boolean topLevel) throws FactoryException  {
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

    private InputStream getInputStream(final String loc) throws IOException {
        InputStream is;
        try {
            final URL url = new URL(loc);
            is = url.openStream();
        } catch (IOException io) {
            is = SecurityActions.getResourceAsStream(loc);
        }
        if (is == null) {
            throw new IOException("Null Inputstream for " + loc);
        }
        return is;
    }*/
}
