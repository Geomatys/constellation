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
package org.constellation.xacml.factory;

import com.sun.xacml.finder.PolicyFinder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBElement;

import org.constellation.xacml.api.XACMLPolicy;
import org.constellation.xacml.CstlXACMLPolicy;
import org.geotoolkit.xacml.xml.policy.ObjectFactory;
import org.geotoolkit.xacml.xml.policy.PolicySetType;
import org.geotoolkit.xacml.xml.policy.PolicyType;


/**
 *  A Policy Factory that creates XACML Policy
 *  or Policy Sets
 *  @author Anil.Saldhana@redhat.com
 *  @since  Jul 5, 2007 
 *  @version $Revision$
 */
public final class PolicyFactory {

    private static final Logger LOGGER = Logger.getLogger("org.constellation.xacml");
    
    private static Class<?> constructingClass = CstlXACMLPolicy.class;

    private PolicyFactory() {}
    
    public static void setConstructingClass(final Class<?> clazz) {
        if (!XACMLPolicy.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("Specified class is not of type XACMLPolicy");
        }
        constructingClass = clazz;
    }

    public static XACMLPolicy createPolicySet(final InputStream policySetFile) throws FactoryException {
        
        return newInstance(getConstructor(), new Object[]{policySetFile, XACMLPolicy.POLICYSET});
    }

    public static XACMLPolicy createPolicySet(final InputStream policySetFile, final PolicyFinder theFinder) throws FactoryException {
        
        return newInstance(getCtrWithFinder(), new Object[] {policySetFile, XACMLPolicy.POLICYSET, theFinder});
    }

    public static XACMLPolicy createPolicy(final InputStream policyFile) throws FactoryException {
        
        return newInstance(getConstructor(), new Object[]{policyFile, XACMLPolicy.POLICY});
    }

    public static XACMLPolicy createPolicy(final PolicyType policyFile) throws FactoryException {
        
        final JAXBElement<PolicyType> jaxbPolicy = new ObjectFactory().createPolicy(policyFile);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JAXB.marshal(jaxbPolicy, baos);
        final ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        return newInstance(getConstructor(), new Object[]{bis, XACMLPolicy.POLICY});
    }
    
    public static XACMLPolicy createPolicySet(final PolicySetType policySetFile) throws FactoryException {
        
        final JAXBElement<PolicySetType> jaxbPolicy = new ObjectFactory().createPolicySet(policySetFile);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JAXB.marshal(jaxbPolicy, baos);
        final ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        return newInstance(getConstructor(), new Object[]{bis, XACMLPolicy.POLICYSET});
    }

    private static Constructor<XACMLPolicy> getConstructor() throws FactoryException {
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
                    new Class[]{InputStream.class, Integer.TYPE, PolicyFinder.class});
        } catch (NoSuchMethodException ex) {
            throw new FactoryException(ex);
        } catch (SecurityException ex) {
            throw new FactoryException(ex);
        }
    }

    private static <T> T newInstance(final Constructor<T> constructor, final Object[] args) throws FactoryException {
        try {
        
            return constructor.newInstance(args);
        
        } catch (InstantiationException ex) {
            throw new FactoryException(ex);
        } catch (IllegalAccessException ex) {
            throw new FactoryException(ex);
        } catch (IllegalArgumentException ex) {
            throw new FactoryException(ex);
        } catch (InvocationTargetException ex) {
            LOGGER.severe("invocation target exception: " + ex.toString());
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new FactoryException(ex);
        }
    }
}
