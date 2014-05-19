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

import java.util.List;


/**
 *  Represents a Policy or a PolicySet in the XACML World
 *  @author Anil.Saldhana@redhat.com
 *  @since  Jul 5, 2007 
 *  @version $Revision$
 */
public interface XACMLPolicy extends ContextMapOp {

    /**
     * Type identifying a PolicySet
     */
    int POLICYSET = 0;
    /**
     * Type identifying a Policy
     */
    int POLICY = 1;

    /**
     * Return a type (PolicySet or Policy)
     * @return int value representing type
     */
    int getType();

    /**
     * A PolicySet can contain policies within.
     * Setter to set the policies inside a policyset
     * @param policies a list of policies
     */
    void setEnclosingPolicies(List<XACMLPolicy> policies);

    /**
     * Return the enclosing policies for a PolicySet
     * @return a list of policies
     */
    List<XACMLPolicy> getEnclosingPolicies();
}
