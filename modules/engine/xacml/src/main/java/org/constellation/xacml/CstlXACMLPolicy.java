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
package org.constellation.xacml;

import com.sun.xacml.AbstractPolicy;
import com.sun.xacml.finder.PolicyFinder;
import org.constellation.xacml.api.XACMLPolicy;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
        final XACMLPolicyUtil policyUtil = new XACMLPolicyUtil();
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
    @Override
    public int getType() {
        return policyType;
    }

    /**
     * @see XACMLPolicy#setEnclosingPolicies
     */
    @Override
    public void setEnclosingPolicies(List<XACMLPolicy> policies) {
        enclosingPolicies.addAll(policies);
    }

    /**
     * @see XACMLPolicy#getEnclosingPolicies()
     */
    @Override
    public List<XACMLPolicy> getEnclosingPolicies() {
        return enclosingPolicies;
    }

    /**
     * @see ContextMapOp#get
     */
    @Override
    public Object get(final XACMLConstants key) {
        return map.get(key);
    }

    /**
     * @see ContextMapOp#set
     */
    @Override
    public void set(final XACMLConstants key, final Object obj) {
        map.put(key, obj);
    }
}
