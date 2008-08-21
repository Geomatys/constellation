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

import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
 

/**
 *  Privileged Blocks
 *  @author Anil.Saldhana@redhat.com
 *  @since  Jul 10, 2007 
 *  @version $Revision$
 */
public class SecurityActions {
    
    /**
     * Obtain the Thread Context ClassLoader.
     */
    public static ClassLoader getContextClassLoader() {
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
    }
    
    
    /**
     * Return the URL of the specified resource 
     */
    public static URL getResource(String url) {
        ClassLoader cl = getContextClassLoader();
        return cl.getResource(url);
    }
    
    /**
     * Return an input stream of the specified resource. 
     */
    public static InputStream getResourceAsStream(String url) {
        ClassLoader cl = getContextClassLoader();
        return cl.getResourceAsStream(url);
    }
    
    /**
     * Return an input stream of the specified resource. 
     */
    public static Class<?> loadClass(String name) throws ClassNotFoundException {
        ClassLoader cl = getContextClassLoader();
        return cl.loadClass(name);
    }
}
