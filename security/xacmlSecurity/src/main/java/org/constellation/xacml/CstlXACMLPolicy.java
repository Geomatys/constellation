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
package org.constellation.xacml;

import com.sun.xacml.AbstractPolicy;
import com.sun.xacml.finder.PolicyFinder;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.constellation.xacml.api.XACMLPolicy;
import org.xml.sax.SAXException;


/**
 *  JBossXACML Policy
 *  @author Anil.Saldhana@redhat.com
 *  @since  Jul 6, 2007 
 *  @version $Revision$
 */
public class CstlXACMLPolicy implements XACMLPolicy {

    private final PolicyFinder finder                 = new PolicyFinder();
    private final List<XACMLPolicy> enclosingPolicies = new ArrayList<XACMLPolicy>();
    private int policyType                            = XACMLPolicy.POLICY;
    private final Map<XACMLConstants, Object> map     = new HashMap<XACMLConstants, Object>();

    /**
     * Construct a CstlXACMLPolicy  
     * @param url url to the policy file
     * @param type type (Policy or PolicySet) 
     * @throws Exception
     * @see XACMLConstants
     */
    public CstlXACMLPolicy(final URL url, final int type) throws IOException, SAXException {
        this(url.openStream(), type);
    }

    /**
     * Construct a CstlXACMLPolicy
     * @param is Inputstream to the policy file
     * @param type type (Policy or PolicySet)
     * @throws Exception
     * @see XACMLConstants
     */
    public CstlXACMLPolicy(final InputStream is, final int type) throws IOException, SAXException {
        this(is, type, null);
    }

    /**
     * Construct a CstlXACMLPolicy instance
     * @param is Inputstream to the policy/policyset file
     * @param type policy or policyset
     * @param theFinder PolicySet processing needs this
     * @throws Exception
     * @see XACMLConstants
     */
    public CstlXACMLPolicy(final InputStream is, final int type, final PolicyFinder theFinder) throws IOException, SAXException {
        final AbstractPolicy policy;
        XACMLPolicyUtil policyUtil = new XACMLPolicyUtil();
        policyType = type;
        if (type == XACMLPolicy.POLICYSET) {
            policy = policyUtil.createPolicySet(is, (theFinder != null) ? theFinder : finder);
            map.put(XACMLConstants.POLICY_FINDER, theFinder);
        } else if (type == XACMLPolicy.POLICY) {
            policy = policyUtil.createPolicy(is);
        } else {
            throw new IllegalArgumentException("Unknown type");
        }
        map.put(XACMLConstants.UNDERLYING_POLICY, policy);
    }

    /**
     * @see XACMLPolicy#getType()
     * @see XACMLConstants
     */
    public int getType() {
        return policyType;
    }

    /**
     * @see XACMLPolicy#setEnclosingPolicies
     */
    public void setEnclosingPolicies(List<XACMLPolicy> policies) {
        enclosingPolicies.addAll(policies);
    }

    /**
     * @see XACMLPolicy#getEnclosingPolicies()
     */
    public List<XACMLPolicy> getEnclosingPolicies() {
        return enclosingPolicies;
    }

    /**
     * @see ContextMapOp#get
     */
    public Object get(final XACMLConstants key) {
        return map.get(key);
    }

    /**
     * @see ContextMapOp#set
     */
    public void set(final XACMLConstants key, final Object obj) {
        map.put(key, obj);
    }
}
