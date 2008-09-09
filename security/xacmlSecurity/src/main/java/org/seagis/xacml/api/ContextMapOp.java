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
package net.seagis.xacml.api;

import net.seagis.xacml.XACMLConstants;


/**
 *  Interface defining operations
 *  on a context map
 *  @author Anil.Saldhana@redhat.com
 *  @since  Jul 6, 2007 
 *  @version $Revision$
 */
public interface ContextMapOp {

    /**
     * Get an element from the map
     * @param key Key
     * @return object from the map
     */
    Object get(XACMLConstants key);

    /**
     * Set an object on the map
     * @param key Key for the map
     * @param obj Object to be placed
     */
    void set(XACMLConstants key, Object obj);
}
