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
package net.seagis.xacml.bridge;

import com.sun.xacml.AbstractPolicy;
import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.MatchResult;
import com.sun.xacml.PolicyMetaData;
import com.sun.xacml.PolicySet;
import com.sun.xacml.VersionConstraints;
import com.sun.xacml.ctx.Status;
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
    protected PolicyFinder policyFinder = null;
    
    private Logger logger = Logger.getLogger("net.seagis.xacml.bridge");

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
        logger.info(context.getResourceId().encode());
        
        AbstractPolicy selectedPolicy = null;
        final MatchResult match = policySet.match(context);
        int result = match.getResult();
        if (result == MatchResult.MATCH) {
            selectedPolicy = policySet;
        }
        
        int i = 0;
        while (result == MatchResult.NO_MATCH && i < policies.size()) {
            result = policies.get(i).match(context).getResult();
            if (result == MatchResult.MATCH) {
                selectedPolicy = policies.get(i);
            }
            i++;
        }
        
        // if target matching was indeterminate, then return the error
        if (result == MatchResult.INDETERMINATE) {
            logger.info("undterminate matching");
            return new PolicyFinderResult(match.getStatus());        // see if the target matched
        }
        if (result == MatchResult.MATCH) {
            logger.info("succefull matching");
            return new PolicyFinderResult(selectedPolicy);
        }
        if (result == MatchResult.NO_MATCH) {
            logger.info("no match: ");
            return new PolicyFinderResult(match.getStatus());  
        }

        logger.info("returning null");
        return new PolicyFinderResult(selectedPolicy);
    }

    @Override
    public PolicyFinderResult findPolicy(final URI idReference, final int type,
            final VersionConstraints constraints, final PolicyMetaData parentMetaData) {
        logger.info("par ici");
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
