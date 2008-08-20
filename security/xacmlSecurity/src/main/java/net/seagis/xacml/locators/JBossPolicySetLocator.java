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
package net.seagis.xacml.locators;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.seagis.xacml.bridge.PolicySetFinderModule;
import net.seagis.xacml.XACMLConstants;
import net.seagis.xacml.api.XACMLPolicy;
import com.sun.xacml.AbstractPolicy; 
import com.sun.xacml.PolicySet;

/**
 *  Locator for a PolicySet
 *  @author Anil.Saldhana@redhat.com
 *  @since  Jul 6, 2007 
 *  @version $Revision$
 */
public class JBossPolicySetLocator extends AbstractJBossPolicyLocator {

    private List<PolicySetFinderModule> pfml = new ArrayList<PolicySetFinderModule>();

    public JBossPolicySetLocator() {
    }

    public JBossPolicySetLocator(Set<XACMLPolicy> policies) {
        setPolicies(policies);
    }

    @Override
    public void setPolicies(Set<XACMLPolicy> policies) {
        for (XACMLPolicy xp : policies) {
            if (xp.getType() == XACMLPolicy.POLICYSET) {
                pfml.add(getPopulatedPolicySetFinderModule(xp));
            }
        }
        this.map.put(XACMLConstants.POLICY_FINDER_MODULE.key, pfml);
    }

    private PolicySetFinderModule getPopulatedPolicySetFinderModule(XACMLPolicy xpolicy) {

        PolicySetFinderModule psfm = new PolicySetFinderModule();
        //Check for enclosed policies
        List<AbstractPolicy> sunxacmlPolicies = new ArrayList<AbstractPolicy>();
        this.recursivePopulate(xpolicy, sunxacmlPolicies, psfm);

        // ISSUE HERE NO method set for this insructions =>  psfm.set((PolicySet) xpolicy.get(XACMLConstants.UNDERLYING_POLICY), sunxacmlPolicies);
        
        //Make this PolicySetFinderModule the module for this policy set
        xpolicy.set(XACMLConstants.POLICY_FINDER_MODULE, psfm);
        return psfm;
    }

    private void recursivePopulate(XACMLPolicy policy, List<AbstractPolicy> policies, PolicySetFinderModule psfm) {
        List<XACMLPolicy> policyList = policy.getEnclosingPolicies();
        for (XACMLPolicy xp : policyList) {
            AbstractPolicy p = (AbstractPolicy) xp.get(XACMLConstants.UNDERLYING_POLICY);
            policies.add(p);
            if (p instanceof PolicySet) {
                this.recursivePopulate(xp, policies, psfm);
            }
        }

    }

    public Object get(XACMLConstants key) {
        return map.get(key.key);
    }

    public void set(XACMLConstants key, Object obj) {
        map.put(key.key, obj);
    }
}
