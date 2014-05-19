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

import com.sun.xacml.AbstractPolicy;
import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.MatchResult;
import com.sun.xacml.PolicyMetaData;
import com.sun.xacml.PolicySet;
import com.sun.xacml.VersionConstraints;
import com.sun.xacml.finder.PolicyFinder;
import com.sun.xacml.finder.PolicyFinderModule;
import com.sun.xacml.finder.PolicyFinderResult;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


/**
 *  PolicyFinderModule for PolicySet
 *  @author Anil.Saldhana@redhat.com
 *  @since  Jul 6, 2007
 *  @version $Revision$
 */
public class PolicySetFinderModule extends PolicyFinderModule {

    private final PolicySet policySet;
    private final List<AbstractPolicy> policies = new ArrayList<AbstractPolicy>();
    protected PolicyFinder policyFinder         = null;
    
    private static final Logger LOGGER = Logger.getLogger("org.constellation.xacml.bridge");

    public PolicySetFinderModule() {
        this(null);
    }

    public PolicySetFinderModule(final PolicySet policySet) {
        this(policySet, null);
    }

    public PolicySetFinderModule(final PolicySet policySet, final List<AbstractPolicy> policies) {
        this.policySet = policySet;
        if (policies != null && !policies.isEmpty()) {
            this.policies.addAll(policies);
        }
    }

    @Override
    public void init(final PolicyFinder finder) {
        policyFinder = finder;
    }

    /**
     * Finds the applicable policy (if there is one) for the given context.
     *
     * @param context the evaluation context
     *
     * @return an applicable policy, if one exists, or an error
     */
    @Override
    public PolicyFinderResult findPolicy(final EvaluationCtx context) {
        LOGGER.finer(context.getResourceId().encode());
        
        AbstractPolicy selectedPolicy = null;
        final MatchResult match = policySet.match(context);
        final int result = match.getResult();
        if (result == MatchResult.MATCH) {
            selectedPolicy = policySet;
        }
        
        // if target matching was indeterminate, then return the error
        if (result == MatchResult.INDETERMINATE) {
            LOGGER.finer("undeterminate matching");
            return new PolicyFinderResult(match.getStatus());        // see if the target matched
        }
        if (result == MatchResult.MATCH) {
            LOGGER.finer("succefull matching");
            return new PolicyFinderResult(selectedPolicy);
        }
        if (result == MatchResult.NO_MATCH) {
            LOGGER.finer("no match: ");
            return new PolicyFinderResult(match.getStatus());  
        }

        LOGGER.finer("returning null");
        return new PolicyFinderResult(selectedPolicy);
    }

    @Override
    public PolicyFinderResult findPolicy(final URI idReference, final int type,
            final VersionConstraints constraints, final PolicyMetaData parentMetaData) {
        for (AbstractPolicy p : policies) {
            if (p.getId().compareTo(idReference) == 0) {
                return new PolicyFinderResult(p);
            }
        }
        return new PolicyFinderResult();
    }

    @Override
    public boolean isRequestSupported() {
        return true;
    }

    /**
     * Always returns true, since reference-based retrieval is supported.
     *
     * @return true
     */
    @Override
    public boolean isIdReferenceSupported() {
        return true;
    }
}
