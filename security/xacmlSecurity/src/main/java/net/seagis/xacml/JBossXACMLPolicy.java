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
package net.seagis.xacml;

import com.sun.xacml.AbstractPolicy;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.seagis.xacml.bridge.JBossPolicyFinder;
import net.seagis.xacml.api.XACMLPolicy;
import org.xml.sax.SAXException;


/**
 *  JBossXACML Policy
 *  @author Anil.Saldhana@redhat.com
 *  @since  Jul 6, 2007 
 *  @version $Revision$
 */
public class JBossXACMLPolicy implements XACMLPolicy {

    private final JBossPolicyFinder finder = new JBossPolicyFinder();
    private final List<XACMLPolicy> enclosingPolicies = new ArrayList<XACMLPolicy>();
    private int policyType = XACMLPolicy.POLICY;
    private final Map<XACMLConstants, Object> map = new HashMap<XACMLConstants, Object>();

    /**
     * Construct a JBossXACMLPolicy  
     * @param url url to the policy file
     * @param type type (Policy or PolicySet) 
     * @throws Exception
     * @see XACMLConstants
     */
    public JBossXACMLPolicy(final URL url, final int type) throws IOException, SAXException {
        this(url.openStream(), type);
    }

    /**
     * Construct a JBossXACMLPolicy
     * @param is Inputstream to the policy file
     * @param type type (Policy or PolicySet)
     * @throws Exception
     * @see XACMLConstants
     */
    public JBossXACMLPolicy(final InputStream is, final int type) throws IOException, SAXException {
        this(is, type, null);
    }

    /**
     * Construct a JBossXACMLPolicy instance
     * @param is Inputstream to the policy/policyset file
     * @param type policy or policyset
     * @param theFinder PolicySet processing needs this
     * @throws Exception
     * @see XACMLConstants
     */
    public JBossXACMLPolicy(final InputStream is, final int type,
            final JBossPolicyFinder theFinder) throws IOException, SAXException {
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
