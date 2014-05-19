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
package org.constellation.xacml.api;

import java.util.Set;


/**
 *  Represents a XACML PDP
 *  @author Anil.Saldhana@redhat.com
 *  @since  Jul 5, 2007 
 *  @version $Revision$
 */
public interface PolicyDecisionPoint {

    /**
     * Set a set of Policy/PolicySet instances on the PDP
     * - Remember to also pass a set of PolicyLocators 
     * if you have not used a JBossXACML config file
     * @param policies  a Set of Policy/PolicySet instances
     */
    void setPolicies(Set<XACMLPolicy> policies);

    /**
     * Set a set of policy locators.
     * - This method is primarily used when the policy/policyset
     * instances are created without the usage of the JBossXACML 
     * Config File. In this case, do not forget to set the policy
     * objects in the locators via their setPolicies method
     * @param locators a set of PolicyLocator instances
     */
    void setLocators(Set<PolicyLocator> locators);

    /**
     * Method to evaluate a XACML Request
     * @param request The RequestContext that contains the XACML Request
     */
    ResponseContext evaluate(RequestContext request);
}
