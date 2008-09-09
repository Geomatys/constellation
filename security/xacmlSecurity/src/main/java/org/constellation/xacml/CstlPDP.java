/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007, JBoss Inc.
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import net.seagis.xacml.factory.FactoryException;
import net.seagis.xacml.factory.PolicyFactory;
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
public class CstlPDP implements PolicyDecisionPoint {
    
    private Set<PolicyLocator> locators = new HashSet<PolicyLocator>();
    
    private Set<XACMLPolicy> policies = new HashSet<XACMLPolicy>();
    
    private final PolicyFinder policyFinder = new PolicyFinder();

    private Logger logger = Logger.getLogger("net.seagis.xacml");
    
    /**
     * Build a new empty PDP.
     */
    public CstlPDP() {}
    
    /**
     * Build a PDP with the following policySet.
     */
    public CstlPDP(PolicySetType policySet) throws FactoryException {
        
        XACMLPolicy xpolicySet = PolicyFactory.createPolicySet(policySet);
        
        List<XACMLPolicy> enclosing = new ArrayList<XACMLPolicy>();
        for (PolicyType p: policySet.getPoliciesChild()) {
            XACMLPolicy policy = PolicyFactory.createPolicy(p);
            enclosing.add(policy);
        }
        xpolicySet.setEnclosingPolicies(enclosing);
        
        // we add the policies to the PDP
        policies.add(xpolicySet);
        
        locators.add(new JBossPolicyLocator(policies));
    }

    
    /**
     * @see PolicyDecisionPoint#evaluate(RequestContext)
     */
    public ResponseContext evaluate(final RequestContext request) {
        final HashSet<PolicyFinderModule> policyModules = new HashSet<PolicyFinderModule>();
        
        //Go through the Locators
        for (PolicyLocator locator : locators) {
            final PolicyFinderModule finderModulesList =  (PolicyFinderModule) locator.get(XACMLConstants.POLICY_FINDER_MODULE);
            if (finderModulesList == null) {
                throw new IllegalStateException("Locator " + locator.getClass().getName() + " has no policy finder modules");
            }
            policyModules.add(finderModulesList);
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
        final ResponseContext response = new CstlResponseContext();
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
        locators.add(new JBossPolicyLocator(policies));
    }
}
