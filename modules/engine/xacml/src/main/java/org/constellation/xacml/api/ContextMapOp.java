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
package org.constellation.xacml.api;

import org.constellation.xacml.XACMLConstants;


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
