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
package org.constellation.xacml.bridge;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.Policy;
import com.sun.xacml.PolicyMetaData;
import com.sun.xacml.VersionConstraints;
import com.sun.xacml.finder.PolicyFinder;
import com.sun.xacml.finder.PolicyFinderModule;
import com.sun.xacml.finder.PolicyFinderResult;

import java.net.URI;


/**
 *  PolicyFinderModule that returns the enclosing Policy Object
 *  @author Anil.Saldhana@redhat.com
 *  @since  Jul 6, 2007
 *  @version $Revision$
 */
public class WrapperPolicyFinderModule extends PolicyFinderModule {

    protected PolicyFinder policyFinder;
    private final Policy policy;

    public WrapperPolicyFinderModule(final Policy policy) {
        this.policy = policy;
    }

    /**
     * @see PolicyFinderModule#init(com.sun.xacml.finder.PolicyFinder)
     */
    @Override
    public void init(final PolicyFinder policyFinder) {
        this.policyFinder = policyFinder;
    }

    /**
     * @see PolicyFinderModule#findPolicy(com.sun.xacml.EvaluationCtx)
     */
    @Override
    public PolicyFinderResult findPolicy(final EvaluationCtx evaluationCtx) {
        return new PolicyFinderResult(policy);
    }

    /**
     * @see PolicyFinderModule#findPolicy(java.net.URI, int,
     *        com.sun.xacml.VersionConstraints, com.sun.xacml.PolicyMetaData)
     */
    @Override
    public PolicyFinderResult findPolicy(final URI arg0, final int arg1,
            final VersionConstraints arg2, final PolicyMetaData arg3) {
        return new PolicyFinderResult(policy);
    }

    /**
     * @see PolicyFinderModule#isRequestSupported()
     */
    @Override
    public boolean isRequestSupported() {
        return true;
    }
}
