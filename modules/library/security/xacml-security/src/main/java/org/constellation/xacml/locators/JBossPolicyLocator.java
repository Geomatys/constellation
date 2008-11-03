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
package org.constellation.xacml.locators;

import com.sun.xacml.AbstractPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.constellation.xacml.bridge.WrapperPolicyFinderModule;
import org.constellation.xacml.XACMLConstants;
import org.constellation.xacml.api.XACMLPolicy;
import com.sun.xacml.Policy;
import com.sun.xacml.PolicySet;
import com.sun.xacml.finder.PolicyFinderModule;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.constellation.xacml.api.ContextMapOp;
import org.constellation.xacml.api.PolicyLocator;
import org.constellation.xacml.bridge.PolicySetFinderModule;

/**
 *  Policy Locator for plain XACML Policy instances
 *  @author Anil.Saldhana@redhat.com
 *  @since  Jul 6, 2007 
 *  @version $Revision$
 */
public class JBossPolicyLocator implements PolicyLocator, ContextMapOp {
   
    private Map<String, Object> map = new HashMap<String, Object>();
    
    private PolicyFinderModule policyFinderModule;

    private Logger logger = Logger.getLogger("org.constellation.xacml.locators"); 
    
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
                Policy p = (Policy) xp.get(XACMLConstants.UNDERLYING_POLICY);
                policyFinderModule =  new WrapperPolicyFinderModule(p);
                
            } else if (xp.getType() == XACMLPolicy.POLICYSET){
                PolicySet ps = (PolicySet) xp.get(XACMLConstants.UNDERLYING_POLICY);
                
                List<AbstractPolicy> poli = new ArrayList<AbstractPolicy>();
                for (XACMLPolicy xp2 : xp.getEnclosingPolicies()) {
                    Policy p = (Policy) xp2.get(XACMLConstants.UNDERLYING_POLICY);
                    poli.add(p);
                }
                policyFinderModule = new PolicySetFinderModule(ps, poli);
                
                
            } else {
                logger.info("unexpected Policy type:" + xp.getType());
            }
        }
        this.map.put(XACMLConstants.POLICY_FINDER_MODULE.key, policyFinderModule);
    }
    
    public Object get(XACMLConstants key) {
        return map.get(key.key);
    }

    public void set(XACMLConstants key, Object obj) {
        map.put(key.key, obj);
    }
}
