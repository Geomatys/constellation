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
