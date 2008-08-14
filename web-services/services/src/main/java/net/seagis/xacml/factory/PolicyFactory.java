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
package net.seagis.xacml.factory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBElement;

import net.seagis.xacml.api.XACMLPolicy;
import net.seagis.xacml.policy.ObjectFactory;
import net.seagis.xacml.policy.PolicyType;
import net.seagis.xacml.bridge.JBossPolicyFinder;
import net.seagis.xacml.JBossXACMLPolicy;


/**
 *  A Policy Factory that creates XACML Policy
 *  or Policy Sets
 *  @author Anil.Saldhana@redhat.com
 *  @since  Jul 5, 2007 
 *  @version $Revision$
 */
public class PolicyFactory {

    public static Class<?> constructingClass = JBossXACMLPolicy.class;

    public static void setConstructingClass(final Class<?> clazz) {
        if (XACMLPolicy.class.isAssignableFrom(clazz) == false) {
            throw new IllegalArgumentException("Specified class is not of type XACMLPolicy");
        }
        constructingClass = clazz;
    }

    public static XACMLPolicy createPolicySet(final InputStream policySetFile)
            throws FactoryException
    {
        return newInstance(getCtr(), new Object[]{policySetFile, XACMLPolicy.POLICYSET});
    }

    public static XACMLPolicy createPolicySet(final InputStream policySetFile,
            final JBossPolicyFinder theFinder) throws FactoryException
    {
        return newInstance(getCtrWithFinder(),
                new Object[] {policySetFile, XACMLPolicy.POLICYSET, theFinder});
    }

    public static XACMLPolicy createPolicy(final InputStream policyFile) throws FactoryException {
        return newInstance(getCtr(), new Object[]{policyFile, XACMLPolicy.POLICY});
    }

    public static XACMLPolicy createPolicy(final PolicyType policyFile) throws FactoryException {
        final JAXBElement<PolicyType> jaxbPolicy = new ObjectFactory().createPolicy(policyFile);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JAXB.marshal(jaxbPolicy, baos);
        final ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        return newInstance(getCtr(), new Object[]{bis, XACMLPolicy.POLICY});
    }

    private static Constructor<XACMLPolicy> getCtr() throws FactoryException {
        try {
            return (Constructor<XACMLPolicy>) constructingClass.getConstructor(
                    new Class[]{InputStream.class, Integer.TYPE});
        } catch (NoSuchMethodException ex) {
            throw new FactoryException(ex);
        }
    }

    private static Constructor<XACMLPolicy> getCtrWithFinder() throws FactoryException {
        try {
            return (Constructor<XACMLPolicy>) constructingClass.getConstructor(
                    new Class[]{InputStream.class, Integer.TYPE, JBossPolicyFinder.class});
        } catch (NoSuchMethodException ex) {
            throw new FactoryException(ex);
        } catch (SecurityException ex) {
            throw new FactoryException(ex);
        }
    }

    private static <T> T newInstance(final Constructor<T> constructor, final Object[] args)
            throws FactoryException
    {
        try {
            return constructor.newInstance(args);
        } catch (InstantiationException ex) {
            throw new FactoryException(ex);
        } catch (IllegalAccessException ex) {
            throw new FactoryException(ex);
        } catch (IllegalArgumentException ex) {
            throw new FactoryException(ex);
        } catch (InvocationTargetException ex) {
            throw new FactoryException(ex);
        }
    }
}
