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
package net.seagis.xacml.bridge;

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
