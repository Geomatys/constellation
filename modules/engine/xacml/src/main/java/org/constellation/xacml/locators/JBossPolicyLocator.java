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
package org.constellation.xacml.locators;

import com.sun.xacml.AbstractPolicy;
import com.sun.xacml.Policy;
import com.sun.xacml.PolicySet;
import com.sun.xacml.finder.PolicyFinderModule;
import org.constellation.xacml.XACMLConstants;
import org.constellation.xacml.api.ContextMapOp;
import org.constellation.xacml.api.PolicyLocator;
import org.constellation.xacml.api.XACMLPolicy;
import org.constellation.xacml.bridge.PolicySetFinderModule;
import org.constellation.xacml.bridge.WrapperPolicyFinderModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 *  Policy Locator for plain XACML Policy instances
 *  @author Anil.Saldhana@redhat.com
 *  @since  Jul 6, 2007 
 *  @version $Revision$
 */
public class JBossPolicyLocator implements PolicyLocator, ContextMapOp {
   
    private final Map<String, Object> map = new HashMap<String, Object>();
    
    private PolicyFinderModule policyFinderModule;

    private static final Logger LOGGER = Logger.getLogger("org.constellation.xacml.locators");
    
    /**
     * Build a new Policy locator.
     */
    public JBossPolicyLocator() {
    }

    /**
     * Build a new Policy locator with the specified set of policy.
     */
    public JBossPolicyLocator(Set<XACMLPolicy> policies) {
        setPolicies(policies);
    }

    /**
     * Set the specified List of policies.
     * 
     * @param policies
     */
    @Override
    public void setPolicies(Set<XACMLPolicy> policies) {
        for (XACMLPolicy xp : policies) {
            if (xp.getType() == XACMLPolicy.POLICY) {
                final Policy p = (Policy) xp.get(XACMLConstants.UNDERLYING_POLICY);
                policyFinderModule =  new WrapperPolicyFinderModule(p);
                
            } else if (xp.getType() == XACMLPolicy.POLICYSET){
                final PolicySet ps = (PolicySet) xp.get(XACMLConstants.UNDERLYING_POLICY);
                
                final List<AbstractPolicy> poli = new ArrayList<AbstractPolicy>();
                for (XACMLPolicy xp2 : xp.getEnclosingPolicies()) {
                    final Policy p = (Policy) xp2.get(XACMLConstants.UNDERLYING_POLICY);
                    poli.add(p);
                }
                policyFinderModule = new PolicySetFinderModule(ps, poli);
                
                
            } else {
                LOGGER.info("unexpected Policy type:" + xp.getType());
            }
        }
        this.map.put(XACMLConstants.POLICY_FINDER_MODULE.key, policyFinderModule);
    }
    
    @Override
    public Object get(XACMLConstants key) {
        return map.get(key.key);
    }

    @Override
    public void set(XACMLConstants key, Object obj) {
        map.put(key.key, obj);
    }
}
