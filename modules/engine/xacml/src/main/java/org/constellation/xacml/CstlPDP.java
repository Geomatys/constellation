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

import org.constellation.xacml.factory.FactoryException;
import org.constellation.xacml.factory.PolicyFactory;
import org.constellation.xacml.api.PolicyDecisionPoint;
import org.constellation.xacml.api.PolicyLocator;
import org.constellation.xacml.api.RequestContext;
import org.constellation.xacml.api.ResponseContext;
import org.constellation.xacml.api.XACMLPolicy;
import org.constellation.xacml.locators.JBossPolicyLocator;
import org.geotoolkit.xacml.xml.policy.PolicySetType;
import org.geotoolkit.xacml.xml.policy.PolicyType;



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

    /**
     * Build a new empty PDP.
     */
    public CstlPDP() {}
    
    /**
     * Build a PDP with the following policySet.
     */
    public CstlPDP(PolicySetType policySet) throws FactoryException {
        
        final XACMLPolicy xpolicySet = PolicyFactory.createPolicySet(policySet);
        
        final List<XACMLPolicy> enclosing = new ArrayList<XACMLPolicy>();
        for (PolicyType p: policySet.getPoliciesChild()) {
            final XACMLPolicy policy = PolicyFactory.createPolicy(p);
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
    @Override
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
    @Override
    public void setLocators(final Set<PolicyLocator> locators) {
        this.locators = locators;
    }

    /**
     * @see PolicyDecisionPoint#setPolicies(Set)
     */
    @Override
    public void setPolicies(final Set<XACMLPolicy> policies) {
        this.policies = policies;
        locators.add(new JBossPolicyLocator(policies));
    }
}
